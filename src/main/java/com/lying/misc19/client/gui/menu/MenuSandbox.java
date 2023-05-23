package com.lying.misc19.client.gui.menu;

import javax.annotation.Nullable;

import com.lying.misc19.init.M19Menus;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MenuSandbox extends AbstractContainerMenu
{
	private final ContainerData editorData;
	private ISpellComponent arrangement = null;
	
	private final Slot boughInput;
	
	public MenuSandbox(int containerId, Inventory inv) { this(containerId, inv, new SimpleContainerData(1), new SimpleContainer(1), null, 255); }
	
	public MenuSandbox(int containerId, Inventory inv, ContainerData dataIn, @Nullable Container treeIn, @Nullable ISpellComponent spellIn, int capIn)
	{
		super(M19Menus.SANDBOX_MENU.get(), containerId);
		this.arrangement = spellIn;
		
		this.addSlot(boughInput = new Slot(treeIn, 0, 136, 110) 
		{
			public boolean mayPickup(Player player) { return false; }
//			public boolean isActive() { return false; }
		});
		
		this.editorData = dataIn;
		setCap(capIn);
		
		this.addDataSlots(dataIn);
		
	}
	
	public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	
	public boolean stillValid(Player player) { return true; }
	
	public ISpellComponent arrangement() { return this.arrangement; }
	
	public void setArrangement(ISpellComponent spellIn) { this.arrangement = spellIn; }
	
	public int getCap() { return this.editorData.get(0); }
	
	public void setCap(int capIn) { this.editorData.set(0, capIn); }
}
