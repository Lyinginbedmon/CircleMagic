package com.lying.misc19.blocks;

import javax.annotation.Nullable;

import com.lying.misc19.blocks.entity.PhantomBlockEntity;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PhantomBlock extends Block implements EntityBlock
{
	public PhantomBlock(Properties properties)
	{
		super(properties.sound(SoundType.AMETHYST));
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PhantomBlockEntity(pos, state); }
	
	public RenderShape getRenderShape(BlockState p_49098_) { return RenderShape.INVISIBLE; }
	
	public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) { return 1F; }
	
	public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) { return true; }
	
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return context.isHoldingItem(M19Items.PHANTOM_CUBE_ITEM.get()) ? Shapes.block() : Shapes.empty();
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return Shapes.block(); }
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, M19BlockEntities.PHANTOM_CUBE.get(), world.isClientSide() ? PhantomBlockEntity::tickClient : PhantomBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
