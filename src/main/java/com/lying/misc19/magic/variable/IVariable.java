package com.lying.misc19.magic.variable;

import javax.annotation.Nonnull;

import com.lying.misc19.init.SpellVariables;
import com.lying.misc19.magic.variable.VariableSet.VariableType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IVariable
{
	public VariableType type();
	public Component translate();
	
	/** Recaches the value of this variable based on the available world information.<br>Mostly used for entity variables. */
	public default void recache(Level worldIn) { }
	
	public default double asDouble() { return 0D; }
	public boolean asBoolean();
	public default Vec3 asVec() { return Vec3.ZERO; }
	public default Entity asEntity() { return VarEntity.makeFakeEntity(); }
	public default VarStack asStack() { return new VarStack(this); }
	
	public default boolean equals(@Nonnull IVariable var2) { return this == var2; }
	public boolean greater(@Nonnull IVariable var2);
	public default boolean less(@Nonnull IVariable var2) { return !(equals(var2) || greater(var2)); }
	public IVariable add(@Nonnull IVariable var2);
	public default IVariable subtract(@Nonnull IVariable var2) { return add(var2.multiply(new VarDouble(-1D))); }
	public IVariable multiply(@Nonnull IVariable var2);
	public IVariable divide(@Nonnull IVariable var2);
	
	public static CompoundTag saveToNBT(IVariable variable, CompoundTag compound)
	{
		compound.putString("ID", SpellVariables.getRegistryName(variable).toString());
		CompoundTag data = new CompoundTag();
		variable.save(data);
		if(!data.isEmpty())
			compound.put("Data", data);
		return compound;
	}
	
	public default CompoundTag save(CompoundTag compound) { return compound; }
	
	public default void load(CompoundTag compound) { }
	
	public ResourceLocation getRegistryName();
	public void setRegistryName(ResourceLocation nameIn);
}