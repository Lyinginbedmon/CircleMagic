package com.lying.misc19.init;

import com.lying.misc19.blocks.entity.*;
import com.lying.misc19.reference.Reference;

import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class M19BlockEntities
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.ModInfo.MOD_ID);
	
	public static final RegistryObject<BlockEntityType<PhantomBlockEntity>> PHANTOM_CUBE = BLOCK_ENTITIES.register("phantom_cube", () -> BlockEntityType.Builder.of(PhantomBlockEntity::new, M19Blocks.PHANTOM_CUBE.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "phantom_cube")));
	public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, M19Blocks.CRUCIBLE.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "crucible")));
	public static final RegistryObject<BlockEntityType<FairyJarBlockEntity>> FAIRY_JAR = BLOCK_ENTITIES.register("fairy_jar", () -> BlockEntityType.Builder.of(FairyJarBlockEntity::new, M19Blocks.FAIRY_JAR.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "fairy_jar")));
	
	public static final RegistryObject<BlockEntityType<InscriptionBlockEntity>> INSCRIPTION = BLOCK_ENTITIES.register("inscribed", () -> BlockEntityType.Builder.of(InscriptionBlockEntity::new, 
			M19Blocks.INSCRIBED_ACACIA.get(),
			M19Blocks.INSCRIBED_BIRCH.get(),
			M19Blocks.INSCRIBED_CRIMSON.get(),
			M19Blocks.INSCRIBED_DARK_OAK.get(),
			M19Blocks.INSCRIBED_JUNGLE.get(),
			M19Blocks.INSCRIBED_MANGROVE.get(),
			M19Blocks.INSCRIBED_OAK.get(),
			M19Blocks.INSCRIBED_SPRUCE.get(),
			M19Blocks.INSCRIBED_STONE.get(),
			M19Blocks.INSCRIBED_WARPED.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "inscribed")));
}
