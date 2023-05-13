package com.lying.misc19.magic.variable;

import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.reference.Reference;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class VarLevel extends VariableBase
{
	private Level world;
	
	public VarLevel(Level worldIn)
	{
		this.world = worldIn;
	}
	
	public IVariable createFresh() { return new VarLevel(null); }
	
	public Component translate()
	{
		return Component.translatable("variable."+Reference.ModInfo.MOD_ID+".level", get().dimension().location().toString());
	}
	
	public VariableType type() { return VariableType.WORLD; }
	
	public boolean asBoolean() { return true; }
	public boolean greater(IVariable var2) { return false; }
	public IVariable add(IVariable var2) { return this; }
	public IVariable multiply(IVariable var2) { return this; }
	public IVariable divide(IVariable var2) { return this; }
	
	public Level get() { return this.world; }
}
