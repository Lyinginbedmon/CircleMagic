package com.lying.misc19.magic.variable;

import com.lying.misc19.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class VarBool extends VarDouble
{
	public static final IVariable TRUE = new VarBool(true);
	public static final IVariable FALSE = new VarBool(false);
	
	public VarBool(boolean var) { super(var ? 1D : 0D); }
	
	public IVariable createFresh() { return new VarBool(false); }
	
	public Component translate()
	{
		return Component.translatable("variable."+Reference.ModInfo.MOD_ID+".boolean", asBoolean());
	}
	
	public CompoundTag save(CompoundTag compound)
	{
		compound.putBoolean("Value", this.value > 0D);
		return compound;
	}
	
	public void load(CompoundTag compound)
	{
		this.value = compound.getBoolean("Value") ? 1D : 0D;
	}
}