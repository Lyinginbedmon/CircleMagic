package com.lying.circles.blocks;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.lying.circles.blocks.entity.FairyJarBlockEntity;
import com.lying.circles.blocks.entity.FairyPersonalityModel;
import com.lying.circles.blocks.entity.FairyPersonalityModel.Emotion;
import com.lying.circles.blocks.entity.FairyPersonalityModel.EmotiveEvent;
import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.CMItems;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
	
	@SuppressWarnings("deprecation")
	public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state)
	{
		ItemStack stack = super.getCloneItemStack(world, pos, state);
		world.getBlockEntity(pos, CMBlockEntities.FAIRY_JAR.get()).ifPresent((fairy) -> fairy.saveToItem(stack));
		return stack;
	}
	
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		ItemStack heldStack = player.getItemInHand(hand);
		if(heldStack.getItem() == Items.NAME_TAG && heldStack.hasCustomHoverName())
		{
			Optional<FairyJarBlockEntity> tile = world.getBlockEntity(pos, CMBlockEntities.FAIRY_JAR.get());
			tile.ifPresent((fairy) -> fairy.rename((MutableComponent)heldStack.getHoverName()));
			if(!player.getAbilities().instabuild)
				heldStack.shrink(1);
			return InteractionResult.CONSUME;
		}
		
		return InteractionResult.PASS;
	}
	
	public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player)
	{
		Optional<FairyJarBlockEntity> tile = world.getBlockEntity(pos, CMBlockEntities.FAIRY_JAR.get());
		if(tile.isPresent())
		{
			if(!world.isClientSide() && !player.isCreative())
			{
				ItemStack stack = new ItemStack(CMItems.FAIRY_JAR_ITEM.get());
				tile.get().saveToItem(stack);
				ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, stack);
				itemEntity.setDefaultPickUpDelay();
				world.addFreshEntity(itemEntity);
			}
		}
		
		super.playerWillDestroy(world, pos, state, player);
	}
	
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flags)
	{
		CompoundTag blockData = BlockItem.getBlockEntityData(stack);
		if(blockData == null || blockData.isEmpty() || !blockData.contains("Personality", Tag.TAG_COMPOUND))
			return;
		
		FairyPersonalityModel personality = new FairyPersonalityModel(RandomSource.create());
		personality.readFromNbt(blockData.getCompound("Personality"));
		
		tooltip.add(personality.name().withStyle(ChatFormatting.GRAY));
		
		if(flags.isAdvanced())
		{
			tooltip.add(Component.empty());
			if(blockData.contains("LastPlaced", Tag.TAG_COMPOUND))
			{
				BlockPos pos = NbtUtils.readBlockPos(blockData.getCompound("LastPlaced"));
				tooltip.add(Component.literal("Last Placed: "+pos.toShortString()).withStyle(ChatFormatting.GRAY));
			}
			
			tooltip.add(Component.literal("Personality:"));
			Map<EmotiveEvent, Pair<Emotion, Float>> model = personality.getModel();
			for(EmotiveEvent event : EmotiveEvent.values())
			{
				Pair<Emotion, Float> entry = model.getOrDefault(event, Pair.of(Emotion.NEUTRAL, 0F));
				tooltip.add(Component.literal(" * "+event.getSerializedName()+": "+entry.getFirst().getSerializedName()+" +"+entry.getSecond()).withStyle(ChatFormatting.GRAY));
			}
		}
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new FairyJarBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, CMBlockEntities.FAIRY_JAR.get(), world.isClientSide() ? FairyJarBlockEntity::tickClient : FairyJarBlockEntity::tickServer);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
	{
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
	}
}
