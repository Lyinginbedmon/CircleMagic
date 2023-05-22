package com.lying.misc19.blocks;

import javax.annotation.Nullable;

import com.lying.misc19.blocks.entity.MagicTreeBlockEntity;
import com.lying.misc19.init.M19BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

public class MagicTree extends DoublePlantBlock implements ICruciblePart, EntityBlock
{
	public MagicTree(Properties propertiesIn)
	{
		super(propertiesIn);
	}
	
	public PartType partType(BlockPos pos, BlockState state, Level world) { return PartType.BOUGH; }
	
	public boolean isMainHalf(BlockState state) { return state.getValue(HALF) == DoubleBlockHalf.LOWER; }
	
	public RenderShape getRenderShape(BlockState state)
	{
		return isMainHalf(state) ? RenderShape.MODEL : RenderShape.INVISIBLE;
	}
	
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(isMainHalf(state))
			return super.use(state, world, pos, player, hand, hitResult);
		else
		{
			BlockPos posBelow = pos.below();
			BlockState stateBelow = world.getBlockState(posBelow);
			return super.use(stateBelow, world, posBelow, player, hand, hitResult);
		}
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return isMainHalf(state) ? new MagicTreeBlockEntity(pos, state) : null;
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, M19BlockEntities.MAGIC_TREE.get(), world.isClientSide() ? MagicTreeBlockEntity::tickClient : MagicTreeBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
