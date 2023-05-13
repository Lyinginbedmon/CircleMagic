package com.lying.misc19.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class Pendulum extends Item
{
	public Pendulum(Item.Properties properties)
	{
		super(properties);
	}
	
	public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
	
	public int getUseDuration(ItemStack stack) { return 72000; }
	
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(stack);
	}
}
