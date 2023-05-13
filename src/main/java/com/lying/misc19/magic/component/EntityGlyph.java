package com.lying.misc19.magic.component;

import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarDouble;
import com.lying.misc19.magic.variable.VarVec;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.VariableType;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class EntityGlyph extends OperationGlyph
{
	public boolean isValidInput(ISpellComponent componentIn)
	{
		return super.isValidInput(componentIn) && inputs().isEmpty();
	}
	
	public static class Position extends EntityGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			if(!inputs().isEmpty())
			{
				IVariable var = getVariable(0, variablesIn);
				if(var.type() == VariableType.ENTITY)
					return new VarVec(var.asVec());
			}
			
			return VariableSet.DEFAULT;
		}
	}
	
	public static class Look extends EntityGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			if(!inputs().isEmpty())
			{
				IVariable var = getVariable(0, variablesIn);
				if(var.type() == VariableType.ENTITY)
					return new VarVec(var.asEntity().getLookAngle());
			}
			
			return VariableSet.DEFAULT;
		}
	}
	
	public static class Motion extends EntityGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			if(!inputs().isEmpty())
			{
				IVariable var = getVariable(0, variablesIn);
				if(var.type() == VariableType.ENTITY)
					return new VarVec(var.asEntity().getDeltaMovement());
			}
			
			return VariableSet.DEFAULT;
		}
	}
	
	public static class Health extends EntityGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			if(!inputs().isEmpty())
			{
				IVariable var = getVariable(0, variablesIn);
				if(var.type() == VariableType.ENTITY && var.asEntity() instanceof LivingEntity)
					return new VarDouble(((LivingEntity)var.asEntity()).getHealth());
			}
			
			return VariableSet.DEFAULT;
		}
	}
	
	public static class Target extends EntityGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			if(!inputs().isEmpty())
			{
				IVariable var = getVariable(0, variablesIn);
				if(var.type() == VariableType.ENTITY)
				{
					Entity ent = var.asEntity();
					return RootGlyph.getEntityTarget(ent);
				}
			}
			
			return VariableSet.DEFAULT;
		}
	}
}
