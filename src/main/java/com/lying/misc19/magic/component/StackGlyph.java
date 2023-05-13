package com.lying.misc19.magic.component;

import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.VariableType;

public abstract class StackGlyph extends OperationGlyph
{
	public boolean isValidInput(ISpellComponent componentIn) { return super.isValidInput(componentIn) && inputs().isEmpty(); }
	
	public static class StackGet extends StackGlyph
	{
		public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn) && inputs().size() < 2; }
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputs().size() < 2)
				return VariableSet.DEFAULT;
			
			IVariable stack, index;
			IVariable slot1 = getVariable(0, variablesIn);
			IVariable slot2 = getVariable(1, variablesIn);
			if(slot2.type() == VariableType.DOUBLE)
			{
				index = slot2;
				stack = slot1;
			}
			else
			{
				stack = slot2;
				index = slot1;
			}
			
			return stack.asStack().getFromStack((int)index.asDouble());
		}
	}
	
	public static class StackAdd extends StackGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			return getVariable(0, variablesIn);
		}
		
		public VariableSet execute(VariableSet variablesIn)
		{
			if(inputs().isEmpty())
				return variablesIn;
			
			IVariable input = getResult(variablesIn);
			for(ISpellComponent output : outputs())
				if(output.type() == Type.VARIABLE)
				{
					VariableSigil variable = (VariableSigil)output;
					variablesIn = variable.set(variablesIn, variable.get(variablesIn).asStack().addToStack(input));
				}
			
			return variablesIn;
		}
	}
	
	public static class StackSub extends StackGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			return getVariable(0, variablesIn);
		}
		
		public VariableSet execute(VariableSet variablesIn)
		{
			if(inputs().isEmpty())
				return variablesIn;
			
			IVariable input = getResult(variablesIn);
			for(ISpellComponent output : outputs())
				if(output.type() == Type.VARIABLE)
				{
					VariableSigil variable = (VariableSigil)output;
					variablesIn = variable.set(variablesIn, variable.get(variablesIn).asStack().removeFromStack((int)input.asDouble()));
				}
			
			return variablesIn;
		}
	}
}
