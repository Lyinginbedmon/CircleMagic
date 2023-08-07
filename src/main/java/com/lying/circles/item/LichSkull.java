package com.lying.circles.item;

import com.google.common.base.Predicates;
import com.lying.circles.capabilities.LivingData;
import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.data.CMEntityTags;
import com.lying.circles.init.CMStatusEffects;
import com.lying.circles.reference.Reference;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class LichSkull extends Item
{
	public LichSkull(Properties properties)
	{
		super(properties.stacksTo(1).durability(64));
	}
	
	public int getEnchantmentLevel(ItemStack stack, Enchantment ench) { return ench == Enchantments.BINDING_CURSE ? 1 : super.getEnchantmentLevel(stack, ench); }
	
	public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
	
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack itemstack = player.getItemInHand(hand);
		
		if(!world.isClientSide())
		{
			Vec3 eyePos = player.getEyePosition(), lookEnd = eyePos.add(player.getLookAngle().scale(16D));
			EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(world, player, eyePos, lookEnd, player.getBoundingBox().inflate(16D), Predicates.alwaysTrue());
			
			if(hitResult.getType() == Type.ENTITY)
			{
				Entity hitEnt = hitResult.getEntity();
				if(hitEnt instanceof LivingEntity)
				{
					LivingEntity living = (LivingEntity)hitEnt;
					if(!living.getType().is(CMEntityTags.HAS_MANA) || isLich(living))
						return InteractionResultHolder.fail(itemstack);
					
					LivingData vicData = LivingData.getCapability(living);
					LivingData licData = LivingData.getCapability(player);
					
					if(vicData != null && licData != null)
					{
						boolean hasDrain = living.hasEffect(CMStatusEffects.MANA_DRAIN.get());
						int amp = hasDrain ? 1 + living.getEffect(CMStatusEffects.MANA_DRAIN.get()).getAmplifier() : 1;
						float drain = Math.min((float)Math.pow(5F, amp), vicData.getCurrentMana());
						if(licData.getCurrentCapacity() > 0F)
							drain = Math.min(drain, licData.getCurrentCapacity() - licData.getCurrentMana());
						
						vicData.spendMana(drain);
						licData.spendMana(-drain);
						
						if(hasDrain && amp < 5)
							living.addEffect(new MobEffectInstance(CMStatusEffects.MANA_DRAIN.get(), Reference.Values.TICKS_PER_SECOND * 5, amp + 1), player);
						else
							living.addEffect(new MobEffectInstance(CMStatusEffects.MANA_DRAIN.get(), Reference.Values.TICKS_PER_SECOND * 5), player);
					}
					
					player.getCooldowns().addCooldown(this, Reference.Values.TICKS_PER_SECOND * 2);
				}
			}
		}
		
		return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
	}
	
	public static boolean isLich(LivingEntity player)
	{
		if(player.getType() == EntityType.PLAYER)
		{
			PlayerData data = PlayerData.getCapability((Player)player);
			return data != null && data.isALich();
		}
		return false;
	}
}
