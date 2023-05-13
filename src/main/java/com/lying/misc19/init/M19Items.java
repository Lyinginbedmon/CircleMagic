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
    
	public static Item register(String nameIn, Item itemIn)
	{
		ITEMS.register(Reference.ModInfo.MOD_ID+"."+nameIn, () -> itemIn);
		return itemIn;
	}
}
