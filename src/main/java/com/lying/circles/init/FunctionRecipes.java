package com.lying.circles.init;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lying.circles.CircleMagic;
import com.lying.circles.data.recipe.CreationRecipe;
import com.lying.circles.data.recipe.FunctionRecipe;
import com.lying.circles.data.recipe.ImbueRecipe;
import com.lying.circles.data.recipe.StatusEffectRecipe;
import com.lying.circles.magic.Element;
import com.lying.circles.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class FunctionRecipes extends SimpleJsonResourceReloadListener
{
	public static final DeferredRegister<Function<JsonObject, FunctionRecipe<?>>> REGISTRY = DeferredRegister.create(FunctionRecipe.REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	public static final Supplier<IForgeRegistry<Function<JsonObject, FunctionRecipe<?>>>> RECIPE_REGISTRY	= REGISTRY.makeRegistry(() -> (new RegistryBuilder<Function<JsonObject, FunctionRecipe<?>>>()).hasTags());
	
	public static final RegistryObject<Function<JsonObject, FunctionRecipe<?>>> CREATION = REGISTRY.register("creation_function", () -> CreationRecipe::deserialize);
	public static final RegistryObject<Function<JsonObject, FunctionRecipe<?>>> STATUS_EFFECT = REGISTRY.register("status_effect_function", () -> StatusEffectRecipe::deserialize);
	public static final RegistryObject<Function<JsonObject, FunctionRecipe<?>>> IMBUE = REGISTRY.register("imbue_function", () -> ImbueRecipe::deserialize);
	
	private static FunctionRecipes instance;
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	
	private final Map<ResourceLocation, Map<ResourceLocation, FunctionRecipe<?>>> recipeMap = new HashMap<>();
	
	public FunctionRecipes()
	{
		super(GSON, "function_recipes");
	}
	
	public static FunctionRecipes getInstance()
	{
		if(instance == null)
			instance = new FunctionRecipes();
		return instance;
	}
	
	@Nullable
	private static RegistryObject<Function<JsonObject, FunctionRecipe<?>>> getConstructor(ResourceLocation location)
	{
		for(RegistryObject<Function<JsonObject, FunctionRecipe<?>>> entry : REGISTRY.getEntries())
			if(entry.getId().equals(location) && entry.isPresent())
				return entry;
		return null;
	}
	
	@Nullable
	public FunctionRecipe<?> getMatchingRecipe(EnumSet<Element> elements, RegistryObject<Function<JsonObject, FunctionRecipe<?>>> registry)
	{
		for(FunctionRecipe<?> recipe : recipeMap.getOrDefault(registry.getId(), new HashMap<>()).values())
			if(recipe.matches(elements))
				return recipe;
		return null;
	}
	
	public Collection<FunctionRecipe<?>> getRecipes(RegistryObject<Function<JsonObject, FunctionRecipe<?>>> registry)
	{
		return recipeMap.getOrDefault(registry.getId(), new HashMap<>()).values();
	}
	
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager manager, ProfilerFiller filler)
	{
		CircleMagic.LOG.info("Attempting to load function recipes from data, entries: "+objectIn.size());
		getInstance().recipeMap.clear();
		objectIn.forEach((name, json) -> {
            try
            {
            	JsonObject obj = json.getAsJsonObject();
            	if(!obj.has("Type"))
            		throw new JsonParseException("No function type specified");
            	
            	ResourceLocation typeID = new ResourceLocation(obj.get("Type").getAsString());
            	RegistryObject<Function<JsonObject, FunctionRecipe<?>>> functionType = getConstructor(typeID);
            	if(functionType == null)
            		throw new IllegalArgumentException();
            	
            	FunctionRecipe<?> recipe = functionType.get().apply(obj);
            	if(recipe == null)
            		throw new Exception("Function recipe deserializer returned null");
            	
            	Map<ResourceLocation, FunctionRecipe<?>> entries = getInstance().recipeMap.getOrDefault(typeID, new HashMap<>());
            	entries.put(recipe.getId(), recipe);
            	getInstance().recipeMap.put(typeID, entries);
            }
            catch (IllegalArgumentException | JsonParseException e)
            {
            	CircleMagic.LOG.error("Failed to load function recipe {}: {}", name, e.getMessage());
            }
            catch(Exception e)
            {
            	CircleMagic.LOG.error("Unrecognised error loading function recipe {}", name, e.getMessage());
            }
        });
		
		if(getInstance().recipeMap.isEmpty())
			CircleMagic.LOG.warn("No function recipes found, is this intended?");
		else
			for(Entry<ResourceLocation, Map<ResourceLocation, FunctionRecipe<?>>> entry : getInstance().recipeMap.entrySet())
			{
				CircleMagic.LOG.info("# For recipe type "+entry.getKey().getPath()+":");
				entry.getValue().values().forEach((recipe) -> CircleMagic.LOG.info("# * "+recipe.getId().getPath()+" of class ["+recipe.getClass().getSimpleName()+"]"));
			}
	}
	
}
