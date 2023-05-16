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
    
    public static final RegistryObject<Item> INSCRIPTION_STONE_ITEM = ITEMS.register("inscription_stone", () -> new BlockItem(M19Blocks.INSCRIPTION_STONE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIPTION_OAK_ITEM = ITEMS.register("inscription_oak", () -> new BlockItem(M19Blocks.INSCRIPTION_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIPTION_SPRUCE_ITEM = ITEMS.register("inscription_spruce", () -> new BlockItem(M19Blocks.INSCRIPTION_SPRUCE.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIPTION_BIRCH_ITEM = ITEMS.register("inscription_birch", () -> new BlockItem(M19Blocks.INSCRIPTION_BIRCH.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIPTION_DARK_OAK_ITEM = ITEMS.register("inscription_dark_oak", () -> new BlockItem(M19Blocks.INSCRIPTION_DARK_OAK.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIPTION_CRIMSON_ITEM = ITEMS.register("inscription_crimson", () -> new BlockItem(M19Blocks.INSCRIPTION_CRIMSON.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIPTION_WARPED_ITEM = ITEMS.register("inscription_warped", () -> new BlockItem(M19Blocks.INSCRIPTION_WARPED.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> INSCRIPTION_MANGROVE_ITEM = ITEMS.register("inscription_mangrove", () -> new BlockItem(M19Blocks.INSCRIPTION_MANGROVE.get(), new Item.Properties().tab(TAB)));
    
	public static Item register(String nameIn, Item itemIn)
	{
		ITEMS.register(Reference.ModInfo.MOD_ID+"."+nameIn, () -> itemIn);
		return itemIn;
	}
}
