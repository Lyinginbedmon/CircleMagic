package com.lying.misc19.magic.variable;

import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class VarDouble extends VariableBase
{
	protected double value;
	
	public VarDouble(double valueIn) { this.value = valueIn; }
	
	public IVariable createFresh() { return new VarDouble(0D); }
	
	public VariableType type() { return VariableType.DOUBLE; }
	
	public Component translate()
	{
		return Component.translatable("variable."+Reference.ModInfo.MOD_ID+".double", (double)Math.round(value * 100) / 100);
	}
	
	public double asDouble() { return this.value; }
	
	public boolean asBoolean() { return this.value > 0D; }
	
	public boolean greater(IVariable var2)
	{
		switch(var2.type())
		{
			case DOUBLE:
			case STACK:
				return this.value < var2.asDouble();
			case VECTOR:
			case ENTITY:
			default:
				return false;
		}
	}
	
	public IVariable add(IVariable var2)
	{
		switch(var2.type())
		{
			case DOUBLE:
			case STACK:
				return new VarDouble(this.value + var2.asDouble());
			case VECTOR:
				return new VarVec(var2.asVec().add(value, value, value));
			case ENTITY:
			default:
				return new VarDouble(this.value);
		}
	}
	
	public IVariable multiply(IVariable var2)
	{
		switch(var2.type())
		{
			case DOUBLE:
				return new VarDouble(this.value * var2.asDouble());
			case VECTOR:
				return new VarVec(var2.asVec().scale(value));
			case ENTITY:
			case STACK:
			default:
				return new VarDouble(this.value);
		}
	}
	
	public IVariable divide(IVariable var2)
	{
		switch(var2.type())
		{
			case DOUBLE:
				return multiply(new VarDouble(1 / var2.asDouble()));
			case VECTOR:
				Vec3 vecVal = var2.asVec();
				return multiply(new VarVec(new Vec3(1 / vecVal.x, 1 / vecVal.y, 1 / vecVal.z)));
			case ENTITY:
			case STACK:
			default:
				return new VarDouble(this.value);
		}
	}
	
	public CompoundTag save(CompoundTag compound)
	{
		compound.putDouble("Value", this.value);
		return compound;
	}
	
	public void load(CompoundTag compound)
	{
		this.value = compound.getDouble("Value");
	}
}