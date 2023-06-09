package com.lying.circles.client.renderer.magic.components;

import java.util.function.BiConsumer;

import com.lying.circles.client.Canvas;
import com.lying.circles.client.Canvas.ExclusionCircle;
import com.lying.circles.client.Canvas.Sprite;
import com.lying.circles.client.SpellTexture;
import com.lying.circles.client.renderer.magic.ComponentRenderers;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.magic.component.ComponentBase;

import net.minecraft.world.phys.Vec2;

public class ComponentRenderer
{
	public int spriteScale() { return 16; }
	
	/** Adds the given component and all of its descendants to the canvas */
	public final void addToCanvasRecursive(ISpellComponent component, Canvas canvas)
	{
		int colour = ((ComponentBase)component).getErrorState().color();
		canvas.addElement(new Sprite(component.spriteLocation(), component.position(), spriteScale(), spriteScale(), colour), Canvas.SPRITES);
		addToCanvas(component, canvas);
		component.inputs().forEach((input) -> ComponentRenderers.get(input.getRegistryName()).addToCanvasRecursive(input, canvas));
		component.outputs().forEach((output) -> ComponentRenderers.get(output.getRegistryName()).addToCanvasRecursive(output, canvas));
	}
	
	public void addToCanvas(ISpellComponent component, Canvas canvas)
	{
		Vec2 pos = component.position();
		canvas.addElement(new ExclusionCircle(pos, spriteScale() - 6), Canvas.EXCLUSIONS);
	}
	
	public final void addToTextureRecursive(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		addToTexture(component, func);
		component.inputs().forEach((input) -> ComponentRenderers.get(input.getRegistryName()).addToTextureRecursive(input, func));
		component.outputs().forEach((output) -> ComponentRenderers.get(output.getRegistryName()).addToTextureRecursive(output, func));
	}
	
	public void addToTexture(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		func.accept(SpellTexture.addCircle(component.position(), spriteScale() - 6, 1.25F, true), Canvas.GLYPHS);
	}
}
