package com.lying.circles.init;

import com.lying.circles.blocks.Crucible;
import com.lying.circles.blocks.CurruidDust;
import com.lying.circles.blocks.FairyJar;
import com.lying.circles.blocks.ImbuedBlock;
import com.lying.circles.blocks.InscribedBlock;
import com.lying.circles.blocks.MagicSapling;
import com.lying.circles.blocks.MagicTree;
import com.lying.circles.blocks.ManaCrystal;
import com.lying.circles.blocks.PhantomBlock;
import com.lying.circles.blocks.Sandbox;
import com.lying.circles.blocks.Statue;
import com.lying.circles.blocks.TilledSand;
import com.lying.circles.reference.Reference;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CMBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.ModInfo.MOD_ID);
    
    public static final RegistryObject<Block> SANDBOX = BLOCKS.register("sandbox", () -> new Sandbox(BlockBehaviour.Properties.of(Material.WOOD).noOcclusion()));
    public static final RegistryObject<Block> PHANTOM_CUBE = BLOCKS.register("phantom_cube", () -> new PhantomBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().strength(-1.0F, 3600000.0F).noLootTable()));
    public static final RegistryObject<Block> CURRUID_DUST = BLOCKS.register("curruid_dust", () -> new CurruidDust(BlockBehaviour.Properties.of(Material.AMETHYST, MaterialColor.COLOR_CYAN).sound(SoundType.AMETHYST).instabreak()));
    public static final RegistryObject<Block> CURRUID_BLOCK = BLOCKS.register("curruid_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_BLUE).sound(SoundType.AMETHYST)));
    
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
    
    public static final RegistryObject<Block> STATUE = BLOCKS.register("statue", () -> new Statue(BlockBehaviour.Properties.of(Material.STONE).noOcclusion()));
    
    // Dwarf in the flask
    	/**
    	 * First crafted item<br>
    	 * Unlocks recipe for multiblock<br>
    	 * As part of multiblock: Gives suggestions and hints in editor
    	 */
    public static final RegistryObject<Block> FAIRY_JAR = BLOCKS.register("fairy_jar", () -> new FairyJar(BlockBehaviour.Properties.of(Material.GLASS).sound(SoundType.GLASS).strength(0.3F).noOcclusion()));
    
    // Crucible of Inscription
    	/**
    	 * Central inscribing block<br>
    	 * Resembles a cauldron<br>
    	 * Can only inscribe scrolls with 5 or fewer components by default
    	 */
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register("crucible", () -> new Crucible(BlockBehaviour.Properties.of(Material.METAL).lightLevel((state) -> 1).noOcclusion()));
    
    // Magecrafted Bough
    	/**
    	 * Planted as sapling, grown to use in multiblock<br>
    	 * Resembles a young tree or bonsai tree that has grown into a spiral<br>
    	 * As part of multiblock: Holds objects for inscribing
    	 */
    public static final RegistryObject<Block> MAGIC_SAPLING = BLOCKS.register("magic_sapling", () -> new MagicSapling(BlockBehaviour.Properties.of(Material.PLANT).noOcclusion().noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> POTTED_MAGIC_SAPLING = BLOCKS.register("potted_magic_sapling", () -> new FlowerPotBlock(() -> (FlowerPotBlock)Blocks.FLOWER_POT, () -> CMBlocks.MAGIC_SAPLING.get(), BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()));
    public static final RegistryObject<Block> MAGIC_TREE = BLOCKS.register("magic_tree", () -> new MagicTree(BlockBehaviour.Properties.of(Material.WOOD).noOcclusion().sound(SoundType.WOOD)));
    
    // Pillars
    	/**
    	 * Inscribed block between two decorative blocks<br>
    	 * Placed in concentric rings around crucible in sets of 3, 5, 7 etc.<br>
    	 * As part of multiblock: Increases editor component cap
    	 */
    public static final RegistryObject<Block> IMBUED_STONE = BLOCKS.register("imbued_stone", () -> new ImbuedBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Block> IMBUED_OAK = BLOCKS.register("imbued_oak", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_SPRUCE = BLOCKS.register("imbued_spruce", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_BIRCH = BLOCKS.register("imbued_birch", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_ACACIA = BLOCKS.register("imbued_acacia", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_JUNGLE = BLOCKS.register("imbued_jungle", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_DARK_OAK = BLOCKS.register("imbued_dark_oak", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_CRIMSON = BLOCKS.register("imbued_crimson", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_WARPED = BLOCKS.register("imbued_warped", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    public static final RegistryObject<Block> IMBUED_MANGROVE = BLOCKS.register("imbued_mangrove", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD)));
    
    public static final RegistryObject<Block> INSCRIBED_STONE = BLOCKS.register("inscribed_stone", () -> new InscribedBlock(BlockBehaviour.Properties.of(Material.STONE), IMBUED_STONE.get()));
    public static final RegistryObject<Block> INSCRIBED_OAK = BLOCKS.register("inscribed_oak", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_OAK.get()));
    public static final RegistryObject<Block> INSCRIBED_SPRUCE = BLOCKS.register("inscribed_spruce", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_SPRUCE.get()));
    public static final RegistryObject<Block> INSCRIBED_BIRCH = BLOCKS.register("inscribed_birch", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_BIRCH.get()));
    public static final RegistryObject<Block> INSCRIBED_ACACIA = BLOCKS.register("inscribed_acacia", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_ACACIA.get()));
    public static final RegistryObject<Block> INSCRIBED_JUNGLE = BLOCKS.register("inscribed_jungle", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_JUNGLE.get()));
    public static final RegistryObject<Block> INSCRIBED_DARK_OAK = BLOCKS.register("inscribed_dark_oak", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_DARK_OAK.get()));
    public static final RegistryObject<Block> INSCRIBED_CRIMSON = BLOCKS.register("inscribed_crimson", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_CRIMSON.get()));
    public static final RegistryObject<Block> INSCRIBED_WARPED = BLOCKS.register("inscribed_warped", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_WARPED.get()));
    public static final RegistryObject<Block> INSCRIBED_MANGROVE = BLOCKS.register("inscribed_mangrove", () -> InscribedBlock.pillar(BlockBehaviour.Properties.of(Material.WOOD), IMBUED_MANGROVE.get()));
    
    // [Decorative spell holder, name pending]
    	/**
    	 * Displays last spell used aimed at it<br>
    	 * Controls for PYR offsets and speeds
    	 */
    
    // Tilled sand
    	/**
    	 * Created at the base of completed pillars or by tilling sand with a hoe<br>
    	 * Behaves like minecart rails in connection patterns, otherwise purely decorative
    	 */
    public static final RegistryObject<Block> TILLED_SAND = BLOCKS.register("tilled_sand", () -> new TilledSand(14406560, BlockBehaviour.Properties.of(Material.SAND, MaterialColor.SAND).strength(0.5F).sound(SoundType.SAND), Blocks.SAND.defaultBlockState()));
    public static final RegistryObject<Block> TILLED_RED_SAND = BLOCKS.register("tilled_red_sand", () -> new TilledSand(11098145, BlockBehaviour.Properties.of(Material.SAND, MaterialColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.SAND), Blocks.RED_SAND.defaultBlockState()));
    
    public static final RegistryObject<Block> MANA_CRYSTAL = BLOCKS.register("mana_crystal", () -> new ManaCrystal(BlockBehaviour.Properties.of(Material.AMETHYST)));
    
    public static void init() { }
}
