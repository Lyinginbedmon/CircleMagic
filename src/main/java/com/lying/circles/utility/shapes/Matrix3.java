package com.lying.circles.utility.shapes;

import com.lying.circles.CircleMagic;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/** Describes a matrix for 3 dimensions */
public class Matrix3
{
	private Double[][] values = new Double[4][4];
	
	public Matrix3()
	{
		for(int i=0; i<4; i++)
			for(int j=0; j<4; j++)
				values[i][j] = (i == j ? 1D : 0D);
	}
	
	public Matrix3(Double[] row0, Double[] row1, Double[] row2, Double[] row3)
	{
		this();
		for(int i=0; i<3; i++)
		{
			set(0, i, row0[i]);
			set(1, i, row1[i]);
			set(2, i, row2[i]);
			set(3, i, row3[i]);
		}
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
		return new Vec2((float)applied.y, (float)applied.z);
	}
	
	/** Applies this matrix to the given vector and returns the 2D equivalent */
	public Vec3 to3D(Vec2 vec)
	{
		return applyTo(new Vec3(0, (double)vec.x, (double)vec.y));
	}
}