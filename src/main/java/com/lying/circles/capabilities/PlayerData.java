package com.lying.circles.capabilities;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.circles.blocks.Statue;
import com.lying.circles.client.ClientSetupEvents;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.init.CMCapabilities;
import com.lying.circles.init.CMDamageSource;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.network.PacketSyncPlayerData;
import com.lying.circles.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerData implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "player_data");
	/** Highest amount of curruisis a given limb can have */
	public static final int MAX_CURRUISIS = 3;
	/** Highest possible total curruisis across all body parts */
	public static final int TOTAL_CURRUISIS = MAX_CURRUISIS * EnumBodyPart.values().length;
	/** Total amount of damage any given limb can take */
	public static final float HEALTH_PER_LIMB = 20F / 6F; 
	
	private Player thePlayer;
	
	private Map<EnumBodyPart, Integer> curruisisMap = new HashMap<>();
	private boolean diedToCurruisis = false;
	
	private boolean isLich = false;
	/** Degrees of decay on each limb */
	private Map<EnumBodyPart, Float> skinDecay = new HashMap<>();
	/** Current degrees of decay on each limb */
	private Map<EnumBodyPart, Float> skinDecayOld = new HashMap<>();
	
	private int tickCounter = 0;
	
	public PlayerData(Player playerIn)
	{
		this.thePlayer = playerIn;
	}
	
	public void setPlayer(Player playerIn) { this.thePlayer = playerIn; }
	
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return CMCapabilities.PLAYER_DATA.orEmpty(cap, LazyOptional.of(() -> this));
	}
	
	public static PlayerData getCapability(Player player)
	{
		if(player == null)
			return null;
		else if(player.getLevel().isClientSide())
			return ClientSetupEvents.getPlayerData(player);
		
		PlayerData data = player.getCapability(CMCapabilities.PLAYER_DATA).orElse(new PlayerData(player));
		data.thePlayer = player;
		return data;
	}
	
	public CompoundTag serializeNBT()
	{
		CompoundTag data = new CompoundTag();
		
		data.putBoolean("WasPetrified", diedToCurruisis);
		
		ListTag curruisis = new ListTag();
		for(Entry<EnumBodyPart, Integer> entry : curruisisMap.entrySet())
			if(entry.getValue() > 0)
			{
				CompoundTag nbt = new CompoundTag();
				nbt.putString("Limb", entry.getKey().getSerializedName());
				nbt.putInt("Value", entry.getValue());
				curruisis.add(nbt);
			}
		data.put("Curruisis", curruisis);
		
		data.putBoolean("IsLich", isLich);
		if(isLich)
		{
			ListTag partList = new ListTag();
			for(EnumBodyPart limb : EnumBodyPart.values())
			{
				CompoundTag compound = new CompoundTag();
				compound.putString("Name", limb.getSerializedName());
				compound.putFloat("Value", this.skinDecay.getOrDefault(limb, 0F));
				partList.add(compound);
			}
			data.put("LichData", partList);
		}
		
		return data;
	}
	
	public void deserializeNBT(CompoundTag nbt)
	{
		this.diedToCurruisis = nbt.getBoolean("WasPetrified");
		
		this.curruisisMap.clear();
		ListTag curruisis = nbt.getList("Curruisis", Tag.TAG_COMPOUND);
		for(int i=0; i<curruisis.size(); i++)
		{
			CompoundTag data = curruisis.getCompound(i);
			EnumBodyPart limb = EnumBodyPart.fromString(data.getString("Limb"));
			int value = data.getInt("Value");
			if(value > 0 && limb != null)
				this.curruisisMap.put(limb, value);
		}
		
		this.isLich = nbt.getBoolean("IsLich");
		if(nbt.contains("LichData", Tag.TAG_LIST))
		{
			ListTag partList = nbt.getList("LichData", Tag.TAG_COMPOUND);
			
			this.skinDecay.clear();
			for(int i=0; i<partList.size(); i++)
			{
				CompoundTag compound = partList.getCompound(i);
				this.skinDecay.put(EnumBodyPart.fromString(compound.getString("Name")), compound.getFloat("Value"));
			}
		}
	}
	
	public void tick(Level worldIn)
	{
		if(!this.thePlayer.isAlive())
			return;
		
		if(worldIn.isClientSide())
			tickClient(worldIn);
		else
			tickServer(worldIn);
	}
	
	private void tickClient(Level worldIn)
	{
		if(isALich())
		{
			/** Increment all limb values towards their targets */
			for(EnumBodyPart limb : EnumBodyPart.values())
			{
				float decay = skinDecayOld.getOrDefault(limb, 0F);
				float target = skinDecay.getOrDefault(limb, 0F);
				
				if(decay != target)
				{
					float sep = (target - decay);
					
					
					skinDecayOld.put(limb, decay + (float)(Math.min(1F / Reference.Values.TICKS_PER_SECOND, Math.abs(sep)) * Math.signum(sep)));
				}
			}
		}
	}
	
	private void tickServer(Level worldIn)
	{
		if(++tickCounter % Reference.Values.TICKS_PER_SECOND > 0)
			return;
		
		if(this.diedToCurruisis)
		{
			ameliorateCurruisis(this.thePlayer.getRandom());
			this.diedToCurruisis = false;
		}
		else if(isFullyCurruided() && !this.thePlayer.isInvulnerableTo(CMDamageSource.PETRIFICATION) && !isALich())
		{
			BlockPos feetPos = this.thePlayer.blockPosition();
			BlockPos headPos = new BlockPos(feetPos.getX(), this.thePlayer.getEyeY(), feetPos.getZ());
			if(worldIn.getBlockState(feetPos).canBeReplaced(Fluids.FLOWING_WATER) && worldIn.getBlockState(headPos).canBeReplaced(Fluids.FLOWING_WATER))
			{
				worldIn.setBlockAndUpdate(feetPos, CMBlocks.STATUE.get().defaultBlockState());
				worldIn.setBlockAndUpdate(headPos, CMBlocks.STATUE.get().defaultBlockState().cycle(Statue.HALF));
			}
			
			this.thePlayer.hurt(CMDamageSource.PETRIFICATION, Float.MAX_VALUE);
		}
		
		if(isALich())
		{
			LivingData.trySpendManaFrom(thePlayer, 1F);
			
			/** If the player is at full health, reset all decay targets */
			if(thePlayer.getHealth() >= thePlayer.getAttributeValue(Attributes.MAX_HEALTH))
			{
				if(!skinDecay.isEmpty())
				{
					skinDecay.clear();
					markDirty();
				}
			}
			else
			{
				/**
				 * Calculate total decay displayed
				 * Compare to player's current health within range of 1 to max health
				 * 
				 * If higher: Reduce decay
				 * If lower: Increase decay
				 */
				
				/** Total damage represented by decay */
				float totalDecay = 0F;
				for(Float val : this.skinDecay.values())
					totalDecay += val;
				
				/** Player health within margin */
				float actualDecay = Math.max(0F, (float)thePlayer.getAttributeValue(Attributes.MAX_HEALTH) - (thePlayer.getHealth() + 1));
				
				if(actualDecay != totalDecay)
				{
					attemptDecay(Math.signum(actualDecay - totalDecay), RandomSource.create());
					markDirty();
				}
			}
		}
	}
	
	private void attemptDecay(float amount, RandomSource rand)
	{
		/** If amount is zero, no changes need to be made */
		if(amount == 0F)
			return;
		
		for(EnumBodyPart limb : skinDecay.keySet())
			amount = decayLimb(limb, amount, rand);
		
		/** Spread damage to additional limbs if necessary */
		if(amount > 0F && skinDecay.size() < EnumBodyPart.values().length)
		{
			EnumSet<EnumBodyPart> options = skinDecay.isEmpty() ? EnumSet.allOf(EnumBodyPart.class) : EnumSet.noneOf(EnumBodyPart.class);
			
			if(!skinDecay.isEmpty())
			{
				Collection<EnumBodyPart> existing = skinDecay.keySet();
				for(EnumBodyPart limb : existing)
					if(skinDecay.get(limb) >= 0.6F)
						options.addAll(limb.getPotentialSpread());
				existing.forEach((limb) -> options.remove(limb));
			}
			
			if(!options.isEmpty())
			{
				EnumBodyPart limb = options.toArray(new EnumBodyPart[0])[rand.nextInt(options.size())]; 
				amount = decayLimb(limb, amount, rand);
			}
		}
		
		/** If amount has not been expended, repeat process until it has been */
		if(amount != 0F)
			attemptDecay(amount, rand);
	}
	
	private float decayLimb(EnumBodyPart limb, float amount, RandomSource rand)
	{
		float current = skinDecay.getOrDefault(limb, 0F);
		if(current == 0F && amount < 0)
			return amount;
		else if(current == HEALTH_PER_LIMB && amount > 0F)
			return amount;
		
		float add = Mth.clamp(amount * rand.nextFloat(), -current, HEALTH_PER_LIMB - current);
		skinDecay.put(limb, current + add);
		return amount - add;
	}
	
	public boolean hasCurruisis() { return !this.curruisisMap.isEmpty(); }
	
	public void clearCurruisis() { this.curruisisMap.clear(); markDirty(); }
	
	public void ameliorateCurruisis(RandomSource rand)
	{
		if(!isFullyCurruided())
			return;
		
		for(int i=(int)(TOTAL_CURRUISIS * 0.6D); i>0; i--)
		{
			Set<EnumBodyPart> limbs = this.curruisisMap.keySet();
			EnumBodyPart[] limbsArray = limbs.toArray(new EnumBodyPart[0]);
			
			EnumBodyPart limb = limbsArray[rand.nextInt(limbsArray.length)];
			int value = this.curruisisMap.get(limb) - 1;
			if(value <= 0)
				this.curruisisMap.remove(limb);
			else
				setCurruisis(limb, value);
		}
		markDirty();
	}
	
	/** Returns the progression of curruisis, as a float value between 0 and 1 */
	public float curruisisIntensity()
	{
		float intensity = 0F;
		for(EnumBodyPart limb : EnumBodyPart.values())
			intensity += (float)this.curruisisMap.getOrDefault(limb, 0);
		return intensity / (float)TOTAL_CURRUISIS;
	}
	
	public boolean isFullyCurruided()
	{
		for(EnumBodyPart limb : EnumBodyPart.values())
			if(!this.curruisisMap.containsKey(limb) || this.curruisisMap.get(limb) < MAX_CURRUISIS)
				return false;
		
		return true;
	}
	
	public Map<EnumBodyPart, Integer> getCurruisis() { return this.curruisisMap; }
	
	public void addCurruisis(RandomSource rand)
	{
		if(isALich())
			return;
		
		if(this.curruisisMap.isEmpty())
		{
			// Pick a random limb and set it to curruisis=1
			this.curruisisMap.put(EnumBodyPart.getRandomLimb(rand), 1);
		}
		else if(!isFullyCurruided())
		{
			// Find a random afflicted limb with curruisis < MAX_CURRUISIS
			// Increment
			// Else, select random available limb to grow to, and set it to curruisis=1
			
			boolean success = false;
			for(EnumBodyPart limb : this.curruisisMap.keySet())
			{
				int value = this.curruisisMap.get(limb);
				if(value < MAX_CURRUISIS)
				{
					setCurruisis(limb, value + 1);
					success = true;
					break;
				}
			}
			
			if(!success)
			{
				EnumSet<EnumBodyPart> options = EnumSet.noneOf(EnumBodyPart.class);
				Set<EnumBodyPart> existing = this.curruisisMap.keySet();
				for(EnumBodyPart part : this.curruisisMap.keySet())
					part.getPotentialSpread().forEach((limb) -> 
					{
						if(!existing.contains(limb) && !options.contains(limb))
							options.add(limb);
					});
				
				if(options.size() > 1 && options.contains(EnumBodyPart.HEAD))
					options.remove(EnumBodyPart.HEAD);
				
				EnumBodyPart[] optionsArray = options.toArray(new EnumBodyPart[0]);
				setCurruisis(optionsArray[rand.nextInt(optionsArray.length)], 1);
			}
		}
	}
	
	public void flagPetrified() { this.diedToCurruisis = true; markDirty(); }
	
	public void setCurruisis(EnumBodyPart part, int severity)
	{
		this.curruisisMap.put(part, severity);
		this.markDirty();
	}
	
	public static boolean isLich(Entity player)
	{
		if(player.getType() == EntityType.PLAYER)
		{
			PlayerData data = PlayerData.getCapability((Player)player);
			return data != null && data.isALich();
		}
		return false;
	}
	
	public boolean isALich() { return this.isLich; }
	
	public void setIsLich(boolean bool)
	{
		this.isLich = bool;
		markDirty();
	}
	
	public void markDirty()
	{
		if(!this.thePlayer.getLevel().isClientSide())
			PacketHandler.sendToAll((ServerLevel)this.thePlayer.getLevel(), new PacketSyncPlayerData(this.thePlayer.getUUID(), this.serializeNBT()));
	}
	
	public float getSkinDecay(EnumBodyPart limb)
	{
		float decay = Mth.clamp(this.skinDecayOld.getOrDefault(limb, 0F) / HEALTH_PER_LIMB, 0F, 1F); 
		return (float)(int)(decay * 10F) / 10F;
	}
	
	public boolean hasSkinDecay()
	{
		for(EnumBodyPart limb : EnumBodyPart.values())
			if(getSkinDecay(limb) > 0F)
				return true;
		return false;
	}
	
	public static enum EnumBodyPart implements StringRepresentable
	{
		HEAD,
		TORSO,
		LEFT_ARM,
		RIGHT_ARM,
		LEFT_LEG,
		RIGHT_LEG;
		
		public static EnumBodyPart getRandomLimb(RandomSource rand) { return values()[rand.nextInt(values().length)]; }
		
		public String getSerializedName() { return name().toLowerCase() ;}
		
		public EnumSet<EnumBodyPart> getPotentialSpread()
		{
			switch(this)
			{
				case HEAD:		return EnumSet.of(TORSO);
				case LEFT_ARM:	return EnumSet.of(TORSO);
				case LEFT_LEG:	return EnumSet.of(TORSO);
				case RIGHT_ARM:	return EnumSet.of(TORSO);
				case RIGHT_LEG:	return EnumSet.of(TORSO);
				case TORSO:		return EnumSet.of(HEAD, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG);
			}
			
			return EnumSet.noneOf(EnumBodyPart.class);
		}
		
		@Nullable
		public static EnumBodyPart fromString(String name)
		{
			for(EnumBodyPart part : values())
				if(part.getSerializedName().equalsIgnoreCase(name))
					return part;
			return null;
		}
	}
}
