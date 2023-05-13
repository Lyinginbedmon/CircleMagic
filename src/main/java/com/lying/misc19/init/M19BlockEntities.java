package com.lying.misc19.init;

import com.lying.misc19.blocks.entity.PhantomBlockEntity;
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
}
