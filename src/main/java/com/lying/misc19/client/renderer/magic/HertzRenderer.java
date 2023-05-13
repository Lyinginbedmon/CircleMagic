package com.lying.misc19.client.renderer.magic;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.Canvas.Circle;
import com.lying.misc19.client.Canvas.ExclusionCircle;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class HertzRenderer extends ComponentRenderer
{
	public void addToCanvas(ISpellComponent component, Canvas canvas)
	{
		Vec2 pos = component.position();
		canvas.addElement(new ExclusionCircle(pos, spriteScale() - 6), Canvas.SPRITES);
		canvas.addElement(new Circle(pos, spriteScale() - 6, 1.25F), Canvas.SPRITES);
	}
}
