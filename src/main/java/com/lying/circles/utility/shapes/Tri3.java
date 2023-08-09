package com.lying.circles.utility.shapes;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Tri3
{
	private final Tri2 tri;
	private final Matrix3 matrixTo2D, matrixTo3D;
	
	private final Vec3 a, b, c;
	
	private Tri3(Matrix3 matrix2D, Matrix3 matrix3D, Vec3 a, Vec3 b, Vec3 c)
	{
		this.tri = new Tri2(Vec2.ZERO, matrix2D.to2D(b.subtract(a)), matrix2D.to2D(c.subtract(a)));
		this.matrixTo2D = matrix2D;
		this.matrixTo3D = matrix3D;
		
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public static Tri3 make(Vec3 aIn, Vec3 bIn, Vec3 cIn) throws IllegalArgumentException
	{
		if(checkParallel(aIn, bIn, cIn))
			throw new IllegalArgumentException();
		
		Vec3 q = bIn.subtract(aIn);
		Vec3 r = cIn.subtract(aIn);
		Vec3 e0 = q.normalize().cross(r.normalize());
		Vec3 e1 = e0.normalize().cross(q.normalize());
		Vec3 e2 = e0.cross(e1);
		
		Matrix3 matrix2D = new Matrix3(
				new Double[] {e0.x, e0.y, e0.z, 0D}, 
				new Double[] {e1.x, e1.y, e1.z, 0D}, 
				new Double[] {e2.x, e2.y, e2.z, 0D}, 
				new Double[] {0D, 0D, 0D, 1D});
		
		Matrix3 matrix3D = new Matrix3(
				new Double[] {e0.x, e1.x, e2.x, 0D}, 
				new Double[] {e0.y, e1.y, e2.y, 0D}, 
				new Double[] {e0.z, e1.z, e2.z, 0D}, 
				new Double[] {0D, 0D, 0D, 1D});
		
		return new Tri3(matrix2D, matrix3D, aIn, bIn, cIn);
	}
	
	/**
	 * Returns true if the direction of any one line matches the direction of another.<br>
	 * This indicates that they are parallel and hence cannot describe a triangle.
	 */
	public static boolean checkParallel(Vec3 a, Vec3 b, Vec3 c)
	{
		Vec3 aToB = b.subtract(a).normalize();
		Vec3 aToC = c.subtract(a).normalize();
		Vec3 bToC = c.subtract(b).normalize();
		
		if(aToC == aToB || aToC == aToB.scale(-1D))
			return true;
		else if(aToC == bToC || aToC == bToC.scale(-1D))
			return true;
		else if(aToB == bToC || aToB == bToC.scale(-1D))
			return true;
		
		return false;
	}
	
	/** Returns a triangle whose circumscribed sphere contains all given points */
	public static Tri3 makeTriangleContaining(Vec3... points)
	{
		/**
		 * Find largest and smallest coordinates on all axises (points A and B)
		 * Calculate triangle circumcentre as midpoint between A and B
		 * Calculate triangle circumradius as distance between A and B + 100 to ensure containment
		 * Calculate triangle points as a right-angled triangle based on the direction from A to B
		 */
		
		Vec3 largest = null, smallest = null;
		for(int i=0; i<points.length; i++)
		{
			Vec3 point = points[i];
			
			if(largest == null)
				largest = point;
			else
			{
				Vec3 toPoint = point.subtract(largest);
				largest = largest.add(new Vec3(toPoint.x > 0 ? toPoint.x : 0, toPoint.y > 0 ? toPoint.y : 0, toPoint.z > 0 ? toPoint.z : 0));
			}
			
			if(smallest == null)
				smallest = point;
			else
			{
				Vec3 toPoint = point.subtract(smallest);
				smallest = smallest.add(new Vec3(toPoint.x < 0 ? toPoint.x : 0, toPoint.y < 0 ? toPoint.y : 0, toPoint.z < 0 ? toPoint.z : 0));
			}
		}
		
		Vec3 offset = largest.subtract(smallest);
		Vec3 center = smallest.add(offset.scale(0.5F));
		
		float turn = (float)Math.toRadians(120D);
		Vec3 dir = offset.scale(100F);
		Vec3 a = center.add(dir);
		Vec3 b = center.add(dir = dir.yRot(turn));
		Vec3 c = center.add(dir.yRot(turn));
		
		return make(a, b, c);
	}
	
	public List<Vec3> points() { return List.of(a, b, c); }
	
	public List<Line3> lines(){ return List.of(new Line3(a, b), new Line3(a, c), new Line3(b, c)); }
	
	public Vec3 circumcenter() { return this.matrixTo3D.to3D(this.tri.circumcenter).add(a); }
	
	public double circumradius() { return this.tri.circumRadius; }
	
	public boolean contains(Vec3 point) { return this.tri.contains(this.matrixTo2D.to2D(point.subtract(a))); }
	
	/** Applies the Bowyer-Watson algorithm to generate a Delaunay triangulation mesh from the given points */
	public static List<Tri3> generateDelaunayMesh(Vec3... points)
	{
		List<Tri3> mesh = Lists.newArrayList();
		Tri3 superTri = makeTriangleContaining(points);
		mesh.add(superTri);
		
		for(int i=0; i<points.length; i++)
		{
			Vec3 point = points[i];
			
			List<Tri3> trisNext = Lists.newArrayList();
			List<Tri3> badTris = Lists.newArrayList();
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
					Vec3 a = edge.getA();
					Vec3 b = edge.getB();
					if(!checkParallel(a, b, point))
						trisNext.add(make(a, b, point));
				});
			
			mesh = trisNext;
		}
		
		mesh.removeIf((tri) -> 
		{
			for(Vec3 point : tri.points())
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
	public static List<Line3> triMeshToUniqueLines(List<Tri3> triangles)
	{
		List<Line3> edges = Lists.newArrayList();
		triangles.forEach((tri) -> tri.lines().forEach((line) -> 
		{
			int lineInd = -1;
			for(int i=0; i<edges.size(); i++)
				if(edges.get(i).equals(line))
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