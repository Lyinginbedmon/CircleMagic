package com.lying.circles.magic.component;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarDouble;
import com.lying.circles.magic.variable.VarVec;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.magic.variable.VariableSet.VariableType;
import com.lying.circles.reference.Reference;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class EntityGlyph extends OperationGlyph
{
	protected static final String RETURN = "gui."+Reference.ModInfo.MOD_ID+".entity_";
	
	public boolean isValidInput(ISpellComponent componentIn)
	{
		return super.isValidInput(componentIn) && inputs().isEmpty();
	}
	
	public ComponentError getErrorState()
	{
		if(inputs().isEmpty() || outputs().isEmpty() && state() != ComponentState.INPUT)
			return ComponentError.ERROR;
		else 
			return ComponentError.GOOD;
	}
	
	public List<MutableComponent> extendedTooltip()
	{
		List<MutableComponent> tooltip = Lists.newArrayList();
		if(inputs().isEmpty())
			tooltip.add(ERROR_NEED_MORE_INPUT);
		else if(outputs().isEmpty() && state() != ComponentState.INPUT)
			tooltip.add(ERROR_NO_OUTPUT);
		else
			tooltip.add(standardOutput());
		
		return tooltip;
	}
	
	public static class Position extends EntityGlyph
	{
		public Component getResultString() { return inputs().isEmpty() ? ERROR_NEED_MORE_INPUT : Component.translatable(RETURN+"pos", describeVariable(inputs().get(0), null)); }
		
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
		public Component getResultString() { return inputs().isEmpty() ? ERROR_NEED_MORE_INPUT : Component.translatable(RETURN+"look", describeVariable(inputs().get(0), null)); }
		
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
		public Component getResultString() { return inputs().isEmpty() ? ERROR_NEED_MORE_INPUT : Component.translatable(RETURN+"vel", describeVariable(inputs().get(0), null)); }
		
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
		public Component getResultString() { return inputs().isEmpty() ? ERROR_NEED_MORE_INPUT : Component.translatable(RETURN+"hp", describeVariable(inputs().get(0), null)); }
		
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
		public Component getResultString() { return inputs().isEmpty() ? ERROR_NEED_MORE_INPUT : Component.translatable(RETURN+"target", describeVariable(inputs().get(0), null)); }
		
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
