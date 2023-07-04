package com.lying.circles.client.gui.menu;

import javax.annotation.Nullable;

import com.lying.circles.init.CMMenus;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.network.PacketSyncArrangementServer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
	
	public MenuSandbox(int containerId, Inventory inv) { this(containerId, inv, new SimpleContainer(1), null, -1, BlockPos.ZERO, null); }
	
	public MenuSandbox(int containerId, Inventory inv, @Nullable Container treeIn, @Nullable ISpellComponent spellIn, int capIn, BlockPos tilePos, @Nullable BlockPos fairyPos)
	{
		super(CMMenus.SANDBOX_MENU.get(), containerId);
		this.arrangement = spellIn;
		
		if(treeIn == null)
			this.addSlot(boughInput = new Slot(new SimpleContainer(1), 0, 136, 110));
		else
			this.addSlot(boughInput = new Slot(treeIn, 0, 136, 110) 
			{
				public boolean mayPickup(Player player) { return false; }
				public boolean isActive() { return false; }
			});
		
		this.editorData = new SimpleContainerData(8);
		setCap(capIn);
		this.editorData.set(0, tilePos.getX());
		this.editorData.set(1, tilePos.getY());
		this.editorData.set(2, tilePos.getZ());
		
		this.editorData.set(4, fairyPos != null ? 1 : 0);
		if(fairyPos != null)
		{
			this.editorData.set(5, fairyPos.getX());
			this.editorData.set(6, fairyPos.getY());
			this.editorData.set(7, fairyPos.getZ());
		}
		else
		{
			this.editorData.set(5, 0);
			this.editorData.set(6, 0);
			this.editorData.set(7, 0);
		}
		this.addDataSlots(this.editorData);
	}
	
	public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	
	public boolean stillValid(Player player) { return true; }
	
	public BlockPos tilePos()
	{
		return new BlockPos(this.editorData.get(0), this.editorData.get(1), this.editorData.get(2));
	}
	
	public boolean hasFairy() { return this.editorData.get(4) > 0; }
	
	public BlockPos fairyPos()
	{
		return new BlockPos(this.editorData.get(5), this.editorData.get(6), this.editorData.get(7));
	}
	
	public ISpellComponent arrangement() { return this.arrangement; }
	
	public void setArrangement(@Nullable ISpellComponent spellIn, boolean sync)
	{
		if(spellIn != null)
			spellIn.organise();
		this.arrangement = spellIn;
		
		if(sync)
			PacketHandler.sendToServer(new PacketSyncArrangementServer(tilePos(), spellData()));
	}
	
	public int getCap() { return this.editorData.get(3); }
	
	public void setCap(int capIn) { this.editorData.set(3, capIn); }
	
	public CompoundTag spellData()
	{
		return this.arrangement == null ? new CompoundTag() : ISpellComponent.saveToNBT(this.arrangement);
	}
	
	public void removed(Player player)
	{
		super.removed(player);
		if(!player.getLevel().isClientSide())
			PacketHandler.sendToServer(new PacketSyncArrangementServer(tilePos(), spellData()));
	}
}
