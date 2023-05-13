package com.lying.misc19.magic.variable;

import net.minecraft.resources.ResourceLocation;

public abstract class VariableBase implements IVariable
{
	private ResourceLocation registryName;
	
	public ResourceLocation getRegistryName() { return this.registryName; }
	public void setRegistryName(ResourceLocation nameIn) { this.registryName = nameIn; }
}
