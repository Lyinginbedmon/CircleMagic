package com.lying.misc19.init;

import com.lying.misc19.blocks.PhantomBlock;
import com.lying.misc19.blocks.Sandbox;
import com.lying.misc19.reference.Reference;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class M19Blocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.ModInfo.MOD_ID);
    
    public static final RegistryObject<Block> SANDBOX = BLOCKS.register("sandbox", () -> new Sandbox(BlockBehaviour.Properties.of(Material.WOOD).noOcclusion()));
    public static final RegistryObject<Block> PHANTOM_CUBE = BLOCKS.register("phantom_cube", () -> new PhantomBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().strength(-1.0F, 3600000.0F).noLootTable()));
    
    public static void init() { }
}
