package com.lying.misc19.magic.variable;

import com.lying.misc19.magic.Element;
import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.reference.Reference;

import net.minecraft.network.chat.Component;

public class VarElement extends VariableBase
{
	private final Element element;
	
	public VarElement(Element elementIn)
	{
		this.element = elementIn;
	}
	
	public VariableType type() { return VariableType.ELEMENT; }
	
	public Component translate() { return Component.translatable("variable."+Reference.ModInfo.MOD_ID+".element", element.name()); }
	
	public boolean asBoolean() { return false; }
	public boolean greater(IVariable var2) { return false; }
	public IVariable add(IVariable var2) { return this; }
	public IVariable multiply(IVariable var2) { return this; }
	public IVariable divide(IVariable var2) { return this; }
	
	public Element get() { return this.element; }
}
