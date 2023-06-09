package com.lying.circles.blocks.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.circles.blocks.ICruciblePart;
import com.lying.circles.blocks.ICruciblePart.PartType;
import com.lying.circles.client.Canvas;
import com.lying.circles.client.gui.menu.MenuSandbox;
import com.lying.circles.init.CMBlockEntities;
import com.lying.circles.init.SpellComponents;
import com.lying.circles.item.ISpellContainer;
import com.lying.circles.magic.ISpellComponent;
import com.lying.circles.network.PacketHandler;
import com.lying.circles.network.PacketSyncArrangementClient;
import com.lying.circles.reference.Reference;
import com.lying.circles.utility.CrucibleManager;
import com.lying.circles.utility.SpellTextureManager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrucibleBlockEntity extends BlockEntity implements MenuProvider, ArrangementHolder
{
	public static final double RANGE = 16D;
	public static final int SPACING = 5;
	private static final AABB RENDER_AABB = new AABB(-RANGE, -1, -RANGE, RANGE, 1, RANGE);
	
	private boolean hasNotifiedManager = false;
	
	@OnlyIn(Dist.CLIENT)
	private Canvas canvas = new Canvas(SpellTextureManager.getNewTexture(), 8);
	private boolean needsRedrawing = true;
	
	private ISpellComponent arrangement = null;
	
	public CrucibleBlockEntity(BlockPos pos, BlockState state)
	{
		super(CMBlockEntities.CRUCIBLE.get(), pos, state);
	}
	
	public AABB getRenderBoundingBox() { return RENDER_AABB.move(getBlockPos()); }
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.put("Spell", arrangement == null ? new CompoundTag() : ISpellComponent.saveToNBT(arrangement));
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		
		CompoundTag spellData = compound.getCompound("Spell");
		this.arrangement = spellData.isEmpty() ? null : SpellComponents.readFromNBT(compound.getCompound("Spell"));
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, CrucibleBlockEntity tile)
	{
		
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, CrucibleBlockEntity tile)
	{
		if(!tile.hasNotifiedManager)
		{
			CrucibleManager.instance(world).addCrucibleAt(pos);
			tile.hasNotifiedManager = true;
		}
	}
	
	public ISpellComponent arrangement() { return this.arrangement; }
	
	public void setArrangement(ISpellComponent spellIn)
	{
		this.arrangement = spellIn;
		this.needsRedrawing = true;
		markDirty();
	}
	
	@OnlyIn(Dist.CLIENT)
	public Canvas getCanvas()
	{
		if(this.needsRedrawing)
		{
			this.canvas.clear();
			this.canvas.populate(arrangement());
			this.needsRedrawing = false;
		}
		return this.canvas;
	}
	
	public int glyphCap()
	{
		CrucibleManager manager = CrucibleManager.instance(getLevel());
		double cap = 5;
		BlockPos cruciblePos = getBlockPos();
		Map<Integer, List<BlockPos>> pillarMap = CrucibleManager.delineatePartsAround(manager.getPartsOfType(PartType.PILLAR, cruciblePos), cruciblePos);
		if(pillarMap.isEmpty())
			return (int)cap;
		for(int ring : pillarMap.keySet())
		{
			List<BlockPos> pillars = pillarMap.get(ring);
			/**
			 * Distance of this ring from the crucible<br>
			 * Pillars need to be placed as close to this distance from the crucible as possible for maximum efficiency
			 */
			double idealDist = ring * SPACING;
			
			/**
			 * Average distance between pillars in this ring<br>
			 * Pillars need to be placed as close to this distance apart as possible for maximum efficiency
			 */
			double avgDist = 0D;
			for(int i=0; i<pillars.size(); i++)
			{
				BlockPos pillarA = pillars.get(i);
				BlockPos pillarB = pillars.get((i + 1) % pillars.size());
				
				Vec2 vecA = new Vec2(pillarA.getX() + 0.5F, pillarA.getZ() + 0.5F);
				Vec2 vecB = new Vec2(pillarB.getX() + 0.5F, pillarB.getZ() + 0.5F);
				
				avgDist += Math.sqrt(vecA.distanceToSqr(vecB));
			}
			avgDist /= pillars.size();
			
			for(int i=0; i<pillars.size(); i++)
			{
				BlockPos pillar = pillars.get(i);
				BlockPos neighbour = pillars.get((i + 1) % pillars.size());
				Vec2 pillarVec = new Vec2(pillar.getX() + 0.5F, pillar.getZ() + 0.5F);
				Vec2 neighbourVec = new Vec2(neighbour.getX() + 0.5F, neighbour.getZ() + 0.5F);
				
				InscribedBlockEntity tile = (InscribedBlockEntity)getLevel().getBlockEntity(pillar);
				cap += Math.round(tile.getTotalCapBonusFor(Math.sqrt(pillarVec.distanceToSqr(neighbourVec)), avgDist, cruciblePos, idealDist));
			}
		}
		return (int)cap;
	}
	
	public boolean hasSuggestions()
	{
		Level world = getLevel();
		for(BlockPos pos : CrucibleManager.instance(world).getPartsOfType(PartType.FAIRY, getBlockPos()))
		{
			BlockState state = world.getBlockState(pos);
			ICruciblePart part = (ICruciblePart)state.getBlock();
			if(part.canProvideSuggestions(pos, state, world, getBlockPos()))
				return true;
		}
		return false;
	}
	
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	public CompoundTag getUpdateTag()
	{
		CompoundTag compound = new CompoundTag();
		this.saveAdditional(compound);
		return compound;
	}
	
	public void markDirty()
	{
		if(getLevel() != null)
		{
			BlockState state = getBlockState();
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
			setChanged();
		}
	}
	
	public void openEditorFor(Player player)
	{
		player.openMenu(this);
		
		if(!player.getLevel().isClientSide())
			PacketHandler.sendTo((ServerPlayer)player, new PacketSyncArrangementClient(ISpellComponent.saveToNBT(this.arrangement)));
	}
	
	@Nullable
	public MagicTreeBlockEntity getFirstClosestItem(Level world)
	{
		Map<Integer, List<BlockPos>> boughMap = CrucibleManager.instance(world).getDelineatedPartsOfType(PartType.BOUGH, getBlockPos());
		List<Integer> keySet = Lists.newArrayList();
		keySet.addAll(boughMap.keySet());
		Collections.sort(keySet);
		
		for(Integer ring : keySet)
			for(BlockPos bough : boughMap.get(ring))
			{
				MagicTreeBlockEntity boughTile = (MagicTreeBlockEntity)world.getBlockEntity(bough);
				if(!boughTile.isEmpty())
					return boughTile;
			}
		return null;
	}
	
	@Nullable
	public BlockPos getClosestFairy(@Nullable Level world)
	{
		if(world == null || world.isClientSide())
			return null;
		
		BlockPos fairy = null;
		for(BlockPos jar : CrucibleManager.instance(world).getPartsOfType(PartType.FAIRY, getBlockPos()))
		{
			double dist = jar.distSqr(getBlockPos());
			if(dist <= (RANGE * RANGE))
				if(fairy == null || dist < fairy.distSqr(getBlockPos()))
					fairy = jar;
		}
		return fairy;
	}
	
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
	{
		if(!hasArrangement())
		{
			ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
			if(heldStack.getItem() instanceof ISpellContainer)
				setArrangement(((ISpellContainer)heldStack.getItem()).getSpell(heldStack.getTag()));
		}
		
		Container tree = getFirstClosestItem(player.getLevel());
		if(tree == null)
			tree = new SimpleContainer(2);
		
		return new MenuSandbox(containerId, inventory, tree, arrangement(), glyphCap(), getBlockPos(), getClosestFairy(player.getLevel()));
	}
	
	public Component getDisplayName() { return Component.translatable("gui."+Reference.ModInfo.MOD_ID+".crucible"); }
}
