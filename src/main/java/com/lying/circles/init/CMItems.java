package com.lying.circles.init;

import com.lying.circles.item.CurruidDustItem;
import com.lying.circles.item.FairyJarItem;
import com.lying.circles.item.Pendulum;
import com.lying.circles.item.ScrollItem;
import com.lying.circles.reference.Reference;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CMItems
{
	public static final CreativeModeTab TAB = new CreativeModeTab(12, Reference.ModInfo.MOD_ID)
			{
				public ItemStack makeIcon() { return new ItemStack(PENDULUM.get()); }
			};
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.ModInfo.MOD_ID);
    
    public static final RegistryObject<Item> PENDULUM = ITEMS.register("pendulum", () -> new Pendulum(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> PENDULUM_WEIGHT = ITEMS.register("pendulum_weight", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STATUE_ITEM = ITEMS.register("statue", () -> new BlockItem(CMBlocks.STATUE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> MAGIC_SCROLL = ITEMS.register("magic_scroll", () -> new ScrollItem(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> CURRUID_DUST = ITEMS.register("curruid_dust", () -> new CurruidDustItem(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> CURRUID_BLOCK = ITEMS.register("curruid_block", () -> new BlockItem(CMBlocks.CURRUID_BLOCK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> CURRUID_GEM = ITEMS.register("curruid_gem", () -> new Item(new Item.Properties().tab(TAB)));
    
    // Block items
    public static final RegistryObject<Item> SANDBOX_ITEM = ITEMS.register("sandbox", () -> new BlockItem(CMBlocks.SANDBOX.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> PHANTOM_CUBE_ITEM = ITEMS.register("phantom_cube", () -> new BlockItem(CMBlocks.PHANTOM_CUBE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> CRUCIBLE_ITEM = ITEMS.register("crucible", () -> new BlockItem(CMBlocks.CRUCIBLE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> FAIRY_JAR_ITEM = ITEMS.register("fairy_jar", () -> new FairyJarItem(new Item.Properties().tab(TAB).stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_SAPLING_ITEM = ITEMS.register("magic_sapling", () -> new BlockItem(CMBlocks.MAGIC_SAPLING.get(), new Item.Properties().tab(TAB)));
    
    public static final RegistryObject<Item> IMBUED_STONE_ITEM = ITEMS.register("imbued_stone", () -> new BlockItem(CMBlocks.IMBUED_STONE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_OAK_ITEM = ITEMS.register("imbued_oak", () -> new BlockItem(CMBlocks.IMBUED_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_SPRUCE_ITEM = ITEMS.register("imbued_spruce", () -> new BlockItem(CMBlocks.IMBUED_SPRUCE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_BIRCH_ITEM = ITEMS.register("imbued_birch", () -> new BlockItem(CMBlocks.IMBUED_BIRCH.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_JUNGLE_ITEM = ITEMS.register("imbued_jungle", () -> new BlockItem(CMBlocks.IMBUED_JUNGLE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_ACACIA_ITEM = ITEMS.register("imbued_acacia", () -> new BlockItem(CMBlocks.IMBUED_ACACIA.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_DARK_OAK_ITEM = ITEMS.register("imbued_dark_oak", () -> new BlockItem(CMBlocks.IMBUED_DARK_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_CRIMSON_ITEM = ITEMS.register("imbued_crimson", () -> new BlockItem(CMBlocks.IMBUED_CRIMSON.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_WARPED_ITEM = ITEMS.register("imbued_warped", () -> new BlockItem(CMBlocks.IMBUED_WARPED.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_MANGROVE_ITEM = ITEMS.register("imbued_mangrove", () -> new BlockItem(CMBlocks.IMBUED_MANGROVE.get(), new Item.Properties().tab(TAB)));
    
    public static final RegistryObject<Item> INSCRIBED_STONE_ITEM = ITEMS.register("inscribed_stone", () -> new BlockItem(CMBlocks.INSCRIBED_STONE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_OAK_ITEM = ITEMS.register("inscribed_oak", () -> new BlockItem(CMBlocks.INSCRIBED_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_SPRUCE_ITEM = ITEMS.register("inscribed_spruce", () -> new BlockItem(CMBlocks.INSCRIBED_SPRUCE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_BIRCH_ITEM = ITEMS.register("inscribed_birch", () -> new BlockItem(CMBlocks.INSCRIBED_BIRCH.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_JUNGLE_ITEM = ITEMS.register("inscribed_jungle", () -> new BlockItem(CMBlocks.INSCRIBED_JUNGLE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_ACACIA_ITEM = ITEMS.register("inscribed_acacia", () -> new BlockItem(CMBlocks.INSCRIBED_ACACIA.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_DARK_OAK_ITEM = ITEMS.register("inscribed_dark_oak", () -> new BlockItem(CMBlocks.INSCRIBED_DARK_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_CRIMSON_ITEM = ITEMS.register("inscribed_crimson", () -> new BlockItem(CMBlocks.INSCRIBED_CRIMSON.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_WARPED_ITEM = ITEMS.register("inscribed_warped", () -> new BlockItem(CMBlocks.INSCRIBED_WARPED.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_MANGROVE_ITEM = ITEMS.register("inscribed_mangrove", () -> new BlockItem(CMBlocks.INSCRIBED_MANGROVE.get(), new Item.Properties().tab(TAB)));
    
    public static final RegistryObject<Item> MANA_CRYSTAL = ITEMS.register("mana_crystal", () -> new BlockItem(CMBlocks.MANA_CRYSTAL.get(), new Item.Properties().tab(TAB)));
    
	public static Item register(String nameIn, Item itemIn)
	{
		ITEMS.register(Reference.ModInfo.MOD_ID+"."+nameIn, () -> itemIn);
		return itemIn;
	}
	
	public static void addBrewingRecipes()
	{
		BrewingRecipeRegistry.addRecipe(
				Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)), 
				Ingredient.of(CMItems.CURRUID_DUST.get()), 
				new ItemStack(CMItems.FAIRY_JAR_ITEM.get()));
	}
}
