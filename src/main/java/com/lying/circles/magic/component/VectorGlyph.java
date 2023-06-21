package com.lying.circles.magic.component;

import java.util.List;
import java.util.Map;

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
import net.minecraft.world.phys.Vec3;

public abstract class VectorGlyph extends OperationGlyph
{
	protected static final String RETURN = "gui."+Reference.ModInfo.MOD_ID+".vector_";
	protected static final Param VEC_1 = Param.of("vec_1", VariableType.VECTOR);
	protected static final Param VEC_2 = Param.of("vec_2", VariableType.VECTOR);
	
	protected VectorGlyph(Param... params) { super(params); }
	protected VectorGlyph() { this(VEC_1, VEC_2); }
	
	public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn) && inputs().size() < 2; }
	
	protected abstract IVariable applyTo(Vec3 var1, Vec3 var2);
	
	public List<MutableComponent> extendedTooltip()
	{
		List<MutableComponent> tooltip = Lists.newArrayList();
		if(inputs().size() < paramCount())
			tooltip.add(ERROR_NEED_MORE_INPUT);
		else if(outputs().isEmpty() && state() != ComponentState.INPUT)
			tooltip.add(ERROR_NO_OUTPUT);
		else
			tooltip.add(standardOutput());
		
		return tooltip;
	}
	
	public IVariable getResult(VariableSet variablesIn)
	{
		if(!inputsMet(variablesIn))
			return VariableSet.DEFAULT;
		
		Map<String, IVariable> params = collectParams(variablesIn, inputs(), VEC_1, VEC_2);
		Vec3 var1 = VEC_1.get(params).asVec();
		Vec3 var2 = VEC_2.get(params).asVec();
		return applyTo(var1, var2);
	}
	
	public static class Dot extends VectorGlyph
	{
		public Component getResultString()
		{
			return inputs().size() < 2 ? RETURN_0 : Component.translatable(RETURN+"dot", describeVariable(inputs().get(0), VariableType.VECTOR), describeVariable(inputs().get(1), VariableType.VECTOR));
		}
		
		protected IVariable applyTo(Vec3 var1, Vec3 var2) { return new VarDouble(var1.dot(var2)); }
	}
	
	public static class Cross extends VectorGlyph
	{
		public Component getResultString()
		{
			return inputs().size() < 2 ? RETURN_0 : Component.translatable(RETURN+"cross", describeVariable(inputs().get(0), VariableType.VECTOR), describeVariable(inputs().get(1), VariableType.VECTOR));
		}
		
		protected IVariable applyTo(Vec3 var1, Vec3 var2) { return new VarVec(var1.cross(var2)); }
	}
	
	public static class Normalise extends OperationGlyph
	{
		public Normalise() { super(VEC_1); }
		
		public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn) && inputs().isEmpty(); }
		
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
		
		public Component getResultString()
		{
			return inputs().isEmpty() ? RETURN_0 : Component.translatable(RETURN+"normalise", describeVariable(inputs().get(0), VariableType.VECTOR));
		}
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputsMet(variablesIn))
				return new VarVec(VEC_1.get(collectParams(variablesIn, inputs(), VEC_1)).asVec().normalize());
			return VariableSet.DEFAULT;
		}
	}
	
	public static class Length extends OperationGlyph
	{
		public Length() { super(VEC_1); }
		
		public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn) && inputs().isEmpty(); }
		
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
		
		public Component getResultString()
		{
			return inputs().isEmpty() ? RETURN_0 : Component.translatable(RETURN+"length", describeVariable(inputs().get(0), VariableType.VECTOR));
		}
		
		public IVariable getResult(VariableSet variablesIn)
		{
			if(inputsMet(variablesIn))
				return new VarDouble(VEC_1.get(collectParams(variablesIn, inputs(), VEC_1)).asVec().length());
			return VariableSet.DEFAULT;
		}
	}
	
	public static class Compose extends OperationGlyph
	{
		public boolean isValidInput(ISpellComponent componentIn) { return componentIn.type() == Type.VARIABLE && inputs().size() < 3; }
		
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
		
		public Component getResultString()
		{
			if(inputs().isEmpty())
				return Component.literal("[0, 0, 0]");
			
			Component desc1 = inputs().size() > 0 ? describeVariable(inputs().get(0), VariableType.DOUBLE) : Component.empty();
			Component desc2 = inputs().size() > 1 ? describeVariable(inputs().get(1), VariableType.DOUBLE) : Component.empty();
			Component desc3 = inputs().size() > 2 ? describeVariable(inputs().get(2), VariableType.DOUBLE) : Component.empty();
			switch(inputs().size())
			{
				case 1:
					return Component.literal("["+desc1.getString()+", 0, 0]");
				case 2:
					return Component.literal("["+desc1.getString()+", 0, "+desc2.getString()+"]");
				case 3:
					return Component.literal("["+desc1.getString()+", "+desc2.getString()+", "+desc3.getString()+"]");
			}
			return RETURN_NAN;
		}
		
		public IVariable getResult(VariableSet variablesIn)
		{
			double x = 0D, y = 0D, z = 0D;
			switch(inputs().size())
			{
				case 0:
					break;
				case 1:
					x = getVariable(0, variablesIn).asDouble();
					break;
				case 2:
					x = getVariable(0, variablesIn).asDouble();
					z = getVariable(1, variablesIn).asDouble();
					break;
				case 3:
					x = getVariable(0, variablesIn).asDouble();
					y = getVariable(1, variablesIn).asDouble();
					z = getVariable(2, variablesIn).asDouble();
					break;
			}
			
			return new VarVec(new Vec3(x, y, z));
		}
	}
}
