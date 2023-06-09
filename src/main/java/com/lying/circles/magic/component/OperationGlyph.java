package com.lying.circles.magic.component;

import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarDouble;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.utility.CMUtils;

import net.minecraft.world.phys.Vec2;

public abstract class OperationGlyph extends ComponentBase
{
	protected OperationGlyph(Param... params) { super(params); }
	
	public Category category() { return Category.OPERATION; }
	
	public Type type() { return Type.OPERATION; }
	
	public boolean isValidInput(ISpellComponent componentIn)
	{
		return isNestedOperation() ? (inputs().size() < 3 && componentIn.type() == Type.VARIABLE) : ISpellComponent.canBeInput(componentIn) && inputs().size() < 4;
	}
	
	public boolean isValidOutput(ISpellComponent componentIn)
	{
		return isNestedOperation() ? false : componentIn.type() == Type.VARIABLE;
	}
	
	public boolean isNestedOperation()
	{
		return parent() != null ? (!parent().type().isContainer() || parent().isInput(this)) : false;
	}
	
	public void organise()
	{
		if(!isNestedOperation())
		{
			super.organise();
			return;
		}
		
		float spin = 180F / inputGlyphs.size();
		Vec2 offset = CMUtils.rotate(right().scale(20), spin / 2);
		for(ISpellComponent input : inputGlyphs)
		{
			input.setParent(this, ComponentState.INPUT);
			input.setPositionAndOrganise(offset.x, offset.y);
			offset = CMUtils.rotate(offset, spin);
		}
	}
	
	/** Returns the product of this operation, without setting outputs */
	public abstract IVariable getResult(VariableSet variablesIn);
	
	public VariableSet execute(VariableSet variablesIn)
	{
		return setOutputs(variablesIn, getResult(variablesIn));
	}
	
	public static class Set extends OperationGlyph
	{
		public boolean isValidInput(ISpellComponent componentIn) { return super.isValidInput(componentIn) && inputs().isEmpty(); }
		
		public IVariable getResult(VariableSet variablesIn) { return getVariable(0, variablesIn.glyphExecuted(castingCost())); }
	}
	
	public static class Add extends OperationGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			variablesIn.glyphExecuted(castingCost());
			IVariable value = VariableSet.DEFAULT;
			for(int i=0; i<inputs().size(); i++)
			{
				IVariable variable = getVariable(i, variablesIn);
				if(i == 0)
					value = variable;
				else
					value = value.add(variable);
			}
			return value;
		}
	}
	
	public static class Subtract extends OperationGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			variablesIn.glyphExecuted(castingCost());
			IVariable value = VariableSet.DEFAULT;
			for(int i=0; i<inputs().size(); i++)
			{
				IVariable variable = getVariable(i, variablesIn);
				if(i == 0)
					value = variable;
				else
					value = value.subtract(variable);
			}
			
			return value;
		}
	}
	
	public static class Multiply extends OperationGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			variablesIn.glyphExecuted(castingCost());
			IVariable value = VariableSet.DEFAULT;
			for(int i=0; i<inputs().size(); i++)
			{
				IVariable variable = getVariable(i, variablesIn);
				if(i == 0)
					value = variable;
				else
					value = value.multiply(variable);
			}
			
			return value;
		}
	}
	
	public static class Divide extends OperationGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			variablesIn.glyphExecuted(castingCost());
			IVariable value = VariableSet.DEFAULT;
			for(int i=0; i<inputs().size(); i++)
			{
				IVariable variable = getVariable(i, variablesIn);
				if(i == 0)
					value = variable;
				else
					value = value.divide(variable);
			}
			
			return value;
		}
	}
	
	public static class Modulus extends OperationGlyph
	{
		public IVariable getResult(VariableSet variablesIn)
		{
			variablesIn.glyphExecuted(castingCost());
			if(inputs().size() < 2)
				return VariableSet.DEFAULT;
			
			return new VarDouble(getVariable(0, variablesIn).asDouble() % getVariable(1, variablesIn).asDouble());
		}
	}
}