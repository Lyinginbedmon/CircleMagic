package com.lying.misc19.blocks.entity;

import com.lying.misc19.blocks.ICruciblePart;
import com.lying.misc19.blocks.InscribedBlock;
import com.lying.misc19.client.particle.M19Particles;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Blocks;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class InscribedBlockEntity extends BlockEntity
{
	private static final double OSCILLATE_RANGE = 5D;
	private float ticksActive = 0;
	
	public InscribedBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.INSCRIPTION.get(), pos, state);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
	}
	
	public boolean isValid()
	{
		return ((InscribedBlock)getBlockState().getBlock()).isActive(getBlockPos(), getLevel());
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, InscribedBlockEntity tile)
	{
		// TODO Add particles moving away from exposed horizontal faces
		if(tile.isValid())
			tile.ticksActive++;
		else
			tile.ticksActive = 0;
		
		for(Direction face : Direction.Plane.HORIZONTAL)
		{
			if(!world.isEmptyBlock(pos.relative(face)))
				continue;
			
			world.addParticle(M19Particles.SQUARES.get(), pos.getX() + face.getStepX(), pos.getY(), pos.getZ() + face.getStepZ(), face.getStepX(), 0D, face.getStepZ());
		}
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, InscribedBlockEntity tile)
	{
		if(tile.isValid())
		{
			if(world.random.nextInt(Reference.Values.TICKS_PER_SECOND * 5) == 0)
				convertSand(pos.below(2), world);
			
			Vec3 tilePos = new Vec3(pos.getX() + 0.5D, pos.getY() - 1, pos.getZ() + 0.5D);
			AABB area = new AABB(tilePos.x - (OSCILLATE_RANGE / 2), tilePos.y, tilePos.z - (OSCILLATE_RANGE / 2), tilePos.x + (OSCILLATE_RANGE / 2), tilePos.y + 3, tilePos.z + (OSCILLATE_RANGE / 2));
			world.getEntitiesOfClass(ItemEntity.class, area, (ent) -> ent.isAlive() && ent.isOnGround()).forEach((item) -> 
			{
				double dist = Math.sqrt(item.distanceToSqr(tilePos));
				if(dist >= OSCILLATE_RANGE / 2 || world.random.nextInt(4) == 0)
					return;
				
				Vec3 dir = item.position().subtract(tilePos).normalize();
				double strength = 1D - (dist / OSCILLATE_RANGE);
				item.setDeltaMovement(item.getDeltaMovement().add(dir.scale(0.005D * strength)));
			});
		}
	}
	
	private static void convertSand(BlockPos pos, Level world)
	{
		for(int x=-1; x<=1; x++)
			for(int z=-1; z<=1; z++)
			{
				if(x == 0 && z == 0)
					continue;
				
				BlockPos offset = pos.offset(x, 0, z);
				if(!world.isEmptyBlock(offset.above()))
					continue;
				
				BlockState state = world.getBlockState(offset);
				if(state.getBlock() == Blocks.SAND)
					world.setBlockAndUpdate(offset, M19Blocks.TILLED_SAND.get().defaultBlockState());
				else if(state.getBlock() == Blocks.RED_SAND)
					world.setBlockAndUpdate(offset, M19Blocks.TILLED_RED_SAND.get().defaultBlockState());
			}
	}
	
	/** Calculates the efficiency of this pillar for the given crucible based on its distance to the crucible and its next neighbour around it */
	public double getTotalCapBonusFor(double distToNeighbour, double idealDistToNeighbour, BlockPos cruciblePos, double idealDistToCrucible)
	{
		Vec2 pillarVec = new Vec2(getBlockPos().getX() + 0.5F, getBlockPos().getZ() + 0.5F);
		Vec2 crucibleVec = new Vec2(cruciblePos.getX() + 0.5F, cruciblePos.getZ() + 0.5F);
		double distToCrucible = Math.sqrt(pillarVec.distanceToSqr(crucibleVec));
		double spaceEfficiency = 1 - ((distToCrucible <= idealDistToCrucible ? idealDistToCrucible - distToCrucible : distToCrucible - idealDistToCrucible) / idealDistToCrucible);
		
		double spacingEfficiency = 1 - ((distToNeighbour <= idealDistToNeighbour ? idealDistToNeighbour - distToNeighbour : distToNeighbour - idealDistToNeighbour) / idealDistToNeighbour);
		
		int baseBonus = ((ICruciblePart)getBlockState().getBlock()).glyphCapBonus(getBlockPos(), getBlockState(), getLevel(), cruciblePos);
		return baseBonus * (spaceEfficiency * spacingEfficiency);
	}
	
	public float renderTicks() { return ticksActive; }
	
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
}
