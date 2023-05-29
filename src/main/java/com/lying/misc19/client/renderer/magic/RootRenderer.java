package com.lying.misc19.client.renderer.magic;

import java.util.function.BiConsumer;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.Canvas.ExclusionCircle;
import com.lying.misc19.client.SpellTexture;
import com.lying.misc19.magic.ISpellComponent;

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
		func.accept(SpellTexture.addDiamond(1.5F, pos, component.up(), spriteScale() + 5), Canvas.GLYPHS);
		
		func.accept(SpellTexture.addRegularPolygon(6, 50, pos, component.up(), 1.5F), Canvas.DECORATIONS);
		func.accept(SpellTexture.addCircle((int)pos.x, (int)pos.y, 75, 1.25F, false), Canvas.DECORATIONS);
		func.accept(SpellTexture.addCircle((int)pos.x, (int)pos.y, 85, 1.25F, false), Canvas.DECORATIONS);
	}
}
