package com.lying.misc19.client.gui.screen;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableList;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.ISpellComponent.Category;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GlyphList extends ContainerObjectSelectionList<GlyphList.Entry>
{
	private static final Minecraft mc = Minecraft.getInstance();
	private final ScreenSandbox mainScreen;
	private Category currentCategory = Category.ROOT;
	private List<Component> tooltip = Lists.newArrayList();
	
	public GlyphList(Minecraft mc, ScreenSandbox parentScreen, int width, int height, int startY, int endY, int entryHeight)
	{
		super(mc, width, height, startY, endY, entryHeight);
		this.mainScreen = parentScreen;
		this.setRenderBackground(false);
		this.setRenderSelection(false);
		setCategory(currentCategory);
	}
	
	public Category currentCategory() { return this.currentCategory; }
	
	public void incCategory(@Nullable ISpellComponent arrangement)
	{
		changeCategory(arrangement, 1);
	}
	
	public void decCategory(@Nullable ISpellComponent arrangement)
	{
		changeCategory(arrangement, -1);
	}
	
	private void changeCategory(@Nullable ISpellComponent arrangement, int dir)
	{
		if(arrangement == null && currentCategory == Category.ROOT)
			return;
		
		int index = currentCategory.index() + dir;
		currentCategory = Category.byIndex(index);
		if(!isCategoryValid(currentCategory, arrangement))
			changeCategory(arrangement, dir);
		else
			setCategory(currentCategory);
	}
	
	public void checkCategory(@Nullable ISpellComponent arrangement)
	{
		if(!isCategoryValid(currentCategory, arrangement))
			incCategory(arrangement);
	}
	
	private boolean isCategoryValid(Category cat, @Nullable ISpellComponent arrangement)
	{
		switch(cat)
		{
			case ROOT:	return arrangement == null;
			case HERTZ:	return arrangement != null && arrangement.inputs().isEmpty();
			default:	return arrangement != null;
		}
	}
	
	public void setCategory(Category cat)
	{
		this.clearEntries();
		this.setScrollAmount(0D);
		for(ISpellComponent comp : SpellComponents.byCategory(cat, true))
			this.addEntry(new Entry(mainScreen, this, comp.getRegistryName()));
	}
	
	public int getScrollbarPosition()
	{
		return this.width;
	}
	
	public void render(PoseStack p_93447_, int p_93448_, int p_93449_, float partialTicks)
	{
		tooltip.clear();
		super.render(p_93447_, p_93448_, p_93449_, partialTicks);
		
		Screen.drawCenteredString(p_93447_, mc.font, currentCategory.translate(), this.width / 2, (20 - mc.font.lineHeight) / 2, 16777215);
		
		if(!tooltip.isEmpty())
			mainScreen.renderComponentTooltip(p_93447_, tooltip, p_93448_, p_93449_);
	}
	
	public static class Entry extends ContainerObjectSelectionList.Entry<GlyphList.Entry>
	{
		private static final String CLIP = "...";
		private static final Minecraft mc = Minecraft.getInstance();
		private static final int maxNameWidth = 120;
		private final ScreenSandbox mainScreen;
		private final GlyphList mainList;
		private final Button pickButton;
		private final Component displayName;
		private List<Component> tooltip = Lists.newArrayList();
		
		public Entry(ScreenSandbox parentScreen, GlyphList parentList, ResourceLocation glyph)
		{
			this.mainScreen = parentScreen;
			this.mainList = parentList;
			
			ISpellComponent component = SpellComponents.create(glyph);
			displayName = component.translatedName();
			pickButton = new ImageButton(0, 0, 16, 16, 0, 0, 0, component.spriteLocation(), 16, 16, (button) -> { mainScreen.setNewPart(glyph); });
			
			tooltip.add(component.category().translate());
			tooltip.add(component.description().withStyle(ChatFormatting.ITALIC));
		}
		
		public List<? extends GuiEventListener> children() { return ImmutableList.of(pickButton); }
		
		public List<? extends NarratableEntry> narratables() { return ImmutableList.of(pickButton); }
		
		public void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int p_93529_, int p_93530_, boolean isHovered, float partialTicks)
		{
			rowLeft += 25;
			this.pickButton.x = rowLeft + (this.pickButton.getWidth() / 2) + 5;
			this.pickButton.y = rowTop + (rowHeight - 16) / 2;
			this.pickButton.render(matrixStack, p_93529_, p_93530_, partialTicks);
			
			if(this.pickButton.isMouseOver(p_93529_, p_93530_))
				mainList.tooltip.addAll(tooltip);
			
			Component display = displayName;
			if(mc.font.width(display) > maxNameWidth)
			{
				String sequence = display.getString();
				String snippet = "";
				while(mc.font.width(snippet) < maxNameWidth - mc.font.width(CLIP))
				{
					char charAt = sequence.charAt(0);
					snippet += charAt;
					sequence = sequence.substring(1);
				}
				snippet += CLIP;
				display = Component.literal(snippet).setStyle(display.getStyle());
			}
			
			Screen.drawString(matrixStack, mc.font, display, rowLeft + 20 + this.pickButton.getWidth(), (rowTop + rowHeight / 2 - mc.font.lineHeight / 2), 16777215);
		}
		
	}
}
