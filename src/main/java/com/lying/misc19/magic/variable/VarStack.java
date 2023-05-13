package com.lying.misc19.magic.variable;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.init.SpellVariables;
import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class VarStack extends VariableBase
{
	private List<IVariable> variables = Lists.newArrayList();
	
	public VarStack(IVariable... variablesIn)
	{
		for(int i=0; i<variablesIn.length; i++)
			this.variables.add((variablesIn[i]));
	}
	
	public Component translate()
	{
		return Component.translatable("variable."+Reference.ModInfo.MOD_ID+".stack", variables.size());
	}
	
	public IVariable createFresh() { return new VarStack(); }
	
	public VariableType type() { return VariableType.STACK; }
	
	public VariableType stackType() { return variables.isEmpty() ? VariableType.DOUBLE : variables.get(0).type(); }
	
	public VarStack asStack() { return this; }
	
	public boolean asBoolean() { return variables.isEmpty() ? false : variables.get(0).asBoolean(); }
	
	public double asDouble() { return variables.size(); }
	
	public boolean greater(IVariable var2)
	{
		return var2.type() == VariableType.STACK ? variables.size() > ((VarStack)var2).variables.size() :  false;
	}
	
	public IVariable add(IVariable var2) { return this; }
	
	public IVariable multiply(IVariable var2) { return this; }
	
	public IVariable divide(IVariable var2) { return this; }
	
	public IVariable addToStack(IVariable var2)
	{
		if(asDouble() == 0D)
			return new VarStack(var2);
		
		VarStack stack = new VarStack();
		stack.variables.addAll(this.variables);
		if(getFromStack(0).type() != var2.type())
			return stack;
		else
			stack.variables.add(var2);
		
		return stack;
	}
	
	public IVariable getFromStack(int index)
	{
		return variables.get(index % variables.size());
	}
	
	public IVariable removeFromStack(int index)
	{
		variables.remove(index % variables.size());
		return this;
	}
	
	public CompoundTag save(CompoundTag compound)
	{
		if(!variables.isEmpty())
		{
			ListTag vars = new ListTag();
			this.variables.forEach((var) -> vars.add(var.save(new CompoundTag())));
			compound.put("Vars", vars);
		}
		return compound;
	}
	
	public void load(CompoundTag compound)
	{
		if(!compound.contains("Vars", Tag.TAG_LIST))
			return;
		
		ListTag vars = compound.getList("Vars", Tag.TAG_COMPOUND);
		for(int i=0; i<vars.size(); i++)
			this.variables.add(SpellVariables.readFromNbt(vars.getCompound(i)));
	}
	
	public List<IVariable> entries() { return this.variables; }
}
