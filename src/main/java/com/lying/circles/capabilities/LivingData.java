package com.lying.circles.capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.circles.client.ClientSetupEvents;
import com.lying.circles.init.CMCapabilities;
import com.lying.circles.init.CMDamageSource;
import com.lying.circles.init.CMStatusEffects;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.network.PacketSyncLivingData;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.ManaReserve;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LivingData implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "living_data");
	private static final float INITIAL_CAPACITY = 1000F;
	
	private LivingEntity theEntity;
	
	private float manaCapacity = -1F;
	private float currentMana = manaCapacity;
	private float recoveryRate = 1F;
	
	private int tickCounter = 0;
	
	public LivingData(LivingEntity livingIn)
	{
		this.theEntity = livingIn;
		this.manaCapacity = getBaseMaxMana(livingIn);
	}
	
	public void setLiving(LivingEntity playerIn) { this.theEntity = playerIn; }
	
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return CMCapabilities.LIVING_DATA.orEmpty(cap, LazyOptional.of(() -> this));
	}
	
	@Nullable
	public static LivingData getCapability(LivingEntity player)
	{
		if(player == null)
			return null;
		else if(player.getLevel().isClientSide())
			return ClientSetupEvents.getLivingData(player);
		
		LivingData data = player.getCapability(CMCapabilities.LIVING_DATA).orElse(new LivingData(player));
		data.theEntity = player;
		return data;
	}
	
	public CompoundTag serializeNBT()
	{
		CompoundTag data = new CompoundTag();
		
		data.putFloat("Mana", this.currentMana);
		data.putFloat("Capacity", manaCapacity);
		data.putFloat("Recovery", recoveryRate);
		
		return data;
	}
	
	public void deserializeNBT(CompoundTag nbt)
	{
		this.currentMana = nbt.getFloat("Mana");
		this.manaCapacity = nbt.getFloat("Capacity");
		this.recoveryRate = nbt.getFloat("Recovery");
	}
	
	public void tick(Level worldIn)
	{
		if(!this.theEntity.isAlive() || tickCounter++ % Reference.Values.TICKS_PER_SECOND > 0)
			return;
		
		float capacity = getCurrentCapacity();
		float recoveryRate = getRecoveryRate();
		if(currentMana < capacity || capacity < 0 || recoveryRate < 0)
		{
			currentMana += recoveryRate;
			markDirty();
		}
		else if(currentMana > capacity && capacity > 0)
		{
			currentMana = manaCapacity;
			markDirty();
		}
		
		if(currentMana <= 0)
		{
			ManaReserve reserve = ManaReserve.instance(worldIn);
			float value = Math.abs(currentMana - 1);
			if(reserve != null)
			{
				float inReserve = reserve.getManaFor(this.theEntity.getUUID());
				float delta = Math.min(value, inReserve);
				value -= delta;
				reserve.addManaTo(this.theEntity.getUUID(), -delta);
			}
			
			if(value > 0)
				this.theEntity.hurt(CMDamageSource.OUT_OF_MANA, value);
			
			this.currentMana = 1;
			markDirty();
		}
		else if(currentMana > this.manaCapacity * 2 && !this.theEntity.isInvulnerableTo(CMDamageSource.TOO_MUCH_MANA))
		{
			Level world = this.theEntity.level;
			if(world.isClientSide())
				return;
			
			resetMana();
			this.theEntity.hurt(CMDamageSource.TOO_MUCH_MANA, Float.MAX_VALUE);
			
			Explosion.BlockInteraction blockInteraction = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this.theEntity) ? Explosion.BlockInteraction.BREAK : Explosion.BlockInteraction.NONE;
			world.explode(this.theEntity, this.theEntity.getX(), this.theEntity.getY(), this.theEntity.getZ(), 3F, blockInteraction);
		}
	}
	
	public static float getBaseMaxMana(LivingEntity living) { return living == null ? INITIAL_CAPACITY : (float)(living.getAttributeBaseValue(Attributes.MAX_HEALTH) / 20D) * INITIAL_CAPACITY; }
	
	public static boolean trySpendManaFrom(LivingEntity living, float amount)
	{
		LivingData data = LivingData.getCapability(living);
		return data != null && data.spendMana(amount);
	}
	
	public void resetMana() { this.currentMana = this.manaCapacity; markDirty(); }
	
	public boolean spendMana(float amount)
	{
		currentMana -= amount;
		markDirty();
		return currentMana > 0;
	}
	
	public float getCurrentMana() { return this.currentMana; }
	
	public float getRecoveryRate()
	{
		float bonus = this.recoveryRate;
		
		if(this.theEntity.getType() == EntityType.PLAYER)
		{
			PlayerData data = PlayerData.getCapability((Player)this.theEntity);
			if(data.isALich())
				bonus = 0F;
			else if(data.hasCurruisis())
				bonus *= 1F - (data.curruisisIntensity() * 2F);
		}
		
		if(hasLeyPower())
			bonus += this.theEntity.getEffect(CMStatusEffects.LEY_POWER.get()).getAmplifier() + 1;
		
		return bonus;
	}
	
	public float getCurrentCapacity()
	{
		return hasLeyPower() ? -1F : this.manaCapacity;
	}
	
	public float getNativeCapacity() { return this.manaCapacity; }
	
	private boolean hasLeyPower() { return this.theEntity.hasEffect(CMStatusEffects.LEY_POWER.get()) && this.theEntity.getEffect(CMStatusEffects.LEY_POWER.get()).getDuration() > 0; }
	
	public void markDirty()
	{
		if(!this.theEntity.getLevel().isClientSide())
			PacketHandler.sendToAll((ServerLevel)this.theEntity.getLevel(), new PacketSyncLivingData(this.theEntity.getUUID(), this.theEntity.position(), this.serializeNBT()));
	}
}
