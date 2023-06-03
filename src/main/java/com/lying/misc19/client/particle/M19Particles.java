package com.lying.misc19.client.particle;

import com.lying.misc19.reference.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class M19Particles 
{
	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Reference.ModInfo.MOD_ID);
	
	public static final RegistryObject<SimpleParticleType> SQUARES = PARTICLES.register("squares", () -> new SimpleParticleType(false));
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
    public static void registerFactories(final RegisterParticleProvidersEvent event)
    {
		Minecraft mc = Minecraft.getInstance();
		mc.particleEngine.register(SQUARES.get(), new SquareParticle.Factory());
    }
}
