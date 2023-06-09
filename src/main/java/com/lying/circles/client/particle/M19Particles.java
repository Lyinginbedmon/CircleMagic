package com.lying.circles.client.particle;

import com.lying.circles.reference.Reference;

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
	
	@SubscribeEvent
    public static void registerFactories(final RegisterParticleProvidersEvent event)
    {
		event.register(SQUARES.get(), SquareParticle.Factory::new);
    }
}
