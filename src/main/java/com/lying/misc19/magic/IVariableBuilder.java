package com.lying.misc19.magic;

import com.lying.misc19.magic.variable.IVariable;

@FunctionalInterface
public interface IVariableBuilder
{
	public IVariable create();
}
