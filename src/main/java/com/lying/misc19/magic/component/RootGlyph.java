package com.lying.misc19.magic.component;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;
import com.lying.misc19.entities.SpellEntity;
import com.lying.misc19.init.M19Entities;
import com.lying.misc19.magic.ComponentCircle;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarEntity;
import com.lying.misc19.magic.variable.VarVec;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.magic.variable.VariableSet.VariableType;
import com.lying.misc19.utility.SpellData;
import com.lying.misc19.utility.SpellManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract class RootGlyph extends ComponentCircle.Basic
{
	public Category category() { return Category.ROOT; }
	
	public Type type() { return Type.ROOT; }
	
	public void setParent(ISpellComponent parent) { }
	
	public int calculateRuns(VariableSet variablesIn) { return 1; }
	
	public boolean isValidInput(ISpellComponent component) { return component.type() == Type.HERTZ; }
	
	public Vec2 core() { return position(); }
	
	public abstract Entity positionAndOrientSpell(Entity spellEntity, LivingEntity caster);
	
	public void updatePositionAndOrient(Entity spellEntity, LivingEntity caster) { positionAndOrientSpell(spellEntity, caster); }
	
	public int tickRate()
	{
		if(inputs().isEmpty())
			return 1;
		
		return inputs().isEmpty() ? 1 : ((HertzGlyph)inputs().get(0)).getTickRate();
	}
	
	protected Pair<Float, Float> separations() { return Pair.of(30F, 80F); }
	
	public abstract void addSpellToWorld(ISpellComponent spell, Level world, LivingEntity caster);
	
	public void performExecution(@Nonnull Level world, @Nonnull LivingEntity caster, @Nonnull VariableSet variablesIn)
	{
		variablesIn.resetExecutions();
		variablesIn.recacheBeforeExecution(world);
		
		updateCoreVariables(world, caster, variablesIn);
		execute(variablesIn);
		payManaCost(caster, variablesIn.totalCastingCost());
	}
	
	public static void payManaCost(@Nonnull LivingEntity caster, int cost)
	{
		// TODO Subtract variables mana from caster and damage if necessary
	}
	
	public abstract VariableSet populateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn);
	
	public abstract VariableSet updateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn);

	/** Populates the variable set with CASTER and WORLD variables only */
	public static class Dummy extends RootGlyph
	{
		public VariableSet updateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn) { return variablesIn; }
		
		public void addSpellToWorld(ISpellComponent spell, Level world, LivingEntity caster)
		{
			world.addFreshEntity(positionAndOrientSpell(SpellEntity.create(spell, caster, world), caster));
		}
		
		public int castingCost() { return 0; }
		
		public VariableSet populateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn) { return variablesIn; }
		
		public Entity positionAndOrientSpell(Entity spellEntity, LivingEntity caster) { return spellEntity; }
	}
	
	/** Populates the variable set with POSITION, LOOK, and TARGET variable */
	public static class Self extends RootGlyph
	{
		public VariableSet populateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn)
		{
			populateTarget(caster, variablesIn);
			variablesIn.set(Slot.POSITION, new VarVec(caster.position()));
			variablesIn.set(Slot.LOOK, new VarVec(caster.getLookAngle()));
			return variablesIn;
		}
		
		public VariableSet updateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn)
		{
			return populateCoreVariables(world, caster, variablesIn);
		}
		
		public void addSpellToWorld(ISpellComponent spell, Level world, LivingEntity caster)
		{
			SpellData data = new SpellData(spell, caster);
			SpellManager.instance(world).addSpellOn(data, caster);
		}
		
		public Entity positionAndOrientSpell(Entity spellEntity, LivingEntity caster) { return spellEntity; }
	}
	
	/** Populates the variable set with the POSITION variable */
	public static class Position extends RootGlyph
	{
		public VariableSet populateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn) { return variablesIn; }
		
		public void addSpellToWorld(ISpellComponent spell, Level world, LivingEntity caster)
		{
			world.addFreshEntity(positionAndOrientSpell(SpellEntity.create(spell, caster, world), caster));
		}
		
		public VariableSet updateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn)
		{
			variablesIn.set(Slot.POSITION, new VarVec(caster.position()));
			return variablesIn;
		}
		
		public Entity positionAndOrientSpell(Entity spellEntity, LivingEntity caster)
		{
			spellEntity.setPos(caster.position());
			spellEntity.setXRot(90F);
			spellEntity.setYRot(caster.getYRot());
			return spellEntity;
		}
	}
	
	/** Populates the variable set with POSITION and TARGET variables */
	public static class Target extends RootGlyph
	{
		public VariableSet populateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn)
		{
			populateTarget(caster, variablesIn);
			return variablesIn;
		}
		
		public void addSpellToWorld(ISpellComponent spell, Level world, LivingEntity caster)
		{
			SpellData data = new SpellData(spell, caster);
			IVariable targetVar = data.getVariable(Slot.TARGET);
			if(targetVar.type() == VariableType.ENTITY)
				SpellManager.instance(world).addSpellOn(data, targetVar.asEntity());
			else
				world.addFreshEntity(positionAndOrientSpell(SpellEntity.create(spell, caster, world), caster));
		}
		
		public VariableSet updateCoreVariables(Level world, LivingEntity caster, VariableSet variablesIn)
		{
			if(variablesIn.get(Slot.TARGET).type() == VariableType.ENTITY)
			{
				Entity ent = variablesIn.get(Slot.TARGET).asEntity();
				variablesIn.set(Slot.POSITION, new VarVec(ent.position().add(0, ent.getBbHeight() / 2, 0)));
				variablesIn.set(Slot.LOOK, new VarVec(ent.getLookAngle()));
			}
			return variablesIn;
		}
		
		public Entity positionAndOrientSpell(Entity spellEntity, LivingEntity caster)
		{
			VariableSet variable = populateTarget(caster, new VariableSet());
			IVariable var = variable.get(Slot.TARGET);
			if(var.type() == VariableType.ENTITY)
				return spellEntity;
			else if(var.type() == VariableType.VECTOR)
			{
				Vec3 posVec = variable.get(Slot.POSITION).asVec();
				spellEntity.setPos(posVec);
				spellEntity.setXRot(caster.getXRot());
				spellEntity.setYRot(caster.getYRot());
				
				BlockPos blockPos = new BlockPos(posVec.x, posVec.y, posVec.z);
				spellEntity.setPos(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
				
				Vec3 look = variable.get(Slot.LOOK).asVec();
				Direction face = Direction.fromNormal((int)look.x(), (int)look.y(), (int)look.z());
				if(face.getAxis().isHorizontal())
				{
					spellEntity.setXRot(0F);
					spellEntity.setYRot((float)(face.get2DDataValue() * 90));
				}
				else
				{
					spellEntity.setXRot((float)(-90F * face.getAxisDirection().getStep()));
					spellEntity.setYRot(caster.getYRot() - 180F);
				}
			}
			return spellEntity;
		}
	}
	
	protected VariableSet populateTarget(LivingEntity caster, VariableSet variablesIn)
	{
		Vec3 eyePos = caster.getEyePosition();
		Vec3 lookVec = caster.getLookAngle();
		Vec3 lookEnd = eyePos.add(lookVec.scale(64D));
		
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(caster.getLevel(), caster, eyePos, lookEnd, caster.getBoundingBox().inflate(64D), Predicates.alwaysTrue());
		if(entityHit != null && entityHit.getType() == HitResult.Type.ENTITY)
		{
			Entity targetEntity = entityHit.getEntity();
			variablesIn.set(Slot.TARGET, new VarEntity(targetEntity));
			variablesIn.set(Slot.POSITION, new VarVec(targetEntity.position().add(0, targetEntity.getBbHeight() / 2, 0)));
			variablesIn.set(Slot.LOOK, new VarVec(targetEntity.getLookAngle()));
			return variablesIn;
		}
		else
		{
			HitResult trace = caster.getLevel().clip(new ClipContext(eyePos, lookEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
			switch(trace.getType())
			{
				case BLOCK:
					BlockHitResult block = (BlockHitResult)trace;
					BlockPos targetBlock = block.getBlockPos();
					Vec3i look = block.getDirection().getNormal();
					BlockPos pos = targetBlock.offset(block.getDirection().getNormal());
					variablesIn.set(Slot.TARGET, new VarVec(targetBlock.getX() + 0.5D, targetBlock.getY(), targetBlock.getZ() + 0.5D));
					variablesIn.set(Slot.POSITION, new VarVec(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
					variablesIn.set(Slot.LOOK, new VarVec(look.getX(), look.getY(), look.getZ()));
					break;
				case ENTITY:
					EntityHitResult entity = (EntityHitResult)trace;
					Entity targetEntity = entity.getEntity();
					variablesIn.set(Slot.TARGET, new VarEntity(targetEntity));
					variablesIn.set(Slot.POSITION, new VarVec(targetEntity.position().add(0, targetEntity.getBbHeight() / 2, 0)));
					variablesIn.set(Slot.LOOK, new VarVec(targetEntity.getLookAngle()));
					break;
				case MISS:
				default:
					variablesIn.set(Slot.POSITION, new VarVec(caster.getEyePosition()));
					break;
			}
		}
		return variablesIn;
	}
	
	public static IVariable getEntityTarget(Entity caster)
	{
		Vec3 eyePos = caster.getEyePosition();
		Vec3 lookVec = caster.getLookAngle();
		Vec3 lookEnd = eyePos.add(lookVec.scale(64D));
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(caster.getLevel(), caster, eyePos, lookEnd, caster.getBoundingBox().inflate(64D), (entity) -> entity.isAlive() && entity.getType() != M19Entities.SPELL.get());
		if(entityHit != null && entityHit.getType() == HitResult.Type.ENTITY)
			return new VarEntity(entityHit.getEntity());
		else
		{
			HitResult trace = caster.getLevel().clip(new ClipContext(eyePos, lookEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
			switch(trace.getType())
			{
				case BLOCK:
					BlockHitResult block = (BlockHitResult)trace;
					BlockPos pos = block.getBlockPos().offset(block.getDirection().getNormal());
					return new VarVec(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
				case ENTITY:
					EntityHitResult entity = (EntityHitResult)trace;
					return new VarEntity(entity.getEntity());
				case MISS:
				default:
					return VariableSet.DEFAULT;
			}
		}
	}
}
