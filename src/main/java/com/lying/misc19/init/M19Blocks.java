package com.lying.misc19.init;

import com.lying.misc19.blocks.*;
import com.lying.misc19.reference.Reference;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    
    /**
     * KEY QUESTIONS:
     *  * How do we want to limit spell creation based on the scribing area?
     *  * What do we want dedicated inscription areas to look like?
     * 
     * Main scribing area
     * 	* Where the editor is accessed
     *  * Stores an arrangement until it is inscribed
     *  * Displays the held arrangement in the world
     *  * Can inscribe scrolls, staves, baubles, etc.
     * Expansion blocks to the main scribing block
     * 	* How do they affect the editor?
     *  * What visual benefit do that give?
     *  * Can they be provided in multiple visual forms?
     */
    
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register("crucible", () -> new Crucible(BlockBehaviour.Properties.of(Material.METAL).noOcclusion()));
    public static final RegistryObject<Block> FAIRY_JAR = BLOCKS.register("fairy_jar", () -> new FairyJar(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion()));
    public static final RegistryObject<Block> MAGIC_SAPLING = BLOCKS.register("magic_sapling", () -> new Block(BlockBehaviour.Properties.of(Material.PLANT).noOcclusion()));
    public static final RegistryObject<Block> MAGIC_TREE = BLOCKS.register("magic_tree", () -> new MagicTree(BlockBehaviour.Properties.of(Material.WOOD).noOcclusion()));
    
    // Dwarf in the flask
    	/*
    	 * First crafted item
    	 * Unlocks recipe for multiblock
    	 * As part of multiblock: Gives suggestions and hints in editor
    	 */
    
    // Crucible of Inscription
    	/*
    	 * Central inscribing block
    	 * Resembles cauldron
    	 * Can only inscribe scrolls with 5 or fewer components by default
    	 */
    
    // Magecrafted Bough
    	/*
    	 * Planted as sapling, grown to use in multiblock
    	 * Resembles a young tree or bonsai tree that has grown into a spiral
    	 * As part of multiblock: Holds objects for inscribing
    	 */
    
    // Pillars
    	/*
    	 * Inscribed block between two decorative blocks
    	 * Placed in concentric rings around crucible in sets of 3, 5, 7 etc.
    	 * As part of multiblock: Increases editor component cap
    	 */
    public static final RegistryObject<Block> INSCRIPTION_STONE = BLOCKS.register("inscription_stone", () -> new InscribedBlock(BlockBehaviour.Properties.of(Material.STONE), Blocks.STONE));
    public static final RegistryObject<Block> INSCRIPTION_OAK = BLOCKS.register("inscription_oak", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.OAK_LOG));
    public static final RegistryObject<Block> INSCRIPTION_SPRUCE = BLOCKS.register("inscription_spruce", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.SPRUCE_LOG));
    public static final RegistryObject<Block> INSCRIPTION_BIRCH = BLOCKS.register("inscription_birch", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.BIRCH_LOG));
    public static final RegistryObject<Block> INSCRIPTION_ACACIA = BLOCKS.register("inscription_acacia", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.ACACIA_LOG));
    public static final RegistryObject<Block> INSCRIPTION_JUNGLE = BLOCKS.register("inscription_jungle", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.JUNGLE_LOG));
    public static final RegistryObject<Block> INSCRIPTION_DARK_OAK = BLOCKS.register("inscription_dark_oak", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.DARK_OAK_LOG));
    public static final RegistryObject<Block> INSCRIPTION_CRIMSON = BLOCKS.register("inscription_crimson", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.CRIMSON_STEM));
    public static final RegistryObject<Block> INSCRIPTION_WARPED = BLOCKS.register("inscription_warped", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.WARPED_STEM));
    public static final RegistryObject<Block> INSCRIPTION_MANGROVE = BLOCKS.register("inscription_mangrove", () -> InscribedBlock.log(BlockBehaviour.Properties.of(Material.WOOD), Blocks.MANGROVE_LOG));
    
    // [Decorative spell holder]
    	/*
    	 * Displays last spell used aimed at it
    	 * Controls for PYR offsets and speeds
    	 */
    
    public static void init() { }
}
