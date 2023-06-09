package com.lying.circles.utility;

import com.lying.circles.reference.Reference;

import net.minecraft.resources.ResourceLocation;

public class SpellTextureManager
{
	public static final ResourceLocation TEXTURE_EDITOR_MAIN = new ResourceLocation(Reference.ModInfo.MOD_ID, "magic/editor_display");
	public static final ResourceLocation TEXTURE_EDITOR_HELD = new ResourceLocation(Reference.ModInfo.MOD_ID, "magic/editor_held");
	
	private static int INDEX = 0;
	
	public static ResourceLocation getNewTexture() { return new ResourceLocation(Reference.ModInfo.MOD_ID, "spell_texture_"+(INDEX++)); }
}
