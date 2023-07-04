package com.lying.circles.init;

import com.lying.circles.potion.CMMobEffect;
import com.lying.circles.reference.Reference;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CMStatusEffects
{
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Reference.ModInfo.MOD_ID);
	
	public static final RegistryObject<MobEffect> LEY_POWER = EFFECTS.register("ley_power", () -> new CMMobEffect(MobEffectCategory.BENEFICIAL, -1));
}
