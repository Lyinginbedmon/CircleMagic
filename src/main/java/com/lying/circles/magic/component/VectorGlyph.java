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

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

public abstract class VectorGlyph extends OperationGlyph
{
	protected static final Param VEC_1 = Param.of("vec_1", VariableType.VECTOR);
	protected static final Param VEC_2 = Param.of("vec_2", VariableType.VECTOR);
	
	protected VectorGlyph(Param... params) { super(params); }
	protected VectorGlyph() { this(VEC_1, VEC_2); }
	
	public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn) && inputs().size() < 2; }
	
	protected abstract IVariable applyTo(Vec3 var1, Vec3 var2);
	
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
		protected IVariable applyTo(Vec3 var1, Vec3 var2) { return new VarDouble(var1.dot(var2)); }
	}
	
	public static class Cross extends VectorGlyph
	{
		protected IVariable applyTo(Vec3 var1, Vec3 var2) { return new VarVec(var1.cross(var2)); }
	}
	
	public static class Normalise extends OperationGlyph
	{
		public Normalise() { super(VEC_1); }
		
		public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn) && inputs().isEmpty(); }
		
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
			Component desc1 = inputs().size() > 0 ? describeVariable(inputs().get(0), VariableType.DOUBLE) : Component.empty();
			Component desc2 = inputs().size() > 1 ? describeVariable(inputs().get(1), VariableType.DOUBLE) : Component.empty();
			Component desc3 = inputs().size() > 2 ? describeVariable(inputs().get(2), VariableType.DOUBLE) : Component.empty();
			switch(inputs().size())
			{
				case 0:
					tooltip.add(Component.literal("Insufficient inputs").withStyle(ChatFormatting.RED));
					break;
				case 1:
					tooltip.add(Component.literal("Resulting vector: ["+desc1.getString()+", 0, 0]"));
					break;
				case 2:
					tooltip.add(Component.literal("Resulting vector: ["+desc1.getString()+", 0, "+desc2.getString()+"]"));
					break;
				case 3:
					tooltip.add(Component.literal("Resulting vector: ["+desc1.getString()+", "+desc2.getString()+", "+desc3.getString()+"]"));
					break;
			}
			
			if(outputs().isEmpty() && state() != ComponentState.INPUT)
				tooltip.add(Component.literal("Has no output!").withStyle(ChatFormatting.RED));
			
			return tooltip;
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
