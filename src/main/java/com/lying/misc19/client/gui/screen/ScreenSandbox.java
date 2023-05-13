package com.lying.misc19.client.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import com.lying.misc19.client.Canvas;
import com.lying.misc19.client.gui.menu.MenuSandbox;
import com.lying.misc19.client.renderer.ComponentRenderers;
import com.lying.misc19.client.renderer.RenderUtils;
import com.lying.misc19.init.M19Items;
import com.lying.misc19.init.SpellComponents;
import com.lying.misc19.item.ScrollItem;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.ISpellComponent.Category;
import com.lying.misc19.magic.ISpellComponent.Type;
import com.lying.misc19.reference.Reference;
import com.lying.misc19.utility.M19Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class ScreenSandbox extends Screen implements MenuAccess<MenuSandbox>
{
	public static final ResourceLocation HIGHLIGHT_TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/sandbox_highlight.png");
	private final MenuSandbox menu;
	private final Inventory playerInv;
	private Vec2 position = Vec2.ZERO;
	private Vec2 lastPosition = Vec2.ZERO;
	private Vec2 moveStart = null;
	private boolean isMoving = false;
	
	private int ticksOpen = 0;
	private Canvas canvas = null;
	
	private Vec2 lastClicked = Vec2.ZERO;
	private int clickTicks = 0;
	
	/** The last part we were hovering over, if any */
	private ISpellComponent hoveredPart = null;
	
	private GlyphList glyphList;
	/** The part we have selected to add with left-click */
	public ISpellComponent attachPart = null;
	
	private ISpellComponent selectedPart = null;
	
	private Button printButton, nextCatButton, prevCatButton;
	private ImageButton copyButton, pasteButton;
	
	public ScreenSandbox(MenuSandbox menuIn, Inventory inv, Component p_96550_)
	{
		super(p_96550_);
		this.menu = menuIn;
		this.playerInv = inv;
	}
	
	public MenuSandbox getMenu() { return this.menu; }
	
	protected void init()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.addWidget(this.glyphList = new GlyphList(this.minecraft, this, 150, this.height, 20, this.height - 20, 20));
		this.addRenderableWidget(prevCatButton = new Button(0, 0, 10, 20, Component.literal("<"), (button) -> 
		{
			this.glyphList.decCategory(menu.arrangement());
		}));
		this.addRenderableWidget(nextCatButton = new Button(140, 0, 10, 20, Component.literal(">"), (button) -> 
		{
			this.glyphList.incCategory(menu.arrangement());
		}));
		
		this.addRenderableWidget(printButton = new Button(0, this.height - 20, 100, 20, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".sandbox_print"), (button) -> 
		{
			ItemStack spell = new ItemStack(M19Items.MAGIC_SCROLL.get());
			if(menu.arrangement() != null)
			{
				ScrollItem.setSpell(spell, menu.arrangement());
				this.playerInv.add(spell);
			}
			onClose();
		}));
		
		this.addRenderableWidget(copyButton = makeButton(105, this.height - 20, 16, 16, 0, 0, 16, new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/sandbox_copy.png"), (button) -> 
		{
			this.minecraft.keyboardHandler.setClipboard(ISpellComponent.saveToNBT(menu.arrangement()).toString());
		}, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".sandbox_copy"), this));
		this.addRenderableWidget(pasteButton = makeButton(130, this.height - 20, 16, 16, 0, 0, 16, new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/sandbox_paste.png"), (button) -> 
		{
			String clipboard = this.minecraft.keyboardHandler.getClipboard();
			try
			{
				Tag parsed = TagParser.parseTag(clipboard);
				if(parsed.getId() == Tag.TAG_COMPOUND)
				{
					ISpellComponent comp = SpellComponents.readFromNBT((CompoundTag)parsed);
					if(comp.getRegistryName() != SpellComponents.GLYPH_DUMMY)
					{
						if(menu.arrangement() == null && comp.type() == Type.ROOT)
						{
							menu.setArrangement(comp);
							this.glyphList.setCategory(Category.CIRCLE);
							this.position = Vec2.ZERO;
						}
						else
							setNewPart(comp);
					}
				}
			}
			catch(Exception e) { }
		}, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".sandbox_paste"), this));
	}
	
	public void tick()
	{
		this.ticksOpen++;
		
		this.printButton.active = this.copyButton.active = this.copyButton.visible = menu.arrangement() != null;
		this.pasteButton.active = this.pasteButton.visible = !this.minecraft.keyboardHandler.getClipboard().isEmpty();
		
		this.glyphList.checkCategory(menu.arrangement());
		this.nextCatButton.active = this.prevCatButton.active = menu.arrangement() != null;
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		if(clickTicks > 0)
			clickTicks--;
		
		if(menu.arrangement() != null)
		{
			ISpellComponent arrangement = menu.arrangement();
			float scrollX = position.x, scrollY = position.y;
			if(isMoving)
			{
				Vec2 shift = new Vec2(mouseX - moveStart.x, mouseY - moveStart.y);
				if(shift.length() > 5)
				{
					scrollX += mouseX - moveStart.x;
					scrollY += mouseY - moveStart.y;
				}
			}
			
			arrangement.setPosition((width / 2) + scrollX, (height / 2) + scrollY);
			
			Vec2 currentPos = new Vec2(scrollX, scrollY);
			if(currentPos != lastPosition)
				updateCanvas(arrangement);
			this.canvas.drawIntoGUI(matrixStack, width, height);
			this.lastPosition = currentPos;
			
			hoveredPart = getComponentAt(mouseX, mouseY);
		}
		
		if(this.selectedPart != null)
			drawHighlightAround(selectedPart, matrixStack, partialTicks);
		
		this.glyphList.setLeftPos(0);
		this.glyphList.render(matrixStack, mouseX, mouseY, partialTicks);
		
		if(hoveredPart != null && attachPart == null)
		{
			List<Component> tooltip = Lists.newArrayList();
			tooltip.add(hoveredPart.translatedName().withStyle(ChatFormatting.BOLD));
			tooltip.add(hoveredPart.category().translate());
			tooltip.add(hoveredPart.description().withStyle(ChatFormatting.ITALIC));
			
			this.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
		}
		else if(attachPart != null)
		{
			attachPart.setPosition(mouseX, mouseY);
			ComponentRenderers.renderGUI(attachPart, matrixStack, width, height);
			
			if(hoveredPart != null)
			{
				boolean input = getAddState(hoveredPart, mouseX, mouseY, attachPart);
				boolean valid = input ? hoveredPart.isValidInput(attachPart) : hoveredPart.isValidOutput(attachPart);
				if(valid)
					this.renderTooltip(matrixStack, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".sandbox.add_"+(input ? "input" : "output")), mouseX, mouseY);
			}
		}
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	/** Returns true if the part will be added as an input, false for an output */
	private boolean getAddState(ISpellComponent target, int mouseX, int mouseY, ISpellComponent part)
	{
		boolean canBeInput = target.isValidInput(part);
		boolean canBeOutput = target.isValidOutput(part);
		
		if(canBeInput == canBeOutput)
		{
			Vec2 up = target.up();
			Vec2 core = target.position();
			Vec2 dir = new Vec2((float)mouseX - core.x, (float)mouseY - core.y).normalized();
			return up.dot(dir) > 0;
		}
		else
			return canBeInput;
	}
	
	private void updateCanvas(ISpellComponent arrangement)
	{
		this.canvas = ComponentRenderers.populateCanvas(arrangement, null);
	}
	
	public void setNewPart(ResourceLocation component)
	{
		setNewPart(SpellComponents.create(component));
		clearSelected();
	}
	
	public void setNewPart(ISpellComponent component)
	{
		this.attachPart = SpellComponents.readFromNBT(ISpellComponent.saveToNBT(component));
		
		if(component.type() == Type.ROOT && menu.arrangement() == null && tryAddGlyph(null, component, false))
			attachPart = null;
		else if(component.type() == Type.HERTZ && menu.arrangement() != null && tryAddGlyph(menu.arrangement(), component, true))
			attachPart = null;
	}
	
	@Nullable
	public ISpellComponent getComponentAt(int mouseX, int mouseY)
	{
		if(this.glyphList.isMouseOver(mouseX, mouseY))
			return null;
		
		Vec2 pos = new Vec2(mouseX, mouseY);
		double minDist = Double.MAX_VALUE;
		ISpellComponent hovered = null;
		
		for(ISpellComponent component : getTotalComponents())
		{
			double dist = Math.sqrt(pos.distanceToSqr(component.position()));
			int scale = ComponentRenderers.get(component.getRegistryName()).spriteScale();
			
			if(dist <= scale && dist < minDist)
			{
				hovered = component;
				minDist = dist;
			}
		}
		
		return hovered;
	}
	
	public List<ISpellComponent> getTotalComponents()
	{
		return menu.arrangement() == null ? Lists.newArrayList() : menu.arrangement().getParts();
	}
	
	public boolean tryAddGlyph(ISpellComponent recipient, ISpellComponent input, boolean asInput)
	{
		if(menu.arrangement() == null)
		{
			if(input.category() == Category.ROOT)
			{
				menu.setArrangement(input);
				menu.arrangement().organise();
				updateCanvas(menu.arrangement());
				this.glyphList.incCategory(menu.arrangement());
				return true;
			}
		}
		else if(recipient != null)
		{
			boolean result = false;
			if(asInput && recipient.isValidInput(input))
			{
				recipient.addInput(input);
				result = true;
			}
			else if(!asInput && recipient.isValidOutput(input))
			{
				recipient.addOutput(input);
				result = true;
			}
			if(result)
			{
				recipient.organise();
				updateCanvas(menu.arrangement());
			}
			return result;
		}
		return false;
	}
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(this.glyphList.isMouseOver(x, y))
			return this.glyphList.mouseClicked(x, y, mouseKey);
		else
		{
			// Double click to centre clicked point
			Vec2 clickPos = new Vec2((float)x, (float)y);
			if(this.clickTicks > 0 && this.lastClicked.distanceToSqr(clickPos) < 10)
				this.position = this.position.add(clickPos.add(new Vec2(width / 2, height / 2).negated()).negated());
			
			this.lastClicked = clickPos;
			this.clickTicks = 10;
		}
		
		if(attachPart != null)
		{
			if(mouseKey == 0)
			{
				boolean asInput = false;
				ISpellComponent target = null;
				
				if(menu.arrangement() != null && hoveredPart != null)
				{
					target = hoveredPart;
					asInput = getAddState(target, (int)x, (int)y, attachPart);
				}
				
				if(tryAddGlyph(target, attachPart, asInput))
				{
					attachPart = null;
					return true;
				}
			}
			else if(mouseKey == 1)
			{
				attachPart = null;
				return true;
			}
		}
		else if(mouseKey == 0 && !super.mouseClicked(x, y, mouseKey) && !this.glyphList.isMouseOver(x, y))
		{
			if(hoveredPart == null)
			{
				this.isMoving = true;
				moveStart = new Vec2((int)x, (int)y);
			}
			this.selectedPart = hoveredPart;
			return true;
		}
		return false;
	}
	
	public boolean mouseReleased(double x, double y, int mouseKey)
	{
		if(this.glyphList.isMouseOver(x, y))
			return this.glyphList.mouseReleased(x, y, mouseKey);
		
		if(mouseKey == 0 && isMoving)
		{
			float xOff = (float)x - moveStart.x;
			float yOff = (float)y - moveStart.y;
			Vec2 addMove = new Vec2(xOff, yOff);
			position = position.add(addMove);
			
			isMoving = false;
			moveStart = null;
		}
		return super.mouseReleased(x, y, mouseKey);
	}
	
	public boolean keyPressed(int keyID, int scanCode, int modifiers)
	{
		if(keyID == GLFW.GLFW_KEY_ESCAPE)
			return super.keyPressed(keyID, scanCode, modifiers);
		
		if(selectedPart != null)
		{
			if(keyID == GLFW.GLFW_KEY_DELETE || keyID == GLFW.GLFW_KEY_BACKSPACE)
			{
				handleDelete(selectedPart);
				clearSelected();
			}
			else if(modifiers == 2)	// Left control held
				if(keyID == GLFW.GLFW_KEY_X)
				{
					// Cut
					setNewPart(selectedPart);
					handleDelete(selectedPart);
					clearSelected();
				}
				else if(keyID == GLFW.GLFW_KEY_C)
				{
					// Copy
					setNewPart(selectedPart);
				}
			return true;
		}
		else
			switch(keyID)
			{
				case GLFW.GLFW_KEY_LEFT:
					this.position = this.position.add(new Vec2(10, 0));
					return true;
				case GLFW.GLFW_KEY_RIGHT:
					this.position = this.position.add(new Vec2(-10, 0));
					return true;
				case GLFW.GLFW_KEY_UP:
					this.position = this.position.add(new Vec2(0, 10));
					return true;
				case GLFW.GLFW_KEY_DOWN:
					this.position = this.position.add(new Vec2(0, -10));
					return true;
				default:
					return super.keyPressed(keyID, scanCode, modifiers);
			}
	}
	
	private void handleDelete(ISpellComponent part)
	{
		if(part == null)
			return;
		
		ISpellComponent parent = part.parent();
		if(parent == null && part.type() == Type.ROOT)
			clearArrangement();
		else
		{
			parent.remove(part);
			parent.organise();
			updateCanvas(menu.arrangement());
		}
	}
	
	private void clearSelected() { this.selectedPart = null; }
	
	private void clearArrangement()
	{
		menu.setArrangement(null);
		this.glyphList.setCategory(Category.ROOT);
	}
	
	private void drawHighlightAround(ISpellComponent part, PoseStack matrixStack, float partialTicks)
	{
		double totalTicks = this.ticksOpen + partialTicks;
		Vec2 highlightPos = part.position();
		float size = ComponentRenderers.get(part.getRegistryName()).spriteScale() + 20F + (float)(Math.sin(totalTicks * 0.1D) * 2.5F);
		
		double rads = Math.toRadians(totalTicks);
		double cos = Math.cos(rads);
		double sin = Math.sin(rads);
		
		Vec2[] vertices = new Vec2[]{
			M19Utils.rotate(new Vec2(-size / 2, -size / 2), cos, sin),
			M19Utils.rotate(new Vec2(+size / 2, -size / 2), cos, sin),
			M19Utils.rotate(new Vec2(+size / 2, +size / 2), cos, sin),
			M19Utils.rotate(new Vec2(-size / 2, +size / 2), cos, sin)};
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, HIGHLIGHT_TEXTURE);
		RenderSystem.setShaderColor(0F, 0.5F, 1F, 1F);
		RenderUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, (buffer) -> 
		{
			buffer.vertex(highlightPos.x + vertices[0].x, highlightPos.y + vertices[0].y, 0).uv(0F, 0F).endVertex();
			buffer.vertex(highlightPos.x + vertices[3].x, highlightPos.y + vertices[3].y, 0).uv(0F, 1F).endVertex();
			buffer.vertex(highlightPos.x + vertices[2].x, highlightPos.y + vertices[2].y, 0).uv(1F, 1F).endVertex();
			buffer.vertex(highlightPos.x + vertices[1].x, highlightPos.y + vertices[1].y, 0).uv(1F, 0F).endVertex();
		});
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	
	private ImageButton makeButton(int posX, int posY, int width, int height, int texXStart, int texYStart, int yHoverOffset, ResourceLocation texture, Button.OnPress onPress, Component displayText, Screen parent)
	{
		Button.OnTooltip tooltip = new Button.OnTooltip()
		{
			private final Minecraft mc = Minecraft.getInstance();
			private final Component text = displayText;
			
			public void onTooltip(Button button, PoseStack matrixStack, int mouseX, int mouseY)
			{
				if(button.active)
					parent.renderTooltip(matrixStack, mc.font.split(this.text, Math.max(parent.width / 2 - 43, 170)), mouseX, mouseY);
			};
			
			public void narrateTooltip(Consumer<Component> consumer)
			{
				consumer.accept(this.text);
			}
		};
		return new ImageButton(posX, posY, width, height, texXStart, texYStart, yHoverOffset, texture, 16, 32, onPress, tooltip, displayText);
	}
}
