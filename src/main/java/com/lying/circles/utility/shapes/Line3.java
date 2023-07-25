package com.lying.circles.utility.shapes;

import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

public class Line3 extends Tuple<Vec3, Vec3>
{
	public Line3(Vec3 a, Vec3 b)
	{
		super(a, b);
	}
	
	public String toString() { return "Line["+vecString(getA())+", "+vecString(getB())+"]"; }
	
	public boolean equals(Line3 line2)
	{
		return
				(line2.getA().distanceTo(getA()) == 0D && line2.getB().distanceTo(getB()) == 0D) ||
				(line2.getB().distanceTo(getA()) == 0D && line2.getA().distanceTo(getB()) == 0D);
	}
	
	private String vecString(Vec3 p)
	{
		return "(" + (double)(int)(p.x * 10)/10D + ", " + (double)(int)(p.y * 10)/10D + ", " + (double)(int)(p.z * 10)/10D + ")";
	}
	
	public double distanceTo(Vec3 p)
	{
		Vec3 b = getB();
		
		// x = a + tn
		Vec3 a = getA();
		Vec3 n = b.subtract(a).normalize();
		
		// Vector from a to p
		Vec3 pa = p.subtract(a);
		
		// Projected length to line
		double len = pa.dot(n);
		
		// Closest point on line to p
		Vec3 point = a.add(n.scale(len));
		
		// Distance check to ensure p is actually between a and b
		if(point.distanceTo(a) > b.distanceTo(a))
			return Double.MAX_VALUE;
		
		return p.distanceTo(point);
	}
}