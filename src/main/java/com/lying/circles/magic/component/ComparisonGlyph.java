package com.lying.circles.magic.component;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarBool;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.magic.variable.VariableSet.VariableType;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class ComparisonGlyph extends OperationGlyph
{
	/** Outputs 1 if all input values are of equal value */
	public static class Equals extends ComparisonGlyph
	{
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				return ComponentError.ERROR;
			else if(inputs().size() < 2 || allInputsStatic())
				return ComponentError.WARNING;
			else 
				return ComponentError.GOOD;
		}
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
			{
				if(inputs().size() < 2)
					tooltip.add(WARNING_ALWAYS_TRUE);
				
				if(allInputsStatic())
					tooltip.add(WARNING_ALL_STATIC);
				
				tooltip.add(standardOutput());
			}
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			String val = "";
			if(inputs().size() < 2)
				return RETURN_1;
			
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), VariableType.DOUBLE).getString();
				if(i < inputs().size() - 1)
					val += " == ";
			}
			return Component.literal(val);
		}
		
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
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT || inputs().isEmpty())
				return ComponentError.ERROR;
			else if(inputs().size() == 1 || allInputsStatic())
				return ComponentError.WARNING;
			else 
				return ComponentError.GOOD;
		}
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			
			if(inputs().size() < 1)
				tooltip.add(ERROR_NEED_MORE_INPUT);
			else if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
			{
				if(inputs().size() == 1)
					tooltip.add(WARNING_ALWAYS_TRUE);
				
				if(allInputsStatic())
					tooltip.add(WARNING_ALL_STATIC);
				
				tooltip.add(standardOutput());
			}
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_1;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), VariableType.DOUBLE).getString();
				if(i < inputs().size() - 1)
					val += " > ";
			}
			return Component.literal(val);
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
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT || inputs().isEmpty())
				return ComponentError.ERROR;
			else if(inputs().size() == 1 || allInputsStatic())
				return ComponentError.WARNING;
			else 
				return ComponentError.GOOD;
		}
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 1)
				tooltip.add(ERROR_NEED_MORE_INPUT);
			else if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
			{
				if(inputs().size() == 1)
					tooltip.add(WARNING_ALWAYS_TRUE);
				
				if(allInputsStatic())
					tooltip.add(WARNING_ALL_STATIC);
				
				tooltip.add(standardOutput());
			}
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_1;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), VariableType.DOUBLE).getString();
				if(i < inputs().size() - 1)
					val += " < ";
			}
			return Component.literal(val);
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
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				return ComponentError.ERROR;
			else if(inputs().isEmpty() || allInputsStatic())
				return ComponentError.WARNING;
			else 
				return ComponentError.GOOD;
		}
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
			{
				if(inputs().isEmpty())
					tooltip.add(WARNING_ALWAYS_TRUE);
				
				if(allInputsStatic())
					tooltip.add(WARNING_ALL_STATIC);
				
				tooltip.add(standardOutput());
			}
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_1;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += " & ";
			}
			return Component.literal(val);
		}
		
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
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				return ComponentError.ERROR;
			else if(inputs().isEmpty() || allInputsStatic())
				return ComponentError.WARNING;
			else 
				return ComponentError.GOOD;
		}
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
			{
				if(inputs().isEmpty())
					tooltip.add(WARNING_ALWAYS_TRUE);
				
				if(allInputsStatic())
					tooltip.add(WARNING_ALL_STATIC);
				
				tooltip.add(standardOutput());
			}
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().isEmpty())
				return RETURN_1;
			
			String val = "NAND{";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += ", ";
			}
			return Component.literal(val + "}");
		}
		
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
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				return ComponentError.ERROR;
			else if(inputs().isEmpty() || allInputsStatic())
				return ComponentError.WARNING;
			else 
				return ComponentError.GOOD;
		}
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			{
				if(inputs().isEmpty())
					tooltip.add(WARNING_ALWAYS_FALSE);
				
				if(allInputsStatic())
					tooltip.add(WARNING_ALL_STATIC);
				
				tooltip.add(standardOutput());
			}
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().isEmpty())
				return RETURN_0;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += " | ";
			}
			return Component.literal(val);
		}
		
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
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				return ComponentError.ERROR;
			else if(inputs().size() < 1 || allInputsStatic())
				return ComponentError.WARNING;
			else 
				return ComponentError.GOOD;
		}
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
			{
				if(inputs().size() < 2)
					tooltip.add(WARNING_ALWAYS_TRUE);
				
				if(allInputsStatic())
					tooltip.add(WARNING_ALL_STATIC);
				
				tooltip.add(standardOutput());
			}
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_1;
			
			String val = "XOR{";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += ", ";
			}
			return Component.literal(val + "}");
		}
		
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
