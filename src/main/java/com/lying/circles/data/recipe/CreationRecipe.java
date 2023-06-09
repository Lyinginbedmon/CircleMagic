package com.lying.circles.data.recipe;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.lying.circles.init.FunctionRecipes;
import com.lying.circles.magic.Element;
import com.lying.circles.reference.Reference;

import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CreationRecipe extends FunctionRecipe<CreationRecipe>
{
	private BlockState blockPlaced = null;
	private EntityType<?> entitySpawned = null;
	private CompoundTag entityData = null;
	private CommandFunction.CacheableFunction onEntity = CommandFunction.CacheableFunction.NONE;
	
	protected CreationRecipe(String idIn, Element... elementsIn)
	{
		super(new ResourceLocation(Reference.ModInfo.MOD_ID, "creation/"+idIn), FunctionRecipes.CREATION, elementsIn);
	}
	
	protected CreationRecipe(ResourceLocation idIn, Element... elementsIn)
	{
		super(idIn, FunctionRecipes.CREATION, elementsIn);
	}
	
	public CreationRecipe(String idIn, BlockState stateIn, Element... elements)
	{
		this(idIn, elements);
		this.blockPlaced = stateIn;
	}
	
	public CreationRecipe(String idIn, EntityType<?> typeIn, Element... elements)
	{
		this(idIn, elements);
		this.entitySpawned = typeIn;
		this.entityData = new CompoundTag();
	}
	
	public CreationRecipe(String idIn, EntityType<?> typeIn, CompoundTag dataIn, Element... elements)
	{
		this(idIn, typeIn, elements);
		this.entityData = dataIn;
	}
	
	public void toJson(JsonObject obj)
	{
		if(blockPlaced != null)
			obj.addProperty("Block", NbtUtils.writeBlockState(blockPlaced).toString());
		
		if(entitySpawned != null)
		{
			obj.addProperty("EntityType", EntityType.getKey(entitySpawned).toString());
			
			if(!entityData.isEmpty())
				obj.addProperty("CustomData", entityData.toString());
		}
		
		if(onEntity != null && onEntity != CommandFunction.CacheableFunction.NONE)
			obj.addProperty("Function", onEntity.getId().toString());
	}
	
	public BlockState getState() { return this.blockPlaced; }
	
	public EntityType<?> getEntity() { return this.entitySpawned; }
	
	public boolean hasEntity() { return entitySpawned != null; }
	
	@Nullable
	public Entity createEntityAt(Level world, Vec3 pos)
	{
		if(this.entitySpawned == null)
			return null;
		
		CompoundTag spawnData = entityData == null ? new CompoundTag() : entityData.copy();
		spawnData.putString("id", EntityType.getKey(entitySpawned).toString());
		
		ServerLevel level = (ServerLevel)world;
		return EntityType.loadEntityRecursive(spawnData, level, (entity) -> 
		{
            entity.moveTo(pos.x, pos.y, pos.z, entity.getYRot(), entity.getXRot());
            return entity;
		});
	}
	
	public CommandFunction.CacheableFunction getFunction() { return this.onEntity; }
	
	public static CreationRecipe deserialize(JsonObject obj)
	{
		CreationRecipe recipe = new CreationRecipe(new ResourceLocation(obj.get("ID").getAsString()), loadElements(obj));
		
		if(obj.has("Block"))
		{
			try
			{
				recipe.blockPlaced = NbtUtils.readBlockState(TagParser.parseTag(obj.get("Block").getAsString()));
			}
			catch(Exception e) { }
		}
		
		if(obj.has("EntityType"))
		{
			Optional<EntityType<?>> type = EntityType.byString(obj.get("EntityType").getAsString());
			if(type != null && type.isPresent())
				recipe.entitySpawned = type.get();
			
			recipe.entityData = new CompoundTag();
			if(obj.has("CustomData"))
			{
				try
				{
					recipe.entityData = TagParser.parseTag(obj.get("CustomData").getAsString());
				}
				catch(Exception e) { }
			}
		}
		
		if(obj.has("Function"))
			recipe.onEntity = new CommandFunction.CacheableFunction(new ResourceLocation(GsonHelper.getAsString(obj, "Function")));
		
		return recipe;
	}
}