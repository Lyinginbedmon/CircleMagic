package com.lying.circles.init;

import com.lying.circles.client.gui.menu.MenuSandbox;
import com.lying.circles.reference.Reference;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CMMenus
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.ModInfo.MOD_ID);
    
    public static final RegistryObject<MenuType<MenuSandbox>> SANDBOX_MENU = MENUS.register("sandbox", () -> new MenuType<MenuSandbox>(MenuSandbox::new));
    
	public static MenuType<?> register(String nameIn, MenuType<?> menuIn)
	{
		MENUS.register(Reference.ModInfo.MOD_ID+"."+nameIn, () -> menuIn);
		return menuIn;
	}
}
