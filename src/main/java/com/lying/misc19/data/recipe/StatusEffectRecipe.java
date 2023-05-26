package com.lying.misc19.data.recipe;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.misc19.init.FunctionRecipes;
import com.lying.misc19.magic.Element;
import com.lying.misc19.reference.Reference;

import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectInstance;

public class StatusEffectRecipe extends FunctionRecipe<StatusEffectRecipe>
{
	List<MobEffectInstance> mobEffects = Lists.newArrayList();
	CommandFunction.CacheableFunction onEntity = CommandFunction.CacheableFunction.NONE;
	
	protected StatusEffectRecipe(String idIn, Element... elementsIn)
	{
		super(new ResourceLocation(Reference.ModInfo.MOD_ID, "status_effect/"+idIn), FunctionRecipes.STATUS_EFFECT, elementsIn);
	}
	
	protected StatusEffectRecipe(ResourceLocation idIn, Element... elementsIn)
	{
		super(idIn, FunctionRecipes.STATUS_EFFECT, elementsIn);
	}
	
	public StatusEffectRecipe(String idIn, List<MobEffectInstance> mobEffectsIn, Element... elements)
	{
		this(idIn, elements);
		this.mobEffects = mobEffectsIn;
	}
	
	public StatusEffectRecipe(String idIn, MobEffectInstance mobEffectIn, Element... elements)
	{
		this(idIn, List.of(mobEffectIn), elements);
	}
	
	public void toJson(JsonObject obj)
	{
		if(!mobEffects.isEmpty())
		{
			JsonArray effects = new JsonArray();
			this.mobEffects.forEach((effect) -> effects.add(effect.save(new CompoundTag()).toString()));
			obj.add("Effects", effects);
		}
		
		if(onEntity != null && onEntity != CommandFunction.CacheableFunction.NONE)
			obj.addProperty("Function", onEntity.getId().toString());
	}
	
	public List<MobEffectInstance> getEffects()
	{
		List<MobEffectInstance> effects = Lists.newArrayList();
		this.mobEffects.forEach((effect) -> effects.add(MobEffectInstance.load(effect.save(new CompoundTag()))));
		return effects;
	}
	
	public CommandFunction.CacheableFunction getFunction() { return this.onEntity; }
	
	public static StatusEffectRecipe deserialize(JsonObject obj)
	{
		StatusEffectRecipe recipe = new StatusEffectRecipe(new ResourceLocation(obj.get("ID").getAsString()), loadElements(obj));
		
		if(obj.has("Effects"))
		{
			JsonArray effects = obj.get("Effects").getAsJsonArray();
			for(int i=0; i<effects.size(); i++)
			{
				try
				{
					CompoundTag nbt = TagParser.parseTag(effects.get(i).getAsString());
					recipe.mobEffects.add(MobEffectInstance.load(nbt));
				}
				catch(Exception e) { }
			}
		}
		
		if(obj.has("Function"))
			recipe.onEntity = new CommandFunction.CacheableFunction(new ResourceLocation(GsonHelper.getAsString(obj, "Function")));
		
		return recipe;
	}
}