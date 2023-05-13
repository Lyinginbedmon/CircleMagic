package com.lying.misc19.client.renderer.magic;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.Canvas.Circle;
import com.lying.misc19.client.Canvas.Line;
import com.lying.misc19.magic.ISpellComponent;

import net.minecraft.world.phys.Vec2;

public class CircleRenderer extends ComponentRenderer
{
	public void addToCanvas(ISpellComponent component, Canvas canvas)
	{
		super.addToCanvas(component, canvas);
		
		Vec2 pos = component.position();
		Vec2 core = component.core();
		Vec2 up = component.up();
		
		canvas.addElement(new Line(pos.add(up.scale(-10F)), core.add(up.scale(10F)), 1F), Canvas.DECORATIONS);
		canvas.addElement(new Circle(core, 10, 1.25F), Canvas.DECORATIONS);
		canvas.addElement(new Circle(component.core(), 55, 1.25F), Canvas.DECORATIONS);
		canvas.addElement(new Circle(component.core(), 65, 1.25F), Canvas.DECORATIONS);
	}
}
