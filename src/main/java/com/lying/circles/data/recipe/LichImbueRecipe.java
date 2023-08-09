package com.lying.circles.data.recipe;

import java.util.EnumSet;

import com.google.gson.JsonObject;
import com.lying.circles.capabilities.PlayerData;
import com.lying.circles.init.CMItems;
import com.lying.circles.magic.Element;
import com.lying.circles.reference.Reference;

import net.minecraft.commands.CommandFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LichImbueRecipe extends ImbueRecipe
{
	public static final ResourceLocation RECIPE_ID = new ResourceLocation("lich_ritual");
	
	public LichImbueRecipe(String idIn, Element[] elementsIn, Block[] blocksIn, BlockState[] statesIn)
	{
		this(new ResourceLocation(Reference.ModInfo.MOD_ID, "imbue/"+idIn), elementsIn, blocksIn, statesIn);
	}
	
	public LichImbueRecipe(ResourceLocation idIn, Element[] elementsIn, Block[] blocksIn, BlockState[] statesIn)
	{
		super(idIn, elementsIn, blocksIn, statesIn);
		this.specialRecipeId = RECIPE_ID;
	}
	
	protected static ImbueRecipe generateNew(JsonObject obj)
	{
		return new LichImbueRecipe(new ResourceLocation(obj.get("ID").getAsString()), loadElements(obj), loadBlocks(obj), loadBlockStates(obj));
	}
	
	public void toJson(JsonObject obj)
	{
		if(onEntity != null && onEntity != CommandFunction.CacheableFunction.NONE)
			obj.addProperty("Function", onEntity.getId().toString());
	}
	
	public boolean matches(EnumSet<Element> elements, Level worldIn, BlockPos pos, Entity caster)
	{
		return super.matches(elements, worldIn, pos, caster) && caster.distanceToSqr(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) < (Math.sqrt(SEARCH_RANGE) + 1);
	}
	
	public void applyToCaster(Entity entity)
	{
		if(entity.getType() == EntityType.PLAYER && !PlayerData.isLich(entity))
		{
			Player player = (Player)entity;
			PlayerData data = PlayerData.getCapability(player);
			data.setIsLich(true);
			
			player.getInventory().add(new ItemStack(CMItems.LICH_SKULL.get()));
		}
	}
}
