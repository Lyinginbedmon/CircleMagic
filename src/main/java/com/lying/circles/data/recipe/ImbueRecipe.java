package com.lying.circles.data.recipe;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.circles.init.FunctionRecipes;
import com.lying.circles.magic.Element;
import com.lying.circles.reference.Reference;

import net.minecraft.commands.CommandFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ImbueRecipe extends FunctionRecipe<ImbueRecipe>
{
	private Block[] blockIngredients;
	private BlockState[] blockStateIngredients;
	
	private BlockState blockPlaced = null;
	private EntityType<?> entitySpawned = null;
	private CompoundTag entityData = null;
	private CommandFunction.CacheableFunction onEntity = CommandFunction.CacheableFunction.NONE;
	
	protected ImbueRecipe(String idIn, Element[] elementsIn, Block[] blocksIn, BlockState[] statesIn)
	{
		super(new ResourceLocation(Reference.ModInfo.MOD_ID, "imbue/"+idIn), FunctionRecipes.IMBUE, elementsIn);
		this.blockIngredients = blocksIn;
		this.blockStateIngredients = statesIn;
	}
	
	protected ImbueRecipe(ResourceLocation idIn, Element[] elementsIn, Block[] blocksIn, BlockState[] statesIn)
	{
		super(idIn, FunctionRecipes.IMBUE, elementsIn);
		this.blockIngredients = blocksIn;
		this.blockStateIngredients = statesIn;
	}
	
	public ImbueRecipe(String idIn, BlockState stateIn, Element[] elementsIn, Block[] blocksIn, BlockState[] statesIn)
	{
		this(idIn, elementsIn, blocksIn, statesIn);
		this.blockPlaced = stateIn;
	}
	
	public ImbueRecipe(String idIn, EntityType<?> typeIn, Element[] elementsIn, Block[] blocksIn, BlockState[] statesIn)
	{
		this(idIn, elementsIn, blocksIn, statesIn);
		this.entitySpawned = typeIn;
		this.entityData = new CompoundTag();
	}
	
	public ImbueRecipe(String idIn, EntityType<?> typeIn, CompoundTag dataIn, Element[] elementsIn, Block[] blocksIn, BlockState[] statesIn)
	{
		this(idIn, elementsIn, blocksIn, statesIn);
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
	
	public boolean matches(EnumSet<Element> elements, Level worldIn, BlockPos pos)
	{
		if(!super.matches(elements))
			return false;
		
		if(blockIngredients.length > 0)
		{
			for(Block block : blockIngredients)
			{
				boolean blockFound = false;
				
				for(int y=-3; y<3; y++)
					for(int x=-3; x<3; x++)
						for(int z=-3; z<3; z++)
						{
							BlockPos position = pos.offset(x, y, z);
							if(worldIn.getBlockState(position).getBlock() == block)
							{
								blockFound = true;
								break;
							}
						}
				
				if(!blockFound)
					return false;
			}
		}
		
		if(blockStateIngredients.length > 0)
		{
			for(BlockState block : blockStateIngredients)
			{
				boolean blockFound = false;
				
				for(int y=-3; y<3; y++)
					for(int x=-3; x<3; x++)
						for(int z=-3; z<3; z++)
						{
							BlockPos position = pos.offset(x, y, z);
							if(worldIn.getBlockState(position) == block)
							{
								blockFound = true;
								break;
							}
						}
				
				if(!blockFound)
					return false;
			}
		}
		
		return true;
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
	
	public void consumeIngredients(Level worldIn, BlockPos pos)
	{
		if(blockStateIngredients.length > 0)
		{
			for(BlockState block : blockStateIngredients)
			{
				boolean found = false;
				for(int y=-3; y<3; y++)
				{
					if(found) break;
					for(int x=-3; x<3; x++)
					{
						if(found) break;
						for(int z=-3; z<3; z++)
						{
							BlockPos position = pos.offset(x, y, z);
							if(worldIn.getBlockState(position) == block)
							{
								worldIn.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
								found = true;
								break;
							}
						}
					}
				}
			}
		}
		
		if(blockIngredients.length > 0)
		{
			for(Block block : blockIngredients)
			{
				boolean found = false;
				for(int y=-3; y<3; y++)
				{
					if(found) break;
					for(int x=-3; x<3; x++)
					{
						if(found) break;
						for(int z=-3; z<3; z++)
						{
							BlockPos position = pos.offset(x, y, z);
							if(worldIn.getBlockState(position).getBlock() == block)
							{
								worldIn.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
								found = true;
								break;
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public JsonObject serialize()
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("ID", id.toString());
		obj.addProperty("Type", functionType.getId().toString());
		
		JsonObject ingredients = new JsonObject();
		
		JsonArray elementSet = new JsonArray();
		for(Element element : elements.toArray(new Element[0]))
			elementSet.add(element.getSerializedName());
		ingredients.add("Elements", elementSet);
		
		// Store Blocks
		JsonArray blockSet = new JsonArray();
		for(Block block : blockIngredients)
			blockSet.add(Registry.BLOCK.getKey(block).toString());
		ingredients.add("Blocks", blockSet);
		
		// Store Block States
		JsonArray blockStateSet = new JsonArray();
		for(BlockState blockState : blockStateIngredients)
			blockStateSet.add(NbtUtils.writeBlockState(blockState).toString());
		ingredients.add("BlockStates", blockStateSet);
		
		obj.add("Ingredients", ingredients);
		
		toJson(obj);
		return obj;
	}
	
	public static ImbueRecipe deserialize(JsonObject obj)
	{
		ImbueRecipe recipe = new ImbueRecipe(new ResourceLocation(obj.get("ID").getAsString()), loadElements(obj), loadBlocks(obj), loadBlockStates(obj));
		
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
	
	protected static Element[] loadElements(JsonObject obj)
	{
		EnumSet<Element> elements = EnumSet.noneOf(Element.class);
		JsonArray ingredients = obj.get("Ingredients").getAsJsonObject().get("Elements").getAsJsonArray();
		for(int i=0; i<ingredients.size(); i++)
		{
			Element element = Element.fromString(ingredients.get(i).getAsString());
			if(element != null)
				elements.add(element);
		}
		return elements.toArray(new Element[0]);
	}
	
	@SuppressWarnings("deprecation")
	protected static Block[] loadBlocks(JsonObject obj)
	{
		List<Block> states = Lists.newArrayList();
		JsonArray ingredients = obj.get("Ingredients").getAsJsonObject().get("Blocks").getAsJsonArray();
		for(int i=0; i<ingredients.size(); i++)
		{
			Block block = null;
			try
			{
				block = Registry.BLOCK.get(new ResourceLocation(ingredients.get(i).getAsString()));
			}
			catch(Exception e) { }
			if(block != null)
				states.add(block);
		}
		return states.toArray(new Block[0]);
	}
	
	protected static BlockState[] loadBlockStates(JsonObject obj)
	{
		List<BlockState> states = Lists.newArrayList();
		JsonArray ingredients = obj.get("Ingredients").getAsJsonObject().get("BlockStates").getAsJsonArray();
		for(int i=0; i<ingredients.size(); i++)
		{
			BlockState state = null;
			try
			{
				state = NbtUtils.readBlockState(TagParser.parseTag(ingredients.get(i).getAsString()));
			}
			catch(Exception e) { }
			if(state != null)
				states.add(state);
		}
		return states.toArray(new BlockState[0]);
	}
}