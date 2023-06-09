package com.lying.circles.client.renderer.magic.components;

import java.util.function.BiConsumer;

import com.lying.circles.client.Canvas;
import com.lying.circles.client.Canvas.ExclusionCircle;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.utility.CMUtils;

import net.minecraft.world.phys.Vec2;

public class FunctionRenderer extends ComponentRenderer
{
	public void addToCanvas(ISpellComponent component, Canvas canvas)
	{
		Vec2 pos = component.position();
		canvas.addElement(new ExclusionCircle(pos, spriteScale() - 6), Canvas.SPRITES);
	}
	
	public void addToTexture(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		func.accept(PixelPolygon.regularPolygon(5, spriteScale() - 6, component.core(), component.up(), 1.25F, true), Canvas.GLYPHS);
		func.accept(PixelPolygon.regularPolygon(5, spriteScale() - 6, component.core(), CMUtils.rotate(component.up(), 36D), 1.25F, true), Canvas.GLYPHS);
	}
}
