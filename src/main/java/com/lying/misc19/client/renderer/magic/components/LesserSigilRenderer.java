package com.lying.misc19.client.renderer.magic.components;

import java.util.function.BiConsumer;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.SpellTexture;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class LesserSigilRenderer extends ComponentRenderer
{
	public void addToTexture(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		Vec2 pos = component.position();
		Vec2 up = component.up();
		int scale = spriteScale();
		
		switch(component.state())
		{		
			case OUTPUT:
				func.accept(SpellTexture.addCircle(pos, spriteScale() - 6, 1.25F, true), Canvas.GLYPHS);
				func.accept(PixelPolygon.regularPolygon(3, scale, pos, up.negated(), 1.25F, true), Canvas.GLYPHS + 1);
				break;
			case NORMAL:
				func.accept(SpellTexture.addCircle(pos, spriteScale() - 6, 1.25F, true), Canvas.GLYPHS);
				break;
			case INPUT:
			default:
				func.accept(SpellTexture.addCircle(pos, spriteScale() - 6, 0F, true), Canvas.GLYPHS + 1);
				break;
		}
	}
}
