package com.lying.circles.client.renderer.magic.components;

import java.util.function.BiConsumer;

import com.lying.circles.client.Canvas;
import com.lying.circles.client.SpellTexture;
import com.lying.circles.client.Canvas.ExclusionCircle;
import com.lying.circles.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class RootRenderer extends ComponentRenderer
{
	public int spriteScale() { return 24; }
	
	public void addToCanvas(ISpellComponent component, Canvas canvas)
	{
		Vec2 pos = component.position();
		canvas.addElement(new ExclusionCircle(pos, spriteScale() + 5), Canvas.EXCLUSIONS);
	}
	
	public void addToTexture(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		Vec2 pos = component.position();
		Vec2 up = component.up();
		func.accept(PixelPolygon.diamond(pos, up, spriteScale() + 5, 1.5F), Canvas.GLYPHS);
		
		func.accept(PixelPolygon.regularPolygon(6, 50, pos, up, 1.5F, false), Canvas.DECORATIONS);
		func.accept(SpellTexture.addCircle(pos, 75, 1.25F, false), Canvas.DECORATIONS + 20);
		func.accept(SpellTexture.addCircle(pos, 85, 1.25F, false), Canvas.DECORATIONS + 20);
	}
}
