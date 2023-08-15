package com.lying.circles.utility.shapes;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.phys.Vec2;

public class Tri2
{
	public final Vec2 a, b, c;
	
	public final Vec2 circumcenter;
	public final double circumRadius;
	
	public Tri2(Vec2 aIn, Vec2 bIn, Vec2 cIn) throws IllegalArgumentException
	{
		a = aIn;
		b = bIn;
		c = cIn;
		
		/** Triangles cannot be composed of two or more parallel lines */
		if(checkParallel(a, b, c))
			throw new IllegalArgumentException();
		
		Pair<Vec2, Float> circumscribed = calculateCircumcircle(a, b, c);
		circumcenter = circumscribed.getFirst();
		circumRadius = circumscribed.getSecond();
	}
	
	public boolean equals(Tri2 tri)
	{
		if(tri.circumcenter.distanceToSqr(circumcenter) > 0 || tri.circumRadius != circumRadius)
			return false;
		
		return points().containsAll(tri.points());
	}
	
	/**
	 * Returns true if the direction of one line matches the direction of another.<br>
	 * This indicates that they are parallel and hence cannot describe a triangle.
	 */
	public static boolean checkParallel(Vec2 a, Vec2 b, Vec2 c)
	{
		Line2[] lines = new Line2[] {new Line2(a, b), new Line2(a, c), new Line2(b, c)};
		
		for(int i=0; i<lines.length; i++)
		{
			Line2 lineA = lines[i];
			for(int j=0; j<lines.length; j++)
			{
				if(j==i)
					continue;
				
				Line2 lineB = lines[j];
				if(Line2.areParallel(lineA, lineB))
					return true;
			}
		}
		return false;
	}
	
	public String toString() { return "Tri2[["+ a.x+", "+a.y +"], ["+ b.x+", "+b.y +"], ["+ c.x+", "+c.y +"]]"; }
	
	public List<Vec2> points() { return List.of(a, b, c); }
	
	public List<Line2> lines(){ return List.of(new Line2(a, b), new Line2(a, c), new Line2(b, c)); }
	
	public boolean contains(Tri2 tri) { return contains(tri.a) || contains(tri.b) || contains(tri.c); }
	
	public boolean contains(Line2 edge) { return contains(edge.getA()) || contains(edge.getB()); }
	
	public boolean contains(Vec2 pos) { return pos.distanceToSqr(circumcenter) < (circumRadius * circumRadius); }
	
	public Collection<Tri2> connectVerticesTo(Vec2 d)
	{
		List<Tri2> tris = Lists.newArrayList();
		
		Set.of(
				new Vec2[]{a, b, d, c}, 
				new Vec2[]{a, c, d, b}, 
				new Vec2[]{b, c, d, a}
				).forEach((combo) -> 
		{
			if(!checkParallel(combo[0], combo[1], combo[2]))
			{
				Tri2 tri = new Tri2(combo[0], combo[1], combo[2]);
				if(!tri.contains(combo[3]))
					tris.add(tri);
			}
		});
		
		return tris;
	}
	
	@Nullable
	private static Pair<Vec2, Float> calculateCircumcircle(Vec2 a, Vec2 b, Vec2 c)
	{
		// Calculate circumcenter position
		// Circumcenter = intercept point of lines perpendicular to line AB and AC from the respective midpoints
		
		Vec2 fromAtoB = b.add(a.negated());
		Vec2 abMid = a.add(fromAtoB.scale(0.5F));
		Vec2 abNormal = new Vec2(-fromAtoB.y, fromAtoB.x).normalized();
		
		Vec2 fromAtoC = c.add(a.negated());
		Vec2 acMid = a.add(fromAtoC.scale(0.5F));
		Vec2 acNormal = new Vec2(-fromAtoC.y, fromAtoC.x).normalized();
		
		Line2 ab = new Line2(abMid.add(abNormal), abMid.add(abNormal.negated()));
		Line2 ac = new Line2(acMid.add(acNormal), acMid.add(acNormal.negated()));
		Vec2 circumcenter = ab.intercept(ac, true);
		
		return Pair.of(circumcenter, a.add(circumcenter.negated()).length());
	}
	
	/** Returns an oversized triangle whose circumscribed sphere contains all given points */
	public static Tri2 makeTriangleContaining(Vec2... points)
	{
		/**
		 * Find largest and smallest coordinates on all axises (points A and B)
		 * Calculate triangle circumcentre as midpoint between A and B
		 * Calculate triangle circumradius as distance between A and B + 100 to ensure containment
		 * Calculate triangle points as a right-angled triangle based on the direction from A to B
		 */
		
		Vec2 largest = null, smallest = null;
		for(int i=0; i<points.length; i++)
		{
			Vec2 point = points[i];
			
			if(largest == null)
				largest = point;
			else
			{
				Vec2 toPoint = point.add(largest.negated());
				largest = largest.add(new Vec2(toPoint.x > 0 ? toPoint.x : 0, toPoint.y > 0 ? toPoint.y : 0));
			}
			
			if(smallest == null)
				smallest = point;
			else
			{
				Vec2 toPoint = point.add(smallest.negated());
				smallest = smallest.add(new Vec2(toPoint.x < 0 ? toPoint.x : 0, toPoint.y < 0 ? toPoint.y : 0));
			}
		}
		
		Vec2 offset = largest.add(smallest.negated());
		Vec2 center = smallest.add(offset.scale(0.5F));
		
		double turn = Math.toRadians(120D);
		Vec2 dir;
		Vec2 a = center.add(dir = offset.scale(100F));
		Vec2 b = center.add(dir = new Vec2((float)(Math.cos(turn) * dir.x - Math.sin(turn) * dir.y), (float)(Math.sin(turn) * dir.x + Math.cos(turn) * dir.y)));
		Vec2 c = center.add(new Vec2((float)(Math.cos(turn) * dir.x - Math.sin(turn) * dir.y), (float)(Math.sin(turn) * dir.x + Math.cos(turn) * dir.y)));
		
		return new Tri2(a, b, c);
	}
	
	public static List<Tri2> generateDelaunayMesh(Vec2... points)
	{
		// The triangle mesh
		List<Tri2> mesh = Lists.newArrayList();
		
		// Initial super triangle containing all points
		Tri2 superTri = makeTriangleContaining(points);
		mesh.add(superTri);
		
		for(int i=0; i<points.length; i++)
		{
			Vec2 point = points[i];
			
			List<Tri2> trisNext = Lists.newArrayList();
			List<Tri2> badTris = Lists.newArrayList();
			mesh.forEach((tri) -> 
			{
				if(tri.contains(point))
					badTris.add(tri);
				else
					trisNext.add(tri);
			});
			
			if(!badTris.isEmpty())
				triMeshToUniqueLines(badTris).forEach((edge) -> 
				{
					Vec2 a = edge.getA();
					Vec2 b = edge.getB();
					if(!checkParallel(a, b, point))
						trisNext.add(new Tri2(a, b, point));
				});
			
			mesh = trisNext;
		}
		
		// Remove any triangles connected to the super triangle
		mesh.removeIf((tri) -> 
		{
			for(Vec2 point : tri.points())
				if(superTri.points().contains(point))
					return true;
			
			return false;
		});
		
		return mesh;
	}
	
	/**
	 * Returns a list of unique lines contained in the given mesh.<br>
	 * Note: Duplicate lines are not retained in any form.
	 */
	public static List<Line2> triMeshToUniqueLines(List<Tri2> triangles)
	{
		List<Line2> edges = Lists.newArrayList();
		triangles.forEach((tri) -> tri.lines().forEach((line) -> 
		{
			int lineInd = -1;
			for(int i=0; i<edges.size(); i++)
				if(edges.get(i).simpleEquals(line))
				{
					lineInd = i;
					break;
				}
			
			if(lineInd >= 0)
				edges.remove(lineInd);
			else
				edges.add(line);
		}));
		
		return edges;
	}
}
