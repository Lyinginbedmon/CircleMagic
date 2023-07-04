package com.lying.circles.init;

import net.minecraft.world.damagesource.DamageSource;

public class CMDamageSource
{
	public static final DamageSource PETRIFICATION = new DamageSource("curruisis").bypassArmor().bypassEnchantments().bypassMagic();
	
	public static final DamageSource OUT_OF_MANA = new DamageSource("out_of_mana").bypassArmor().bypassEnchantments().bypassMagic();
	
	public static final DamageSource TOO_MUCH_MANA = new DamageSource("too_much_mana").bypassArmor().bypassEnchantments().bypassMagic();
}
