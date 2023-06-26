package com.lying.circles.magic.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.init.SpellComponents;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.magic.variable.VariableSet.VariableType;
import com.lying.circles.network.PacketAddComponentEffect;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CMUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract class ComponentBase implements ISpellComponent
{
	protected static final MutableComponent ERROR_NO_OUTPUT = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".error_no_output").withStyle(ChatFormatting.RED);
	protected static final MutableComponent ERROR_NEED_MORE_INPUT = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".error_more_inputs").withStyle(ChatFormatting.RED);
	
	private ISpellComponent parent = null;
	private ComponentState state = ComponentState.NORMAL;
	
	private final Param[] inputNeeds;
	protected final List<ISpellComponent> inputGlyphs = Lists.newArrayList();
	protected final List<ISpellComponent> outputGlyphs = Lists.newArrayList();
	
	private ResourceLocation registryName = SpellComponents.GLYPH_DUMMY;
	private float posX = 0F, posY = 0F;
	
	protected ComponentBase(Param... parameters) { this.inputNeeds = parameters; }
	
	public static void notifySpellEffect(Level world, String nameIn, Vec3 pos, CompoundTag dataIn, int ticks)
	{
		if(world.isClientSide())
			return;
		
		PacketAddComponentEffect pkt = new PacketAddComponentEffect(nameIn, pos, dataIn, ticks);
		world.players().forEach((player) -> 
		{
			if(player.distanceToSqr(pos) < (64D * 64D))
				PacketHandler.sendTo((ServerPlayer)player, pkt);
		});
	}
	
	protected int paramCount() { return this.inputNeeds.length; }
	
	public void setParent(ISpellComponent parentIn) { this.parent = parentIn; }
	public void setParent(ISpellComponent parentIn, ComponentState stateIn)
	{
		this.parent = parentIn;
		this.state = stateIn;
	}
	
	public ISpellComponent parent() { return this.parent; }
	
	public ComponentState state() { return this.state; }
	
	public abstract ComponentError getErrorState();
	
	public void setPosition(float x, float y)
	{
		posX = x;
		posY = y;
	}
	
	public Vec2 position()
	{
		if(parent() == null)
			return new Vec2(posX, posY);
		else
			return new Vec2(posX, posY).add(parent().position());
	}
	
	public void setRegistryName(ResourceLocation location) { this.registryName = location; }
	
	public ResourceLocation getRegistryName() { return this.registryName; }
	
	public void organise()
	{
		float spin = 180F / inputGlyphs.size();
		Vec2 offset = CMUtils.rotate(left().scale(20), spin / 2);
		for(ISpellComponent input : inputGlyphs)
		{
			input.setParent(this, ComponentState.INPUT);
			input.setPositionAndOrganise(offset.x, offset.y);
			offset = CMUtils.rotate(offset, spin);
		}
		
		spin = 180F / outputGlyphs.size();
		offset = CMUtils.rotate(right().scale(20), spin / 2);
		for(ISpellComponent output : outputGlyphs)
		{
			output.setParent(this, ComponentState.OUTPUT);
			output.setPositionAndOrganise(offset.x, offset.y);
			offset = CMUtils.rotate(offset, spin);
		}
	}
	
	/** Returns true if every input to this component can never change its value */
	protected boolean allInputsStatic()
	{
		if(inputGlyphs.isEmpty())
			return false;
		
		for(ISpellComponent component : inputs())
			if(!((ComponentBase)component).allInputsStatic())
				return false;
		
		return true;
	}
	
	public void addInput(ISpellComponent component)
	{
		this.inputGlyphs.add(component);
		organise();
	}
	
	public List<ISpellComponent> inputs(){ return this.inputGlyphs; }
	
	public void addOutput(ISpellComponent component)
	{
		this.outputGlyphs.add(component);
		organise();
	}
	
	public List<ISpellComponent> outputs() { return this.outputGlyphs; }
	
	public void remove(ISpellComponent part)
	{
		if(inputGlyphs.contains(part))
			inputGlyphs.remove(part);
		else if(outputGlyphs.contains(part))
			outputGlyphs.remove(part);
	}
	
	/** Retrieves the variable attached to the given input index, if it is a variable glyph */
	protected IVariable getVariable(int index, VariableSet variablesIn)
	{
		if(inputGlyphs.isEmpty() || index >= inputGlyphs.size())
			return VariableSet.DEFAULT;
		
		return getVariable(this.inputGlyphs.get(index), variablesIn);
	}
	
	protected static IVariable getVariable(ISpellComponent input, VariableSet variablesIn)
	{
		if(input.type() == Type.VARIABLE)
			return ((VariableSigil)input).get(variablesIn);
		else if(input instanceof OperationGlyph)
			return ((OperationGlyph)input).getResult(variablesIn);
		else
			return VariableSet.DEFAULT;
	}
	
	public static MutableComponent describeVariable(ISpellComponent variable, @Nullable VariableType type)
	{
		if(variable.type() == Type.VARIABLE)
		{
			VariableSigil var = (VariableSigil)variable;
			if(variable instanceof VariableSigil.Constant)
			{
				if(type == null)
					return describeVariable(variable, var.get(null).type());
				
				IVariable value = var.get(null);
				switch(type)
				{
					case DOUBLE:
						return Component.literal(String.valueOf(value.asDouble()));
					case VECTOR:
						return Component.literal(String.valueOf(value.asVec()));
					case STACK:
						return (MutableComponent)value.asStack().translate();
					case ELEMENT:
						return (MutableComponent)value.translate();
					case ENTITY:
						return Component.translatable("gui."+Reference.ModInfo.MOD_ID+".variable_entity");
					case WORLD:
						break;
					default:
						return (MutableComponent)value.translate();
				}
			}
			else
				return ((VariableSigil.Local)var).slot().translate();
		}
		else if(variable instanceof OperationGlyph)
			return Component.translatable("gui."+Reference.ModInfo.MOD_ID+".variable_operation", ((OperationGlyph)variable).getResultString());
		return variable.translatedName();
	}
	
	public Param[] getInputNeeds() { return inputNeeds; }
	
	public boolean inputsMet(VariableSet variablesIn)
	{
		if(inputNeeds.length == 0)
			return true;
		
		List<VariableType> needs = Lists.newArrayList();
		for(int i=0; i<inputNeeds.length; i++)
			needs.add(inputNeeds[i].type);
		
		for(int i=0; i<inputs().size(); i++)
			needs.remove(getVariable(i, variablesIn).type());
		
		return needs.isEmpty();
	}
	
	/** Sets all output variable glyphs to the given variable */
	protected VariableSet setOutputs(VariableSet variablesIn, IVariable value)
	{
		for(ISpellComponent glyph : outputs())
			if(glyph.type() == Type.VARIABLE)
				variablesIn = ((VariableSigil)glyph).set(variablesIn, value);
		return variablesIn;
	}
	
	/** Creates a map containing all variables needed by this glyph, based on its inputs */
	protected static Map<String, IVariable> collectParams(VariableSet variablesIn, List<ISpellComponent> inputGlyphs, Param... parameters)
	{
		Map<String, IVariable> params = new HashMap<>();
		List<ISpellComponent> inputs = Lists.newArrayList();
		inputs.addAll(inputGlyphs);
		
		for(Param param : parameters)
		{
			ISpellComponent paramInput = null;
			for(ISpellComponent input : inputs)
			{
				IVariable var = getVariable(input, variablesIn);
				if(param.matches(var))
				{
					params.put(param.name, var);
					paramInput = input;
					break;
				}
			}
			if(paramInput != null)
				inputs.remove(paramInput);
		}
		
		return params;
	}
	
	protected static class Param
	{
		private final VariableType type;
		private final String name;
		
		private Param(String nameIn, VariableType typeIn)
		{
			name = nameIn;
			type = typeIn;
		}
		
		public static Param of(String nameIn, VariableType typeIn) { return new Param(nameIn, typeIn); }
		
		// XXX Allow for variable polymorphism instead of rigid type-matching?
		public boolean matches(IVariable variable) { return type == variable.type(); }
		
		public IVariable get(Map<String, IVariable> paramsIn) { return paramsIn.getOrDefault(name, VariableSet.DEFAULT); }
	}
	
	public static enum ComponentError
	{
		GOOD(ChatFormatting.WHITE),
		WARNING(ChatFormatting.GOLD),
		ERROR(ChatFormatting.RED);
		
		private final int colour;
		private final ChatFormatting nameColour;
		
		private ComponentError(ChatFormatting colourIn)
		{
			this.nameColour = colourIn;
			this.colour = colourIn.getColor();
		}
		
		public ChatFormatting displayColor() { return this.nameColour; }
		
		public int color() { return this.colour; }
		
		public boolean isProblem() { return this != GOOD; }
	}
}
