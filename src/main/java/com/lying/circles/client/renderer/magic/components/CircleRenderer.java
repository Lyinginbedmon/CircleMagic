package com.lying.circles.client.renderer.magic.components;

import java.util.function.BiConsumer;

import com.lying.circles.client.Canvas;
import com.lying.circles.client.SpellTexture;
import com.lying.circles.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class CircleRenderer extends ComponentRenderer
{
	public void addToTexture(ISpellComponent component, BiConsumer<PixelProvider,Integer> func)
	{
		super.addToTexture(component, func);
		
		Vec2 pos = component.position();
		Vec2 core = component.core();
		Vec2 up = component.up();
		func.accept(SpellTexture.addCircle(core, 10, 1.25F, false), Canvas.DECORATIONS + 20);
		func.accept(SpellTexture.addCircle(core, 55, 1.25F, false), Canvas.DECORATIONS + 20);
		func.accept(SpellTexture.addCircle(core, 65, 1.25F, false), Canvas.DECORATIONS + 20);
		func.accept(SpellTexture.addLine(pos.add(up.scale(-10F)), core.add(up.scale(10F)), 1.25F), Canvas.DECORATIONS + 20);
	}
}
