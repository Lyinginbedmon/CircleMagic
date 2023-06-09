package com.lying.circles.magic;

import com.lying.circles.magic.variable.IVariable;

@FunctionalInterface
public interface IVariableBuilder
{
	public IVariable create();
}
