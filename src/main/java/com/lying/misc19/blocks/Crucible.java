package com.lying.misc19.blocks;

import javax.annotation.Nullable;

import com.lying.misc19.blocks.entity.CrucibleBlockEntity;
import com.lying.misc19.client.gui.menu.MenuSandbox;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.M19Blocks;
import com.lying.misc19.item.ISpellContainer;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class Crucible extends Block implements MenuProvider, EntityBlock
{
	public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
	
	public Crucible(Properties p_49795_)
	{
		super(p_49795_);
		this.registerDefaultState(this.defaultBlockState().setValue(HAS_WATER, false));
	}
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateHolder)
	{
		stateHolder.add(HAS_WATER);
	}
	
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		ItemStack heldStack = player.getItemInHand(hand);
		if(state.getValue(HAS_WATER))
		{
			if(heldStack.is(Items.BUCKET))
				return fillBucket(world, pos, player, heldStack, hand);
			else if(!world.isClientSide())
			{
				player.openMenu(this);
				return InteractionResult.CONSUME;
			}
		}
		else if(heldStack.is(Items.WATER_BUCKET))
			return emptyBucket(world, pos, player, heldStack, hand);
		
		return InteractionResult.PASS;
	}
	
	private static InteractionResult fillBucket(Level world, BlockPos pos, Player player, ItemStack heldStack, InteractionHand hand)
	{
		if(!world.isClientSide())
		{
			Item item = heldStack.getItem();
			player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(Items.WATER_BUCKET)));
			player.awardStat(Stats.USE_CAULDRON);
			player.awardStat(Stats.ITEM_USED.get(item));
			world.setBlockAndUpdate(pos, M19Blocks.CRUCIBLE.get().defaultBlockState());
			world.playSound((Player)null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1F, 1F);
			world.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, pos);
		}
		
		return InteractionResult.sidedSuccess(world.isClientSide());
	}
	
	private static InteractionResult emptyBucket(Level world, BlockPos pos, Player player, ItemStack heldStack, InteractionHand hand)
	{
		if(!world.isClientSide())
		{
			Item item = heldStack.getItem();
			player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, new ItemStack(Items.BUCKET)));
			player.awardStat(Stats.FILL_CAULDRON);
			player.awardStat(Stats.ITEM_USED.get(item));
			world.setBlockAndUpdate(pos, M19Blocks.CRUCIBLE.get().defaultBlockState().setValue(HAS_WATER, true));
			world.playSound((Player)null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1F, 1F);
			world.gameEvent((Entity)null, GameEvent.FLUID_PLACE, pos);
		}
		
		return InteractionResult.sidedSuccess(world.isClientSide());
	}
	
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
	{
		ISpellComponent spell = null;
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
		if(stack.getItem() instanceof ISpellContainer)
			spell = ((ISpellContainer)stack.getItem()).getSpell(stack.getTag());
		return new MenuSandbox(containerId, inventory, spell);
	}
	
	public Component getDisplayName()
	{
		return Component.translatable("gui."+Reference.ModInfo.MOD_ID+".crucible");
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new CrucibleBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, M19BlockEntities.CRUCIBLE.get(), world.isClientSide() ? CrucibleBlockEntity::tickClient : CrucibleBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
