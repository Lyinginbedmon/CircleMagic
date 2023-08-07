package com.lying.circles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.init.CMDamageSource;
import com.lying.circles.init.CMItems;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
	private boolean isLich(Player living)
	{
		PlayerData data = PlayerData.getCapability(living);
		return data != null && data.isALich();
	}
	
	private ItemStack getLichSkull(Player living)
	{
		for(int i=0; i<living.getInventory().getContainerSize(); i++)
		{
			ItemStack stack = living.getInventory().getItem(i);
			if(stack.getItem() == CMItems.LICH_SKULL.get())
				return stack;
		}
		return ItemStack.EMPTY;
	}
	
	@Inject(method = "getMobType()Lnet/minecraft/world/entity/MobType;", at = @At("HEAD"), cancellable = true)
	public void onLichMobType(final CallbackInfoReturnable<MobType> ci)
	{
		LivingEntity living = (LivingEntity)(Object)this;
		if(living.getType() == EntityType.PLAYER && isLich((Player)living))
			ci.setReturnValue(MobType.UNDEAD);
	}
	
	@Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
	public void onLichStarve(DamageSource source, float amount, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity living = (LivingEntity)(Object)this;
		Level world = living.level;
		if(living.getType() == EntityType.PLAYER && isLich((Player)living))
			if(source == DamageSource.STARVE)
			{
				ci.setReturnValue(false);
				ci.cancel();
			}
			else if(source == CMDamageSource.OUT_OF_MANA)
			{
				/**
				 * If the lich would take damage from lack of mana,
				 * * Instead, damage the skull in their inventory
				 * 
				 * If the skull would break from this damage,
				 * * Revoke the lich's lichdom
				 */
				if(world.isClientSide())
				{
					ci.setReturnValue(false);
					ci.cancel();
					return;
				}
				
				ItemStack skull = getLichSkull((Player)living);
				if(skull.isEmpty())
					return;
				
				skull.hurtAndBreak(Math.min((int)amount, skull.getMaxDamage() - skull.getDamageValue()), ((ServerPlayer)living), (player) -> 
				{
					PlayerData data = PlayerData.getCapability(player);
					if(data != null)
						data.setIsLich(false);
					
					player.level.broadcastEntityEvent(player, (byte)35);
				});
				
				ci.setReturnValue(false);
				ci.cancel();
			}
	}
}
