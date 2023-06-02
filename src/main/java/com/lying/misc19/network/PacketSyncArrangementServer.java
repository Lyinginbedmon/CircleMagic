package com.lying.misc19.network;

import java.util.function.Supplier;

import com.lying.misc19.blocks.entity.ArrangementHolder;
import com.lying.misc19.init.SpellComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncArrangementServer
{
	private BlockPos position;
	private CompoundTag arrangement;
	
	public PacketSyncArrangementServer(BlockPos pos)
	{
		this.position = pos;
		this.arrangement = new CompoundTag();
	}
	
	public PacketSyncArrangementServer(BlockPos pos, CompoundTag spell)
	{
		this(pos);
		arrangement = spell;
	}
	
	public static PacketSyncArrangementServer decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketSyncArrangementServer(par1Buffer.readBlockPos(), par1Buffer.readNbt());
	}
	
	public static void encode(PacketSyncArrangementServer msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeBlockPos(msg.position);
		par1Buffer.writeNbt(msg.arrangement);
	}
	
	public static void handle(PacketSyncArrangementServer msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> 
		{
			if(context.getDirection().getReceptionSide().isClient())
				return;
			
			Level world = context.getSender().getLevel();
			BlockEntity tile = world.getBlockEntity(msg.position);
			if(tile != null && tile instanceof ArrangementHolder)
				((ArrangementHolder)tile).setArrangement(msg.arrangement.isEmpty() ? null : SpellComponents.readFromNBT(msg.arrangement));
		});
	}
}
