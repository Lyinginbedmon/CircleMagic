package com.lying.misc19.magic.variable;

import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class VarVec extends VariableBase
{
	private Vec3 value = Vec3.ZERO;
	
	public VarVec(Vec3 vecIn) { this.value = vecIn; }
	public VarVec(double x, double y, double z) { this.value = new Vec3(x, y, z); }
	public VarVec(Direction dir) { this(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()); }
	
	public IVariable create() { return new VarVec(Vec3.ZERO); }
	
	public VarStack asStack() { return new VarStack(new VarDouble(value.x), new VarDouble(value.y), new VarDouble(value.z)); }
	
	public VariableType type() { return VariableType.VECTOR; }
	
	public Component translate()
	{
		return Component.translatable("variable."+Reference.ModInfo.MOD_ID+".vector", 
				(double)Math.round(value.x * 100) / 100, 
				(double)Math.round(value.y * 100) / 100, 
				(double)Math.round(value.z * 100) / 100);
	}
	
	public Vec3 asVec() { return this.value; }
	
	public boolean asBoolean() { return this.value.length() > 0; }
	
	public boolean greater(IVariable var2) { return false; }
	
	public IVariable add(IVariable var2)
	{
		switch(var2.type())
		{
			case VECTOR:
				return new VarVec(this.value.add(var2.asVec()));
			case DOUBLE:
				double doubleVal = var2.asDouble();
				return new VarVec(this.value.add(doubleVal, doubleVal, doubleVal));
			case ENTITY:
			default:
				return new VarVec(this.value);
		}
	}
	
	public IVariable multiply(IVariable var2)
	{
		switch(var2.type())
		{
			case VECTOR:
				return new VarVec(this.value.multiply(var2.asVec()));
			case DOUBLE:
				double doubleVal = var2.asDouble();
				return new VarVec(this.value.multiply(doubleVal, doubleVal, doubleVal));
			case ENTITY:
			default:
				return new VarVec(this.value);
		}
	}
	
	public IVariable divide(IVariable var2)
	{
		switch(var2.type())
		{
			case VECTOR:
				Vec3 vecVal = var2.asVec();
				return multiply(new VarVec(new Vec3(1 / vecVal.x, 1 / vecVal.y, 1 / vecVal.z)));
			case DOUBLE:
				return multiply(new VarDouble(1 / var2.asDouble()));
			case ENTITY:
			default:
				return new VarVec(this.value);
		}
	}
	
	public IVariable dot(VarVec var2) { return new VarDouble(this.value.dot(var2.value)); }
	
	public IVariable cross(VarVec var2) { return new VarVec(this.value.cross(var2.value)); }
	
	public IVariable normalise() { return new VarVec(this.value.normalize()); }
	
	public IVariable length() { return new VarDouble(this.value.length()); }
	
	public CompoundTag save(CompoundTag compound)
	{
		compound.putDouble("X", this.value.x);
		compound.putDouble("Y", this.value.y);
		compound.putDouble("Z", this.value.z);
		return compound;
	}
	
	public void load(CompoundTag compound)
	{
		this.value = new Vec3(compound.getDouble("X"), compound.getDouble("Y"), compound.getDouble("Z"));
	}
}