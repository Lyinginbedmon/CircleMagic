package com.lying.circles.magic.component;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarBool;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.magic.variable.VariableSet.VariableType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class ComparisonGlyph extends OperationGlyph
{
	public List<MutableComponent> extendedTooltip()
	{
		List<MutableComponent> tooltip = Lists.newArrayList();
		if(inputs().size() < 2)
			tooltip.add(Component.literal("Insufficient inputs").withStyle(ChatFormatting.RED));
		
		if(outputs().isEmpty() && state() != ComponentState.INPUT)
			tooltip.add(Component.literal("Has no output!").withStyle(ChatFormatting.RED));
		
		return tooltip;
	}
	
	/** Outputs 1 if all input values are of equal value */
	public static class Equals extends ComparisonGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputs().size() > 1)
			{
				IVariable target = getVariable(0, variablesIn);
				for(int i=1; i<inputs().size(); i++)
				{
					IVariable value = getVariable(i, variablesIn);
					if(!value.equals(target))
						return VarBool.FALSE;
				}
			}
			return VarBool.TRUE;
		}
	}
	
	/** Outputs 1 if all double input values are greater than their preceding input value */
	public static class Greater extends ComparisonGlyph
	{
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 1)
				tooltip.add(Component.literal("Insufficient inputs").withStyle(ChatFormatting.RED));
			else if(inputs().size() == 1)
				tooltip.add(Component.literal("Will always return true").withStyle(ChatFormatting.RED));
			else
			{
				String var = "";
				for(int i=0; i<inputs().size(); i++)
				{
					var += describeVariable(inputs().get(i), VariableType.DOUBLE).getString();
					if(i < inputs().size() - 1)
						var += " > ";
				}
				tooltip.add(Component.literal(var));
			}
			
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(Component.literal("Has no output!").withStyle(ChatFormatting.RED));
			
			return tooltip;
		}
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputs().size() > 1)
			{
				IVariable target = getVariable(0, variablesIn);
				for(int i=1; i<inputs().size(); i++)
				{
					IVariable value = getVariable(i, variablesIn);
					if(!value.greater(target))
						return VarBool.FALSE;
					target = value;
				}
			}
			
			return VarBool.TRUE;
		}
	}
	
	/** Outputs 1 if all double input values are less than their preceding input value */
	public static class Less extends ComparisonGlyph
	{
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 1)
				tooltip.add(Component.literal("Insufficient inputs").withStyle(ChatFormatting.RED));
			else if(inputs().size() == 1)
				tooltip.add(Component.literal("Will always return true").withStyle(ChatFormatting.RED));
			else
			{
				String var = "";
				for(int i=0; i<inputs().size(); i++)
				{
					var += describeVariable(inputs().get(i), VariableType.DOUBLE).getString();
					if(i < inputs().size() - 1)
						var += " < ";
				}
				tooltip.add(Component.literal(var));
			}
			
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(Component.literal("Has no output!").withStyle(ChatFormatting.RED));
			
			return tooltip;
		}
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputs().size() > 1)
			{
				IVariable target = getVariable(0, variablesIn);
				for(int i=1; i<inputs().size(); i++)
				{
					IVariable value = getVariable(i, variablesIn);
					if(!value.less(target))
						return VarBool.FALSE;
					target = value;
				}
			}
			
			return VarBool.TRUE;
		}
	}
	
	/** Outputs 1 if all input values are true */
	public static class And extends ComparisonGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			for(int i=0; i<inputs().size(); i++)
				if(!getVariable(i, variablesIn).asBoolean())
					return VarBool.FALSE;
			return VarBool.TRUE;
		}
	}
	
	/** Outputs 1 if all input values are false */
	public static class NAnd extends And
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			for(int i=0; i<inputs().size(); i++)
				if(getVariable(i, variablesIn).asBoolean())
					return VarBool.FALSE;
			return VarBool.TRUE;
		}
	}
	
	/** Outputs 1 if any input value is true */
	public static class Or extends ComparisonGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			for(int i=0; i<inputs().size(); i++)
				if(getVariable(i, variablesIn).asBoolean())
					return VarBool.TRUE;
			return VarBool.FALSE;
		}
	}
	
	/** Outputs 1 if only one input value is true */
	public static class XOR extends ComparisonGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			boolean foundTrue = false;
			for(int i=0; i<inputs().size(); i++)
			{
				IVariable value = getVariable(i, variablesIn);
				if(value.asBoolean())
					if(foundTrue)
						return VarBool.FALSE;
					else
						foundTrue = true;
			}
			return foundTrue ? VarBool.TRUE : VarBool.FALSE;
		}
	}
}
