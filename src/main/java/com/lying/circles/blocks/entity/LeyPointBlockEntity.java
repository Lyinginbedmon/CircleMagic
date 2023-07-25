package com.lying.circles.blocks.entity;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import com.lying.circles.data.CMBlockTags;
import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.init.CMStatusEffects;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.LeylineManager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;

public class LeyPointBlockEntity extends BlockEntity
{
	private static final double RANGE = LeylineManager.LEYLINE_RANGE;
	private static final int VICINITY = 6;
	private static final float TOTAL_MANA = 1000;
	private static final int MAX_CURRUID = 24;
	
	private int serverTicks = 0;
	private boolean reportedToManager = false;
	
	private float manaStorage = TOTAL_MANA;
	private float manaRecovery = 5F;
	
	public LeyPointBlockEntity(BlockPos pos, BlockState state)
	{
		super(CMBlockEntities.LEY_POINT.get(), pos, state);
	}
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putFloat("Mana", manaStorage);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.manaStorage = compound.getFloat("Mana");
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, LeyPointBlockEntity blockEntity)
	{
		if(blockEntity.serverTicks++ % Reference.Values.TICKS_PER_SECOND == 0)
		{
			int minY = (int)Math.max(pos.getY() - RANGE, world.dimensionType().minY());
			int maxY = (int)(minY + (RANGE * 2));
			AABB bounds = new AABB(pos.getX() - RANGE, minY, pos.getZ() - RANGE, pos.getX() + RANGE, maxY, pos.getZ() + RANGE);
			world.getEntitiesOfClass(LivingEntity.class, bounds).forEach((living) ->  living.addEffect(new MobEffectInstance(CMStatusEffects.LEY_POWER.get(), Reference.Values.TICKS_PER_SECOND * 5)));
			
			if(!blockEntity.reportedToManager)
			{
				LeylineManager manager = LeylineManager.instance(world);
				if(manager != null)
				{
					manager.addLeyPoint(pos);
					blockEntity.reportedToManager = true;
				}
			}
			
			// Place curruid powder in vicinity
			if(world.random.nextInt(500) == 0)
				blockEntity.placeCurruid();
			
			// Affect mana
			blockEntity.handleMana();
		}
	}
	
	public void placeCurruid()
	{
		BlockState dust = CMBlocks.CURRUID_DUST.get().defaultBlockState();
		BlockState block = CMBlocks.CURRUID_BLOCK.get().defaultBlockState();
		
		// Replace a replaceable block with dust
		if(scanRegionAround(worldPosition, level, (pos,world) -> 
		{
			BlockState state = world.getBlockState(pos);
			return !state.is(CMBlockTags.CURRUID) && state.canBeReplaced(Fluids.FLOWING_WATER) && dust.canSurvive(world, pos);
		}, (pos,world) -> world.setBlockAndUpdate(pos, dust)))
			return;
		
		// Replace dust with block
		if(scanRegionAround(worldPosition, level, 
				(pos,world) -> (world.getBlockState(pos).getBlock() == CMBlocks.CURRUID_DUST.get()), 
				(pos,world) ->  world.setBlockAndUpdate(pos, block)))
			return;
		
		// Replace air with block
		if(scanRegionAround(worldPosition, level, 
				(pos,world) -> world.isEmptyBlock(pos), 
				(pos,world) ->  world.setBlockAndUpdate(pos, block)))
			return;
		
		// Replace any block with block
		if(scanRegionAround(worldPosition, level, 
				(pos,world) -> (world.getBlockState(pos).getBlock() != CMBlocks.LEY_POINT.get()), 
				(pos,world) ->  world.setBlockAndUpdate(pos, block)))
			return;
	}
	
	private static boolean scanRegionAround(BlockPos worldPosition, Level level, BiPredicate<BlockPos, Level> predicate, BiConsumer<BlockPos, Level> consumer)
	{
		// TODO Reverse direction of spiral to start at the fringes and move inwards
		int radius2 = VICINITY * VICINITY;
		for(int index=0; index<VICINITY*2; index++)
		{
			int yDiv = (int)Math.ceil(index * 0.5D);
			int yOff = index%2 == 0 ? -yDiv : yDiv;
			
			Vec2 offset = new Vec2(0, 0);
			Vec2 dir = new Vec2(0, -1);
			for(int i=0; i<radius2; i++)
			{
				int x = (int)offset.x;
				int z = (int)offset.y;
				
				BlockPos position = worldPosition.offset(x, yOff, z);
				if(predicate.test(position, level))
				{
					consumer.accept(position, level);
					return true;
				}
				
				if(x == z || (x < 0 && x == -z) || (x > 0 && x == 1-z))
					dir = new Vec2(-dir.y, dir.x);
				
				offset = offset.add(dir);
			}
		}
		return false;
	}
	
	public void handleMana()
	{
		int curruid = getCurruidAround(worldPosition, level, VICINITY);
		
		float curruisis = (float)curruid / MAX_CURRUID;
		
		float totalRecovery = (1F - (curruisis * 2)) * manaRecovery;
		if((totalRecovery < 0 && manaStorage > 0) || (manaStorage < TOTAL_MANA && totalRecovery > 0))
		{
			manaStorage = Mth.clamp(manaStorage + totalRecovery, 0, TOTAL_MANA);
			setChanged();
		}
		
		if(manaStorage == 0F)
		{
			// Detonate
			level.destroyBlock(worldPosition, false);
			
			Explosion.BlockInteraction blockInteraction = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.BREAK : Explosion.BlockInteraction.NONE;
			level.explode(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 3F, blockInteraction);
		}
	}
	
	/** Returns a count of all blocks tagged as curruid around the origin */
	private static int getCurruidAround(BlockPos origin, Level world, int radius)
	{
		int tally = 0;
		int radius2 = radius * radius;
		for(int index=0; index<radius*2; index++)
		{
			int yDiv = (int)Math.ceil(index * 0.5D);
			int yOff = index%2 == 0 ? -yDiv : yDiv;
			
			Vec2 offset = new Vec2(0, 0);
			Vec2 dir = new Vec2(0, -1);
			for(int i=0; i<radius2; i++)
			{
				int x = (int)offset.x;
				int z = (int)offset.y;
				
				BlockPos position = origin.offset(x, yOff, z);
				if(world.getBlockState(position).is(CMBlockTags.CURRUID))
				{
					if(tally++ >= MAX_CURRUID)
						return tally;
				}
				
				if(x == z || (x < 0 && x == -z) || (x > 0 && x == 1-z))
					dir = new Vec2(-dir.y, dir.x);
				
				offset = offset.add(dir);
			}
		}
		return tally;
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, LeyPointBlockEntity blockEntity)
	{
		;
	}
	
	public void setRemoved()
	{
		super.setRemoved();
		
		LeylineManager manager = LeylineManager.instance(level);
		if(manager != null)
			manager.removeLeyPoint(worldPosition);
	}
}
