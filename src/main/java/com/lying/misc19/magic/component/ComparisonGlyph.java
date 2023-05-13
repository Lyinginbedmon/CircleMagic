package com.lying.misc19.magic.component;

import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarBool;
import com.lying.misc19.magic.variable.VariableSet;

public abstract class ComparisonGlyph extends OperationGlyph
{
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
