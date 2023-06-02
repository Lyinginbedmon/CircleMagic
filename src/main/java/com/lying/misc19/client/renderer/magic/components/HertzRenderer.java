package com.lying.misc19.client.renderer.magic.components;

import java.util.function.BiConsumer;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.Canvas.ExclusionCircle;
import com.lying.misc19.client.SpellTexture;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class HertzRenderer extends ComponentRenderer
{
	public void addToCanvas(ISpellComponent component, Canvas canvas)
	{
		Vec2 pos = component.position();
		canvas.addElement(new ExclusionCircle(pos, spriteScale() - 6), Canvas.SPRITES);
	}
	
	public void addToTexture(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		func.accept(SpellTexture.addCircle(component.position(), spriteScale() - 6, 1.25F, true), Canvas.GLYPHS - 1);
	}
}
