package com.lying.circles.utility.shapes;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

// FIXME Properly calculate conversion matrix for 3D vectors to 2D plane
public class Tri3
	{
		private final Vec3 a, b, c;
		
		private final Vec3 circumcenter;
		private final double circumcircleRadius;
		
		public Tri3(Vec3 aIn, Vec3 bIn, Vec3 cIn) throws IllegalArgumentException
		{
			a = aIn;
			b = bIn;
			c = cIn;
			
			// Check for parallel lines
			if(checkParallel(a, b, c))
				throw new IllegalArgumentException();
			
			// New methodology, incomplete
			Pair<Vec3,Double> circumCalc = calculateCircumcircle(a, b, c);
			if(circumCalc == null)
				throw new IllegalArgumentException();
			
			circumcenter = circumCalc.getFirst();
			circumcircleRadius = circumCalc.getSecond();
		}
		
		public static Tri3 make(Vec3 aIn, Vec3 bIn, Vec3 cIn)
		{
			
			return null;
		}
		
		/**
		 * Returns true if the direction one line matches the direction of another.<br>
		 * This indicates that they are parallel and hence cannot describe a triangle.
		 */
		public static boolean checkParallel(Vec3 a, Vec3 b, Vec3 c)
		{
			Vec3 lineAB = b.subtract(a).normalize();
			Vec3 lineAC = c.subtract(a).normalize();
			Vec3 lineCB = c.subtract(b).normalize();
			Vec3[] lines = new Vec3[] {lineAB, lineAC, lineCB};
			
			for(int i=0; i<lines.length; i++)
			{
				Vec3 vecA = lines[i];
				for(int j=0; j<lines.length; j++)
				{
					if(j==i)
						continue;
					
					Vec3 vecB = lines[j];
					if(vecB.equals(vecA) || vecB.scale(-1D).equals(vecA))
						return true;
				}
			}
			return false;
		}
		
		public String toString() { return "Tri3[" + a.toString()+", "+b.toString()+", "+c.toString()+"]"; }
		
		public boolean equals(Tri3 tri2)
		{
			for(Vec3 point : points())
				if(!tri2.points().contains(point))
					return false;
			return true;
		}
		
		/** Returns true if the circumcircle of this triangle contains the given point */
		public boolean circleContains(Vec3 pos)
		{
			return !points().contains(pos) && circumcenter.distanceTo(pos) < circumcircleRadius;
		}
		
		/** Devolves this triangle into a set of triangles connected to the given point */
		public Collection<Tri3> connectTo(Vec3 pos)
		{
			List<Tri3> tris = Lists.newArrayList();
			
			if(!checkParallel(a, b, pos))
				tris.add(new Tri3(a, b, pos));
			
			if(!checkParallel(a, pos, c))
				tris.add(new Tri3(a, pos, c));
			
			if(!checkParallel(pos, b, c))
				tris.add(new Tri3(pos, b, c));
			
			return tris;
		}
		
		public Set<Vec3> points() { return Set.of(a, b, c); }
		
		public Set<Line3> lines(){ return Set.of(new Line3(a, b), new Line3(a, c), new Line3(b, c)); }
		
		@Nullable
		private static Pair<Vec3,Double> calculateCircumcircle(Vec3 a, Vec3 b, Vec3 c)
		{
//			System.out.println("Calculating circumcircle between points "+a.toString()+", "+b.toString()+" and "+c.toString());
			
//			System.out.println("# Converting points to 2D where A = [0, 0]");
			// Convert all points to a local coordinate system relative to point A (which therefore becomes [0, 0, 0])
			Vec3 localB = b.subtract(a);
			Vec3 localC = c.subtract(a);
			
//			System.out.println("# Local coordinates: "+localB.toString()+" and "+localC.toString());
			
			// Rotate all points along the Y axis equal to yaw between A and B x -1
			float yawFrom0 = (float)Math.atan2(localB.x, localB.z);
			localB = localB.yRot(-yawFrom0);
			localC = localC.yRot(-yawFrom0);
			
//			System.out.println("# After yaw: "+localB.toString()+" and "+localC.toString());
			
			// Rotate all points along the X axis equal to pitch between A and C x -1
			// This leaves all points on a flat plane with point A always at [0, 0, 0]
			float pitchFrom0 = (float)Math.asin(-localC.y);
			localB = localB.xRot(-pitchFrom0);
			localC = localC.xRot(-pitchFrom0);
			
//			System.out.println("# After pitch: "+localB.toString()+" and "+localC.toString());
			
			// Convert all points to 2D vectors
			Vec2 b2D, c2D = Vec2.ZERO;
			int convertMode = -1;
			if(localB.x == 0D && localC.x == 0D)
			{
				b2D = new Vec2((float)localB.y, (float)localB.z);
				c2D = new Vec2((float)localC.y, (float)localC.z);
				convertMode = 0;
			}
			else if(localB.y == 0D && localC.y == 0D)
			{
				b2D = new Vec2((float)localB.x, (float)localB.z);
				c2D = new Vec2((float)localC.x, (float)localC.z);
				convertMode = 1;
			}
			else if(localB.z == 0D && localC.z == 0D)
			{
				b2D = new Vec2((float)localB.x, (float)localB.y);
				c2D = new Vec2((float)localC.x, (float)localC.y);
				convertMode = 2;
			}
			else
				throw new IllegalArgumentException();
			
//			System.out.println("# Points converted to [0, 0], ["+b2D.x+", "+b2D.y+"] and ["+c2D.x+", "+c2D.y+"]");
			
			// Calculate circumcenter position
			// Circumcenter = intercept point of lines perpendicular to line AB and AC from the respective midpoints
			Vec2 abMid = b2D.scale(0.5F);
			Vec2 bNormal = new Vec2(-b2D.y, b2D.x).normalized();
			Vec2 acMid = c2D.scale(0.5F);
			Vec2 cNormal = new Vec2(-c2D.y, c2D.x).normalized();
			
			Line2 ab = new Line2(abMid.add(bNormal.scale(Float.MIN_VALUE)), abMid.add(bNormal.scale(Float.MAX_VALUE)));
			Line2 ac = new Line2(acMid.add(cNormal.scale(Float.MIN_VALUE)), acMid.add(cNormal.scale(Float.MAX_VALUE)));
			Vec2 center2D = ab.intercept(ac, true);
			if(center2D == null)
			{
//				System.out.println("# # No intercept point between "+ab.toShortString()+" and "+ac.toShortString());
				return null;
			}
			
			// Rotate circumcenter by pitch and yaw, then add point A, to translate to 3D space
			Vec3 center3D = null;
			switch(convertMode)
			{
				case 0:
					center3D = new Vec3(0D, center2D.x, center2D.y);
					break;
				case 1:
					center3D = new Vec3(center2D.x, 0D, center2D.y);
					break;
				case 2:
					center3D = new Vec3(center2D.x, center2D.y, 0D);
					break;
			}
			center3D = center3D.xRot(pitchFrom0).yRot(yawFrom0).add(a);
//			System.out.println("# Circumcenter: "+center3D.toString());
//			System.out.println("# Radius: "+a.distanceTo(center3D));
			
			return Pair.of(center3D, a.distanceTo(center3D));
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
					
					if(toPoint.x > 0)
						largest = largest.add(toPoint.x, 0, 0);
					
					if(toPoint.y > 0)
						largest = largest.add(0, toPoint.y, 0);
					
					if(toPoint.z > 0)
						largest = largest.add(0, 0, toPoint.z);
				}
				
				if(smallest == null)
					smallest = point;
				else
				{
					Vec3 toPoint = point.subtract(smallest);
					
					if(toPoint.x < 0)
						smallest = smallest.add(toPoint.x, 0, 0);
					
					if(toPoint.y < 0)
						smallest = smallest.add(0, toPoint.y, 0);
					
					if(toPoint.z < 0)
						smallest = smallest.add(0, 0, toPoint.z);
				}
			}
			
			Vec3 offset = largest.subtract(smallest);
			Vec3 center = smallest.add(offset.scale(0.5D));
			double radius = (offset.length() * 0.5D) + 100D;
			
			Vec3 a = center.add(radius, 0, 0);
			Vec3 b = center.add(-0, 0, radius);
			Vec3 c = center.add(-radius, 0, 0);
			
			return new Tri3(a, b, c);
		}
	}