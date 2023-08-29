package com.lying.circles.data;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.lying.circles.reference.Reference;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class CMDataGenerators
{
	public static void onGatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper fileHelper = event.getExistingFileHelper();
		generator.addProvider(event.includeServer(), new CMBlockLootProvider(generator, fileHelper));
		generator.addProvider(event.includeServer(), new CMBlockTags(generator, fileHelper));
		generator.addProvider(event.includeServer(), new CMItemTags(generator, fileHelper));
		generator.addProvider(event.includeServer(), new CMEntityTags(generator, fileHelper));
		generator.addProvider(event.includeServer(), new CMFunctionRecipeProvider(generator, fileHelper));
		generator.addProvider(event.includeServer(), new CMPlacedFeatureTagProvider(generator, fileHelper));
		
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());
		HolderSet.Named<Biome> OVERWORLD_TAG = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY).orElseThrow(), BiomeTags.IS_OVERWORLD);
		Map<ResourceLocation, BiomeModifier> biomeModifiers = new HashMap<>();
		HolderSet<PlacedFeature> STATUES = new HolderSet.Named<>(ops.registry(Registry.PLACED_FEATURE_REGISTRY).orElseThrow(), CMPlacedFeatureTagProvider.STATUES);
		biomeModifiers.put(new ResourceLocation(Reference.ModInfo.MOD_ID, "curruid_statues"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(OVERWORLD_TAG, STATUES, GenerationStep.Decoration.UNDERGROUND_STRUCTURES));
		generator.addProvider(event.includeServer(), JsonCodecProvider.forDatapackRegistry(generator, fileHelper, Reference.ModInfo.MOD_ID, ops, ForgeRegistries.Keys.BIOME_MODIFIERS, biomeModifiers));
	}
}
