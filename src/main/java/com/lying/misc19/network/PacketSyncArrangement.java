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

public class PacketSyncArrangement
{
	private BlockPos position;
	private CompoundTag arrangement;
	
	public PacketSyncArrangement(BlockPos pos)
	{
		this.position = pos;
		this.arrangement = new CompoundTag();
	}
	
	public PacketSyncArrangement(BlockPos pos, CompoundTag spell)
	{
		this(pos);
		arrangement = spell;
	}
	
	public static PacketSyncArrangement decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketSyncArrangement(par1Buffer.readBlockPos(), par1Buffer.readNbt());
	}
	
	public static void encode(PacketSyncArrangement msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeBlockPos(msg.position);
		par1Buffer.writeNbt(msg.arrangement);
	}
	
	public static void handle(PacketSyncArrangement msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		
		if(context.getDirection().getReceptionSide().isServer())
		{
			Level world = context.getSender().getLevel();
			BlockEntity tile = world.getBlockEntity(msg.position);
			
			System.out.println("Packet received by server");
			System.out.println("Destination: "+msg.position.toShortString());
			System.out.println("World: "+world.dimension().toString());
			System.out.println("Block: "+world.getBlockState(msg.position).getBlock().getName().getString());
			System.out.println("Tile found: "+(tile == null ? "NULL" : tile.getClass().getSimpleName()));
			
			if(tile != null && tile instanceof ArrangementHolder)
				((ArrangementHolder)tile).setArrangement(msg.arrangement.isEmpty() ? null : SpellComponents.readFromNBT(msg.arrangement));
		}
	}
}
