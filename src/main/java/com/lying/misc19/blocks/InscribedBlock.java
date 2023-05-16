package com.lying.misc19.blocks;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.lying.misc19.blocks.entity.InscriptionBlockEntity;
import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class InscribedBlock extends Block implements ICruciblePart, EntityBlock
{
	private final Predicate<BlockState> validator;
	
	public InscribedBlock(Properties p_49795_, Predicate<BlockState> validatorIn)
	{
		super(p_49795_);
		this.validator = validatorIn;
	}
	
	public InscribedBlock(Properties p_49795_, Block matIn)
	{
		this(p_49795_, (state) -> state.is(matIn));
	}
	
	public static InscribedBlock log(Properties properties, Block matIn)
	{
		return new InscribedBlock(properties, (state) -> state.is(matIn) && state.getValue(RotatedPillarBlock.AXIS) == Axis.Y);
	}
	
	public PartType partType(BlockPos pos, BlockState state, Level world) { return PartType.PILLAR; }
	
	public boolean isPartValidFor(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos)
	{
		return isActive(pos, world) && (pos.getY() - cruciblePos.getY()) == 1;
	}
	
	public boolean isActive(BlockPos pos, Level world)
	{
		return validator.apply(world.getBlockState(pos.below())) && validator.apply(world.getBlockState(pos.above()));
	}
	
	public int glyphCapBonus(BlockPos pos, BlockState state, Level world, BlockPos cruciblePos) { return 5; }
	
	@SuppressWarnings("deprecation")
	public BlockState updateShape(BlockState state, Direction face, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos)
	{
		return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, face, neighbourState, world, pos, neighbourPos);
	}
	
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		return validator.apply(world.getBlockState(pos.below()));
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new InscriptionBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, M19BlockEntities.INSCRIPTION.get(), world.isClientSide() ? InscriptionBlockEntity::tickClient : null);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
