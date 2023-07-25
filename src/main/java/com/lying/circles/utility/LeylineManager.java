package com.lying.circles.utility;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.reference.Reference;
import com.lying.circles.utility.shapes.Line2;
import com.lying.circles.utility.shapes.Line3;
import com.lying.circles.utility.shapes.Tri2;

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
	private List<Line3> leyLines = Lists.newArrayList();
	
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
		leyLines = calculateLeylinesDelaunay();
	}
	
	public void addLeyPoint(BlockPos point)
	{
		if(!hasPos(point))
		{
			leyPoints.add(point);
			leyLines = calculateLeylinesDelaunay();
			setDirty();
		}
	}
	
	public void removeLeyPoint(BlockPos point)
	{
		if(hasPos(point))
		{
			leyPoints.remove(point);
			leyLines = calculateLeylinesDelaunay();
			setDirty();
		}
	}
	
	public boolean isEmpty() { return this.leyPoints.isEmpty(); }
	
	public int size() { return this.leyPoints.size(); }
	
	/** Returns true if this manager contains the given ley point */
	public boolean hasPos(BlockPos pos)
	{
		for(BlockPos point : leyPoints)
			if(point.distSqr(pos) == 0D)
				return true;
		return false;
	}
	
	public boolean isOnLeyLine(LivingEntity entity)
	{
		if(isEmpty() || this.leyLines.isEmpty())
			return false;
		
		// If entity is within 8 blocks of a ley line
		for(Line3 leyline : leyLines)
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
	public List<Line3> calculateLeyLinesSalesman()
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
		
		List<Line3> lines = Lists.newArrayList();
		for(int i=0; i<visited.size(); i++)
			lines.add(new Line3(visited.get(i), visited.get((i+1)%visited.size())));
		return lines;
	}
	
	/** Implementation of the Bowyer-Watson algorithm for Delaunay triangulation */
	public List<Line3> calculateLeylinesDelaunay()
	{
		List<Vec3> totalPoints = pointsToVec();
		System.out.println("Calculating Delaunay triangulation of "+size()+" points");
		switch(totalPoints.size())
		{
			case 0:
			case 1:
				System.out.println(" - Insufficient points for a leyline");
				return Lists.newArrayList();
			case 2:
				System.out.println(" - Single leyline");
				return List.of(new Line3(totalPoints.get(0), totalPoints.get(1)));
			case 3:
				System.out.println(" - Three leylines only");
				return List.of(new Line3(totalPoints.get(0), totalPoints.get(1)), new Line3(totalPoints.get(0), totalPoints.get(2)), new Line3(totalPoints.get(1), totalPoints.get(2)));
			default:
				System.out.println(" - Full triangulation needed");
				break;
		}
		
		// Triangle mesh
		List<Tri2> mesh = Lists.newArrayList();
		
		// Points to add
		List<Vec2> points = Lists.newArrayList();
		double yLevel = totalPoints.get(0).y;
		totalPoints.forEach((point) -> points.add(new Vec2((float)point.x, (float)point.z)));
		
		// Initialise mesh with super triangle containing all points
		mesh.add(Tri2.makeTriangleContaining(points.toArray(new Vec2[0])));
		
		// Incrementally add points to the mesh
		for(Vec2 point : points)
			mesh = addPointToMesh(point, mesh);
		
		// Remove all triangles remaining from the super triangle
		mesh.removeIf((tri) -> !points.containsAll(tri.points()));
		
		System.out.println(" - Triangles generated: "+mesh.size());
		List<Line3> lines3 = Lists.newArrayList();
		mesh.forEach((tri) -> tri.lines().forEach((line) -> 
		{
			Vec2 a = line.getA();
			Vec2 b = line.getB();
			Line3 line3 = new Line3(new Vec3(a.x, yLevel, a.y), new Vec3(b.x, yLevel, b.y));
			if(!lines3.contains(line3))
				lines3.add(line3);
		}));
		System.out.println(" - Leylines generated: "+lines3.size());
		return lines3;
	}
	
	private static List<Tri2> addPointToMesh(Vec2 point, List<Tri2> mesh)
	{
		System.out.println(" # "+mesh.size()+" tris to test");
		
		// The output mesh
		List<Tri2> trisNext = Lists.newArrayList();
		
		// Triangles rendered invalid by the addition of this point
		List<Tri2> badTris = Lists.newArrayList();
		
		/**
		 * For all triangles in the mesh:
		 * * If they contain the point in their circumcircle, add them to badTris
		 * * Else add them to trisNext
		 */
		mesh.forEach((tri) -> 
		{
			if(tri.contains(point))
				badTris.add(tri);
			else
				trisNext.add(tri);
		});
		
		// Collect all non-duplicate lines from all bad triangles
		// This represents the polygon of affected space
		List<Line2> polygon = Tri2.triMeshToUniqueLines(badTris);
		System.out.println(" # # "+polygon.size()+"-sided polygon generated from "+badTris.size()+" tris");
		
		// Create new triangles from all polygon edges and the point
		polygon.forEach((edge) -> 
		{
			Vec2 a = edge.getA();
			Vec2 b = edge.getB();
			if(!Tri2.checkParallel(a, b, point))
				trisNext.add(new Tri2(a, b, point));
		});
		
		return trisNext;
	}
	
	public void tick()
	{
		if(world == null || world.isClientSide() || size() < 2)
			return;
		
		ServerLevel server = (ServerLevel)world;
		for(Line3 leyline : this.leyLines)
		{
			Vec3 start = leyline.getA();
			Vec3 end = leyline.getB();
			
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
}
