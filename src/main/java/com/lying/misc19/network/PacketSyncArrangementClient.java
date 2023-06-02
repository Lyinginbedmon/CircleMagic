package com.lying.misc19.network;

import java.util.function.Supplier;

import com.lying.misc19.client.gui.screen.ScreenSandbox;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncArrangementClient
{
	private CompoundTag arrangement;
	
	public PacketSyncArrangementClient()
	{
		this.arrangement = new CompoundTag();
	}
	
	public PacketSyncArrangementClient(CompoundTag spell)
	{
		arrangement = spell;
	}
	
	public static PacketSyncArrangementClient decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketSyncArrangementClient(par1Buffer.readNbt());
	}
	
	public static void encode(PacketSyncArrangementClient msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeNbt(msg.arrangement);
	}
	
	@SuppressWarnings("resource")
	public static void handle(PacketSyncArrangementClient msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> 
		{
			if(context.getDirection().getReceptionSide().isServer())
				return;
			
			Screen currentScreen = Minecraft.getInstance().screen;
			if(currentScreen == null || !(currentScreen instanceof ScreenSandbox))
				return;
			
			ISpellComponent spell = msg.arrangement.isEmpty() ? null : SpellComponents.readFromNBT(msg.arrangement);
			ScreenSandbox editor = (ScreenSandbox)currentScreen;
			editor.getMenu().setArrangement(spell, true);
		});
	}
}
