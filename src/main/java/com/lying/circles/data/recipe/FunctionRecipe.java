package com.lying.circles.data.recipe;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.compress.utils.Lists;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.circles.magic.Element;
import com.lying.circles.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public abstract class FunctionRecipe<T>
{
	public static final ResourceKey<Registry<Function<JsonObject, FunctionRecipe<?>>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "function_recipes"));
	
	protected final RegistryObject<Function<JsonObject, FunctionRecipe<?>>> functionType;
	protected final ResourceLocation id;
	protected final Element[] elements;	// FIXME Allow for repeats of elements
	
	protected FunctionRecipe(ResourceLocation idIn, RegistryObject<Function<JsonObject, FunctionRecipe<?>>> typeIn, Element... elementsIn)
	{
		this.id = idIn;
		this.functionType = typeIn;
		this.elements = elementsIn;
	}
	
	public ResourceLocation getId() { return this.id; }
	
	public boolean matches(EnumSet<Element> elementsIn)
	{
		List<Element> supplied = Lists.newArrayList();
		elementsIn.forEach((element) -> supplied.add(element));
		for(Element element : elements)
		{
			if(supplied.contains(element))
				supplied.remove(element);
			else
				return false;
		}
		return true;
	}
	
	/** Stores the recipe to JSON */
	public JsonObject serialize()
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("ID", id.toString());
		obj.addProperty("Type", functionType.getId().toString());
		
		JsonArray ingredients = new JsonArray();
		for(Element element : elements)
			ingredients.add(element.getSerializedName());
		obj.add("Ingredients", ingredients);
		
		toJson(obj);
		return obj;
	}
	
	/** Stores class-specific information to JSON */
	public abstract void toJson(JsonObject obj);
	
	protected static Element[] loadElements(JsonObject obj)
	{
		JsonArray ingredients = obj.get("Ingredients").getAsJsonArray();
		Element[] elements = new Element[ingredients.size()];
		for(int i=0; i<ingredients.size(); i++)
		{
			Element element = Element.fromString(ingredients.get(i).getAsString());
			if(element != null)
				elements[i] = element;
		}
		return elements;
	}
}