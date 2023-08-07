package com.lying.circles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.circles.init.CMItems;

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
	
	@Inject(method = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
	public void onSkullThrow(int slotClicked, int bitMask, ClickType type, Player player, final CallbackInfo ci)
	{
		System.out.println("Slot clicked: "+slotClicked+", Origin slot: "+bitMask+", type: "+type.name());
		Inventory inv = player.getInventory();
		if(type == ClickType.THROW)
		{
			ItemStack itemB = inv.getItem(slotClicked);
			if(isLichSkull(itemB))
				ci.cancel();
		}
		else if((type == ClickType.PICKUP || type == ClickType.QUICK_MOVE) && (bitMask == 0 || bitMask == 1))
		{
			if(!getCarried().isEmpty() && isLichSkull(getCarried()))
			{
				if(slotClicked == -999)
					ci.cancel();
				else if(getSlot(slotClicked).container != inv)
					ci.cancel();
			}
			else
			{
				
			}
		}
		else if(type == ClickType.SWAP)
		{
			// Prevent moving lich skulls out of player inventory
		}
	}
}
