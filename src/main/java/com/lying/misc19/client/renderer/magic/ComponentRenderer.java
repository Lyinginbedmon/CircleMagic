package com.lying.misc19.client.renderer.magic;

import java.util.function.BiConsumer;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.Canvas.ExclusionCircle;
import com.lying.misc19.client.Canvas.Sprite;
import com.lying.misc19.client.SpellTexture;
import com.lying.misc19.client.renderer.ComponentRenderers;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class ComponentRenderer
{
	public int spriteScale() { return 16; }
	
	/** Adds the given component and all of its descendants to the canvas */
	public final void addToCanvasRecursive(ISpellComponent component, Canvas canvas)
	{
		canvas.addElement(new Sprite(component.spriteLocation(), component.position(), spriteScale(), spriteScale()), Canvas.SPRITES);
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
		Vec2 pos = component.position();
		func.accept(SpellTexture.addCircleConflictor((int)pos.x, (int)pos.y, spriteScale() - 6), Canvas.GLYPHS);
	}
}
