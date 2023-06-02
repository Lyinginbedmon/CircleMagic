package com.lying.misc19.api.event;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

/**
 * Events relating to various components of spell operation.<br>
 * These are fired server-side on the mod event bus.<br>
 * They do not have a result and cannot be cancelled.
 */
public class SpellEvent extends Event
{
	private final ISpellComponent spell;
	private final Vec3 spellLocation;
	private final Level spellWorld;
	
	@Nonnull
	private final UUID casterUUID;
	
	@Nullable
	private LivingEntity casterEntity;
	
	public SpellEvent(ISpellComponent spellIn, Level world, Vec3 location, UUID casterID)
	{
		this.spell = SpellComponents.readFromNBT(ISpellComponent.saveToNBT(spellIn));
		this.spellLocation = location;
		this.spellWorld = world;
		this.casterUUID = casterID;
	}
	
	public SpellEvent(ISpellComponent spellIn, Level world, Vec3 location, LivingEntity caster)
	{
		this(spellIn, world, location, caster.getUUID());
		this.casterEntity = caster;
	}
	
	public ISpellComponent spell() { return this.spell; }
	
	public Level world() { return this.spellWorld; }
	
	public Vec3 location() { return this.spellLocation; }
	
	public boolean isCaster(UUID uuidIn) { return this.casterUUID.equals(uuidIn); }
	
	public boolean isCaster(LivingEntity entity) { return entity != null && isCaster(entity.getUUID()); }
	
	public UUID casterUUID() { return this.casterUUID; }
	
	@Nullable
	public LivingEntity caster() { return this.casterEntity; }
	
	/**
	 * Fired whenever a component affects a location in the world.<br>
	 * {@link spell} contains the component that caused the effect.
	 */
	public static class ComponentEffect extends SpellEvent
	{
		private final Vec3 position;
		
		public ComponentEffect(Vec3 positionIn, ISpellComponent spellIn, Level world, Vec3 location, UUID casterID)
		{
			super(spellIn, world, location, casterID);
			this.position = positionIn;
		}
		
		public Vec3 position() { return this.position; }
	}
	
	/** Fired whenever a spell triggers */
	public static class Run extends SpellEvent
	{
		public Run(ISpellComponent spellIn, Level world, Vec3 location, UUID casterID)
		{
			super(spellIn, world, location, casterID);
		}
	}
	
	/** Fired whenever a spell is flagged for removal for any reason */
	public static class End extends SpellEvent
	{
		private final Cause cause;
		
		public End(ISpellComponent spellIn, Level world, Vec3 location, UUID casterID, Cause causeIn)
		{
			super(spellIn, world, location, casterID);
			this.cause = causeIn;
		}
		
		public static End die(ISpellComponent spellIn, Level world, Vec3 location, UUID casterID)
		{
			return new End(spellIn, world, location, casterID, Cause.DIE);
		}
		
		public static End dispel(ISpellComponent spellIn, Level world, Vec3 location, UUID casterID)
		{
			return new End(spellIn, world, location, casterID, Cause.DISPEL);
		}
		
		public static End error(ISpellComponent spellIn, Level world, Vec3 location, UUID casterID)
		{
			return new End(spellIn, world, location, casterID, Cause.ERROR);
		}
		
		public Cause cause() { return this.cause; }
		
		public static enum Cause
		{
			/** The spell ended because the CONTINUE register was not true */
			DIE,
			/** The spell ended because it was dispelled */
			DISPEL,
			/** An unspecified error occured during the spell's operation */
			ERROR;
		}
	}
}
