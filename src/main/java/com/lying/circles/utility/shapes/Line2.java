package com.lying.circles.utility.shapes;

import javax.annotation.Nullable;

import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec2;

public class Line2 extends Tuple<Vec2, Vec2>
{
	// Components of the slope intercept equation of this line
	// y = mx + b OR x = a
	private final float m, b;
	private final Tuple<Float, Float> xRange, yRange;
	private final boolean isVertical;
	
	public Line2(Vec2 posA, Vec2 posB)
	{
		super(posA, posB);
		xRange = new Tuple<Float, Float>(Math.min(posA.x, posB.x), Math.max(posA.x, posB.x));
		yRange = new Tuple<Float, Float>(Math.min(posA.y, posB.y), Math.max(posA.y, posB.y));
		
		float run = getB().x - getA().x;
		float rise = getB().y - getA().y;
		
		isVertical = run == 0;
		if(isVertical)
		{
			m = getA().x;
			b = 0;
		}
		else
		{
			m = run == 0 ? 0 : rise / run;
			b = getA().y - (getA().x * m);
		}
	}
	
	/** Returns true if these two lines have identical complex properties */
	public boolean equals(Line2 line)
	{
		return isVertical == line.isVertical && m == line.m && b == line.b && xRange.equals(line.xRange) && yRange.equals(line.yRange);
	}
	
	/** Returns true if this is the same line, without checking complex properties */
	public boolean simpleEquals(Line2 line)
	{
		return (getA().equals(line.getA()) && getB().equals(line.getB())) || (getA().equals(line.getB()) && getB().equals(getA()));
	}
	
	public String toShortString()
	{
		if(isVertical)
			return "Line[x = "+m+"]";
		else if(m == 0)
			return "Line[y = "+b+"]";
		else
			return "Line[y = ("+m+")x + "+b+"]";
	}
	
	public String toString()
	{
		if(isVertical)
			return "Line[x = "+m+", y range "+xRange.getA()+" - "+xRange.getB()+"]";
		else if(m == 0)
			return "Line[y = "+b+", x range "+xRange.getA()+" - "+xRange.getB()+"]";
		else
			return "Line[y = ("+m+")x + "+b+", x range "+xRange.getA()+" - "+xRange.getB()+"]";
	}
	
	/** Returns true if the given lines are parallel */
	public static boolean areParallel(Line2 a, Line2 b)
	{
		if(a.isVertical && b.isVertical)
			return true;
		else if(!a.isVertical && !b.isVertical)
			return a.m == b.m;
		return false;
	}
	
	/** Returns the point that this line intersects with the given line, if at all */
	@Nullable
	public Vec2 intercept(Line2 line2) { return intercept(line2, false); }
	
	@Nullable
	public Vec2 intercept(Line2 line2, boolean ignoreRange)
	{
		Vec2 intercept = null;
		if(isVertical && line2.isVertical)
		{
			// Two vertical lines = Parallel: No intercept
			// In the unlikely scenario that two vertical lines directly overlap, we treat them as side-by-side
			return null;
		}
		else if(isVertical != line2.isVertical)
		{
			// Handle one vertical and one non-vertical line
			// X of vertical line determines y = mx + B of non-vertical line
			// Intercept if within range of both
			
			Line2 vert = isVertical ? this : line2;
			Line2 hori = isVertical ? line2 : this;
			
			// Horizontal line's position at the X coordinate of the vertical line
			float x = vert.m;
			intercept = new Vec2(x, (hori.m * x) + hori.b);
		}
		else
		{
			// Return null if both lines have the same slope and are therefore parallel
			if(this.m == line2.m)
				return null;
			
			// Handle two non-vertical lines
			float a = this.m, b = line2.m;
			float c = this.b, d = line2.b;
			
			float x = (d - c) / (a - b);
			float y = (a * x) + c;
			
			// Point at which these lines would intersect, if they were of infinite length
			intercept = new Vec2(x, y);
		}
		
		// Optional range check
		return (ignoreRange || (inRange(intercept) && line2.inRange(intercept))) ? intercept : null;
	}
	
	public boolean inRange(Vec2 point)
	{
		if(point.equals(getA()) || point.equals(getB()))
			return false;
		
		if(xRange.getB() != xRange.getA())
			if(point.x < xRange.getA() || point.x > xRange.getB())
				return false;
		
		if(yRange.getB() != yRange.getA())
			if(point.y < yRange.getA() || point.y > yRange.getB())
				return false;
		
		return true;
	}
}