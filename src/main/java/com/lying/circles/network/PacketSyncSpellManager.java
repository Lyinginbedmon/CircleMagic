package com.lying.circles.network;

import java.util.function.Supplier;

import com.lying.circles.client.ClientSetupEvents;
import com.lying.circles.utility.SpellManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncSpellManager
{
	private CompoundTag dataNBT;
	
	public PacketSyncSpellManager(SpellManager dataIn)
	{
		this(dataIn.save(new CompoundTag()));
	}
	
	public PacketSyncSpellManager(CompoundTag dataIn)
	{
		this.dataNBT = dataIn;
	}
	
	public static PacketSyncSpellManager decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketSyncSpellManager(par1Buffer.readNbt());
	}
	
	public static void encode(PacketSyncSpellManager msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeNbt(msg.dataNBT);
	}
	
	public static void handle(PacketSyncSpellManager msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> 
		{
			if(!context.getDirection().getReceptionSide().isServer())
				ClientSetupEvents.getLocalData().read(msg.dataNBT);
		});
	}
}
