package com.lying.misc19.magic;

import org.apache.commons.lang3.tuple.Pair;

import com.lying.misc19.magic.component.ComponentBase;
import com.lying.misc19.magic.variable.VarDouble;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.utility.M19Utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec2;

public abstract class ComponentCircle extends ComponentBase
{
	protected static final int CAPACITY = 6;
	
	public Category category() { return Category.CIRCLE; }
	
	public Type type() { return Type.CIRCLE; }
	
	public boolean isValidInput(ISpellComponent component) { return ISpellComponent.canBeInput(component) && this.inputGlyphs.isEmpty(); }
	
	public boolean isValidOutput(ISpellComponent component)
	{
		if(this.outputGlyphs.size() < CAPACITY)
			switch(component.type())
			{
				case CIRCLE:
				case OPERATION:
				case FUNCTION:
					return true;
				case ROOT:
				case HERTZ:
				case VARIABLE:
				default:
					break;
			}
		return false;
	}
	
	public void organise()
	{
		Vec2 core = core().add(position().negated());
		
		float spin = 180F / inputGlyphs.size();
		Vec2 offset = M19Utils.rotate(left().scale(separations().getLeft()), spin / 2);
		for(ISpellComponent input : inputGlyphs)
		{
			input.setParent(this);
			input.setPositionAndOrganise(core.x + offset.x, core.y + offset.y);
			
			offset = M19Utils.rotate(offset, spin);
		}
		
		spin = 360F / outputGlyphs.size();
		offset = M19Utils.rotate(up().scale(separations().getRight()), spin / 2);
		for(ISpellComponent output : outputGlyphs)
		{
			output.setParent(this);
			output.setPositionAndOrganise(core.x + offset.x, core.y + offset.y);
			
			offset = M19Utils.rotate(offset, spin);
		}
	}
	
	public Vec2 core() { return position().add(up().scale(-100F)); }
	
	protected Pair<Float, Float> separations() { return Pair.of(20F, 60F); }
	
	/** Returns how many times this circle should cycle in this execution */
	public int calculateRuns(VariableSet variablesIn)
	{
		if(!inputs().isEmpty())
			return (int)Math.max(getVariable(0, variablesIn).asDouble(), 0);
		
		return 1;
	}
	
	/** Performs circle execution logic once */
	protected abstract VariableSet doRun(VariableSet variablesIn);
	
	public VariableSet execute(VariableSet variablesIn)
	{
		for(int i=0; i<calculateRuns(variablesIn); i++)
			if(!variablesIn.executionLimited())
				variablesIn = doRun(variablesIn.set(Slot.INDEX, new VarDouble(i)));
		return variablesIn.set(Slot.INDEX, VariableSet.DEFAULT);
	}
	
	/** Sequentially executes all child glyphs per execution call */
	public static class Basic extends ComponentCircle
	{
		public VariableSet doRun(VariableSet variablesIn)
		{
			for(ISpellComponent child : this.outputs())
				variablesIn = child.execute(variablesIn).glyphExecuted(child.castingCost());
			return variablesIn;
		}
	}
	
	/** Executes only one glyph per call, useful for high-cost arrangements */
	public static class Step extends ComponentCircle
	{
		private int index = 0;
		
		public VariableSet execute(VariableSet variablesIn)
		{
			if(calculateRuns(variablesIn) == 0)
			{
				this.index = 0;
				return variablesIn;
			}
			else
				return super.execute(variablesIn);
		}
		
		public VariableSet doRun(VariableSet variablesIn)
		{
			if(variablesIn.executionLimited())
				return variablesIn;
			ISpellComponent current = outputs().get(index++ % outputs().size());
			return current.execute(variablesIn).glyphExecuted(current.castingCost());
		}
		
		public void serialiseNBT(CompoundTag nbt)
		{
			if(outputs().size() > 0 && this.index % outputs().size() > 0)
				nbt.putInt("Index", this.index % outputs().size());
		}
		
		public void deserialiseNBT(CompoundTag nbt) { this.index = nbt.getInt("Index"); }
	}
}
