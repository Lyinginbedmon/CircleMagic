package com.lying.misc19.magic.component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.Misc19;
import com.lying.misc19.init.M19Blocks;
import com.lying.misc19.magic.Element;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarElement;
import com.lying.misc19.magic.variable.VarEntity;
import com.lying.misc19.magic.variable.VarLevel;
import com.lying.misc19.magic.variable.VarStack;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.SpellData;
import com.lying.misc19.utility.SpellManager;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A glyph that performs an actual function based on its inputs and does not have any outputs */
public abstract class FunctionGlyph extends ComponentBase
{
	private final Map<EnumSet<Element>, BiConsumer<VariableSet, List<ISpellComponent>>> AUGMENTATIONS = new HashMap<>();
	private final BiConsumer<VariableSet, List<ISpellComponent>> defaultBehaviour;
	
	protected static final Param POS = Param.of("pos", VariableType.VECTOR);
	protected static final Param ENTITY = Param.of("entity", VariableType.ENTITY);
	private final int cost;
	
	protected FunctionGlyph(int costIn, BiConsumer<VariableSet, List<ISpellComponent>> defaultAction)
	{
		this.cost = costIn;
		this.defaultBehaviour = defaultAction;
	}
	
	protected void addAugmentation(BiConsumer<VariableSet, List<ISpellComponent>> func, Element... elementArray)
	{
		EnumSet<Element> elements = EnumSet.noneOf(Element.class);
		for(Element element : elementArray)
			if(!elements.contains(element))
				elements.add(element);
		AUGMENTATIONS.put(elements, func);
	}
	
	public Category category() { return Category.FUNCTION; }
	
	public Type type() { return Type.FUNCTION; }
	
	public int castingCost() { return cost; }
	
	public boolean isValidInput(ISpellComponent componentIn) { return ISpellComponent.canBeInput(componentIn); }
	
	public boolean isValidOutput(ISpellComponent componentIn) { return false; }
	
	public VariableSet execute(VariableSet variablesIn)
	{
		if(inputsMet(variablesIn))
		{
			EnumSet<Element> elements = EnumSet.noneOf(Element.class);
			List<ISpellComponent> inputs = Lists.newArrayList();
			for(ISpellComponent input : inputs())
			{
				IVariable var = getVariable(input, variablesIn);
				if(var.type() == VariableType.ELEMENT)
				{
					variablesIn.glyphExecuted(input.castingCost());
					Element ele = ((VarElement)var).get();
					if(!elements.contains(ele))
						elements.add(ele);
				}
				else if(var.type() == VariableType.STACK && ((VarStack)var).stackType() == VariableType.ELEMENT)
					for(IVariable variable : var.asStack().entries())
					{
						Element ele = ((VarElement)variable).get();
						if(!elements.contains(ele))
							elements.add(ele);
					}
				else
					inputs.add(input);
			}
			
			if(elements.isEmpty())
				this.defaultBehaviour.accept(variablesIn, inputs);
			else
				AUGMENTATIONS.getOrDefault(elements, defaultBehaviour).accept(variablesIn, inputs);
		}
		return variablesIn;
	}
	
	public static class Debug extends FunctionGlyph
	{
		public Debug() { super(0, Debug::doDebug); }
		
		public boolean isValidInput(ISpellComponent part) { return false; }
		
		protected static void doDebug(VariableSet variablesIn, List<ISpellComponent> inputs)
		{
			List<Component> messages = Lists.newArrayList();
			messages.add(Component.literal("# Debug Glyph #"));
			messages.add(Component.literal("# Glyphs executed: "+variablesIn.totalGlyphs()+" of "+VariableSet.EXECUTION_LIMIT));
			messages.add(Component.literal("# Casting cost: "+variablesIn.totalCastingCost()));
			messages.add(Component.literal("# Register contents:"));
			for(Slot slot : VariableSet.Slot.values())
				if(variablesIn.isUsing(slot))
					messages.add(Component.literal("# * "+slot.name()+": ").append(variablesIn.get(slot).translate()));
			messages.add(Component.literal("# Debug End #"));
			
			if(variablesIn.isUsing(Slot.CASTER))
			{
				Entity caster = variablesIn.get(Slot.CASTER).asEntity();
				if(caster.getType() == EntityType.PLAYER)
				{
					Player player = (Player)caster;
					messages.forEach((line) -> player.displayClientMessage(line, false));
				}
			}
			else
				messages.forEach((line) -> Misc19.LOG.info(line.getString()));
		}
	}
	
	public static class AddMotion extends FunctionGlyph
	{
		public AddMotion()
		{
			super(15, AddMotion::doMove);
			
			addAugmentation(AddMotion::doTP, Element.FINIS);
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
			super(15, (variables, inputs) -> 
			{
				placeEffect(variables, inputs, M19Blocks.PHANTOM_CUBE.get().defaultBlockState());
			});
			
			addAugmentation((variables, inputs) -> 
			{
				placeEffect(variables, inputs, Blocks.STONE.defaultBlockState());
			}, Element.MUNDUS);
			addAugmentation((variables, inputs) -> 
			{
				IVariable var = ComponentBase.getVariable(inputs.get(0), variables);
				if(var.type() == VariableType.VECTOR)
					placeEffect(variables, inputs, Blocks.FIRE.defaultBlockState());
				else if(var.type() == VariableType.ENTITY)
					var.asEntity().setSecondsOnFire(8);
			}, Element.ARDERE);
			addAugmentation((variables,inputs) -> 
			{
				placeEffect(variables, inputs, Blocks.WATER.defaultBlockState());
			}, Element.MARE);
			addAugmentation((variables,inputs) -> 
			{
				placeEffect(variables, inputs, Blocks.SCULK.defaultBlockState());
			}, Element.SCULK);
			addAugmentation((variables,inputs) -> 
			{
				placeEffect(variables, inputs, Blocks.GRASS_BLOCK.defaultBlockState());
			}, Element.ORIGO);
			addAugmentation((variables,inputs) -> 
			{
				placeEffect(variables, inputs, Blocks.END_STONE.defaultBlockState());
			}, Element.FINIS);
			addAugmentation((variables,inputs) -> 
			{
				Level world = ((VarLevel)variables.get(Slot.WORLD)).get();
				Entity caster = ((VarEntity)variables.get(Slot.CASTER)).asEntity();
				
				LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
				if(caster.getType() == EntityType.PLAYER)
					bolt.setCause((ServerPlayer)caster);
				
				Map<String, IVariable> params = ComponentBase.collectParams(variables, inputs, POS);
				bolt.setPos(POS.get(params).asVec());
				world.addFreshEntity(bolt);
			}, Element.FINIS, Element.ARDERE, Element.MUNDUS);
		}
		
		protected static void placeEffect(VariableSet variablesIn, List<ISpellComponent> inputs, BlockState state)
		{
			Map<String, IVariable> params = ComponentBase.collectParams(variablesIn, inputs, POS);
			placeBlock(POS.get(params).asVec(), ((VarLevel)variablesIn.get(Slot.WORLD)).get(), state);
		}
		
		protected static void placeBlock(Vec3 pos, Level world, BlockState state)
		{
			BlockPos blockPos = new BlockPos(pos.x, pos.y, pos.z);
			if(blockPos.getY() < -64)
				return;
			
			if(world.isEmptyBlock(blockPos) || world.getBlockState(blockPos).getMaterial().isReplaceable())
			{
				world.playSound((Player)null, blockPos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS,  0.5F + world.random.nextFloat(), world.random.nextFloat() * 0.7F + 0.6F);
				world.setBlockAndUpdate(blockPos, state);
			}
		}
	}
	
	public static class StatusEffect extends FunctionGlyph
	{
		public StatusEffect()
		{
			super(20, (variables, inputs) -> addEffect(variables, inputs, MobEffects.NIGHT_VISION));
			
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.FIRE_RESISTANCE), Element.ARDERE, Element.MARE);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.DIG_SPEED), Element.ARDERE, Element.FINIS);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.INVISIBILITY), Element.ARDERE, Element.SCULK);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.DAMAGE_BOOST), Element.ARDERE, Element.MUNDUS);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.JUMP), Element.MUNDUS, Element.FINIS);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.MOVEMENT_SLOWDOWN), Element.SCULK, Element.FINIS);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.WEAKNESS), Element.SCULK, Element.MUNDUS);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.DAMAGE_RESISTANCE), Element.MUNDUS);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.MOVEMENT_SPEED), Element.FINIS);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.WATER_BREATHING), Element.MARE);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.REGENERATION), Element.ORIGO);
			addAugmentation((variables, inputs) -> addEffect(variables, inputs, MobEffects.WITHER), Element.SCULK, Element.ORIGO);
		}
		
		private static void addEffect(VariableSet variables, List<ISpellComponent> inputs, MobEffect effect)
		{
			if(inputs.isEmpty())
				return;
			
			Entity caster = variables.get(Slot.CASTER).asEntity();
			IVariable var = getVariable(inputs.get(0), variables);
			if(var.type() == VariableType.ENTITY)
				addStatusEffect(var.asEntity(), effect, caster);
		}
		
		private static void addStatusEffect(Entity ent, MobEffect effect, Entity caster)
		{
			if(!(ent instanceof LivingEntity))
				return;
			
			((LivingEntity)ent).addEffect(new MobEffectInstance(effect, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), caster);
		}
	}
	
	public static class Dispel extends FunctionGlyph
	{
		protected static final Param RAD = Param.of("radius", VariableType.DOUBLE);
		
		public Dispel()
		{
			super(32, Dispel::doDispel);
		}
		
		protected static void doDispel(VariableSet variablesIn, List<ISpellComponent> inputs)
		{
			Map<String, IVariable> params = ComponentBase.collectParams(variablesIn, inputs, POS, RAD);
			Level world = ((VarLevel)variablesIn.get(Slot.WORLD)).get();
			Vec3 pos = POS.get(params).asVec();
			double radius = RAD.get(params).asDouble();
			
			AABB bounds = new AABB(-radius, -radius, -radius, radius, radius, radius).move(pos);
			for(SpellData spell : SpellManager.getSpellsWithin(world, bounds))
				if(spell.getVariable(Slot.UUID1) != variablesIn.get(Slot.UUID1) && spell.getVariable(Slot.UUID2) != variablesIn.get(Slot.UUID2))
					spell.kill();
		}
	}
}
