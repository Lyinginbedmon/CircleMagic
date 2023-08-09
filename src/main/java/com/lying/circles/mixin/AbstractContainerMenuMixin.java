package com.lying.circles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.init.CMItems;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin 
{
	@Shadow
	public ItemStack getCarried() { return ItemStack.EMPTY; }
	
	@Shadow
	public Slot getSlot(int index) { return null; }
	
	private boolean isLichSkull(ItemStack stack) { return !stack.isEmpty() && stack.getItem() == CMItems.LICH_SKULL.get(); }
	
	private boolean isLich() { return PlayerData.isLich((Entity)(Object)this); }
	
	@Inject(method = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
	public void onSkullThrow(int slotClicked, int bitMask, ClickType type, Player player, final CallbackInfo ci)
	{
		if(!isLich()) return;
		
		Inventory inv = player.getInventory();
		Slot slot = slotClicked >= 0 ? getSlot(slotClicked) : null;
		switch(type)
		{
			case PICKUP:
				if(isLichSkull(getCarried()))
				{
					// Negative indices indicate an attempt to drop an item
					if(slotClicked < 0)
						ci.cancel();
					else if(slot.container != inv)
						ci.cancel();
				}
				break;
			case QUICK_MOVE:
				if(isLichSkull(slot.getItem()))
					ci.cancel();
				break;
			case SWAP:
				ItemStack stackA = slot.getItem();
				ItemStack stackB = inv.getItem(bitMask);
				if((isLichSkull(stackA) || isLichSkull(stackB)) && inv != slot.container)
					ci.cancel();
				break;
			case THROW:
				if(isLichSkull(slot.getItem()))
					ci.cancel();
				break;
			case CLONE:
			case PICKUP_ALL:
			case QUICK_CRAFT:
				return;
			default:
				break;
		}
	}
}
