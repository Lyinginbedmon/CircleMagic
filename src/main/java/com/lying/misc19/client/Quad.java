package com.lying.misc19.client;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec2;

/** Helper class for rendering */
public class Quad
{
	private static final Vec2[] DIRECTIONS = new Vec2[] { new Vec2(0, 1), new Vec2(0, -1), new Vec2(1, 0), new Vec2(-1, 0) };
	private final Line ab, bc, cd, da;
	private final Line[] boundaries;
	private final Vec2[] vertices;
	
	public Quad(Vec2 a, Vec2 b, Vec2 c, Vec2 d)
	{
		this.ab = new Line(a, b);
		this.bc = new Line(b, c);
		this.cd = new Line(c, d);
		this.da = new Line(d, a);
		
		this.vertices = new Vec2[] {a, b, c, d};
		this.boundaries = new Line[]{ab, bc, cd, da};
	}
	
	public String toString()
	{
		return "Quad["+String.join("/",
				"["+a().x+", "+a().y+"]",
				"["+b().x+", "+b().y+"]",
				"["+c().x+", "+c().y+"]",
				"["+d().x+", "+d().y+"]")+"]";
	}
	
	public Vec2 a() { return this.vertices[0]; }
	public Vec2 b() { return this.vertices[1]; }
	public Vec2 c() { return this.vertices[2]; }
	public Vec2 d() { return this.vertices[3]; }
	
	public Line ab(){ return ab; }
	public Line bc(){ return bc; }
	public Line cd(){ return cd; }
	public Line da(){ return da; }
	
	public Tuple<Vec2, Vec2> screenBounds()
	{
		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
		for(Vec2 vertex : vertices)
		{
			minX = Math.min(minX, vertex.x);
			maxX = Math.max(maxX, vertex.x);
			minY = Math.min(minY, vertex.y);
			maxY = Math.max(maxY, vertex.y);
		}
		return new Tuple<Vec2, Vec2>(new Vec2(minX, minY), new Vec2(maxX, maxY));
	}
	
	public boolean isWithinScreen(int width, int height)
	{
		for(Vec2 vertex : vertices)
			if(vertex.x >= 0 && vertex.y >= 0 && vertex.x <= width && vertex.y <= height)
				return true;
		return false;
	}
	
	/** Returns the first boundary line of this quad that the given quad intersects with */
	public Line intersects(Quad quad2)
	{
		for(Line bounds : boundaries)
			for(Line boundsB : quad2.boundaries)
				if(bounds.intercept(boundsB) != null)
					return boundsB;
		return null;
	}
	
	/** Returns true if the given line intersects with this quad */
	public boolean intersects(Line line)
	{
		for(Line bounds : boundaries)
			if(bounds.intercept(line) != null)
				return true;
		return false;
	}
	
	/** Returns true if the space of the given quad is entirely within this quad */
	public boolean entirelyOverlaps(Quad quad2)
	{
		// If there are no intersections...
		for(Line vec : boundaries)
			for(Line vec2 : quad2.boundaries)
				if(vec.intercept(vec2) != null)
					return false;
		
		// But at least one vertex of the quad is inside of this quad...
		for(Vec2 vertex : quad2.vertices)
			for(Vec2 dir : DIRECTIONS)
			{
				int intersections = 0;
				Line testLine = new Line(vertex, vertex.add(dir.scale(Float.MAX_VALUE)));
				for(Line bounds : boundaries)
					if(bounds.intercept(testLine) != null)
						intersections++;
				
				/**
				 * There will always be an odd number of intersections if the point is inside
				 * Because if the point is inside, it should only need to hit one boundary line to escape
				 */
				return intersections % 2 != 0;
			}
		
		return false;
	}
	
	/** Returns a set of quads by separating this quad along intercepts with the given line */
	public List<Quad> splitAlong(Line line)
	{
		/**
		 * Check if the left and right lines are both intersected
		 * 	If so, create new quads using the intersections
		 * If not, check if the top and bottom lines are both intersected
		 * 	If so, create new quads using the intersections
		 * Otherwise, return null
		 * FIXME Intersections that do not exclusively produce two quads are currently ignored
		 */
		
		// Intersections on the vertical axis
		Vec2 abInt = ab.intercept(line);
		Vec2 cdInt = cd.intercept(line);
		
		if(abInt != null && cdInt != null)
		{
			Quad q1 = new Quad(a(), abInt, cdInt, d());
			Quad q2 = new Quad(abInt, b(), c(), cdInt);
			return List.of(q1, q2);
		}
		
		// Intersections on the horizontal axis
		Vec2 bcInt = bc.intercept(line);
		Vec2 daInt = da.intercept(line);
		
		if(bcInt != null && daInt != null)
		{
			Quad q1 = new Quad(a(), b(), bcInt, daInt);
			Quad q2 = new Quad(daInt, bcInt, c(), d());
			return List.of(q1, q2);
		}
		
		return List.of(this);
	}
	
	/** Returns true if the region of this quad has zero depth on any side */
	public boolean isUndrawable()
	{
		float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
		
		for(Line vertex : boundaries)
		{
			Vec2 a = vertex.getA();
			if(a.x < minX)
				minX = a.x;
			if(a.x > maxX)
				maxX = a.x;
			if(a.y < minY)
				minY = a.y;
			if(a.y > maxY)
				maxY = a.y;
			
			Vec2 b = vertex.getB();
			if(b.x < minX)
				minX = b.x;
			if(b.x > maxX)
				maxX = b.x;
			if(b.y < minY)
				minY = b.y;
			if(b.y > maxY)
				maxY = b.y;
		}
		
		return maxX - minX == 0 || maxY - minY == 0;
	}
	
	public static class Line extends Tuple<Vec2, Vec2>
	{
		// Components of the slope intercept equation of this line
		// y = mx + b OR x = a
		private final float m, b;
		private final Tuple<Float, Float> xRange, yRange;
		private final boolean isVertical;
		
		public Line(Vec2 posA, Vec2 posB)
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
		
		public String toString()
		{
			if(isVertical)
				return "Line[x = "+m+", y range "+xRange.getA()+" - "+xRange.getB()+"]";
			else if(m == 0)
				return "Line[y = "+b+", x range "+xRange.getA()+" - "+xRange.getB()+"]";
			else
				return "Line[y = ("+m+")x + "+b+", x range "+xRange.getA()+" - "+xRange.getB()+"]";
		}
		
		/** Returns the point that this line intersects with the given line, if at all */
		@Nullable
		public Vec2 intercept(Line line2)
		{
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
				
				Line vert = isVertical ? this : line2;
				Line hori = isVertical ? line2 : this;
				
				// Horizontal line's position at the X coordinate of the vertical line
				float x = vert.m;
				Vec2 intercept = new Vec2(x, (hori.m * x) + hori.b);
				if(inRange(intercept) && line2.inRange(intercept))
					return intercept;
				return null;
			}
			else
			{
				// Return null if both lines have the same slope and are therefore parallel
				if(this.m == line2.m)
					return null;
				
				// Handle two non-vertical lines
				float m1 = this.m, m2 = line2.m;
				float b1 = this.b, b2 = line2.b;
				
				float x = (b2 - m2) / (m1 - b1);
				float y = (m1 * x) + b1;
				
				// Point at which these lines would intersect, if they were of infinite length
				Vec2 intercept = new Vec2(x, y);
				
				// NOTE: Removing this check not only breaks almost all exclusion, it also sends the exclusion function into an infinite loop
				if(inRange(intercept) && line2.inRange(intercept))
					return intercept;
				return null;
			}
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
}
