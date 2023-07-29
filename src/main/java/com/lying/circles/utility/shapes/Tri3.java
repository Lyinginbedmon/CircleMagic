package com.lying.circles.utility.shapes;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.CircleMagic;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Tri3
{
	private final Tri2 tri;
	private final Matrix3 matrixTo2D, matrixTo3D;
	
	private final Vec3 a, b, c;
	
	private Tri3(Matrix3 matrix2D, Matrix3 matrix3D, Vec3 a, Vec3 b, Vec3 c)
	{
		this.tri = new Tri2(matrix2D.to2D(a), matrix2D.to2D(b), matrix2D.to2D(c));
		this.matrixTo2D = matrix2D;
		this.matrixTo3D = matrix3D;
		
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	private Tri3(Matrix3 matrix2D, Matrix3 matrix3D, Vec2 a, Vec2 b, Vec2 c)
	{
		this.tri = new Tri2(Vec2.ZERO, b, c);
		this.matrixTo2D = matrix2D;
		this.matrixTo3D = matrix3D;
		
		this.a = matrix2D.to3D(Vec2.ZERO);
		this.b = matrix2D.to3D(b);
		this.c = matrix2D.to3D(c);
	}
	
	public static Tri3 make(Vec3 aIn, Vec3 bIn, Vec3 cIn) throws IllegalArgumentException
	{
		if(checkParallel(aIn, bIn, cIn))
			throw new IllegalArgumentException();
		
		Vec3 a3 = aIn;
		Vec3 b3 = bIn;
		Vec3 c3 = cIn;
		Vec3 aToB = b3.subtract(a3);
		Vec3 aToC = c3.subtract(a3);
		double yaw = Math.atan2(aToB.x, aToB.z);
		
		Matrix3 yawM = Matrix3.rotationYaw(-Math.asin(-aToC.y));
		a3 = yawM.applyTo(a3);
		b3 = yawM.applyTo(b3);
		c3 = yawM.applyTo(c3);
		
		double pitch = Math.asin(-aToC.y);
		Matrix3 pitM = Matrix3.rotationPitch(-Math.asin(-c3.subtract(a3).y));
		a3 = pitM.applyTo(a3);
		
		Matrix3 matrix2D = Matrix3.mul(yawM, pitM, Matrix3.translation(a3.scale(-1D)));
		Matrix3 matrix3D = Matrix3.mul(Matrix3.rotationYaw(yaw), Matrix3.rotationPitch(pitch), Matrix3.translation(a3));
		
		CircleMagic.LOG.info("Generating 3D<->2D matrices");
		CircleMagic.LOG.info("# Input values in 3D:");
		CircleMagic.LOG.info("# # A: "+(float)(int)(aIn.x * 10F)/10F+", "+(float)(int)(aIn.y * 10F)/10F+", "+(float)(int)(aIn.z * 10F)/10F);
		CircleMagic.LOG.info("# # B: "+(float)(int)(bIn.x * 10F)/10F+", "+(float)(int)(bIn.y * 10F)/10F+", "+(float)(int)(bIn.z * 10F)/10F);
		CircleMagic.LOG.info("# # C: "+(float)(int)(cIn.x * 10F)/10F+", "+(float)(int)(cIn.y * 10F)/10F+", "+(float)(int)(cIn.z * 10F)/10F);
		CircleMagic.LOG.info("# Adjusted values for 2D:");
		Vec2 a2D = matrix2D.to2D(aIn);
		Vec2 b2D = matrix2D.to2D(bIn);
		Vec2 c2D = matrix2D.to2D(cIn);
		CircleMagic.LOG.info("# # A: "+(float)(int)(a2D.x * 10F)/10F+", "+(float)(int)(a2D.y * 10F)/10F);
		CircleMagic.LOG.info("# # B: "+(float)(int)(b2D.x * 10F)/10F+", "+(float)(int)(b2D.y * 10F)/10F);
		CircleMagic.LOG.info("# # C: "+(float)(int)(c2D.x * 10F)/10F+", "+(float)(int)(c2D.y * 10F)/10F);
		CircleMagic.LOG.info("# Values reverted to 3D:");
		Vec3 a3D = matrix3D.to3D(a2D);
		Vec3 b3D = matrix3D.to3D(b2D);
		Vec3 c3D = matrix3D.to3D(c2D);
		CircleMagic.LOG.info("# # A: "+(float)(int)(a3D.x * 10F)/10F+", "+(float)(int)(a3D.y * 10F)/10F+", "+(float)(int)(a3D.z * 10F)/10F);
		CircleMagic.LOG.info("# # B: "+(float)(int)(b3D.x * 10F)/10F+", "+(float)(int)(b3D.y * 10F)/10F+", "+(float)(int)(b3D.z * 10F)/10F);
		CircleMagic.LOG.info("# # C: "+(float)(int)(c3D.x * 10F)/10F+", "+(float)(int)(c3D.y * 10F)/10F+", "+(float)(int)(c3D.z * 10F)/10F);
		
		return new Tri3(matrix2D, matrix3D, matrix2D.to2D(aIn), matrix2D.to2D(bIn), matrix2D.to2D(cIn));
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
	
	public Vec3 circumcenter() { return this.matrixTo3D.to3D(this.tri.circumcenter); }
	
	public double circumradius() { return this.tri.circumRadius; }
	
	public boolean contains(Vec3 point) { return this.tri.contains(this.matrixTo2D.to2D(point)); }
	
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
	
	/** Describes a matrix for 3 dimensions */
	private static class Matrix3
	{
		private Double[][] values = new Double[4][4];
		
		public Matrix3()
		{
			for(int i=0; i<4; i++)
				for(int j=0; j<4; j++)
					values[i][j] = (i == j ? 1D : 0D);
		}
		
		/** Prints the contents of this matrix into the log */
		public void print()
		{
			CircleMagic.LOG.info("Matrix contents:");
			for(int i=0; i<4; i++)
			{
				String row = "";
				for(double val : values[i])
				{
					if(row.length() == 0)
						row = "[";
					else
						row += ", ";
					
					row += String.valueOf((double)((int)(val * 10)) / 10D);
				}
				CircleMagic.LOG.info(row + "]");
			}
		}
		
		public double get(int row, int col) { return values[row][col]; }
		
		public void set(int row, int col, double val) { values[row][col] = val; }
		
		/** Multiplies a set of matrices together in sequence */
		public static Matrix3 mul(Matrix3... matrices)
		{
			Matrix3 matrix = matrices[0];
			for(int i=1; i<matrices.length; i++)
				matrix = mul(matrix, matrices[i]);
			return matrix;
		}
		
		/** Multiplies two matrices together */
		public static Matrix3 mul(Matrix3 matrixA, Matrix3 matrixB)
		{
			Matrix3 matrix = new Matrix3();
			for(int row=0; row<4; row++)
				for(int col=0; col<4; col++)
				{
					double total = 0D;
					for(int i=0; i<4; i++)
						total += matrixA.get(row, i) * matrixB.get(i, col);
					
					matrix.set(row, col, total);
				}
			
			return matrix;
		}
		
		/** Returns a translation matrix of the given vector */
		public static Matrix3 translation(Vec3 vec)
		{
			Matrix3 matrix = new Matrix3();
			
			matrix.set(0, 3, vec.x);
			matrix.set(1, 3, vec.y);
			matrix.set(2, 3, vec.z);
			
			return matrix;
		}
		
		/** Returns a rotation matrix of the given radians on the P axis */
		public static Matrix3 rotationPitch(double pitch)
		{
			Matrix3 matrix = new Matrix3();
			
			if(pitch != 0D)
			{
				double cos = Math.cos(pitch);
				double sin = Math.sin(pitch);
				matrix.set(1, 1, cos);
				matrix.set(1, 2, sin);
				matrix.set(2, 1, -sin);
				matrix.set(2, 2, cos);
			}
			
			return matrix;
		}
		
		/** Returns a rotation matrix of the given radians on the Y axis */
		public static Matrix3 rotationYaw(double yaw)
		{
			Matrix3 matrix = new Matrix3();
			
			if(yaw != 0D)
			{
				double cos = Math.cos(yaw);
				double sin = Math.sin(yaw);
				matrix.set(0, 0, cos);
				matrix.set(0, 2, -sin);
				matrix.set(2, 0, sin);
				matrix.set(2, 2, cos);
			}
			
			return matrix;
		}
		
		/** Applies this matrix to the given 3D vector */
		public Vec3 applyTo(Vec3 vec)
		{
			Double[] values = new Double[] { vec.x, vec.y, vec.z, 1D };
			Double[] results = new Double[4];
			
			for(int row=0; row<4; row++)
			{
				double total = 0D;
				for(int col=0; col<4; col++)
					total += values[col] * get(row, col);
				
				results[row] = total;
			}
			
			return new Vec3(results[0], results[1], results[2]);
		}
		
		/** Applies this matrix to the given vector and returns the 2D equivalent */
		public Vec2 to2D(Vec3 vec)
		{
			Vec3 applied = applyTo(vec);
			return new Vec2((float)applied.x, (float)applied.z);
		}
		
		/** Applies this matrix to the given vector and returns the 3D equivalent */
		public Vec3 to3D(Vec2 vec)
		{
			return applyTo(new Vec3(vec.x, 0, vec.y));
		}
	}
}