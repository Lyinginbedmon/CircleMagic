package com.lying.circles.blocks.entity;

import java.util.Optional;
import java.util.UUID;

import com.lying.circles.blocks.entity.FairyPersonalityModel.Emotion;
import com.lying.circles.blocks.entity.FairyPersonalityModel.EmotiveEvent;
import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.network.PacketFairyLookAt;
import com.lying.circles.network.PacketHandler;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FairyJarBlockEntity extends BlockEntity
{
	public static final double ORB_RADIUS = 0.2D;
	public static final Vec3 ORB_OFFSET = new Vec3(0.5D, ORB_RADIUS, 0.5D);
	
	private Optional<BlockPos> lastPlaced = Optional.empty();
	private double ticksActive = 0;
	private UUID ownerUUID;
	
	private Optional<FairyPersonalityModel> personality = Optional.empty();
	private int blinkTicks = 0;
	
	private Optional<FairyLookHelper> lookHelper = Optional.empty();
	
	public FairyJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(CMBlockEntities.FAIRY_JAR.get(), pos, state);
	}
	
	public void setLevel(Level world)
	{
		super.setLevel(world);
		if(!personality.isPresent())
			personality = Optional.of(makeRandomPersonality(world.random));
		
		if(world.isClientSide())
			lookHelper = Optional.of(new FairyLookHelper(world.random));
	}
	
	public static FairyPersonalityModel makeRandomPersonality(RandomSource random)
	{
		return new FairyPersonalityModel(random);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		this.personality.ifPresent((personality) -> compound.put("Personality", personality.saveToNbt(new CompoundTag())));
		this.lastPlaced.ifPresent((pos) -> compound.put("LastPlaced", NbtUtils.writeBlockPos(pos)));
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.personality.ifPresentOrElse((personality) -> personality.readFromNbt(compound.getCompound("Personality")), () -> 
		{
			FairyPersonalityModel personality = new FairyPersonalityModel(RandomSource.create());
			personality.readFromNbt(compound.getCompound("Personality"));
			this.personality = Optional.of(personality);
		});
		
		if(compound.contains("LastPlaced", Tag.TAG_COMPOUND))
			this.lastPlaced = Optional.of(NbtUtils.readBlockPos(compound.getCompound("LastPlaced")));
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, FairyJarBlockEntity tile)
	{
		tile.clientUpdate();
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, FairyJarBlockEntity tile)
	{
		tile.lastPlaced = Optional.of(tile.getBlockPos());
		tile.personality.ifPresent((personality) -> 
		{
			personality.tick();
			if(personality.isDirty())
				tile.markDirty();
		});
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
	
	public void rename(MutableComponent nameIn) { this.personality.ifPresent((fairy) -> fairy.setName(nameIn)); }
	
	public MutableComponent displayName() { return this.personality.isPresent() ? this.personality.get().name() : Component.empty(); }
	
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
		if(!personality.isPresent())
			return;
		
		Pair<Emotion, Float> impulse = this.personality.get().getImpulseFor(event);
		this.personality.get().addImpulse(impulse.getFirst(), impulse.getSecond());
		PacketHandler.sendToAll((ServerLevel)getLevel(), new PacketFairyLookAt(getBlockPos(), origin));
	}
	
	public float getYaw(float partialTicks) { return lookHelper.isPresent() ? lookHelper.get().yaw(partialTicks) : 0F; }
	
	public float getPitch(float partialTicks) { return lookHelper.isPresent() ? lookHelper.get().pitch(partialTicks) : 0F; }
	
	public BlockPos lastPlaced() { return this.lastPlaced.isPresent() ? this.lastPlaced.get() : BlockPos.ZERO; }
	
	public double renderTicks() { return ticksActive; }
	
	public boolean isBlinking() { return this.blinkTicks > 0; }
	
	public Emotion getExpression() { return this.personality.isPresent() ? this.personality.get().currentExpression() : Emotion.NEUTRAL; }
	
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
}
