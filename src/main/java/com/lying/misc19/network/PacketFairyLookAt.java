package com.lying.misc19.network;

import java.util.Optional;
import java.util.function.Supplier;

import com.lying.misc19.blocks.entity.FairyJarBlockEntity;
import com.lying.misc19.client.ClientSetupEvents;
import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class PacketFairyLookAt
{
	private BlockPos position;
	private Vec3 target;
	
	public PacketFairyLookAt(BlockPos fairyPos, Vec3 lookPos)
	{
		this.position = fairyPos;
		this.target = lookPos;
	}
	
	public static PacketFairyLookAt decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketFairyLookAt(par1Buffer.readBlockPos(), new Vec3(par1Buffer.readDouble(), par1Buffer.readDouble(), par1Buffer.readDouble()));
	}
	
	public static void encode(PacketFairyLookAt msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeBlockPos(msg.position);
		par1Buffer.writeDouble(msg.target.x);
		par1Buffer.writeDouble(msg.target.y);
		par1Buffer.writeDouble(msg.target.z);
	}
	
	public static void handle(PacketFairyLookAt msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> 
		{
			if(context.getDirection().getReceptionSide().isServer())
				return;
			
			Optional<FairyJarBlockEntity> tile = ClientSetupEvents.getLevel().getBlockEntity(msg.position, M19BlockEntities.FAIRY_JAR.get());
			if(tile.isPresent())
				tile.get().lookAt(msg.target);
		});
	}
}
