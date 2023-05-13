package com.lying.misc19.client.renderer.magic;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.Canvas.Circle;
import com.lying.misc19.client.Canvas.ExclusionCircle;
import com.lying.misc19.client.Canvas.Sprite;
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
		canvas.addElement(new Circle(pos, spriteScale() - 6, 1.25F), Canvas.GLYPHS);
	}
}
