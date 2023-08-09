package com.lying.circles.data.recipe;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class ImbueRecipe extends FunctionRecipe<ImbueRecipe> implements IEntityRecipe
{
	// Maximum number of steps to take away from origin in any direction whilst searching for in-world ingredients
	protected static final int SEARCH_RANGE = 2;
	
	public static final Map<ResourceLocation, Function<JsonObject, ImbueRecipe>> RECIPE_CONSTRUCTORS = new HashMap<>();
	
	protected ResourceLocation specialRecipeId = null;
	
	// In-world ingredients
	protected Block[] blockIngredients;
	protected BlockState[] blockStateIngredients;
	
	protected BlockState blockPlaced = null;
	protected EntityType<?> entitySpawned = null;
	protected CompoundTag entityData = null;
	protected CommandFunction.CacheableFunction onEntity = CommandFunction.CacheableFunction.NONE;
	
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
	
	public boolean matches(EnumSet<Element> elements, Level worldIn, BlockPos pos, Entity caster)
	{
		if(!super.matches(elements))
			return false;
		
		List<BlockPos> foundIngredients = Lists.newArrayList();
		if(blockStateIngredients.length > 0)
			for(BlockState block : blockStateIngredients)
			{
				BlockPos foundPos;
				if((foundPos = findBlockStateNearby(pos, worldIn, block, foundIngredients.toArray(new BlockPos[0]))) == null)
					return false;
				
				foundIngredients.add(foundPos);
			}
		
		if(blockIngredients.length > 0)
		{
			for(Block block : blockIngredients)
			{
				BlockPos foundPos;
				if((foundPos = findBlockNearby(pos, worldIn, block, foundIngredients.toArray(new BlockPos[0]))) == null)
					return false;
				
				foundIngredients.add(foundPos);
			}
		}
		
		return true;
	}
	
	/** Searches a 3x3x3 area around the given position for the given block, returns the first instance if any */
	@Nullable
	public static BlockPos findBlockNearby(BlockPos pos, Level worldIn, Block block, BlockPos... toIgnore)
	{
		return searchAreaNearby(pos, worldIn, (position, world) -> world.getBlockState(position).getBlock() == block, toIgnore);
	}
	
	/** Searches a 3x3x3 area around the given position for the given blockstate, returns the first instance if any */
	@Nullable
	public static BlockPos findBlockStateNearby(BlockPos pos, Level worldIn, BlockState blockState, BlockPos... toIgnore)
	{
		return searchAreaNearby(pos, worldIn, (position, world) -> world.getBlockState(position) == blockState, toIgnore);
	}
	
	@Nullable
	protected static BlockPos searchAreaNearby(BlockPos pos, Level worldIn, BiPredicate<BlockPos,Level> predicate, BlockPos... toIgnore)
	{
		for(int cubeRadius = 0; cubeRadius <= SEARCH_RANGE; cubeRadius++)
			if(cubeRadius == 0)
			{
				if(predicate.test(pos, worldIn))
					return pos;
			}
			else
			{
				// Length of the cube on any side
				int cubeSize = (cubeRadius * 2) + 1;
				
				// 2D Circumference of the cube
				int cubeCircumference = (cubeSize * 4) - 4;
				
				// Scan intermediate levels around the circumference only
				for(int index = 0; index < cubeSize - 2; index++)
				{
					// Alternating +/- of incrementing Y offset
					int yDiv = (int)Math.ceil(index * 0.5D);
					int yOff = index%2 == 0 ? -yDiv : yDiv;
					
					// Starting at these values creates a reliable spiral around the circumference
					Vec2 offset = new Vec2(cubeRadius, -cubeRadius + 1);
					Vec2 dir = new Vec2(1, 0);
					
					for(int i=0; i<cubeCircumference; i++)
					{
						int x = (int)offset.x;
						int z = (int)offset.y;
						
						BlockPos position = pos.offset(x, yOff, z);
						if(!arrayContainsPos(position, toIgnore) && predicate.test(position, worldIn))
							return position;
						
						if(x == z || (x < 0 && x == -z) || (x > 0 && x == 1-z))
							dir = new Vec2(-dir.y, dir.x);
						
						offset = offset.add(dir);
					}
				}
				
				// Total 2D footprint of the cube;
				int searchLen = cubeSize * cubeSize;
				
				// Check bottom level (ie. the floor)
				BlockPos resultBot = searchSpiralAround(pos.offset(0, -cubeRadius, 0), worldIn, searchLen, predicate, toIgnore);
				if(resultBot != null)
					return resultBot;
				
				// Check top level (ie. the ceiling)
				BlockPos resultTop = searchSpiralAround(pos.offset(0, cubeRadius, 0), worldIn, searchLen, predicate, toIgnore);
				if(resultTop != null)
					return resultTop;
			}
		
		return null;
	}
	
	@Nullable
	protected static BlockPos searchSpiralAround(BlockPos pos, Level worldIn, int len, BiPredicate<BlockPos,Level> predicate, BlockPos... toIgnore)
	{
		Vec2 offset = new Vec2(0, 0);
		Vec2 dir = new Vec2(0, -1);
		for(int i=0; i<len; i++)
		{
			int x = (int)offset.x;
			int z = (int)offset.y;
			
			BlockPos position = pos.offset(x, 0, z);
			if(!arrayContainsPos(position, toIgnore) && predicate.test(position, worldIn))
				return position;
			
			if(x == z || (x < 0 && x == -z) || (x > 0 && x == 1-z))
				dir = new Vec2(-dir.y, dir.x);
			
			offset = offset.add(dir);
		}
		return null;
	}
	
	private static boolean arrayContainsPos(BlockPos pos, BlockPos... toIgnore)
	{
		for(BlockPos position : toIgnore)
			if(position.distSqr(pos) == 0D)
				return true;
		return false;
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
	
	public void applyToCaster(Entity entity) { }
	
	public void consumeIngredients(Level worldIn, BlockPos pos)
	{
		if(blockStateIngredients.length > 0)
		{
			for(BlockState blockState : blockStateIngredients)
			{
				BlockPos position = findBlockStateNearby(pos, worldIn, blockState);
				if(position != null)
					worldIn.destroyBlock(position, false);
			}
		}
		
		if(blockIngredients.length > 0)
		{
			for(Block block : blockIngredients)
			{
				BlockPos position = findBlockNearby(pos, worldIn, block);
				if(position != null)
					worldIn.destroyBlock(position, false);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public JsonObject serialize()
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("ID", id.toString());
		obj.addProperty("Type", functionType.getId().toString());
		
		if(this.specialRecipeId != null)
			obj.addProperty("Special", this.specialRecipeId.toString());
		
		JsonObject ingredients = new JsonObject();
		
		if(elements.length > 0)
		{
			JsonArray elementSet = new JsonArray();
			for(Element element : elements)
				elementSet.add(element.getSerializedName());
			ingredients.add("Elements", elementSet);
		}
		
		// Store Blocks
		if(blockIngredients.length > 0)
		{
			JsonArray blockSet = new JsonArray();
			for(Block block : blockIngredients)
				blockSet.add(Registry.BLOCK.getKey(block).toString());
			ingredients.add("Blocks", blockSet);
		}
		
		// Store Block States
		if(blockStateIngredients.length > 0)
		{
			JsonArray blockStateSet = new JsonArray();
			for(BlockState blockState : blockStateIngredients)
				blockStateSet.add(NbtUtils.writeBlockState(blockState).toString());
			ingredients.add("BlockStates", blockStateSet);
		}
		
		obj.add("Ingredients", ingredients);
		
		toJson(obj);
		return obj;
	}
	
	public static ImbueRecipe deserialize(JsonObject obj)
	{
		ResourceLocation constructorID = null;
		if(obj.has("Special"))
			constructorID = new ResourceLocation(obj.get("Special").getAsString());
		
		ImbueRecipe recipe = 
				constructorID == null ? 
					generateFresh(obj) : 
					RECIPE_CONSTRUCTORS.getOrDefault(constructorID, ImbueRecipe::generateFresh).apply(obj);
		
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
	
	protected static ImbueRecipe generateFresh(JsonObject obj)
	{
		return new ImbueRecipe(new ResourceLocation(obj.get("ID").getAsString()), loadElements(obj), loadBlocks(obj), loadBlockStates(obj));
	}
	
	protected static Element[] loadElements(JsonObject obj)
	{
		List<Element> elements = Lists.newArrayList();
		JsonObject ingredients = obj.getAsJsonObject("Ingredients");
		if(!ingredients.has("Elements"))
			return new Element[0];
		
		JsonArray elementJson = ingredients.get("Elements").getAsJsonArray();
		for(int i=0; i<elementJson.size(); i++)
		{
			Element element = Element.fromString(elementJson.get(i).getAsString());
			if(element != null)
				elements.add(element);
		}
		return elements.toArray(new Element[0]);
	}
	
	@SuppressWarnings("deprecation")
	protected static Block[] loadBlocks(JsonObject obj)
	{
		List<Block> states = Lists.newArrayList();
		JsonObject ingredients = obj.getAsJsonObject("Ingredients");
		if(!ingredients.has("Blocks"))
			return new Block[0];
		
		JsonArray blockJson = ingredients.get("Blocks").getAsJsonArray();
		for(int i=0; i<blockJson.size(); i++)
		{
			Block block = null;
			try
			{
				block = Registry.BLOCK.get(new ResourceLocation(blockJson.get(i).getAsString()));
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
		JsonObject ingredients = obj.getAsJsonObject("Ingredients");
		if(!ingredients.has("BlockStates"))
			return new BlockState[0];
		
		JsonArray blockStatesJson = ingredients.get("BlockStates").getAsJsonArray();
		for(int i=0; i<blockStatesJson.size(); i++)
		{
			BlockState state = null;
			try
			{
				state = NbtUtils.readBlockState(TagParser.parseTag(blockStatesJson.get(i).getAsString()));
			}
			catch(Exception e) { }
			if(state != null)
				states.add(state);
		}
		return states.toArray(new BlockState[0]);
	}
	
	static
	{
		RECIPE_CONSTRUCTORS.put(LichImbueRecipe.RECIPE_ID, LichImbueRecipe::generateNew);
	}
}