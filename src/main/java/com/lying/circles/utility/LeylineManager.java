package com.lying.circles.utility;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public class LeylineManager extends SavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_leyline_manager";
	
	private List<BlockPos> leyPoints = Lists.newArrayList();
	
	@Nullable
	private Level world;
	
	public LeylineManager() { this(null); }
	public LeylineManager(@Nullable Level worldIn)
	{
		this.world = worldIn;
	}
	
	@Nullable
	public static LeylineManager instance(Level worldIn)
	{
		if(worldIn.isClientSide())
			return null;
		
		LeylineManager manager = ((ServerLevel)worldIn).getDataStorage().computeIfAbsent(LeylineManager::fromNBT, LeylineManager::new, DATA_NAME);
		manager.world = worldIn;
		return manager;
	}
	
	public static LeylineManager fromNBT(CompoundTag tag)
	{
		LeylineManager manager = new LeylineManager();
		manager.read(tag);
		return manager;
	}
	
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag points = new ListTag();
		this.leyPoints.forEach((point) -> points.add(NbtUtils.writeBlockPos(point)));
		nbt.put("Points", points);
		return nbt;
	}
	
	public void read(CompoundTag nbt)
	{
		this.leyPoints.clear();
		ListTag points = nbt.getList("Points", Tag.TAG_COMPOUND);
		for(int i=0; i<points.size(); i++)
			this.leyPoints.add(NbtUtils.readBlockPos(points.getCompound(i)));
	}
	
	public void addLeyPoint(BlockPos point)
	{
		if(!leyPoints.contains(point))
		{
			leyPoints.add(point);
			setDirty();
		}
	}
	
	public void removeLeyPoint(BlockPos point)
	{
		if(leyPoints.contains(point))
		{
			leyPoints.remove(point);
			setDirty();
		}
	}
	
	public boolean isEmpty() { return this.leyPoints.isEmpty(); }
	
	public int size() { return this.leyPoints.size(); }
	
	public boolean isOnLeyLine(LivingEntity entity)
	{
		if(isEmpty())
			return false;
		
		List<BlockPos> leyLines = getLeyLines();
		
		// Determine if entity is within 8 blocks of a line between connected ley points
		
		return false;
	}

	/** Travelling Salesman between all known ley points */
	public List<BlockPos> getLeyLines()
	{
		List<BlockPos> visited = Lists.newArrayList();
		List<BlockPos> remaining = Lists.newArrayList();
		remaining.addAll(leyPoints);
		
		BlockPos currentPos = this.leyPoints.get(0);
		remaining.remove(currentPos);
		visited.add(currentPos);
		
		while(!remaining.isEmpty())
		{
			BlockPos closestUnvisited = null;
			double closestDist = Double.MAX_VALUE;
			for(BlockPos option : remaining)
			{
				double distTo = currentPos.distSqr(option);
				if(distTo > 0 && distTo < closestDist)
				{
					closestDist = distTo;
					closestUnvisited = option;
				}
			}
			
			if(closestUnvisited != null)
			{
				visited.add(currentPos);
				remaining.remove(closestUnvisited);
				currentPos = closestUnvisited;
			}
		}
		visited.add(currentPos);
		return visited;
	}
	
	public void tick()
	{
		if(world == null || world.isClientSide() || size() < 2)
			return;
		
		ServerLevel server = (ServerLevel)world;
		List<BlockPos> leyLines = getLeyLines();
		for(int i=0; i<leyLines.size(); i++)
		{
			BlockPos pointA = leyLines.get(i);
			BlockPos pointB = leyLines.get((i+1)%leyLines.size());
			
			Vec3 start = new Vec3(pointA.getX() + 0.5D, pointA.getY() + 0.5D, pointA.getZ() + 0.5D);
			Vec3 end = new Vec3(pointB.getX() + 0.5D, pointB.getY() + 0.5D, pointB.getZ() + 0.5D);
			Vec3 offset = end.subtract(start);
			for(int j=0; j<10; j++)
			{
				Vec3 position = start.add(offset.scale(0.1D * j));
				
				for(ServerPlayer player : server.players())
					server.sendParticles(player, ParticleTypes.WITCH, false, position.x, position.y, position.z, 8, 0D, 0D, 0D, 0D);
			}
		}
	}
}
