package com.lying.misc19.magic.component;

import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarLevel;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.Slot;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class RandomGlyph extends OperationGlyph
{
	public IVariable getResult(VariableSet variablesIn)
	{
		if(inputs().isEmpty())
			return VariableSet.DEFAULT;
		else if(inputs().size() == 1)
			return getVariable(0, variablesIn);
		
		Level world = ((VarLevel)variablesIn.get(Slot.WORLD)).get();
		RandomSource rand = world.random;
		return getVariable(rand.nextInt(inputs().size()), variablesIn);
	}

}
