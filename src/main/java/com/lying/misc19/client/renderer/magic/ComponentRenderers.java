package com.lying.misc19.client.renderer.magic;

import java.util.HashMap;
import java.util.Map;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.renderer.magic.components.CircleRenderer;
import com.lying.misc19.client.renderer.magic.components.ComponentRenderer;
import com.lying.misc19.client.renderer.magic.components.HertzRenderer;
import com.lying.misc19.client.renderer.magic.components.LesserSigilRenderer;
import com.lying.misc19.client.renderer.magic.components.RootRenderer;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.utility.SpellTextureManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class ComponentRenderers
{
	private static Map<ResourceLocation, ComponentRenderer> REGISTRY = new HashMap<>();
	
	static
	{
		register(new RootRenderer(), SpellComponents.ROOT_CASTER, SpellComponents.ROOT_DUMMY, SpellComponents.ROOT_POSITION, SpellComponents.ROOT_TARGET);
		register(new HertzRenderer(), SpellComponents.HERTZ_MINUTE, SpellComponents.HERTZ_SECOND);
		register(new CircleRenderer(), SpellComponents.CIRCLE_BASIC, SpellComponents.CIRCLE_STEP);
		register(new LesserSigilRenderer(), 
				Slot.AMUN.glyph(), Slot.ANUBIS.glyph(), Slot.APEP.glyph(), Slot.BAST.glyph(), Slot.BES.glyph(), Slot.HATHOR.glyph(), Slot.HORUS.glyph(), Slot.ISIS.glyph(),
				Slot.NEPTHYS.glyph(), Slot.OSIRIS.glyph(), Slot.PTAH.glyph(), Slot.RA.glyph(), Slot.SOBEK.glyph(), Slot.SUTEKH.glyph(), Slot.TAWARET.glyph(), Slot.THOTH.glyph(),
				Slot.AGE.glyph(), Slot.CASTER.glyph(), Slot.CONTINUE.glyph(), Slot.INDEX.glyph(), Slot.LOOK.glyph(), Slot.POSITION.glyph(), Slot.TARGET.glyph());
	}
	
	public static void register(ComponentRenderer renderer, ResourceLocation... names)
	{
		for(ResourceLocation name : names)
			register(name, renderer);
	}
	
	public static void register(ResourceLocation name, ComponentRenderer renderer)
	{
		REGISTRY.put(name, renderer);
	}
	
	public static ComponentRenderer get(ResourceLocation name) { return REGISTRY.getOrDefault(name, new ComponentRenderer()); }
	
	public static void renderWorld(ISpellComponent component, PoseStack matrixStack, MultiBufferSource bufferSource)
	{
		populateCanvas(component).drawIntoWorld(matrixStack, bufferSource);
	}
	
	public static Canvas populateCanvas(ISpellComponent component)
	{
		return populateCanvas(component, SpellTextureManager.TEXTURE_EDITOR_HELD, 4);
	}
	
	public static Canvas populateCanvas(ISpellComponent component, ResourceLocation textureLocation, int resolution)
	{
		Canvas canvas = new Canvas(textureLocation, resolution);
		canvas.populate(component);
		return canvas;
	}
}
