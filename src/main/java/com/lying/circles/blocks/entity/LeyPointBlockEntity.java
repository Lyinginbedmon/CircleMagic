package com.lying.circles.blocks.entity;

import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.CMStatusEffects;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.LeylineManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LeyPointBlockEntity extends BlockEntity
{
	private static final double RANGE = LeylineManager.LEYLINE_RANGE;
	private int serverTicks = 0;
	private boolean reportedToManager = false;
	
	public LeyPointBlockEntity(BlockPos pos, BlockState state)
	{
		super(CMBlockEntities.LEY_POINT.get(), pos, state);
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
		}
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
