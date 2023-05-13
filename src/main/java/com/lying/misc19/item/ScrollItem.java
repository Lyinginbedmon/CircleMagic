package com.lying.misc19.item;

import javax.annotation.Nullable;

import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.ISpellComponent.Type;
import com.lying.misc19.magic.component.RootGlyph;
import com.lying.misc19.magic.variable.VariableSet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ScrollItem extends Item implements ISpellContainer
{
	public ScrollItem(Properties properties)
	{
		super(properties);
	}
	
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		player.getCooldowns().addCooldown(this, 20);
		
		if(!world.isClientSide())
		{
			ISpellComponent spell = getSpell(stack.getOrCreateTag());
			if(spell != null && spell.type() == Type.ROOT)
			{
				RootGlyph root = (RootGlyph)spell;
				root.addSpellToWorld(spell, world, player);
				player.awardStat(Stats.ITEM_USED.get(this));
				return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
			}
			
			if(!player.getAbilities().instabuild)
				stack.shrink(1);
		}
		
		return InteractionResultHolder.pass(stack);
	}
	
	public static ItemStack setSpell(ItemStack stack, ISpellComponent component)
	{
		CompoundTag tag = stack.getOrCreateTag();
		
		CompoundTag spellData = ISpellComponent.saveToNBT(component);
		tag.put("Spell", spellData);
		
		stack.setTag(tag);
		
		return stack;
	}
	
	@Nullable
	public ISpellComponent getSpell(CompoundTag compound)
	{
		if(compound.isEmpty())
		{
			ISpellComponent circle = SpellComponents.create(SpellComponents.ROOT_CASTER).addOutputs(SpellComponents.create(SpellComponents.CIRCLE_BASIC)
				.addOutputs(
					SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_TRUE)).addOutputs(SpellComponents.create(VariableSet.Slot.CONTINUE.glyph())),
					SpellComponents.create(SpellComponents.GLYPH_SET).addInputs(SpellComponents.create(SpellComponents.SIGIL_XYZ)).addOutputs(SpellComponents.create(VariableSet.Slot.AMUN.glyph())),
					SpellComponents.create(SpellComponents.FUNCTION_DEBUG)
				));
			
			return circle;
		}
		else if(compound.contains("Spell", Tag.TAG_COMPOUND))
			return SpellComponents.readFromNBT(compound.getCompound("Spell"));
		else
			return null;
	}
}
