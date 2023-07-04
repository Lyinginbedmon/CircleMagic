package com.lying.circles.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.circles.capabilities.LivingData;
import com.lying.circles.client.ClientSetupEvents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncLivingData
{
	private UUID playerId;
	private Vec3 lastKnownPos;
	private CompoundTag dataNBT;
	
	public PacketSyncLivingData(UUID uuidIn, Vec3 posIn, CompoundTag data)
	{
		this.playerId = uuidIn;
		this.lastKnownPos = posIn;
		this.dataNBT = data;
	}
	
	public static PacketSyncLivingData decode(FriendlyByteBuf par1Buffer)
	{
		double x = par1Buffer.readDouble();
		double y = par1Buffer.readDouble();
		double z = par1Buffer.readDouble();
		return new PacketSyncLivingData(par1Buffer.readUUID(), new Vec3(x, y, z), par1Buffer.readNbt());
	}
	
	public static void encode(PacketSyncLivingData msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeDouble(msg.lastKnownPos.x);
		par1Buffer.writeDouble(msg.lastKnownPos.y);
		par1Buffer.writeDouble(msg.lastKnownPos.z);
		par1Buffer.writeUUID(msg.playerId);
		par1Buffer.writeNbt(msg.dataNBT);
	}
	
	public static void handle(PacketSyncLivingData msg, Supplier<NetworkEvent.Context> cxt)
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
				{
					LivingData.getCapability(player).deserializeNBT(msg.dataNBT);
					return;
				}
				
				double yMin = Math.max(msg.lastKnownPos.y - 16, world.dimensionType().minY());
				double yMax = yMin + 16;
				AABB bounds = new AABB(msg.lastKnownPos.x - 16, yMin, msg.lastKnownPos.z - 16, msg.lastKnownPos.x + 16, yMax, msg.lastKnownPos.z + 16);
				world.getEntitiesOfClass(LivingEntity.class, bounds, (living) -> living.isAlive() && living.getUUID().equals(msg.playerId)).forEach((ent) -> 
					LivingData.getCapability(ent).deserializeNBT(msg.dataNBT));
			}
		});
	}
}
