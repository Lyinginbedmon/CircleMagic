package com.lying.misc19.magic.component;

import java.util.Map;

import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarDouble;
import com.lying.misc19.magic.variable.VarVec;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.VariableType;

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
