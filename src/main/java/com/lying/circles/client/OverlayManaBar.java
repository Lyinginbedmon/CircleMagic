package com.lying.circles.client;

import com.lying.circles.capabilities.LivingData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class OverlayManaBar implements IGuiOverlay
{
	private static Minecraft mc = Minecraft.getInstance();
	
	public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int width, int height)
	{
		if(mc.options.hideGui)
			return;
		
		LivingData living = LivingData.getCapability(mc.player);
		if(living == null)
			return;
		
		float mana = living.getCurrentMana();
		float capacity = living.getNativeCapacity();
		gui.getFont().draw(poseStack, Component.literal((int)mana + " / " + (int)capacity), 10, height - 10, -1);
	}
}
