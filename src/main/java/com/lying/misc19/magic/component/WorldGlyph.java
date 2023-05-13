package com.lying.misc19.magic.component;

import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarDouble;
import com.lying.misc19.magic.variable.VarEntity;
import com.lying.misc19.magic.variable.VarLevel;
import com.lying.misc19.magic.variable.VarStack;
import com.lying.misc19.magic.variable.VarVec;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.magic.variable.VariableSet.VariableType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class WorldGlyph extends OperationGlyph
{
	/** Returns a value between 0 (occupied) and 2 (replaceable) representing the contents of the given position */
	public static class IsBlockEmpty extends WorldGlyph
	{
		public boolean isValidInput(ISpellComponent input) { return super.isValidInput(input) && inputs().isEmpty(); }
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputs().isEmpty())
				return VariableSet.DEFAULT;
			
			Level world = ((VarLevel)variablesIn.get(Slot.WORLD)).get();
			Vec3 vec = getVariable(0, variablesIn).asVec();
			BlockPos pos = new BlockPos(vec.x, vec.y, vec.z);
			
			if(world.isEmptyBlock(pos))
				return new VarDouble(1);
			else if(world.getBlockState(pos).getMaterial().isReplaceable())
				return new VarDouble(2);
			else
				return new VarDouble(0);
		}
	}
	
	/** Returns a stack containing all entities within the given area */
	public static class EntitiesWithin extends WorldGlyph
	{
		public boolean isValidInput(ISpellComponent input) { return super.isValidInput(input) && inputs().size() < 2; }
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputs().size() < 2)
				return new VarStack();
			
			Level world = ((VarLevel)variablesIn.get(Slot.WORLD)).get();
			
			IVariable var0 = getVariable(0, variablesIn);
			IVariable var1 = getVariable(1, variablesIn);
			
			Vec3 vec = var0.asVec();
			double radius = var1.asDouble();
			if(var0.type() == VariableType.DOUBLE)
			{
				radius = var0.asDouble();
				vec = var1.asVec();
			}
			
			double minY = Math.max(-64, vec.y - radius);
			AABB bounds = new AABB(vec.x - radius, minY, vec.z - radius, vec.x + radius, minY + (radius * 2), vec.z + radius);
			
			VarStack stack = new VarStack();
			for(LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, bounds, (ent) -> ent.isAlive() && !ent.isSpectator()))
				stack = (VarStack)stack.addToStack(new VarEntity(ent));
			for(ItemEntity ent : world.getEntitiesOfClass(ItemEntity.class, bounds, (ent) -> ent.isAlive()))
				stack = (VarStack)stack.addToStack(new VarEntity(ent));
			return stack;
		}
	}
	
	/** Performs a ray trace between two given points, returning a variable holding what (if anything) it hit */
	public static class RayTrace extends WorldGlyph
	{
		public boolean isValidInput(ISpellComponent input) { return super.isValidInput(input) && inputs().size() < 2; }
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputs().size() < 2)
				return VariableSet.DEFAULT;
			
			Level world = ((VarLevel)variablesIn.get(Slot.WORLD)).get();
			Vec3 vecA = getVariable(0, variablesIn).asVec();
			Vec3 vecB = getVariable(1, variablesIn).asVec();
			// FIXME Return entity properly
			HitResult trace = world.clip(new ClipContext(vecA, vecB, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
			switch(trace.getType())
			{
				case BLOCK:
					return new VarVec(((BlockHitResult)trace).getLocation());
				case ENTITY:
					return new VarEntity(((EntityHitResult)trace).getEntity());
				case MISS:
				default:
					return VariableSet.DEFAULT;
			}
		}
	}
}
