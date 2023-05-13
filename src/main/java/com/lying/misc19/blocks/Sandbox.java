package com.lying.misc19.blocks;

import com.lying.misc19.client.gui.menu.MenuSandbox;
import com.lying.misc19.item.ISpellContainer;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Sandbox extends Block implements MenuProvider
{
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 6, 16);
	
	public Sandbox(Properties properties)
	{
		super(properties.sound(SoundType.SAND));
	}
	
	public VoxelShape getShape(BlockState p_51309_, BlockGetter p_51310_, BlockPos p_51311_, CollisionContext p_51312_) { return SHAPE; }
	
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		return world.getBlockState(pos.below()).is(Blocks.BOOKSHELF);
	}
	
	@SuppressWarnings("deprecation")
	public BlockState updateShape(BlockState state, Direction face, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos)
	{
		return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, face, neighbourState, world, pos, neighbourPos);
	}
	
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(!world.isClientSide())
		{
			player.openMenu(this);
			return InteractionResult.CONSUME;
		}
		
		return InteractionResult.FAIL;
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
		return Component.translatable("gui."+Reference.ModInfo.MOD_ID+".sandbox");
	}
}
