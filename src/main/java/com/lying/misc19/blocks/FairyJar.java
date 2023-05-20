package com.lying.misc19.blocks;

import javax.annotation.Nullable;

import com.lying.misc19.blocks.entity.FairyJarBlockEntity;
import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FairyJar extends Block implements ICruciblePart, EntityBlock
{
	public static final VoxelShape SHAPE = Shapes.or(Block.box(4, 0, 4, 12, 8, 12), Block.box(5, 8, 5, 11, 11, 11), Block.box(4, 11, 4, 12, 14, 12));
	
	public FairyJar(Properties p_49795_)
	{
		super(p_49795_);
	}
	
	public VoxelShape getShape(BlockState p_51309_, BlockGetter p_51310_, BlockPos p_51311_, CollisionContext p_51312_) { return SHAPE; }
	
	public PartType partType(BlockPos pos, BlockState state, Level world) { return PartType.FAIRY; }
	
	public boolean canProvideSuggestions(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos) { return true; }
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new FairyJarBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, M19BlockEntities.FAIRY_JAR.get(), world.isClientSide() ? FairyJarBlockEntity::tickClient : FairyJarBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
