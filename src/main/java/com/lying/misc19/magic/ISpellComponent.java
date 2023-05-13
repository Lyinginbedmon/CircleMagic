package com.lying.misc19.magic;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.M19Utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public interface ISpellComponent
{
	public void setRegistryName(ResourceLocation location);
	
	public ResourceLocation getRegistryName();
	
	public default boolean playerPlaceable() { return true; }
	
	public default MutableComponent translatedName() { return Component.translatable("magic."+Reference.ModInfo.MOD_ID+"."+getRegistryName().getPath()); }
	public default MutableComponent description() { return Component.translatable("magic."+Reference.ModInfo.MOD_ID+"."+getRegistryName().getPath()+".description"); }
	
	public default ResourceLocation spriteLocation() { return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/magic/"+category().getSerializedName()+"/"+getRegistryName().getPath()+".png"); }
	
	public void setParent(ISpellComponent parentIn);
	
	/** Component this component is associated with */
	public ISpellComponent parent();
	
	/** Sets relative position to parent (if any)*/
	public void setPosition(float x, float y);
	
	/** Global position in arrangement, including offset from parent */
	public Vec2 position();
	
	/** Position that descendants treat as the target for the purposes of their up() function */
	public default Vec2 core() { return position(); }
	
	/** Update the positions of all child components */
	public void organise();
	
	public default void setPositionAndOrganise(float x, float y)
	{
		setPosition(x, y);
		organise();
	}
	
	/** Relative up direction for this glyph */
	public default Vec2 up()
	{
		if(parent() == null)
			return new Vec2(0, -1);
		
		Vec2 pos = position();
		Vec2 par = parent().core();
		
		return new Vec2(par.x - pos.x, par.y - pos.y).normalized();
	}
	
	public default Vec2 left() { return M19Utils.rotate(up(), -90D); }
	public default Vec2 right() { return M19Utils.rotate(up(), 90D); }
	public default Vec2 down() { return up().negated(); }
	
	public Category category();
	
	public Type type();
	
	public default int castingCost() { return 1; }
	
	public VariableSet execute(VariableSet variablesIn);
	
	public default boolean isValidInput(ISpellComponent component) { return false; }
	
	public static boolean canBeInput(ISpellComponent component) { return component.type().isVariable(); }
	
	public default ISpellComponent addInputs(ISpellComponent... components)
	{
		for(int i=0; i<components.length; i++)
		{
			components[i].setParent(this);
			addInput(components[i]);
		}
		
		return this;
	}
	
	public default boolean isInput(ISpellComponent part) { return inputs().contains(part); }
	
	public default boolean isOutput(ISpellComponent part) { return outputs().contains(part); }
	
	public default void addInput(ISpellComponent component) { }
	
	public default List<ISpellComponent> inputs(){ return Lists.newArrayList(); }
	
	public default boolean isValidOutput(ISpellComponent component) { return false; }
	
	public default ISpellComponent addOutputs(ISpellComponent... components)
	{
		for(int i=0; i<components.length; i++)
		{
			components[i].setParent(this);
			addOutput(components[i]);
		}
		return this;
	}
	
	public default void addOutput(ISpellComponent component) { }
	
	public default List<ISpellComponent> outputs() { return Lists.newArrayList(); }
	
	public void remove(ISpellComponent part);
	
	public default List<ISpellComponent> getParts()
	{
		List<ISpellComponent> parts = Lists.newArrayList();
		parts.add(this);
		inputs().forEach((input) -> parts.addAll(input.getParts()));
		outputs().forEach((output) -> parts.addAll(output.getParts()));
		return parts;
	}
	
	public static CompoundTag saveToNBT(ISpellComponent component)
	{
		CompoundTag nbt = saveAtomically(component);
		
		CompoundTag extra = new CompoundTag();
		component.serialiseNBT(extra);
		if(!extra.isEmpty())
			nbt.put("Data", extra);
		
		if(!component.inputs().isEmpty())
		{
			ListTag children = new ListTag();
			component.inputs().forEach((child) -> children.add(saveToNBT(child)));
			nbt.put("Input", children);
		}
		
		if(!component.outputs().isEmpty())
		{
			ListTag children = new ListTag();
			component.outputs().forEach((child) -> children.add(saveToNBT(child)));
			nbt.put("Output", children);
		}
		
		return nbt;
	}
	
	public static CompoundTag saveAtomically(ISpellComponent component)
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putString("ID", component.getRegistryName().getPath());
		return nbt;
	}
	
	public default void serialiseNBT(CompoundTag nbt) { }
	
	public default void deserialiseNBT(CompoundTag nbt) { }
	
	public static enum Category
	{
		CONSTANT(4),
		ELEMENT(7),
		VARIABLE(5),
		OPERATION(3),
		FUNCTION(6),
		CIRCLE(2),
		HERTZ(1),
		ROOT(0);
		
		private final int index;
		
		private Category(int ind)
		{
			this.index = ind;
		}
		
		public MutableComponent translate() { return Component.translatable("magic."+Reference.ModInfo.MOD_ID+".category."+getSerializedName()); }
		
		public String getSerializedName() { return name().toLowerCase(); }
		
		public int index() { return this.index; }
		
		public static Category byIndex(int index)
		{
			while(index < 0)
				index += values().length;
			
			index %= values().length;
			for(Category cat : values())
				if(cat.index == index)
					return cat;
			return ROOT;
		}
	}
	
	public static enum Type
	{
		ROOT(true, false),
		HERTZ(false, false),
		CIRCLE(true, false),
		VARIABLE(false, true),
		OPERATION(false, true),
		FUNCTION(false, false);
		
		private final boolean isContainer;
		private final boolean isVariable;
		
		private Type(boolean containerIn, boolean variableIn)
		{
			this.isContainer = containerIn;
			this.isVariable = variableIn;
		}
		
		/** Container types are the major building blocks of arrangements, such as root and circle glyphs */
		public boolean isContainer() { return this.isContainer; }
		
		/** Variable types provide values to be used as inputs for other glyphs */
		public boolean isVariable() { return this.isVariable; }
	}
}
