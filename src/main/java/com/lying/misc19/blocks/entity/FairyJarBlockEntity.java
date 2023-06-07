package com.lying.misc19.blocks.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.base.Predicate;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.network.PacketFairyLookAt;
import com.lying.misc19.network.PacketHandler;
import com.lying.misc19.reference.Reference;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FairyJarBlockEntity extends BlockEntity
{
	public static final double ORB_RADIUS = 0.2D;
	public static final Vec3 ORB_OFFSET = new Vec3(0.5D, ORB_RADIUS, 0.5D);
	
	private BlockPos lastPlaced = BlockPos.ZERO;
	private double ticksActive = 0;
	private UUID ownerUUID;
	
	private final PersonalityModel personality;
	private int blinkTicks = 0;
	
	private Optional<LookHelper> lookHelper = Optional.empty();
	
	public FairyJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.FAIRY_JAR.get(), pos, state);
		
		Random random = new Random(pos.getX() * pos.getX() - pos.getY() * pos.getY() + pos.getZ() * pos.getZ());
		this.personality = new PersonalityModel(random);
	}
	
	public void setLevel(Level world)
	{
		super.setLevel(world);
		lookHelper = Optional.of(new LookHelper(world.random));
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.put("Personality", this.personality.saveToNbt(new CompoundTag()));
		compound.put("LastPlaced", NbtUtils.writeBlockPos(this.lastPlaced));
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.personality.readFromNbt(compound.getCompound("Personality"));
		this.lastPlaced = NbtUtils.readBlockPos(compound.getCompound("LastPlaced"));
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, FairyJarBlockEntity tile)
	{
		tile.clientUpdate();
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, FairyJarBlockEntity tile)
	{
		tile.lastPlaced = tile.getBlockPos();
		tile.personality.tick();
		
		if(tile.personality.isDirty())
			tile.markDirty();
	}
	
	private void clientUpdate()
	{
		ticksActive++;
		
		Level world = getLevel();
		if(world == null)
			return;
		
		RandomSource random = world.random;
		if(!isBlinking() && random.nextInt(50) == 0)
			blinkTicks = 3;
		else
			blinkTicks--;
		
		lookHelper.ifPresent((look) -> look.tick(orbPos(), world, random));
	}
	
	public void addEmotiveEvent(EmotiveEvent event)
	{
		Level world = getLevel();
		Vec3 dir = new Vec3(0, 0, 1);
		if(world != null)
		{
			RandomSource rand = world.random;
			dir = new Vec3(rand.nextDouble() - 0.5D, 0, rand.nextDouble() - 0.5D);
		}
		addEmotiveEvent(event, orbPos().add(dir));
	}
	
	public void addEmotiveEvent(EmotiveEvent event, Vec3 origin)
	{
		Pair<Emotion, Float> impulse = this.personality.getImpulseFor(event);
		this.personality.addImpulse(impulse.getFirst(), impulse.getSecond());
		PacketHandler.sendToAll((ServerLevel)getLevel(), new PacketFairyLookAt(getBlockPos(), origin));
	}
	
	public float getYaw(float partialTicks) { return lookHelper.isPresent() ? lookHelper.get().yaw(partialTicks) : 0F; }
	
	public float getPitch(float partialTicks) { return lookHelper.isPresent() ? lookHelper.get().pitch(partialTicks) : 0F; }
	
	public BlockPos lastPlaced() { return this.lastPlaced; }
	
	public double renderTicks() { return ticksActive; }
	
	public boolean isBlinking() { return this.blinkTicks > 0; }
	
	public Emotion getExpression() { return this.personality.currentExpression(); }
	
	private Vec3 orbPos()
	{
		return new Vec3(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()).add(ORB_OFFSET);
	}
	
	public void lookAt(Vec3 vecIn)
	{
		this.lookHelper.ifPresent((look) -> look.forceLookAt(vecIn.subtract(orbPos())));
	}
	
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	public CompoundTag getUpdateTag()
	{
		CompoundTag compound = new CompoundTag();
		saveAdditional(compound);
		return compound;
	}
	
	public void markDirty()
	{
		if(getLevel() != null)
		{
			BlockState state = getBlockState();
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
			setChanged();
		}
	}
	
	public boolean isOwner(Entity ent) { return ownerUUID != null && ent.getUUID().equals(ownerUUID); }
	
	public static class LookHelper
	{
		/** How quickly the fairy turns to look at something */
		private static final float FACE_SPEED = 8F;
		
		private static final AABB LOOK_RANGE = new AABB(-1, -1, -1, 1, 1, 1).inflate(16D);
		private static final Predicate<LivingEntity> VALID_LOOK = (entity) -> entity.isAlive() && !entity.isSpectator();
		
		private float lookPitch, lookYaw = 0F;
		
		private float oldPitch, oldYaw = 0F;
		
		/** Ticks until the fairy can look somewhere else */
		private int lookTicks = 0;
		private float targetPitch = 0F;
		private float targetYaw = 0F;
		
		/** Current entity look target */
		private Optional<LivingEntity> lookTarget = Optional.empty();
		
		public LookHelper(RandomSource random)
		{
			setRandomLook(random);
			lookPitch = oldPitch = targetPitch;
			lookYaw = oldYaw = targetYaw;
		}
		
		public void tick(Vec3 orbPos, Level world, RandomSource random)
		{
			oldPitch = lookPitch;
			oldYaw = lookYaw;
			
			lookPitch = getLimited(oldPitch, targetPitch, 1F);
			lookYaw = getLimited(oldYaw, targetYaw, 1F);
			
			if(lookTicks > 0)
			{
				lookTicks--;
				
				this.lookTarget.ifPresent((entity) -> 
				{
					if(VALID_LOOK.apply(entity))
						setLookDir(entity.getEyePosition().subtract(orbPos));
					else
						clearLookTarget();
				});
			}
			else if(random.nextInt(50) == 0)
			{
				// Set a random look target
				lookTarget = Optional.empty();
				
				// Look at random nearby mob
				List<LivingEntity> mobs = world.getEntitiesOfClass(LivingEntity.class, LOOK_RANGE.move(orbPos), VALID_LOOK);
				if(!mobs.isEmpty())
				{
					this.lookTarget = Optional.of(mobs.get(random.nextInt(mobs.size())));
					this.lookTicks = random.nextInt(30) + 60;
				}
				
				// Look at random position
				if(!lookTarget.isPresent())
				{
					setRandomLook(random);
					this.lookTicks = random.nextInt(20) + 30;
				}
			}
		}
			
		private void clearLookTarget()
		{
			this.lookTarget = Optional.empty();
		}
		
		private void setRandomLook(RandomSource random)
		{
			setLookDir((random.nextFloat() - 0.5F) * 60F, random.nextFloat() * 360F);
		}
		
		private float getLimited(float current, float target, float partialTicks)
		{
			float delta = target - current;
			while(delta < -180F)
				delta += 360F;
			while(delta > 180F)
				delta -= 360F;
			
			return current + (Mth.clamp(delta, -FACE_SPEED, FACE_SPEED) * partialTicks);
		}
		
		public float yaw(float partialTicks) { return getLimited(lookYaw, targetYaw, partialTicks); }
		
		public float pitch(float partialTicks) { return getLimited(lookPitch, targetPitch, partialTicks); }
		
		/** Converts a direction vector to corresponding pitch and yaw values */
		public void setLookDir(@Nullable Vec3 vecIn)
		{
			if(vecIn == null) return;
			vecIn = vecIn.normalize();
			setLookDir((float)Math.toDegrees(Math.asin(-vecIn.y)), (float)Math.toDegrees(Math.atan2(vecIn.x, vecIn.z)));
		}
		
		public void setLookDir(float pitch, float yaw)
		{
			this.targetPitch = Mth.clamp(pitch, -30, 80);
			this.targetYaw = yaw;
		}
		
		/** Forces the fairy to look in the given direction for 4 seconds */
		public void forceLookAt(Vec3 direction)
		{
			setLookDir(direction);
			this.lookTicks = 80;
			this.lookTarget = Optional.empty();
		}
	}
	
	public static class PersonalityModel
	{
		private int emotionCool = Reference.Values.TICKS_PER_SECOND;
		private Emotion currentExpression = Emotion.NEUTRAL;
		
		private Map<Emotion, Float> emotionMap = new HashMap<>();
		
		private Map<EmotiveEvent, Pair<Emotion,Float>> model = new HashMap<>();
		
		private boolean isDirty = false;
		
		public PersonalityModel(Random rand)
		{
			for(EmotiveEvent event : EmotiveEvent.values())
			{
				Emotion emote = Emotion.ACTIVE_EMOTIONS[rand.nextInt(Emotion.ACTIVE_EMOTIONS.length)];
				float intensity = rand.nextFloat();
				model.put(event, Pair.of(emote, intensity));
			}
		}
		
		public void tick()
		{
			if(this.emotionCool > 0)
			{
				--this.emotionCool;
				markDirty();
			}
			
			float strongest = getIntensity(this.currentExpression);
			Emotion highest = strongest > 0 ? this.currentExpression : Emotion.NEUTRAL;
			for(Emotion emotion : currentExpression.getMoveOptions())
			{
				float intensity = getIntensity(emotion);
				if(intensity > strongest)
				{
					highest = emotion;
					strongest = intensity;
				}
			}
			degradeImpulses();
			
			if(this.emotionCool <= 0 && highest != currentExpression)
				setExpression(highest);
		}
		
		public Pair<Emotion,Float> getImpulseFor(EmotiveEvent event) { return this.model.getOrDefault(event, Pair.of(Emotion.NEUTRAL, 0F)); }
		
		public void degradeImpulses()
		{
			EnumSet<Emotion> changed = EnumSet.noneOf(Emotion.class);
			for(Emotion emote : this.emotionMap.keySet())
			{
				float value = this.emotionMap.get(emote);
				if(value > 0)
				{
					this.emotionMap.put(emote, Math.max(0F, this.emotionMap.get(emote) - 0.05F));
					changed.add(emote);
				}
			}
			if(!changed.isEmpty())
				markDirty();
		}
		
		public void addImpulse(Emotion emoteIn, float intensityIn)
		{
			if(intensityIn == 0)
				return;
			
			float intensity = getIntensity(emoteIn);
			float totalIntensity = 0;
			for(Emotion emote : this.emotionMap.keySet())
				totalIntensity += this.emotionMap.get(emote);
			float weight = totalIntensity == 0 ? 1F : 1F - (intensity / totalIntensity);
			intensity += (float)(weight * intensityIn);
			
			emotionMap.put(emoteIn, intensity);
		}
		
		public float getIntensity(Emotion emote) { return emotionMap.getOrDefault(emote, 0F); }
		
		public Emotion currentExpression() { return this.currentExpression; }
		
		public void setExpression(Emotion emote)
		{
			this.currentExpression = emote;
			this.emotionCool = Reference.Values.TICKS_PER_SECOND;
			markDirty();
		}
		
		public boolean isDirty() { return this.isDirty; }
		
		public void markDirty() { this.isDirty = true; }
		public void markClean() { this.isDirty = false; }
		
		public CompoundTag saveToNbt(CompoundTag nbt)
		{
			nbt.putString("Expression", this.currentExpression.getSerializedName());
			nbt.putInt("ChangeCool", this.emotionCool);
			ListTag impulses = new ListTag();
			for(Emotion emote : Emotion.values())
			{
				CompoundTag tag = new CompoundTag();
				tag.putString("Name", emote.getSerializedName());
				tag.putFloat("Value", getIntensity(emote));
			}
			nbt.put("Emotions", impulses);
			
			ListTag personality = new ListTag();
			for(EmotiveEvent event : model.keySet())
			{
				CompoundTag tag = new CompoundTag();
				tag.putString("Event", event.getSerializedName());
				
				Pair<Emotion,Float> reaction = model.get(event);
				tag.putString("Reaction", reaction.getFirst().getSerializedName());
				tag.putFloat("Intensity", reaction.getSecond());
				
				personality.add(tag);
			}
			nbt.put("Personality", personality);
			return nbt;
		}
		
		public void readFromNbt(CompoundTag nbt)
		{
			this.currentExpression = Emotion.fromName(nbt.getString("Expression"));
			this.emotionCool = nbt.getInt("ChangeCool");
			
			this.emotionMap.clear();
			ListTag impulses = nbt.getList("Emotions", Tag.TAG_COMPOUND);
			for(int i=0; i<impulses.size(); i++)
			{
				CompoundTag tag = impulses.getCompound(i);
				emotionMap.put(Emotion.fromName(tag.getString("Name")), tag.getFloat("Value"));
			}
			
			this.model.clear();
			ListTag personality = nbt.getList("Personality", Tag.TAG_COMPOUND);
			for(int i=0; i<personality.size(); i++)
			{
				CompoundTag tag = personality.getCompound(i);
				EmotiveEvent event = EmotiveEvent.fromName(tag.getString("Event"));
				if(event == null)
					continue;
				
				Emotion reaction = Emotion.fromName(tag.getString("Reaction"));
				float intensity = tag.getFloat("Intensity");
				model.put(event, Pair.of(reaction, intensity));
			}
		}
	}

	
	public static enum EmotiveEvent implements StringRepresentable
	{
		SHAKE_JAR_OWN,
		SHAKE_JAR_OTHER,
		MOB_HURT,
		OWNER_HURT,
		PLAYER_HURT,
		TRAVEL;
		
		public String getSerializedName() { return this.name().toLowerCase(); }
		
		@Nullable
		public static EmotiveEvent fromName(String nameIn)
		{
			for(EmotiveEvent emote : values())
				if(emote.getSerializedName().equalsIgnoreCase(nameIn))
					return emote;
			return null;
		}
	}
	
	public static enum Emotion implements StringRepresentable
	{
		NEUTRAL,
		HAPPY,
		ANGRY,
		SAD,
		SCARED;
		
		public static Emotion[] ACTIVE_EMOTIONS = new Emotion[] {HAPPY, ANGRY, SAD, SCARED};
		
		private static final Map<Emotion, EnumSet<Emotion>> MOVE_MAP = new HashMap<>();
		
		public String getSerializedName() { return this.name().toLowerCase(); }
		
		public ResourceLocation getTexture() { return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/fairy_jar/"+getSerializedName()+".png"); }
		
		public boolean canMoveInto(Emotion emote) { return getMoveOptions().contains(emote); }
		
		public EnumSet<Emotion> getMoveOptions() { return MOVE_MAP.get(this); }
		
		@NonNull
		public static Emotion fromName(String nameIn)
		{
			for(Emotion emote : values())
				if(emote.getSerializedName().equalsIgnoreCase(nameIn))
					return emote;
			return NEUTRAL;
		}
		
		private static void addMoves(Emotion emote, Emotion... moves)
		{
			EnumSet<Emotion> moveSet = EnumSet.noneOf(Emotion.class);
			for(Emotion emotion : moves)
				moveSet.add(emotion);
			
			MOVE_MAP.put(emote, moveSet);
		}
		
		static
		{
			addMoves(NEUTRAL, HAPPY, ANGRY, SAD, SCARED);
			addMoves(HAPPY, NEUTRAL, SCARED, ANGRY);
			addMoves(SCARED, NEUTRAL, ANGRY);
			addMoves(ANGRY, NEUTRAL, SCARED);
			addMoves(SAD, NEUTRAL, SCARED, ANGRY);
		}
	}
}
