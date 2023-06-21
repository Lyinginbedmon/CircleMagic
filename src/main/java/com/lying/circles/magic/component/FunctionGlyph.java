package com.lying.circles.magic.component;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.google.common.base.Predicate;
import com.lying.circles.CircleMagic;
import com.lying.circles.api.event.SpellEvent;
import com.lying.circles.data.recipe.CreationRecipe;
import com.lying.circles.data.recipe.StatusEffectRecipe;
import com.lying.circles.init.FunctionRecipes;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.init.SpellEffects;
import com.lying.circles.magic.Element;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.magic.variable.IVariable;
import com.lying.circles.magic.variable.VarElement;
import com.lying.circles.magic.variable.VarEntity;
import com.lying.circles.magic.variable.VarLevel;
import com.lying.circles.magic.variable.VarStack;
import com.lying.circles.magic.variable.VariableSet;
import com.lying.circles.magic.variable.VariableSet.Slot;
import com.lying.circles.magic.variable.VariableSet.VariableType;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CMUtils;
import com.lying.circles.utility.SpellData;
import com.lying.circles.utility.SpellManager;

import net.minecraft.commands.CommandFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

/** A glyph that performs an actual function based on its inputs and does not have any outputs */
public abstract class FunctionGlyph extends ComponentBase
{
	protected static final Param POS = Param.of("pos", VariableType.VECTOR);
	protected static final Param ENTITY = Param.of("entity", VariableType.ENTITY);
	private final int cost;
	
	protected FunctionGlyph(int costIn)
	{
		this.cost = costIn;
	}
	
	public Category category() { return Category.FUNCTION; }
	
	public Type type() { return Type.FUNCTION; }
	
	public int castingCost() { return cost; }
	
	public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn); }
	
	public boolean isValidOutput(ISpellComponent componentIn) { return false; }
	
	public void organise()
	{
		Vec2 core = core().add(position().negated());
		
		float spin = 360F / inputGlyphs.size();
		Vec2 offset = CMUtils.rotate(down().scale(20), spin / 2);
		for(ISpellComponent input : inputGlyphs)
			{
				input.setParent(this, ComponentState.INPUT);
				input.setPositionAndOrganise(core.x + offset.x, core.y + offset.y);
				
				offset = CMUtils.rotate(offset, spin);
			}
	}
	
	public VariableSet execute(VariableSet variablesIn)
	{
		performFunction(getElements(variablesIn), filterInputs(variablesIn), variablesIn, ((VarLevel)variablesIn.get(Slot.WORLD)).get());
		return variablesIn;
	}
	
	protected abstract void performFunction(EnumSet<Element> inputElements, List<IVariable> inputVariables, VariableSet totalVariables, Level world);
	
	/** Returns an EnumSet of all element input variables */
	protected EnumSet<Element> getElements(VariableSet variablesIn)
	{
		EnumSet<Element> elements = EnumSet.noneOf(Element.class);
		for(ISpellComponent input : inputs())
		{
			IVariable var = getVariable(input, variablesIn);
			if(!isElementVariable(var))
				continue;
			
			switch(var.type())
			{
				case ELEMENT:
					variablesIn.glyphExecuted(input.castingCost());
					elements.add(((VarElement)var).get());
					break;
				case STACK:
					var.asStack().entries().forEach((entry) -> elements.add(((VarElement)entry).get()));
					variablesIn.glyphExecuted(input.castingCost() * (int)var.asStack().asDouble());
					break;
				default:
					break;
			}
		}
		return elements;
	}
	
	/** Returns all non-element input variables */
	protected List<IVariable> filterInputs(VariableSet variablesIn)
	{
		List<IVariable> vars = Lists.newArrayList();
		for(ISpellComponent input : inputs())
		{
			IVariable var = getVariable(input, variablesIn);
			if(!isElementVariable(var))
				vars.add(var);
		}
		return vars;
	}
	
	@Nullable
	protected static IVariable getFirstOfType(List<IVariable> variables, VariableType type)
	{
		return getFirstOfType(variables, (var) -> var.type() == type);
	}
	
	@Nullable
	protected static IVariable getFirstOfType(List<IVariable> variables, Predicate<IVariable> predicate)
	{
		for(IVariable var : variables)
			if(predicate.apply(var))
				return var;
		return null;
	}
	
	/** Returns true if the variable is an element or a stack of elements */
	private static boolean isElementVariable(IVariable var)
	{
		return var.type() == VariableType.ELEMENT || var.type() == VariableType.STACK && ((VarStack)var).stackType() == VariableType.ELEMENT;
	}
	
	protected static void runCommandOn(Entity target, Level world, CommandFunction.CacheableFunction func)
	{
		if(world.isClientSide() || func == null || func == CommandFunction.CacheableFunction.NONE)
			return;
		
		MinecraftServer server = ((ServerLevel)world).getServer();
		func.get(server.getFunctions()).ifPresent((exec) -> server.getFunctions().execute(exec, target.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
	}
	
	public static class Debug extends FunctionGlyph
	{
		public Debug() { super(0); }
		
		public boolean isValidInput(ISpellComponent part) { return false; }
		
		public ComponentError getErrorState() { return ComponentError.GOOD; }
		
		protected void performFunction(EnumSet<Element> inputElements, List<IVariable> inputVariables, VariableSet totalVariables, Level world)
		{
			List<Component> messages = Lists.newArrayList();
			messages.add(Component.literal("# Debug Glyph #"));
			messages.add(Component.literal("# Glyphs executed: "+totalVariables.totalGlyphs()+" of "+VariableSet.EXECUTION_LIMIT));
			messages.add(Component.literal("# Casting cost: "+totalVariables.totalCastingCost()));
			messages.add(Component.literal("# Register contents:"));
			for(Slot slot : VariableSet.Slot.values())
				if(totalVariables.isUsing(slot))
					messages.add(Component.literal("# * "+slot.name()+": ").append(totalVariables.get(slot).translate()));
			messages.add(Component.literal("# Debug End #"));
			
			if(totalVariables.isUsing(Slot.CASTER))
			{
				Entity caster = totalVariables.get(Slot.CASTER).asEntity();
				if(caster.getType() == EntityType.PLAYER)
				{
					Player player = (Player)caster;
					messages.forEach((line) -> player.displayClientMessage(line, false));
				}
			}
			else
				messages.forEach((line) -> CircleMagic.LOG.info(line.getString()));
		}
	}
	
	public static class AddMotion extends FunctionGlyph
	{
		public AddMotion()
		{
			super(15);
			
//			addAugmentation(AddMotion::doTP, Element.FINIS);
		}
		
		public ComponentError getErrorState() { return inputs().isEmpty() ? ComponentError.ERROR : ComponentError.GOOD; }
		
		protected void performFunction(EnumSet<Element> inputElements, List<IVariable> inputVariables, VariableSet totalVariables, Level world)
		{
			// FIXME Implement motion recipes
		}
		
		protected static void doMove(VariableSet variablesIn, List<ISpellComponent> inputs)
		{
			if(inputs.size() < 2)
				return;
			
			Level world = ((VarLevel)variablesIn.get(Slot.WORLD)).get();
			IVariable var1 = ComponentBase.getVariable(inputs.get(0), variablesIn);
			IVariable var2 = ComponentBase.getVariable(inputs.get(1), variablesIn);
			
			// FIXME Ensure proper motion applied to players
			
			if(var1.type() == VariableType.ENTITY)
				addMotionToEntity(var1.asEntity(), var2.asVec());
			else if(var2.type() == VariableType.ENTITY)
				addMotionToEntity(var2.asEntity(), var1.asVec());
			else if(var1.type() == VariableType.VECTOR && var1.type() == var2.type())
				addMotionToPos(world, var1.asVec(), var2.asVec(), 1D);
		}
		
		private static void addMotionToEntity(Entity ent, Vec3 vel)
		{
			ent.move(MoverType.SELF, vel);
		}
		
		private static void addMotionToPos(Level world, Vec3 pos, Vec3 vel, double maxDist)
		{
			vel = vel.normalize().scale(Math.min(vel.length(), maxDist));
			
			BlockPos currentPos = new BlockPos(pos.x, pos.y, pos.z);
			BlockPos targetPos = currentPos.offset(vel.x, vel.y, vel.z);
			
			if(world.isEmptyBlock(targetPos) || world.getBlockState(targetPos).getMaterial().isReplaceable())
			{
				BlockState state = world.getBlockState(currentPos);
				world.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
				world.setBlockAndUpdate(targetPos, state);
			}
		}
		
		protected static void doTP(VariableSet variablesIn, List<ISpellComponent> inputs)
		{
			if(inputs.size() < 2)
				return;
			
			Level world = ((VarLevel)variablesIn.get(Slot.WORLD)).get();
			IVariable var1 = ComponentBase.getVariable(inputs.get(0), variablesIn);
			IVariable var2 = ComponentBase.getVariable(inputs.get(1), variablesIn);
			
			if(var1.type() == VariableType.ENTITY)
				teleportEntity(var1.asEntity(), var2.asVec());
			else if(var2.type() == VariableType.ENTITY)
				teleportEntity(var2.asEntity(), var1.asVec());
			else if(var1.type() == VariableType.VECTOR && var1.type() == var2.type())
				teleportBlock(world, var1.asVec(), var2.asVec());
		}
		
		private static void teleportEntity(Entity entity, Vec3 pos)
		{
			Vec3 original = entity.position();
			entity.teleportTo(original.x + pos.x, original.y + pos.y, original.z + pos.z);
		}
		
		private static void teleportBlock(Level world, Vec3 pos, Vec3 vel)
		{
			BlockPos currentPos = new BlockPos(pos.x, pos.y, pos.z);
			BlockPos targetPos = currentPos.offset(vel.x, vel.y, vel.z);
			
			if(world.isEmptyBlock(targetPos) || world.getBlockState(targetPos).getMaterial().isReplaceable())
			{
				BlockState state = world.getBlockState(currentPos);
				world.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
				world.setBlockAndUpdate(targetPos, state);
			}
		}
	}
	
	public static class Create extends FunctionGlyph
	{
		public Create()
		{
			super(15);
		}
		
		public ComponentError getErrorState() { return inputs().isEmpty() ? ComponentError.ERROR : ComponentError.GOOD; }
		
		protected void performFunction(EnumSet<Element> inputElements, List<IVariable> inputVariables, VariableSet totalVariables, Level world)
		{
			CreationRecipe recipe = (CreationRecipe)FunctionRecipes.getInstance().getMatchingRecipe(inputElements, FunctionRecipes.CREATION);
			
			Vec3 spellPos = totalVariables.get(Slot.POSITION).asVec();
			if(recipe == null)
			{
				placeBlock(inputVariables, world, CMBlocks.PHANTOM_CUBE.get().defaultBlockState());
				
				Vec3 pos = inputVariables.get(0).asVec();
				CompoundTag data = new CompoundTag();
				data.put("Pos", NbtUtils.writeBlockPos(new BlockPos(pos.x, pos.y, pos.z)));
				notifySpellEffect(world, SpellEffects.PLACE_BLOCK, spellPos, data, Reference.Values.TICKS_PER_SECOND);
			}
			else
			{
				BlockState state = recipe.getState();
				if(state != null)
				{
					placeBlock(inputVariables, world, state);
					
					Vec3 pos = inputVariables.get(0).asVec();
					CompoundTag data = new CompoundTag();
					data.put("Pos", NbtUtils.writeBlockPos(new BlockPos(pos.x, pos.y, pos.z)));
					notifySpellEffect(world, SpellEffects.PLACE_BLOCK, spellPos, data, Reference.Values.TICKS_PER_SECOND);
				}
				
				if(recipe.hasEntity())
				{
					spawnEntity(inputVariables, world, recipe, totalVariables.get(Slot.CASTER).asEntity());
					
					Vec3 pos = inputVariables.get(0).asVec();
					CompoundTag data = new CompoundTag();
					data.putDouble("PosX", pos.x);
					data.putDouble("PosY", pos.y);
					data.putDouble("PosZ", pos.z);
					notifySpellEffect(world, SpellEffects.SPAWN_ENTITY, spellPos, data, Reference.Values.TICKS_PER_SECOND);
				}
				
				CommandFunction.CacheableFunction func = recipe.getFunction();
				if(func != null && func != CommandFunction.CacheableFunction.NONE)
					runCommandOn(getFirstOfType(inputVariables, VariableType.ENTITY).asEntity(), world, func);
			}
		}
		
		protected static void placeBlock(List<IVariable> inputVariables, Level world, BlockState state)
		{
			if(inputVariables.isEmpty() || world.isClientSide())
				return;
			
			if(state.getBlock() instanceof FireBlock && inputVariables.get(0).type() == VariableType.ENTITY)
			{
				inputVariables.get(0).asEntity().setSecondsOnFire(8);
				return;
			}
			
			Vec3 pos = inputVariables.get(0).asVec();
			BlockPos blockPos = new BlockPos(pos.x, pos.y, pos.z);
			if(blockPos.getY() < -64)
				return;
			
			if(world.isEmptyBlock(blockPos) || world.getBlockState(blockPos).getMaterial().isReplaceable())
			{
				world.playSound((Player)null, blockPos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS,  0.5F + world.random.nextFloat(), world.random.nextFloat() * 0.7F + 0.6F);
				world.setBlockAndUpdate(blockPos, state);
			}
		}
		
		protected static void spawnEntity(List<IVariable> inputVariables, Level world, CreationRecipe recipe, Entity caster)
		{
			if(inputVariables.isEmpty() || world.isClientSide())
				return;
			
			Vec3 position = inputVariables.get(0).asVec();
			
			Entity spawnedEntity = recipe.createEntityAt(world, position);
			if(spawnedEntity == null || !spawnedEntity.isAlive())
				return;
			
			if(spawnedEntity.getType() == EntityType.LIGHTNING_BOLT)
				((LightningBolt)spawnedEntity).setCause((ServerPlayer)caster);
			else if(spawnedEntity instanceof TamableAnimal)
			{
				TamableAnimal animal = (TamableAnimal)spawnedEntity;
				animal.setTame(true);
				animal.setOwnerUUID(caster.getUUID());
			}
			
			world.addFreshEntity(spawnedEntity);
		}
	}
	
	public static class StatusEffect extends FunctionGlyph
	{
		public StatusEffect()
		{
			super(20);
		}
		
		public ComponentError getErrorState() { return inputs().isEmpty() ? ComponentError.ERROR : ComponentError.GOOD; }
		
		protected void performFunction(EnumSet<Element> inputElements, List<IVariable> inputVariables, VariableSet totalVariables, Level world)
		{
			if(inputVariables.isEmpty())
				return;
			
			Entity caster = totalVariables.get(Slot.CASTER).asEntity();
			StatusEffectRecipe recipe = (StatusEffectRecipe)FunctionRecipes.getInstance().getMatchingRecipe(inputElements, FunctionRecipes.STATUS_EFFECT);
			if(recipe == null)
				addEffectToEntity(inputVariables, new MobEffectInstance(MobEffects.NIGHT_VISION, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), caster);
			else
			{
				recipe.getEffects().forEach((effect) -> addEffectToEntity(inputVariables, effect, caster));
				
				CommandFunction.CacheableFunction func = recipe.getFunction();
				if(func != null && func != CommandFunction.CacheableFunction.NONE)
					runCommandOn(getFirstOfType(inputVariables, VariableType.ENTITY).asEntity(), world, func);
			}
		}
		
		protected static void addEffectToEntity(List<IVariable> inputVariables, MobEffectInstance effect, Entity caster)
		{
			Entity target = getFirstOfType(inputVariables, (var) -> var.type() == VariableType.ENTITY && var.asEntity() instanceof LivingEntity).asEntity();
			if(target != null)
				((LivingEntity)target).addEffect(effect, caster);
		}
	}
	
	public static class Dispel extends FunctionGlyph
	{
		protected static final Param RAD = Param.of("radius", VariableType.DOUBLE);
		
		public Dispel()
		{
			super(32);
		}
		
		public ComponentError getErrorState() { return inputs().isEmpty() ? ComponentError.ERROR : ComponentError.GOOD; }
		
		protected void performFunction(EnumSet<Element> inputElements, List<IVariable> inputVariables, VariableSet totalVariables, Level world)
		{
			Vec3 pos = getFirstOfType(inputVariables, VariableType.VECTOR).asVec();
			double radius = getFirstOfType(inputVariables, VariableType.DOUBLE).asDouble();
			
			IVariable uuidMost = totalVariables.get(Slot.UUID1);
			IVariable uuidLeast = totalVariables.get(Slot.UUID2);
			
			AABB bounds = new AABB(-radius, -radius, -radius, radius, radius, radius).move(pos);
			for(SpellData spell : SpellManager.getSpellsWithin(world, bounds))
				if(spell.getVariable(Slot.UUID1) != uuidMost && spell.getVariable(Slot.UUID2) != uuidLeast)
				{
					Vec3 targetPos = spell.getVariable(Slot.POSITION).asVec();
					MinecraftForge.EVENT_BUS.post(SpellEvent.End.dispel(spell.arrangement(), world, targetPos, ((VarEntity)spell.getVariable(Slot.CASTER)).uniqueID()));
					
					CompoundTag data = new CompoundTag();
					data.putDouble("PosX", pos.x);
					data.putDouble("PosY", pos.y);
					data.putDouble("PosZ", pos.z);
					notifySpellEffect(world, SpellEffects.DISPEL, totalVariables.get(Slot.POSITION).asVec(), data, Reference.Values.TICKS_PER_SECOND);
					
					spell.kill();
				}
		}
	}
}
