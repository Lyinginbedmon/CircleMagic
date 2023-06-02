package com.lying.misc19.network;

import com.lying.misc19.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler
{
	private static final String PROTOCOL = "1";
	public static SimpleChannel HANDLER;
	
	private static int id = 0;
	
	private PacketHandler(){ }
	
	public static void init()
	{
		HANDLER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Reference.ModInfo.MOD_ID, "chan"),
			() -> PROTOCOL,
			PROTOCOL::equals,
			PROTOCOL::equals);
		
		HANDLER.registerMessage(id(), PacketSyncSpellManager.class, PacketSyncSpellManager::encode, PacketSyncSpellManager::decode, PacketSyncSpellManager::handle);
		HANDLER.registerMessage(id(), PacketSyncArrangementServer.class, PacketSyncArrangementServer::encode, PacketSyncArrangementServer::decode, PacketSyncArrangementServer::handle);
		HANDLER.registerMessage(id(), PacketSyncArrangementClient.class, PacketSyncArrangementClient::encode, PacketSyncArrangementClient::decode, PacketSyncArrangementClient::handle);
		HANDLER.registerMessage(id(), PacketAddComponentEffect.class, PacketAddComponentEffect::encode, PacketAddComponentEffect::decode, PacketAddComponentEffect::handle);
	}
	
	private static int id() { return id++; }
	
	/**
	 * Send message to all within 64 blocks that have this chunk loaded
	 */
	@SuppressWarnings("resource")
	public static void sendToNearby(Level world, BlockPos pos, Object toSend)
	{
		if(world instanceof ServerLevel)
		{
			ServerLevel ws = (ServerLevel) world;
			ws.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).stream().filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64).forEach(p -> HANDLER.send(PacketDistributor.PLAYER.with(() -> p), toSend));
		}
	}
	
	public static void sendToNearby(Level world, Entity e, Object toSend)
	{
		sendToNearby(world, e.blockPosition(), toSend);
	}
	
	public static void sendToAll(ServerLevel world, Object toSend)
	{
		for(ServerPlayer player : world.players())
			HANDLER.send(PacketDistributor.PLAYER.with(() -> player), toSend);
	}
	
	public static void sendTo(ServerPlayer playerMP, Object toSend)
	{
		HANDLER.send(PacketDistributor.PLAYER.with(() -> playerMP), toSend);
	}
	
	public static void sendNonLocal(ServerPlayer playerMP, Object toSend)
	{
		if(playerMP.server.isDedicatedServer() || !playerMP.server.isSingleplayerOwner(playerMP.getGameProfile()))
			sendTo(playerMP, toSend);
	}
	
	public static void sendToServer(Object msg)
	{
		HANDLER.sendToServer(msg);
	}
}
