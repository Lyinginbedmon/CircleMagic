package com.lying.circles.entities;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.circles.client.Canvas;
import com.lying.circles.init.CMEntities;
import com.lying.circles.init.SpellComponents;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.EntityData;
import com.lying.circles.utility.SpellData;
import com.lying.circles.utility.SpellManager;
import com.lying.circles.utility.SpellTextureManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellEntity extends Entity
{
	protected static final EntityDataAccessor<Optional<UUID>> SPELL_ID = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	protected static final EntityDataAccessor<CompoundTag> SPELL_CACHE = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<Integer> VISIBILITY = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
	private static final int VANISH_TIME = Reference.Values.TICKS_PER_SECOND * 2;
	
	@OnlyIn(Dist.CLIENT)
	public Canvas canvas = new Canvas(SpellTextureManager.getNewTexture(), 8);
	public boolean needsRedrawing = true;
	
	protected SpellEntity(Level worldIn) { this(CMEntities.SPELL.get(), worldIn); }
	public SpellEntity(EntityType<SpellEntity> typeIn, Level worldIn)
	{
		super(typeIn, worldIn);
	}
	
	public Packet<?> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }
	
	public static SpellEntity create(ISpellComponent spellIn, LivingEntity owner, Level world)
	{
		return create(new SpellData(spellIn, owner), owner, world);
	}
	
	public static SpellEntity create(SpellData data, LivingEntity owner, Level world)
	{
		SpellEntity spell = new SpellEntity(world);
		spell.getEntityData().set(SPELL_CACHE, ISpellComponent.saveToNBT(data.arrangement()));
		spell.setPos(owner.position().x, owner.position().y + (owner.getBbHeight() / 2F), owner.position().z);
		spell.setYRot(owner.getYRot());
		spell.setXRot(owner.getXRot());
		
		SpellManager manager = SpellManager.instance(world);
		UUID uuid = manager.addSpellOn(data, spell);
		spell.setSpellUUID(uuid);
		return spell;
	}
	
	protected void defineSynchedData()
	{
		getEntityData().define(SPELL_ID, Optional.empty());
		getEntityData().define(VISIBILITY, VANISH_TIME);
		getEntityData().define(SPELL_CACHE, new CompoundTag());
	}
	
	protected void readAdditionalSaveData(CompoundTag compound)
	{
		getEntityData().set(SPELL_ID, Optional.of(compound.getUUID("Spell")));
		getEntityData().set(VISIBILITY, compound.getInt("Vanish"));
		getEntityData().set(SPELL_CACHE, compound.getCompound("Cache"));
	}
	
	protected void addAdditionalSaveData(CompoundTag compound)
	{
		compound.putUUID("Spell", getSpellUUID());
		compound.putInt("Vanish", getEntityData().get(VISIBILITY).intValue());
		compound.put("Cache", getEntityData().get(SPELL_CACHE));
	}
	
	public boolean isAttackable() { return false; }
	
	public float getVisibility() { return getEntityData().get(VISIBILITY).floatValue() / (float)VANISH_TIME; }
	
	public boolean isVanishing() { return getVisibility() != 1F; }
	
	public void tick()
	{
		super.tick();
		if(getLevel().isClientSide())
			return;
		
		// Prevent any spell below the world from surviving
		if(this.getY() <= -64)
			kill();
		
		// Gradual fading out of expired spells
		if(isVanishing())
		{
			int visibility = getEntityData().get(VISIBILITY).intValue() - 1;
			getEntityData().set(VISIBILITY, visibility);
			
			if(visibility <= 0)
				kill();
			
			return;
		}
		
		SpellData spell = getSpell();
		if(spell == null)
			getEntityData().set(VISIBILITY, VANISH_TIME - 1);
		else
			getEntityData().set(SPELL_CACHE, ISpellComponent.saveToNBT(spell.arrangement()));
	}
	
	public void setSpellUUID(UUID uuidIn) { getEntityData().set(SPELL_ID, Optional.of(uuidIn)); }
	
	@Nullable
	public UUID getSpellUUID()
	{
		Optional<UUID> uuid = getEntityData().get(SPELL_ID);
		return uuid.isPresent() ? uuid.get() : null;
	}
	
	@Nullable
	public SpellData getSpell()
	{
		UUID uuid = getSpellUUID();
		if(uuid == null)
			return null;
		
		List<SpellData> spells = SpellManager.instance(getLevel()).getSpellsOn(this);
		for(SpellData spell : spells)
			if(spell.isAlive() && spell.getUUID().equals(uuid))
				return spell;
		
		if(getLevel().isClientSide())
			return new SpellData(SpellComponents.readFromNBT(getEntityData().get(SPELL_CACHE)), (EntityData)null);
		return null;
	}
	
	public void setSpell(ISpellComponent spell)
	{
		getEntityData().set(SPELL_CACHE, ISpellComponent.saveToNBT(spell));
		this.needsRedrawing = true;
	}
	
	@OnlyIn(Dist.CLIENT)
	public Canvas getCanvas()
	{
		if(this.needsRedrawing)
		{
			this.canvas.clear();
			this.canvas.populate(getSpell().arrangement());
			this.needsRedrawing = false;
		}
		return this.canvas;
	}
	
	public void kill()
	{
		super.kill();
		SpellManager.instance(getLevel()).removeSpellsFrom(this);
	}
	
	public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
}
