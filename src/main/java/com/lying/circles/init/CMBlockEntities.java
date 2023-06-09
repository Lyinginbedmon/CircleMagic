package com.lying.circles.init;

import com.lying.circles.blocks.entity.*;
import com.lying.circles.reference.Reference;

import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CMBlockEntities
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.ModInfo.MOD_ID);
	
	public static final RegistryObject<BlockEntityType<PhantomBlockEntity>> PHANTOM_CUBE = BLOCK_ENTITIES.register("phantom_cube", () -> BlockEntityType.Builder.of(PhantomBlockEntity::new, CMBlocks.PHANTOM_CUBE.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "phantom_cube")));
	public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, CMBlocks.CRUCIBLE.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "crucible")));
	public static final RegistryObject<BlockEntityType<FairyJarBlockEntity>> FAIRY_JAR = BLOCK_ENTITIES.register("fairy_jar", () -> BlockEntityType.Builder.of(FairyJarBlockEntity::new, CMBlocks.FAIRY_JAR.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "fairy_jar")));
	public static final RegistryObject<BlockEntityType<MagicTreeBlockEntity>> MAGIC_TREE = BLOCK_ENTITIES.register("magecrafted_bough", () -> BlockEntityType.Builder.of(MagicTreeBlockEntity::new, CMBlocks.MAGIC_TREE.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "magecrafted_bough")));
	
	public static final RegistryObject<BlockEntityType<InscribedBlockEntity>> INSCRIPTION = BLOCK_ENTITIES.register("inscribed", () -> BlockEntityType.Builder.of(InscribedBlockEntity::new, 
			CMBlocks.INSCRIBED_ACACIA.get(),
			CMBlocks.INSCRIBED_BIRCH.get(),
			CMBlocks.INSCRIBED_CRIMSON.get(),
			CMBlocks.INSCRIBED_DARK_OAK.get(),
			CMBlocks.INSCRIBED_JUNGLE.get(),
			CMBlocks.INSCRIBED_MANGROVE.get(),
			CMBlocks.INSCRIBED_OAK.get(),
			CMBlocks.INSCRIBED_SPRUCE.get(),
			CMBlocks.INSCRIBED_STONE.get(),
			CMBlocks.INSCRIBED_WARPED.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "inscribed")));
}
