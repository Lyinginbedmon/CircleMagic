package com.lying.circles.utility;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.reference.Reference;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LeylineManager extends SavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_leyline_manager";
	public static final double LEYLINE_RANGE = 8D;
	
	private List<BlockPos> leyPoints = Lists.newArrayList();
	private List<Line> leyLines = Lists.newArrayList();
	
	@Nullable
	private Level world;
	
	public LeylineManager() { this(null); }
	public LeylineManager(@Nullable Level worldIn)
	{
		this.world = worldIn;
	}
	
	@Nullable
	public static LeylineManager instance(Level worldIn)
	{
		if(worldIn.isClientSide())
			return null;
		
		LeylineManager manager = ((ServerLevel)worldIn).getDataStorage().computeIfAbsent(LeylineManager::fromNBT, LeylineManager::new, DATA_NAME);
		manager.world = worldIn;
		return manager;
	}
	
	public static LeylineManager fromNBT(CompoundTag tag)
	{
		LeylineManager manager = new LeylineManager();
		manager.read(tag);
		return manager;
	}
	
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag points = new ListTag();
		this.leyPoints.forEach((point) -> points.add(NbtUtils.writeBlockPos(point)));
		nbt.put("Points", points);
		return nbt;
	}
	
	public void read(CompoundTag nbt)
	{
		this.leyPoints.clear();
		ListTag points = nbt.getList("Points", Tag.TAG_COMPOUND);
		for(int i=0; i<points.size(); i++)
			this.leyPoints.add(NbtUtils.readBlockPos(points.getCompound(i)));
		leyLines = calculateLeyLinesSalesman();
	}
	
	public void addLeyPoint(BlockPos point)
	{
		if(!leyPoints.contains(point))
		{
			leyPoints.add(point);
			leyLines = calculateLeyLinesSalesman();
			setDirty();
		}
	}
	
	public void removeLeyPoint(BlockPos point)
	{
		if(leyPoints.contains(point))
		{
			leyPoints.remove(point);
			leyLines = calculateLeyLinesSalesman();
			setDirty();
		}
	}
	
	public boolean isEmpty() { return this.leyPoints.isEmpty(); }
	
	public int size() { return this.leyPoints.size(); }
	
	public boolean isOnLeyLine(LivingEntity entity)
	{
		if(isEmpty())
			return false;
		
		// If entity is within 8 blocks of a ley point
		for(Vec3 point : pointsToVec())
			if(point.distanceTo(entity.position()) <= (LEYLINE_RANGE * LEYLINE_RANGE))
				return true;
		
		// If entity is within 8 blocks of a ley line
		for(Line leyline : leyLines)
			if(leyline.distanceTo(entity.position()) < LEYLINE_RANGE)
				return true;
		
		return false;
	}
	
	public List<Vec3> pointsToVec()
	{
		List<Vec3> totalPoints = Lists.newArrayList();
		this.leyPoints.forEach((pos) -> totalPoints.add(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)));
		return totalPoints;
	}
	
	/** Travelling Salesman between all known ley points */
	public List<Line> calculateLeyLinesSalesman()
	{
		List<Vec3> visited = Lists.newArrayList();
		List<Vec3> remaining = Lists.newArrayList();
		remaining.addAll(pointsToVec());
		
		Vec3 currentPos = pointsToVec().get(0);
		remaining.remove(currentPos);
		visited.add(currentPos);
		
		while(!remaining.isEmpty())
		{
			Vec3 closestUnvisited = null;
			double closestDist = Double.MAX_VALUE;
			for(Vec3 option : remaining)
			{
				double distTo = currentPos.distanceToSqr(option);
				if(distTo > 0 && distTo < closestDist)
				{
					closestDist = distTo;
					closestUnvisited = option;
				}
			}
			
			if(closestUnvisited != null)
			{
				visited.add(currentPos);
				remaining.remove(closestUnvisited);
				currentPos = closestUnvisited;
			}
		}
		visited.add(currentPos);
		
		List<Line> lines = Lists.newArrayList();
		for(int i=0; i<visited.size(); i++)
			lines.add(new Line(visited.get(0), visited.get((i+1)%visited.size())));
		return lines;
	}
	
	/** TODO Implement Delaunay triangulation to map leyline network */
	public List<Line> calculateLeylinesDelaunay()
	{
		List<Vec3> totalPoints = pointsToVec();
		System.out.println("Calculating Delaunay triangulation of "+size()+" points");
		switch(size())
		{
			case 0:
			case 1:
				return Lists.newArrayList();
			case 2:
				return List.of(new Line(totalPoints.get(0), totalPoints.get(1)));
			default:
				break;
		}
		
		if(totalPoints.size() == 3)
			return trianglesToUniqueLines(List.of(new Triangle(totalPoints.get(0), totalPoints.get(1), totalPoints.get(2))));
		
		// List of triangles to test
		List<Triangle> unsetTris = Lists.newArrayList();
		unsetTris.add(Triangle.SUPER);
		
		// List of triangles with no conflicts
		List<Triangle> fixedTris = Lists.newArrayList();
		
		int tally = 0;
		while(!unsetTris.isEmpty())
		{
			Triangle tri = unsetTris.get(0);
			
			// Set of points contained within this triangle
			List<Vec3> intersections = Lists.newArrayList();
			totalPoints.forEach((pos) -> 
			{
				if(tri.circleContains(pos))
					intersections.add(pos);
			});
			System.out.println("# Tri "+String.valueOf(tally++)+": "+intersections.size()+" contained points");
			
			// If triangle contains no extra points, fix it, else add all triangles connecting its intersections
			if(intersections.isEmpty())
				fixedTris.add(tri);
			else
				intersections.forEach((point) -> unsetTris.addAll(tri.connectTo(point)));
			
			unsetTris.remove(0);
			System.out.println("Triangles remaining: "+unsetTris.size());
		}
		
		// Remove all triangles with at least one point outside of our point set
		fixedTris.removeIf((tri) -> !(totalPoints.containsAll(tri.points())));
		System.out.println("Final count: "+fixedTris.size()+" triangles");
		return trianglesToUniqueLines(fixedTris);
	}
	
	public List<Line> trianglesToUniqueLines(List<Triangle> triangles)
	{
		List<Line> lines = Lists.newArrayList();
		
		triangles.forEach((tri) -> tri.lines().forEach((line) -> 
			{
				if(!lines.contains(line))
					lines.add(line);
			}));
		
		return lines;
	}
	
	public void tick()
	{
		if(world == null || world.isClientSide() || size() < 2)
			return;
		
		ServerLevel server = (ServerLevel)world;
		for(Line leyline : this.leyLines)
		{
			Vec3 start = leyline.getFirst();
			Vec3 end = leyline.getSecond();
			
			Vec3 offset = end.subtract(start);
			Vec3 normal = offset.normalize();
			for(int j=0; j<(int)offset.length(); j++)
			{
				Vec3 position = start.add(normal.scale(j));
				for(ServerPlayer player : server.players())
					server.sendParticles(player, ParticleTypes.WITCH, false, position.x, position.y, position.z, 1, normal.x * 0.5D, normal.y * 0.5D, normal.z * 0.5D, 0D);
			}
		}
	}
	
	private static class Triangle
	{
		public static Triangle SUPER;
		
		private final Vec3 a, b, c;
		
		private final Vec3 circumcenter;
		private final double circumcircleRadius;
		
		public Triangle(Vec3 aIn, Vec3 bIn, Vec3 cIn) throws IllegalArgumentException
		{
			a = aIn;
			b = bIn;
			c = cIn;
			
			// New methodology, incomplete
//			Pair<Vec3,Double> circumCalc = calculateCircumcircle(a, b, c);
//			circumcenter = circumCalc.getFirst();
//			circumcircleRadius = circumCalc.getSecond();
			
			// Old methodology, broken
			Vec3 ac = c.subtract(a);
			Vec3 ab = b.subtract(a);
			Vec3 abXac = ab.cross(ac);
			Vec3 toCircumcenter = (abXac.cross(ab).scale(ac.lengthSqr()).add(ac.cross(abXac).scale(ab.lengthSqr()))).scale(1D / (2F*abXac.lengthSqr()));
			circumcircleRadius = toCircumcenter.length();
			circumcenter = a.add(toCircumcenter);
		}
		
		public String toString() { return "Triangle[" + a.toString()+", "+b.toString()+", "+c.toString()+"]"; }
		
		@SuppressWarnings("unused")
		public boolean equals(Triangle tri2)
		{
			return tri2.circumcenter == circumcenter && tri2.circumcircleRadius == circumcircleRadius;
		}
		
		/** Returns true if the circumcircle of this triangle contains the given point */
		public boolean circleContains(Vec3 pos)
		{
			if(pos.distanceTo(a) == 0)
				return false;
			else if(pos.distanceTo(b) == 0)
				return false;
			else if(pos.distanceTo(c) == 0)
				return false;
			else
				return circumcenter.distanceTo(pos) <= circumcircleRadius;
		}
		
		/** Devolves this triangle into a set of triangles connected to the given point */
		public Set<Triangle> connectTo(Vec3 pos)
		{
			return Set.of(new Triangle(a, b, pos), new Triangle(a, pos, c), new Triangle(pos, b, c));
		}
		
		public Set<Vec3> points() { return Set.of(a, b, c); }
		
		public Set<Line> lines(){ return Set.of(new Line(a, b), new Line(a, c), new Line(b, c)); }
		
		// FIXME Correct formula to determine triangle circumcircle
		private static Pair<Vec3,Double> calculateCircumcircle(Vec3 a, Vec3 b, Vec3 c)
		{
			// Convert all points to a local coordinate system relative to point A (which therefore becomes [0, 0, 0])
			Vec3 localB = b.subtract(a);
			Vec3 localC = c.subtract(a);
			
			// Rotate all points along the Y axis equal to yaw between A and B x -1
			float yawFrom0 = (float)Math.atan2(localB.x, localB.z);
			localB = localB.yRot(-yawFrom0);
			localC = localC.yRot(-yawFrom0);
			
			// Rotate all points along the X axis equal to pitch between A and C x -1
			// This leaves all points on a flat plane with point A always at [0, 0, 0]
			float pitchFrom0 = (float)Math.asin(-localC.y);
			localB = localB.xRot(-pitchFrom0);
			localC = localC.xRot(-pitchFrom0);
			
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
			
			// Calculate circumcenter position
			// Circumcenter = intercept point of lines perpendicular to line AB and AC from the respective midpoints
			Vec2 abMid = b2D.scale(0.5F);
			Vec2 bNormal = new Vec2(-b2D.y, b2D.x).normalized();
			Vec2 acMid = c2D.scale(0.5F);
			Vec2 cNormal = new Vec2(-c2D.y, c2D.x).normalized();
			
			Vec2 center2D = Vec2.ZERO;
			
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
			
			return Pair.of(center3D, a.distanceTo(center3D));
		}
		
		static
		{
			Vec3 superA = new Vec3(Double.MAX_VALUE, 64, 0);
			Vec3 superB = new Vec3(-superA.z, 64, superA.x);
			Vec3 superC = new Vec3(-superB.z, 64, superB.x);
			SUPER = new Triangle(superA, superB, superC);
			System.out.println("Super triangle center: "+SUPER.circumcenter.toString());
		}
	}
	
	private static class Line extends Pair<Vec3, Vec3>
	{
		// Components of the slope intercept equation of this line
		// y = mx + b OR x = a
		private final double m, b;
		private final boolean isVertical;
		
		public Line(Vec3 a, Vec3 b)
		{
			super(a, b);
			
			double run = getSecond().x - getFirst().x;
			double rise = getSecond().y - getFirst().y;
			
			isVertical = run == 0;
			if(isVertical)
			{
				this.m = getFirst().x;
				this.b = 0;
			}
			else
			{
				this.m = run == 0 ? 0 : rise / run;
				this.b = getFirst().y - (getFirst().x * m);
			}
		}
		
		public String toString() { return "Line["+getFirst().toString()+", "+getSecond().toString()+"]"; }
		
		@SuppressWarnings("unused")
		public boolean equals(Line line2)
		{
			return
					(line2.getFirst().distanceTo(getFirst()) == 0D && line2.getSecond().distanceTo(getSecond()) == 0D) ||
					(line2.getSecond().distanceTo(getFirst()) == 0D && line2.getFirst().distanceTo(getSecond()) == 0D);
		}
		
		public double distanceTo(Vec3 point)
		{
			// TODO Calculate distance of point to line
			return Double.MAX_VALUE;
		}
	}
}
