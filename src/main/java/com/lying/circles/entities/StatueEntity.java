package com.lying.circles.entities;

import com.lying.circles.init.CMEntities;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StatueEntity extends LivingEntity
{
	public StatueEntity(Level p_19871_)
	{
		this(CMEntities.STATUE.get(), p_19871_);
	}
	
	public StatueEntity(EntityType<? extends StatueEntity> entityType, Level world)
	{
		super(entityType, world);
	}
	
	public Packet<?> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }
	
	public boolean isAttackable() { return false; }
	
	public Iterable<ItemStack> getArmorSlots() { return NonNullList.withSize(4, ItemStack.EMPTY); }
	
	public ItemStack getItemBySlot(EquipmentSlot p_21127_) { return ItemStack.EMPTY; }
	
	public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) { }
	
	public HumanoidArm getMainArm() { return HumanoidArm.LEFT; }
}
