package com.lying.misc19.blocks.entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.blocks.ICruciblePart;
import com.lying.misc19.blocks.ICruciblePart.PartType;
import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.gui.menu.MenuSandbox;
import com.lying.misc19.init.M19BlockEntities;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.item.ISpellContainer;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.CrucibleManager;
import com.lying.misc19.utility.SpellTextureManager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrucibleBlockEntity extends BlockEntity implements MenuProvider
{
	private static final AABB RENDER_AABB = new AABB(-16, -1, -16, 16, 1, 16);
	public static final int PILLAR_SPACING = 5;
	
	private Map<PartType, List<BlockPos>> expansionMap = new HashMap<>();
	private boolean hasNotifiedManager = false;
	
	@OnlyIn(Dist.CLIENT)
	private Canvas canvas = new Canvas(SpellTextureManager.getNewTexture(), 8);
	private boolean needsRedrawing = true;
	
	private ISpellComponent arrangement = SpellComponents.create(SpellComponents.ROOT_DUMMY).addOutputs(
			SpellComponents.create(SpellComponents.CIRCLE_BASIC).addInputs(SpellComponents.create(SpellComponents.GLYPH_NAND).addInputs(SpellComponents.create(Slot.AGE.glyph()))).addOutputs(
				SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_FALSE)).addOutputs(SpellComponents.create(Slot.BAST.glyph())),
				SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_TRUE)).addOutputs(SpellComponents.create(Slot.THOTH.glyph())),
				SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_FALSE)).addOutputs(SpellComponents.create(Slot.SUTEKH.glyph()))),
			SpellComponents.create(SpellComponents.CIRCLE_BASIC).addOutputs(
				SpellComponents.create(SpellComponents.GLYPH_XOR).addInputs(SpellComponents.create(Slot.BAST.glyph()), SpellComponents.create(Slot.THOTH.glyph())).addOutputs(SpellComponents.create(Slot.ANUBIS.glyph())),
				SpellComponents.create(SpellComponents.GLYPH_AND).addInputs(SpellComponents.create(Slot.ANUBIS.glyph()), SpellComponents.create(Slot.SUTEKH.glyph())).addOutputs(SpellComponents.create(Slot.HORUS.glyph())),
				SpellComponents.create(SpellComponents.GLYPH_AND).addInputs(SpellComponents.create(Slot.BAST.glyph()), SpellComponents.create(Slot.THOTH.glyph())).addOutputs(SpellComponents.create(Slot.ISIS.glyph())),
				SpellComponents.create(SpellComponents.GLYPH_OR).addInputs(SpellComponents.create(Slot.HORUS.glyph()), SpellComponents.create(Slot.ISIS.glyph())).addOutputs(SpellComponents.create(Slot.RA.glyph())),
				SpellComponents.create(SpellComponents.GLYPH_XOR).addInputs(SpellComponents.create(Slot.ANUBIS.glyph()), SpellComponents.create(Slot.SUTEKH.glyph())).addOutputs(SpellComponents.create(Slot.OSIRIS.glyph()))));
	
	public CrucibleBlockEntity(BlockPos pos, BlockState state)
	{
		super(M19BlockEntities.CRUCIBLE.get(), pos, state);
	}
	
	public AABB getRenderBoundingBox() { return RENDER_AABB.move(getBlockPos()); }
	
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		ListTag expansions = new ListTag();
		for(PartType type : PartType.values())
		{
			ListTag entries = new ListTag();
			getExpansions(type).forEach((block) -> entries.add(NbtUtils.writeBlockPos(block)));
			expansions.add(entries);
		}
		compound.put("Expansions", expansions);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.expansionMap.clear();
		ListTag expansions = compound.getList("Expansions", Tag.TAG_LIST);
		for(PartType type : PartType.values())
		{
			ListTag entries = expansions.getList(type.ordinal());
			List<BlockPos> pos = Lists.newArrayList();
			for(int i=0; i<entries.size(); i++)
				pos.add(NbtUtils.readBlockPos(entries.getCompound(i)));
			this.expansionMap.put(type, pos);
		}
	}
	
	public static void tickClient(Level world, BlockPos pos, BlockState state, CrucibleBlockEntity tile)
	{
		
	}
	
	/** Groups positions together based on the closest multiple of spaces from it to the crucible */
	public static Map<Integer, List<BlockPos>> delineatePillars(List<BlockPos> pillars, BlockPos crucible)
	{
		Map<Integer, List<BlockPos>> delineated = new HashMap<>();
		Vec2 crucibleVec = new Vec2(crucible.getX() + 0.5F, crucible.getZ() + 0.5F);
		
		// Collect all positions together based on their nearest multiple of spacing distance from the crucible
		for(BlockPos pillar : pillars)
		{
			Vec2 pillarVec = new Vec2(pillar.getX() + 0.5F, pillar.getZ() + 0.5F);
			int ring = (int)Math.round(Math.sqrt(pillarVec.distanceToSqr(crucibleVec)) / PILLAR_SPACING);
			List<BlockPos> set = delineated.getOrDefault(ring, Lists.newArrayList());
			set.add(pillar);
			delineated.put(ring, set);
		}
		
		// Sort all rings radially around the crucible
		for(List<BlockPos> val : delineated.values())
			val.sort(new Comparator<BlockPos>()
						{
							public int compare(BlockPos pos1, BlockPos pos2)
							{
								Vec2 vec1 = new Vec2(pos1.getX(), pos1.getZ());
								Vec2 vec2 = new Vec2(pos2.getX(), pos2.getZ());
								double angle1 = Math.atan2(vec1.x, vec1.y) - Math.atan2(crucibleVec.x, crucibleVec.y);
								double angle2 = Math.atan2(vec2.x, vec2.y) - Math.atan2(crucibleVec.x, crucibleVec.y);
								return angle1 > angle2 ? 1 : angle1 < angle2 ? -1 : 0;
							}
						});
		
		return delineated;
	}
	
	public static void tickServer(Level world, BlockPos pos, BlockState state, CrucibleBlockEntity tile)
	{
		if(!tile.hasNotifiedManager)
		{
			CrucibleManager.instance(world).addCrucibleAt(pos);
			tile.hasNotifiedManager = true;
		}
	}
	
	public ISpellComponent arrangement()
	{
		return this.arrangement;
	}
	
	public void setArrangement(ISpellComponent spellIn)
	{
		this.arrangement = spellIn;
		this.needsRedrawing = true;
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
	
	public List<BlockPos> getExpansions(PartType type) { return this.expansionMap.getOrDefault(type, Lists.newArrayList()); }
	
	/** Retrieves all currently-valid positions of the given type, clears any that can't be valid any more */
	protected List<BlockPos> getValidOfType(PartType type)
	{
		List<BlockPos> validated = Lists.newArrayList();
		
		List<BlockPos> invalidated = Lists.newArrayList();
		List<BlockPos> points = getExpansions(type);
		for(BlockPos pos : points)
		{
			BlockState state = getLevel().getBlockState(pos);
			if(state.getBlock() instanceof ICruciblePart)
			{
				ICruciblePart part = (ICruciblePart)state.getBlock();
				if(part.isPartValidFor(pos, state, getLevel(), getBlockPos()))
					validated.add(pos);
			}
			else
				invalidated.add(pos);
		}
		
		if(!invalidated.isEmpty())
		{
			points.removeAll(invalidated);
			this.expansionMap.put(type, points);
			setChanged();
		}
		return validated;
	}
	
	public int glyphCap()
	{
		double cap = 5;
		BlockPos cruciblePos = getBlockPos();
		Vec2 crucibleVec = new Vec2(cruciblePos.getX() + 0.5F, cruciblePos.getZ() + 0.5F);
		Map<Integer, List<BlockPos>> pillarMap = delineatePillars(getValidOfType(PartType.PILLAR), cruciblePos);
		for(int ring : pillarMap.keySet())
		{
			List<BlockPos> pillars = pillarMap.get(ring);
			double idealDist = ring * PILLAR_SPACING;
			
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
				
				BlockState state = getLevel().getBlockState(pillar);
				ICruciblePart part = (ICruciblePart)state.getBlock();
				int bonus = part.glyphCapBonus(pillar, state, getLevel(), cruciblePos);
				
				// FIXME Reduce distance to whole blocks for fitting
				Vec2 pillarVec = new Vec2(pillar.getX() + 0.5F, pillar.getZ() + 0.5F);
				double distToCrucible = Math.sqrt(pillarVec.distanceToSqr(crucibleVec));
				double spaceEfficiency = 1 - ((distToCrucible <= idealDist ? idealDist - distToCrucible : distToCrucible - idealDist) / idealDist);
				
				BlockPos neighbour = pillars.get((i + 1) % pillars.size());
				Vec2 neighbourVec = new Vec2(neighbour.getX() + 0.5F, neighbour.getZ() + 0.5F);
				double distToNeighbour = Math.sqrt(pillarVec.distanceToSqr(neighbourVec));
				double spacingEfficiency = 1 - ((distToNeighbour <= avgDist ? avgDist - distToNeighbour : distToNeighbour - avgDist) / avgDist);
				
				cap += (bonus * (spaceEfficiency * spacingEfficiency));
			}
		}
		
		return (int)cap;
	}
	
	
	
	public boolean hasSuggestions()
	{
		boolean result = false;
		for(BlockPos pos : getValidOfType(PartType.FAIRY))
		{
			BlockState state = getLevel().getBlockState(pos);
			ICruciblePart part = (ICruciblePart)state.getBlock();
			result = result || part.canProvideSuggestions(pos, state, getLevel(), getBlockPos());
		}
		return result;
	}
	
	public void assessAndAddExpansion(BlockPos pos)
	{
		BlockState state = getLevel().getBlockState(pos);
		if(state.getBlock() instanceof ICruciblePart)
		{
			ICruciblePart part = (ICruciblePart)state.getBlock();
			PartType type = part.partType(pos, state, getLevel());
			List<BlockPos> partsOfType = this.expansionMap.getOrDefault(type, Lists.newArrayList());
			if(!partsOfType.contains(pos))
			{
				partsOfType.add(pos);
				this.expansionMap.put(type, partsOfType);
				setChanged();
			}
		}
	}
	
	public void removeExpansion(BlockPos pos)
	{
		boolean found = false;
		Map<PartType, List<BlockPos>> nextMap = new HashMap<>();
		for(Entry<PartType, List<BlockPos>> entry : this.expansionMap.entrySet())
		{
			PartType type = entry.getKey();
			List<BlockPos> points = entry.getValue();
			if(points.contains(pos))
			{
				points.remove(pos);
				found = true;
			}
			nextMap.put(type, points);
		}
		
		if(found)
		{
			this.expansionMap.clear();
			this.expansionMap.putAll(nextMap);
			setChanged();
		}
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
	
	public void openEditorFor(Player player)
	{
		player.openMenu(this);
	}
	
	@Nullable
	public MagicTreeBlockEntity getFirstClosestItem(Level world)
	{
		Map<Integer, List<BlockPos>> boughMap = delineatePillars(getValidOfType(PartType.BOUGH), getBlockPos());
		List<Integer> keySet = Lists.newArrayList();
		keySet.addAll(boughMap.keySet());
		Collections.sort(keySet);
		for(Integer ring : keySet)
		{
			for(BlockPos bough : boughMap.get(ring))
			{
				MagicTreeBlockEntity boughTile = (MagicTreeBlockEntity)world.getBlockEntity(bough);
				if(!boughTile.isEmpty())
					return boughTile;
			}
		}
		return null;
	}
	
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
	{
		ISpellComponent spell = null;
		ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
		if(heldStack.getItem() instanceof ISpellContainer)
			spell = ((ISpellContainer)heldStack.getItem()).getSpell(heldStack.getTag());
		
		return new MenuSandbox(containerId, inventory, new SimpleContainerData(1), getFirstClosestItem(player.getLevel()), spell, glyphCap());
	}
	
	public Component getDisplayName() { return Component.translatable("gui."+Reference.ModInfo.MOD_ID+".crucible"); }
}
