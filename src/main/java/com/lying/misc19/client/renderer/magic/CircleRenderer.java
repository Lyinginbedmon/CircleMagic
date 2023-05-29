package com.lying.misc19.client.renderer.magic;

import java.util.function.BiConsumer;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.SpellTexture;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class CircleRenderer extends ComponentRenderer
{
	public void addToTexture(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		super.addToTexture(component, func);
		
		Vec2 pos = component.position();
		Vec2 core = component.core();
		Vec2 up = component.up();
		func.accept(SpellTexture.addCircle((int)core.x, (int)core.y, 10, 1.25F, false), Canvas.DECORATIONS);
		func.accept(SpellTexture.addCircle((int)core.x, (int)core.y, 55, 1.25F, false), Canvas.DECORATIONS);
		func.accept(SpellTexture.addCircle((int)core.x, (int)core.y, 65, 1.25F, false), Canvas.DECORATIONS);
		func.accept(SpellTexture.addLine(pos.add(up.scale(-10F)), core.add(up.scale(10F)), 1.25F), Canvas.DECORATIONS);
	}
}
