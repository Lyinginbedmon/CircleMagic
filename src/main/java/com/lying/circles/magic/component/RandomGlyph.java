package com.lying.circles.magic.component;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarLevel;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.magic.variable.VariableSet.Slot;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class RandomGlyph extends OperationGlyph
{
	public List<MutableComponent> extendedTooltip()
	{
		List<MutableComponent> tooltip = Lists.newArrayList();
		if(inputs().isEmpty())
			tooltip.add(Component.literal("Will always return 0").withStyle(ChatFormatting.RED));
		else if(inputs().size() == 1)
			tooltip.add(Component.literal("Will always return "+describeVariable(inputs().get(0), null)).withStyle(ChatFormatting.RED));
		else
		{
			String var = "Randomly returns a value from amongst: ";
			for(int i=0; i<inputs().size(); i++)
			{
				var += describeVariable(inputs().get(i), null).getString();
				if(i < inputs().size() - 1)
					var += ", ";
			}
			tooltip.add(Component.literal(var));
		}
		
		if(outputs().isEmpty() && state() != ComponentState.INPUT)
			tooltip.add(Component.literal("Has no output!").withStyle(ChatFormatting.RED));
		
		return tooltip;
	}
	
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
