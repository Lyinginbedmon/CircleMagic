package com.lying.circles.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.client.ClientSetupEvents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncPlayerData
{
	private UUID playerId;
	private CompoundTag dataNBT;
	
	public PacketSyncPlayerData(UUID uuidIn, CompoundTag data)
	{
		this.playerId = uuidIn;
		this.dataNBT = data;
	}
	
	public static PacketSyncPlayerData decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketSyncPlayerData(par1Buffer.readUUID(), par1Buffer.readNbt());
	}
	
	public static void encode(PacketSyncPlayerData msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.playerId);
		par1Buffer.writeNbt(msg.dataNBT);
	}
	
	public static void handle(PacketSyncPlayerData msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> 
		{
			if(!context.getDirection().getReceptionSide().isServer())
			{
				Level world = ClientSetupEvents.getLevel();
				Player player = world.getPlayerByUUID(msg.playerId);
				if(player != null)
					PlayerData.getCapability(player).deserializeNBT(msg.dataNBT);
			}
		});
	}
}
