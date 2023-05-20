package com.lying.misc19.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.misc19.init.M19Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.registries.RegistryObject;

public class TilledSand extends SandBlock
{
	public static final EnumProperty<Shape> SHAPE_PROPERTY = EnumProperty.create("shape", Shape.class);
	private final BlockState revertState;
	
	private static final Map<Block, RegistryObject<Block>> TILLED_MAP = new HashMap<>();
	
	public TilledSand(int colorIn, Properties properties, BlockState revertIn)
	{
		super(colorIn, properties);
		this.revertState = revertIn;
		this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE_PROPERTY, Shape.NORTH_SOUTH));
	}
	
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand)
	{
		super.tick(state, world, pos, rand);
		if(world.getBlockState(pos.above()).getMaterial().isSolid())
			world.setBlockAndUpdate(pos, this.revertState);
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55408_) { p_55408_.add(SHAPE_PROPERTY); }
	
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = super.defaultBlockState();
		Direction dir = context.getHorizontalDirection();
		boolean eastWest = dir == Direction.EAST || dir == Direction.WEST;
		return state.setValue(SHAPE_PROPERTY, eastWest ? Shape.EAST_WEST : Shape.NORTH_SOUTH);
	}
	
	public void onPlace(BlockState stateOld, Level world, BlockPos pos, BlockState stateNew, boolean bool)
	{
		if(!stateNew.is(stateOld.getBlock()))
			updateState(stateOld, world, pos, bool);
	}
	
	public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity)
	{
		if(!entity.isSteppingCarefully())
			trample(world, pos);
	}
	
	public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float par4Float)
	{
		if(!world.isClientSide())
			trample(world, pos);
	}
	
	private void trample(Level world, BlockPos pos)
	{
		if(!world.isClientSide() && world.random.nextInt(4) == 0)
			world.setBlockAndUpdate(pos, this.revertState);
	}
	
	protected BlockState updateState(BlockState state, Level world, BlockPos pos, boolean bool)
	{
		return updateDir(world, pos, state, true);
	}
	
	protected BlockState updateDir(Level world, BlockPos pos, BlockState state, boolean bool)
	{
		if(world.isClientSide())
			return state;
		else
		{
			Shape shape = state.getValue(SHAPE_PROPERTY);
			return (new TilledState(world, pos, state)).place(world.hasNeighborSignal(pos), bool, shape).getState();
		}
	}
	
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighbor, boolean bool)
	{
		if(!world.isClientSide() && world.getBlockState(pos).is(this))
			updateState(state, world, pos, block);
	}
	
	protected void updateState(BlockState state, Level world, BlockPos pos, Block block)
	{
		if(block.defaultBlockState().isSignalSource() && (new TilledState(world, pos, state)).countPotentialConnections() == 3)
			updateDir(world, pos, state, false);
	}
	
	public Shape getDirection(BlockState state, BlockGetter world, BlockPos pos) { return state.getValue(SHAPE_PROPERTY); }
	
	public static boolean isTilledSand(Level world, BlockPos pos) { return isTilledSand(world.getBlockState(pos)); }
	
	public static boolean isTilledSand(BlockState state)
	{
		return state.getBlock() instanceof TilledSand;
	}
	
	public boolean isValidShape(Shape shape) { return true; }
	
	@Nullable
	public static RegistryObject<Block> getTilledVersion(Block block)
	{
		return TILLED_MAP.getOrDefault(block, null);
	}
	
	static
	{
		TILLED_MAP.put(Blocks.SAND, M19Blocks.TILLED_SAND);
		TILLED_MAP.put(Blocks.RED_SAND, M19Blocks.TILLED_RED_SAND);
	}
	
	public static enum Shape implements StringRepresentable
	{
		NORTH_SOUTH("north_south"),
		EAST_WEST("east_west"),
		SOUTH_EAST("south_east"),
		SOUTH_WEST("south_west"),
		NORTH_WEST("north_west"),
		NORTH_EAST("north_east");
		
		private final String name;
		
		private Shape(String p_61743_) { this.name = p_61743_; }
		
		public String toString() { return this.name; }
		
		public String getSerializedName() { return this.name; }
	}
	
	public static class TilledState
	{
		private final Level level;
		private final BlockPos pos;
		private final TilledSand block;
		private BlockState state;
		private final List<BlockPos> connections = Lists.newArrayList();
		
		public TilledState(Level p_55421_, BlockPos p_55422_, BlockState p_55423_) {
			this.level = p_55421_;
			this.pos = p_55422_;
			this.state = p_55423_;
			this.block = (TilledSand)p_55423_.getBlock();
			Shape shape = this.block.getDirection(state, p_55421_, p_55422_);
			this.updateConnections(shape);
		}
		
		public List<BlockPos> getConnections() { return this.connections; }
		
		private void updateConnections(Shape shape)
		{
			this.connections.clear();
			switch (shape) {
				case NORTH_SOUTH:
					this.connections.add(this.pos.north());
					this.connections.add(this.pos.south());
					break;
				case EAST_WEST:
					this.connections.add(this.pos.west());
					this.connections.add(this.pos.east());
					break;
				case SOUTH_EAST:
					this.connections.add(this.pos.east());
					this.connections.add(this.pos.south());
					break;
				case SOUTH_WEST:
					this.connections.add(this.pos.west());
					this.connections.add(this.pos.south());
					break;
				case NORTH_WEST:
					this.connections.add(this.pos.west());
					this.connections.add(this.pos.north());
					break;
				case NORTH_EAST:
					this.connections.add(this.pos.east());
					this.connections.add(this.pos.north());
			}

		}
		
		private void removeSoftConnections()
		{
			for(int i = 0; i < this.connections.size(); ++i)
			{
				TilledState railstate = this.getTilled(this.connections.get(i));
				if (railstate != null && railstate.connectsTo(this))
					this.connections.set(i, railstate.pos);
				else
					this.connections.remove(i--);
			}
		}
		
		private boolean isTilledSand(BlockPos p_55430_)
		{
			return TilledSand.isTilledSand(this.level, p_55430_) || TilledSand.isTilledSand(this.level, p_55430_.above()) || TilledSand.isTilledSand(this.level, p_55430_.below());
		}
		
		@Nullable
		private TilledState getTilled(BlockPos pos)
		{
			BlockState state = this.level.getBlockState(pos);
			if (TilledSand.isTilledSand(state))
				return new TilledState(this.level, pos, state);
			return null;
		}
		
		private boolean connectsTo(TilledState stateIn) { return this.hasConnection(stateIn.pos); }
		
		private boolean hasConnection(BlockPos p_55444_)
		{
			for(int i = 0; i < this.connections.size(); ++i)
			{
				BlockPos blockpos = this.connections.get(i);
				if (blockpos.getX() == p_55444_.getX() && blockpos.getZ() == p_55444_.getZ())
					return true;
			}
			return false;
		}
		
		protected int countPotentialConnections()
		{
			int i = 0;
			for(Direction direction : Direction.Plane.HORIZONTAL)
				if(this.isTilledSand(this.pos.relative(direction)))
					++i;
			return i;
		}
		
		private boolean canConnectTo(TilledState stateIn)
		{
			return this.connectsTo(stateIn) || this.connections.size() != 2;
		}
		
		private void connectTo(TilledState stateIn)
		{
			this.connections.add(stateIn.pos);
			boolean connectedNorth = this.hasConnection(this.pos.north());
			boolean connectedSouth = this.hasConnection(this.pos.south());
			boolean connectedWest = this.hasConnection(this.pos.west());
			boolean connectedEast = this.hasConnection(this.pos.east());
			
			Shape shape = null;
			if (connectedNorth || connectedSouth)
				shape = Shape.NORTH_SOUTH;
			if (connectedWest || connectedEast)
				shape = Shape.EAST_WEST;
			if (connectedSouth && connectedEast && !connectedNorth && !connectedWest)
				shape = Shape.SOUTH_EAST;
			if (connectedSouth && connectedWest && !connectedNorth && !connectedEast)
				shape = Shape.SOUTH_WEST;
			if (connectedNorth && connectedWest && !connectedSouth && !connectedEast)
				shape = Shape.NORTH_WEST;
			if (connectedNorth && connectedEast && !connectedSouth && !connectedWest)
				shape = Shape.NORTH_EAST;
			if (shape == null)
				shape = Shape.NORTH_SOUTH;
			
			if (!this.block.isValidShape(shape))
			{
				this.connections.remove(stateIn.pos);
				return;
			}
			
			this.state = this.state.setValue(TilledSand.SHAPE_PROPERTY, shape);
			this.level.setBlock(this.pos, this.state, 3);
		}

		private boolean hasNeighborTilled(BlockPos p_55447_)
		{
			TilledState tilledState = this.getTilled(p_55447_);
			if (tilledState == null)
				return false;
			
			tilledState.removeSoftConnections();
			return tilledState.canConnectTo(this);
		}

		public TilledState place(boolean p_55432_, boolean p_55433_, Shape p_55434_) {
			boolean neighbourNorth = this.hasNeighborTilled(this.pos.north());
			boolean neighbourSouth = this.hasNeighborTilled(this.pos.south());
			boolean neighbourWest = this.hasNeighborTilled(this.pos.west());
			boolean neighbourEast = this.hasNeighborTilled(this.pos.east());
			boolean axisZ = neighbourNorth || neighbourSouth;
			boolean axisX = neighbourWest || neighbourEast;
			boolean southEast = neighbourSouth && neighbourEast;
			boolean southWest = neighbourSouth && neighbourWest;
			boolean northEast = neighbourNorth && neighbourEast;
			boolean northWest = neighbourNorth && neighbourWest;
			
			Shape shape = null;
			if (axisZ && !axisX)
				shape = Shape.NORTH_SOUTH;
			if (axisX && !axisZ)
				shape = Shape.EAST_WEST;
			if (southEast && !neighbourNorth && !neighbourWest)
				shape = Shape.SOUTH_EAST;
			if (southWest && !neighbourNorth && !neighbourEast)
				shape = Shape.SOUTH_WEST;
			if (northWest && !neighbourSouth && !neighbourEast)
				shape = Shape.NORTH_WEST;
			if (northEast && !neighbourSouth && !neighbourWest)
				shape = Shape.NORTH_EAST;

			if (shape == null)
			{
				if (axisZ && axisX)
					shape = p_55434_;
				else if (axisZ)
					shape = Shape.NORTH_SOUTH;
				else if (axisX)
					shape = Shape.EAST_WEST;
				
				if (p_55432_)
				{
					if (southEast)
						shape = Shape.SOUTH_EAST;
					if (southWest)
						shape = Shape.SOUTH_WEST;
					if (northEast)
						shape = Shape.NORTH_EAST;
					if (northWest)
						shape = Shape.NORTH_WEST;
				}
				else
				{
					if (northWest)
						shape = Shape.NORTH_WEST;
					if (northEast)
						shape = Shape.NORTH_EAST;
					if (southWest)
						shape = Shape.SOUTH_WEST;
					if (southEast)
						shape = Shape.SOUTH_EAST;
				}
			}

			if (shape == null || !this.block.isValidShape(shape))
				shape = p_55434_;
			
			this.updateConnections(shape);
			this.state = this.state.setValue(TilledSand.SHAPE_PROPERTY, shape);
			if (p_55433_ || this.level.getBlockState(this.pos) != this.state)
			{
				this.level.setBlock(this.pos, this.state, 3);
				for(int i = 0; i < this.connections.size(); ++i)
				{
					TilledState railstate = this.getTilled(this.connections.get(i));
					if (railstate != null)
					{
						railstate.removeSoftConnections();
						if (railstate.canConnectTo(this))
							railstate.connectTo(this);
					}
				}
			}
			return this;
		}

		public BlockState getState() { return this.state; }
	}
}
