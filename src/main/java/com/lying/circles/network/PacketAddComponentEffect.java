package com.lying.circles.network;

import java.util.function.Supplier;

import com.lying.circles.client.renderer.magic.ComponentEffectsRegistry;
import com.lying.circles.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class PacketAddComponentEffect
{
	private final ResourceLocation registryName;
	private final Vec3 position;
	private final CompoundTag data;
	private final int duration;
	
	public PacketAddComponentEffect(String nameIn, Vec3 pos, CompoundTag dataIn, int ticks)
	{
		this(new ResourceLocation(Reference.ModInfo.MOD_ID, nameIn), pos, dataIn, ticks);
	}
	
	public PacketAddComponentEffect(ResourceLocation nameIn, Vec3 pos, CompoundTag dataIn, int ticks)
	{
		this.registryName = nameIn;
		this.position = pos;
		this.data = dataIn;
		this.duration = ticks;
	}
	
	public static PacketAddComponentEffect decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketAddComponentEffect(par1Buffer.readResourceLocation(), new Vec3(par1Buffer.readDouble(), par1Buffer.readDouble(), par1Buffer.readDouble()), par1Buffer.readNbt(), par1Buffer.readInt());
	}
	
	public static void encode(PacketAddComponentEffect msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeResourceLocation(msg.registryName);
		par1Buffer.writeDouble(msg.position.x);
		par1Buffer.writeDouble(msg.position.y);
		par1Buffer.writeDouble(msg.position.z);
		par1Buffer.writeNbt(msg.data);
		par1Buffer.writeInt(msg.duration);
	}
	
	public static void handle(PacketAddComponentEffect msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> 
		{
			if(context.getDirection().getReceptionSide().isServer())
				return;
			
			ComponentEffectsRegistry.addNewEffect(msg.registryName, msg.position, msg.data, msg.duration);
		});
	}
}
