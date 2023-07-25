package com.lying.circles.utility.shapes;

import java.util.List;

import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec2;

/** Describes a 4-sided polygon in 2D space */
public class Quad2
{
	private static final Vec2[] DIRECTIONS = new Vec2[] { new Vec2(0, 1), new Vec2(0, -1), new Vec2(1, 0), new Vec2(-1, 0) };
	private final Line2 ab, bc, cd, da;
	private final Line2[] boundaries;
	private final Vec2[] vertices;
	
	public Quad2(Vec2 a, Vec2 b, Vec2 c, Vec2 d)
	{
		this.ab = new Line2(a, b);
		this.bc = new Line2(b, c);
		this.cd = new Line2(c, d);
		this.da = new Line2(d, a);
		
		this.vertices = new Vec2[] {a, b, c, d};
		this.boundaries = new Line2[]{ab, bc, cd, da};
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
	
	public Line2 ab(){ return ab; }
	public Line2 bc(){ return bc; }
	public Line2 cd(){ return cd; }
	public Line2 da(){ return da; }
	
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
	public Line2 intersects(Quad2 quad2)
	{
		for(Line2 bounds : boundaries)
			for(Line2 boundsB : quad2.boundaries)
				if(bounds.intercept(boundsB) != null)
					return boundsB;
		return null;
	}
	
	/** Returns true if the given line intersects with this quad */
	public boolean intersects(Line2 line)
	{
		for(Line2 bounds : boundaries)
			if(bounds.intercept(line) != null)
				return true;
		return false;
	}
	
	/** Returns true if the space of the given quad is entirely within this quad */
	public boolean entirelyOverlaps(Quad2 quad2)
	{
		// If there are no intersections...
		for(Line2 vec : boundaries)
			for(Line2 vec2 : quad2.boundaries)
				if(vec.intercept(vec2) != null)
					return false;
		
		// But at least one vertex of the quad is inside of this quad...
		for(Vec2 vertex : quad2.vertices)
			for(Vec2 dir : DIRECTIONS)
			{
				int intersections = 0;
				Line2 testLine = new Line2(vertex, vertex.add(dir.scale(Float.MAX_VALUE)));
				for(Line2 bounds : boundaries)
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
	public List<Quad2> splitAlong(Line2 line)
	{
		/**
		 * Check if the left and right lines are both intersected
		 * 	If so, create new quads using the intersections
		 * If not, check if the top and bottom lines are both intersected
		 * 	If so, create new quads using the intersections
		 * Otherwise, return null
		 */
		
		// Intersections on the vertical axis
		Vec2 abInt = ab.intercept(line);
		Vec2 cdInt = cd.intercept(line);
		
		if(abInt != null && cdInt != null)
		{
			Quad2 q1 = new Quad2(a(), abInt, cdInt, d());
			Quad2 q2 = new Quad2(abInt, b(), c(), cdInt);
			return List.of(q1, q2);
		}
		
		// Intersections on the horizontal axis
		Vec2 bcInt = bc.intercept(line);
		Vec2 daInt = da.intercept(line);
		
		if(bcInt != null && daInt != null)
		{
			Quad2 q1 = new Quad2(a(), b(), bcInt, daInt);
			Quad2 q2 = new Quad2(daInt, bcInt, c(), d());
			return List.of(q1, q2);
		}
		
		return List.of(this);
	}
	
	/** Returns true if the region of this quad has zero depth on any side */
	public boolean isUndrawable()
	{
		float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
		
		for(Line2 vertex : boundaries)
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
}
