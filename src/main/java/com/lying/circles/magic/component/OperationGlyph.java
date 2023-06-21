package com.lying.circles.magic.component;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarDouble;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CMUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec2;

public abstract class OperationGlyph extends ComponentBase
{
	public static final MutableComponent WARNING_ALWAYS_TRUE = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".warning_true").withStyle(ChatFormatting.GOLD);
	public static final MutableComponent WARNING_ALWAYS_FALSE = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".warning_false").withStyle(ChatFormatting.GOLD);
	protected static final MutableComponent RETURN_0 = Component.literal("0").withStyle(ChatFormatting.GOLD);
	protected static final MutableComponent RETURN_1 = Component.literal("1").withStyle(ChatFormatting.GOLD);
	protected static final MutableComponent RETURN_NAN = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".return_nan").withStyle(ChatFormatting.RED);
	
	protected OperationGlyph(Param... params) { super(params); }
	
	public Category category() { return Category.OPERATION; }
	
	public Type type() { return Type.OPERATION; }
	
	public boolean isValidInput(ISpellComponent componentIn)
	{
		return isNestedOperation() ? (inputs().size() < 3 && componentIn.type() == Type.VARIABLE) : ISpellComponent.canBeInput(componentIn) && inputs().size() < 4;
	}
	
	public boolean isValidOutput(ISpellComponent componentIn)
	{
		return isNestedOperation() ? false : componentIn.type() == Type.VARIABLE && componentIn instanceof VariableSigil.Local && ((VariableSigil.Local)componentIn).slot().isPlayerAssignable();
	}
	
	protected MutableComponent standardOutput() { return Component.translatable("gui."+Reference.ModInfo.MOD_ID+".standard_return", outputsString(), outputStringByState(), getResultString().getString()); }
	
	public String outputsString()
	{
		String val = "[";
		if(isNestedOperation())
			val += parent().translatedName().getString();
		else
			for(int i=0; i<outputs().size(); i++)
			{
				val += describeVariable(outputs().get(i), null).getString();
				if(i < outputs().size() - 1)
					val += ", ";
			}
		return val + "]";
	}
	
	public String outputStringByState()
	{
		return isNestedOperation() ? " -> " : " = ";
	}
	
	public List<MutableComponent> extendedTooltip()
	{
		List<MutableComponent> tooltip = Lists.newArrayList();
		if(inputs().size() < 2)
			tooltip.add(ERROR_NEED_MORE_INPUT);
		else if(outputs().isEmpty() && state() != ComponentState.INPUT)
			tooltip.add(ERROR_NO_OUTPUT);
		else
			tooltip.add(standardOutput());
		
		return tooltip;
	}
	
	public ComponentError getErrorState()
	{
		if(outputs().isEmpty() && state() != ComponentState.INPUT || inputs().size() < 2)
			return ComponentError.ERROR;
		else 
			return ComponentError.GOOD;
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
	
	public abstract Component getResultString();
	
	public VariableSet execute(VariableSet variablesIn)
	{
		return setOutputs(variablesIn, getResult(variablesIn));
	}
	
	public static class Set extends OperationGlyph
	{
		public ComponentError getErrorState()
		{
			if(outputs().isEmpty() && state() != ComponentState.INPUT || inputs().isEmpty())
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
		
		public Component getResultString() { return inputs().isEmpty() ? RETURN_0 : describeVariable(inputs().get(0), null); }
		
		public boolean isValidInput(ISpellComponent componentIn) { return super.isValidInput(componentIn) && inputs().isEmpty(); }
		
		public IVariable getResult(VariableSet variablesIn) { return getVariable(0, variablesIn.glyphExecuted(castingCost())); }
	}
	
	public static class Add extends OperationGlyph
	{
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 2)
				tooltip.add(ERROR_NEED_MORE_INPUT);
			else if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
				tooltip.add(standardOutput());
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_0;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += " + ";
			}
			return Component.literal(val);
		}
		
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
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 2)
				tooltip.add(ERROR_NEED_MORE_INPUT);
			else if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
				tooltip.add(standardOutput());
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_0;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += " - ";
			}
			return Component.literal(val);
		}
		
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
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 2)
				tooltip.add(ERROR_NEED_MORE_INPUT);
			else if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
				tooltip.add(standardOutput());
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_0;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += " x ";
			}
			return Component.literal(val);
		}
		
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
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 2)
				tooltip.add(ERROR_NEED_MORE_INPUT);
			else if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
				tooltip.add(standardOutput());
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_NAN;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += " / ";
			}
			return Component.literal(val);
		}
		
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
		public boolean isValidInput(ISpellComponent input) { return super.isValidInput(input) && inputs().size() < 2; }
		
		public List<MutableComponent> extendedTooltip()
		{
			List<MutableComponent> tooltip = Lists.newArrayList();
			if(inputs().size() < 2)
				tooltip.add(WARNING_ALWAYS_FALSE);
			else if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(ERROR_NO_OUTPUT);
			else
				tooltip.add(standardOutput());
			
			return tooltip;
		}
		
		public Component getResultString()
		{
			if(inputs().size() < 2)
				return RETURN_0;
			
			String val = "";
			for(int i=0; i<inputs().size(); i++)
			{
				val += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					val += " % ";
			}
			return Component.literal(val);
		}
		
		public IVariable getResult(VariableSet variablesIn)
		{
			variablesIn.glyphExecuted(castingCost());
			if(inputs().size() < 2)
				return VariableSet.DEFAULT;
			
			return new VarDouble(getVariable(0, variablesIn).asDouble() % getVariable(1, variablesIn).asDouble());
		}
	}
}