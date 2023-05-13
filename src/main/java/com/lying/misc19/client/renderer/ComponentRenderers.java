package com.lying.misc19.client.renderer;

import java.util.HashMap;
import java.util.Map;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.renderer.magic.CircleRenderer;
import com.lying.misc19.client.renderer.magic.ComponentRenderer;
import com.lying.misc19.client.renderer.magic.HertzRenderer;
import com.lying.misc19.client.renderer.magic.RootRenderer;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;
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
	
	public static void renderGUI(ISpellComponent component, PoseStack matrixStack, int width, int height)
	{
		// FIXME Don't render any component that is outside the bounds of the GUI
		populateCanvas(component, matrixStack).drawIntoGUI(matrixStack, width, height);
	}
	
	public static void renderWorld(ISpellComponent component, PoseStack matrixStack, MultiBufferSource bufferSource)
	{
		populateCanvas(component, matrixStack).drawIntoWorld(matrixStack, bufferSource);
	}
	
	public static Canvas populateCanvas(ISpellComponent component, PoseStack matrixStack)
	{
		Canvas canvas = new Canvas();
		
		ResourceLocation registryName = component.getRegistryName();
		ComponentRenderer renderer = REGISTRY.getOrDefault(registryName, new ComponentRenderer());
		
		renderer.addToCanvasRecursive(component, canvas);
		return canvas;
	}
}
