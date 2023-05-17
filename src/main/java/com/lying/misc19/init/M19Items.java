package com.lying.misc19.init;

import com.lying.misc19.item.Pendulum;
import com.lying.misc19.item.ScrollItem;
import com.lying.misc19.reference.Reference;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class M19Items
{
	public static final CreativeModeTab TAB = new CreativeModeTab(12, Reference.ModInfo.MOD_ID)
			{
				public ItemStack makeIcon() { return new ItemStack(PENDULUM.get()); }
			};
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.ModInfo.MOD_ID);
    
    public static final RegistryObject<Item> PENDULUM = ITEMS.register("pendulum", () -> new Pendulum(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> PENDULUM_WEIGHT = ITEMS.register("pendulum_weight", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MAGIC_SCROLL = ITEMS.register("magic_scroll", () -> new ScrollItem(new Item.Properties().tab(TAB)));
    
    // Block items
    public static final RegistryObject<Item> SANDBOX_ITEM = ITEMS.register("sandbox", () -> new BlockItem(M19Blocks.SANDBOX.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> PHANTOM_CUBE_ITEM = ITEMS.register("phantom_cube", () -> new BlockItem(M19Blocks.PHANTOM_CUBE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> CRUCIBLE_ITEM = ITEMS.register("crucible", () -> new BlockItem(M19Blocks.CRUCIBLE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> FAIRY_JAR_ITEM = ITEMS.register("fairy_jar", () -> new BlockItem(M19Blocks.FAIRY_JAR.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> MAGIC_SAPLING_ITEM = ITEMS.register("magic_sapling", () -> new BlockItem(M19Blocks.MAGIC_SAPLING.get(), new Item.Properties().tab(TAB)));
    
    public static final RegistryObject<Item> IMBUED_STONE_ITEM = ITEMS.register("imbued_stone", () -> new BlockItem(M19Blocks.IMBUED_STONE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_OAK_ITEM = ITEMS.register("imbued_oak", () -> new BlockItem(M19Blocks.IMBUED_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_SPRUCE_ITEM = ITEMS.register("imbued_spruce", () -> new BlockItem(M19Blocks.IMBUED_SPRUCE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_BIRCH_ITEM = ITEMS.register("imbued_birch", () -> new BlockItem(M19Blocks.IMBUED_BIRCH.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_JUNGLE_ITEM = ITEMS.register("imbued_jungle", () -> new BlockItem(M19Blocks.IMBUED_JUNGLE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_ACACIA_ITEM = ITEMS.register("imbued_acacia", () -> new BlockItem(M19Blocks.IMBUED_ACACIA.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_DARK_OAK_ITEM = ITEMS.register("imbued_dark_oak", () -> new BlockItem(M19Blocks.IMBUED_DARK_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_CRIMSON_ITEM = ITEMS.register("imbued_crimson", () -> new BlockItem(M19Blocks.IMBUED_CRIMSON.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_WARPED_ITEM = ITEMS.register("imbued_warped", () -> new BlockItem(M19Blocks.IMBUED_WARPED.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> IMBUED_MANGROVE_ITEM = ITEMS.register("imbued_mangrove", () -> new BlockItem(M19Blocks.IMBUED_MANGROVE.get(), new Item.Properties().tab(TAB)));
    
    public static final RegistryObject<Item> INSCRIBED_STONE_ITEM = ITEMS.register("inscribed_stone", () -> new BlockItem(M19Blocks.INSCRIBED_STONE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_OAK_ITEM = ITEMS.register("inscribed_oak", () -> new BlockItem(M19Blocks.INSCRIBED_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_SPRUCE_ITEM = ITEMS.register("inscribed_spruce", () -> new BlockItem(M19Blocks.INSCRIBED_SPRUCE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_BIRCH_ITEM = ITEMS.register("inscribed_birch", () -> new BlockItem(M19Blocks.INSCRIBED_BIRCH.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_JUNGLE_ITEM = ITEMS.register("inscribed_jungle", () -> new BlockItem(M19Blocks.INSCRIBED_JUNGLE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_ACACIA_ITEM = ITEMS.register("inscribed_acacia", () -> new BlockItem(M19Blocks.INSCRIBED_ACACIA.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_DARK_OAK_ITEM = ITEMS.register("inscribed_dark_oak", () -> new BlockItem(M19Blocks.INSCRIBED_DARK_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_CRIMSON_ITEM = ITEMS.register("inscribed_crimson", () -> new BlockItem(M19Blocks.INSCRIBED_CRIMSON.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_WARPED_ITEM = ITEMS.register("inscribed_warped", () -> new BlockItem(M19Blocks.INSCRIBED_WARPED.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIBED_MANGROVE_ITEM = ITEMS.register("inscribed_mangrove", () -> new BlockItem(M19Blocks.INSCRIBED_MANGROVE.get(), new Item.Properties().tab(TAB)));
    
	public static Item register(String nameIn, Item itemIn)
	{
		ITEMS.register(Reference.ModInfo.MOD_ID+"."+nameIn, () -> itemIn);
		return itemIn;
	}
}
