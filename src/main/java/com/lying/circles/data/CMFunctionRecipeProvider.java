package com.lying.circles.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Sets;
import com.lying.circles.CircleMagic;
import com.lying.circles.data.recipe.CreationRecipe;
import com.lying.circles.data.recipe.FunctionRecipe;
import com.lying.circles.data.recipe.StatusEffectRecipe;
import com.lying.circles.magic.Element;
import com.lying.circles.reference.Reference;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CMFunctionRecipeProvider implements DataProvider
{
	private final DataGenerator.PathProvider pathProvider;
	protected ExistingFileHelper fileHelper;
	
	public CMFunctionRecipeProvider(DataGenerator generatorIn, ExistingFileHelper fileHelperIn)
	{
		this.pathProvider = generatorIn.createPathProvider(DataGenerator.Target.DATA_PACK, "function_recipes");
		this.fileHelper = fileHelperIn;
	}
	
	public String getName()
	{
		return "Circle Magic function recipes";
	}
	
	public void run(CachedOutput cache) throws IOException
	{
		List<FunctionRecipe<?>> recipes = getRecipes();
		
		Set<String> set = Sets.newHashSet();
		Consumer<FunctionRecipe<?>> consumer = (recipe) ->
		{
			if(!set.add(recipe.getId().toString()))
				throw new IllegalStateException("Duplicate function recipe of name "+recipe.getId().toString());
			else
			{
				Path path = this.pathProvider.json(recipe.getId());
				try
				{
					DataProvider.saveStable(cache, recipe.serialize(), path);
				}
				catch(IOException e)
				{
					CircleMagic.LOG.error("Couldn't save function recipe {}", path, e);
				}
			}
		};
		
		recipes.forEach((recipe) -> consumer.accept(recipe));
	}
	
	private List<FunctionRecipe<?>> getRecipes()
	{
		List<FunctionRecipe<?>> recipes = Lists.newArrayList();
		addCreationRecipes(recipes);
		addStatusEffectRecipes(recipes);
		return recipes;
	}
	
	private void addCreationRecipes(List<FunctionRecipe<?>> list)
	{
		list.add(new CreationRecipe("stone", Blocks.STONE.defaultBlockState(), Element.MUNDUS));
		list.add(new CreationRecipe("fire", Blocks.FIRE.defaultBlockState(), Element.ARDERE));
		list.add(new CreationRecipe("water", Blocks.WATER.defaultBlockState(), Element.MARE));
		list.add(new CreationRecipe("sculk", Blocks.SCULK.defaultBlockState(), Element.SCULK));
		list.add(new CreationRecipe("grass", Blocks.GRASS_BLOCK.defaultBlockState(), Element.ORIGO));
		list.add(new CreationRecipe("end_stone", Blocks.END_STONE.defaultBlockState(), Element.FINIS));
		list.add(new CreationRecipe("lightning", EntityType.LIGHTNING_BOLT, Element.FINIS, Element.ARDERE, Element.MUNDUS));
	}
	
	private void addStatusEffectRecipes(List<FunctionRecipe<?>> list)
	{
		list.add(new StatusEffectRecipe("fire_resistance", new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.ARDERE, Element.MARE));
		list.add(new StatusEffectRecipe("dig_speed", new MobEffectInstance(MobEffects.DIG_SPEED, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.ARDERE, Element.FINIS));
		list.add(new StatusEffectRecipe("invisibility", new MobEffectInstance(MobEffects.INVISIBILITY, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.ARDERE, Element.SCULK));
		list.add(new StatusEffectRecipe("damage_boost", new MobEffectInstance(MobEffects.DAMAGE_BOOST, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.ARDERE, Element.MUNDUS));
		list.add(new StatusEffectRecipe("jump", new MobEffectInstance(MobEffects.JUMP, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.MUNDUS, Element.FINIS));
		list.add(new StatusEffectRecipe("movement_slowdown", new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.SCULK, Element.FINIS));
		list.add(new StatusEffectRecipe("weakness", new MobEffectInstance(MobEffects.WEAKNESS, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.SCULK, Element.MUNDUS));
		list.add(new StatusEffectRecipe("damage_resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.MUNDUS));
		list.add(new StatusEffectRecipe("movement_speed", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.FINIS));
		list.add(new StatusEffectRecipe("water_breathing", new MobEffectInstance(MobEffects.WATER_BREATHING, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.MARE));
		list.add(new StatusEffectRecipe("regeneration", new MobEffectInstance(MobEffects.REGENERATION, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.ORIGO));
		list.add(new StatusEffectRecipe("wither", new MobEffectInstance(MobEffects.WITHER, Reference.Values.TICKS_PER_SECOND * 30, 0, false, false), Element.SCULK, Element.ORIGO));
	}
}
