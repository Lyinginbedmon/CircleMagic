package com.lying.circles.magic.component;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.circles.magic.ComponentGlyph;
import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarDouble;
import com.lying.circles.magic.variable.VarVec;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.reference.Reference;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

public abstract class VariableSigil extends ComponentGlyph
{
	protected Category category = Category.VARIABLE;
	
	public Category category() { return category; }
	
	public Type type() { return Type.VARIABLE; }
	
	public abstract IVariable get(VariableSet variablesIn);
	
	public abstract VariableSet set(VariableSet variablesIn, @Nullable IVariable value);
	
	public VariableSet execute(VariableSet variablesIn) { return variablesIn; }
	
	/** Constant variables store useful values and cannot be assigned */
	public static class Constant extends VariableSigil
	{
		private final IVariable value;
		
		public Constant(IVariable varIn)
		{
			this(varIn, Category.CONSTANT);
		}
		
		public Constant(IVariable varIn, Category catIn)
		{
			this.value = varIn;
			this.category = catIn;
		}
		
		public IVariable get(VariableSet variablesIn) { return this.value; }
		
		public VariableSet set(VariableSet variablesIn, @Nullable IVariable value) { return variablesIn; }
		
		public List<MutableComponent> extendedTooltip()
		{
			return state() == ComponentState.OUTPUT ? List.of(Component.literal("Greater Truths cannot be altered").withStyle(ChatFormatting.RED)) : List.of();
		}
		
		public static Constant doubleConst(double varIn) { return new Constant(new VarDouble(varIn)); }
		public static Constant dirConst(Direction varIn) { return new Constant(new VarVec(new Vec3(varIn.getNormal().getX(), varIn.getNormal().getY(), varIn.getNormal().getZ()))); }
		public static Constant vecConst(Vec3 varIn) { return new Constant(new VarVec(varIn)); }
	}
	
	/** Local variables can be set and retrieved by arrangements */
	public static class Local extends VariableSigil
	{
		private final VariableSet.Slot slot;
		
		public Local(VariableSet.Slot slotIn) { this.slot = slotIn; }
		
		public VariableSet.Slot slot() { return slot; }
		
		public MutableComponent translatedName() { return Component.translatable("magic."+Reference.ModInfo.MOD_ID+".local_sigil", slot.translate()); }
		public MutableComponent description() { return slot.isRegister() ? Component.translatable("magic."+Reference.ModInfo.MOD_ID+".local_sigil.description", slot.translate()) : super.description(); }
		
		public boolean playerPlaceable() { return slot.isPlayerPlaceable(); }
		
		public IVariable get(VariableSet variablesIn) { return variablesIn.get(this.slot); }
		
		public VariableSet set(VariableSet variablesIn, @Nullable IVariable value)
		{
			if(slot.isPlayerAssignable())
				variablesIn.set(this.slot, value);
			return variablesIn;
		}
	}
}
