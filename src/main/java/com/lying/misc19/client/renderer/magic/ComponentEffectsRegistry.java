package com.lying.misc19.client.renderer.magic;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.client.renderer.magic.effects.ComponentEffectRenderer;
import com.lying.misc19.reference.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ComponentEffectsRegistry 
{
	public static final ResourceKey<Registry<ComponentEffectRenderer>> REGISTRY_KEY			= ResourceKey.createRegistryKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "component_effects"));
	public static final DeferredRegister<ComponentEffectRenderer> EFFECTS					= DeferredRegister.create(REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	public static final Supplier<IForgeRegistry<ComponentEffectRenderer>> EFFECTS_REGISTRY	= EFFECTS.makeRegistry(() -> (new RegistryBuilder<ComponentEffectRenderer>()).hasTags());
	
	private static final Minecraft mc = Minecraft.getInstance();
	
	private static List<ComponentEffect> activeEffects = Lists.newArrayList();
	
	public static Optional<ComponentEffectRenderer> getRenderer(ResourceLocation registryName)
	{
		for(RegistryObject<ComponentEffectRenderer> entry : EFFECTS.getEntries())
			if(entry.isPresent() && entry.getId().equals(registryName))
				return Optional.of(entry.get());
		return Optional.empty();
	}
	
	public static void tickActiveEffects(final RenderLevelStageEvent event)
	{
		if(activeEffects.isEmpty() || mc.level == null)
			return;
		
		activeEffects.removeIf((effect) -> !effect.isAlive());
		
		// Limited to 64 effects within 64 blocks of the player to limit client-side lag
		Vec3 playerPos = mc.player.position();
		for(int i=0; i<Math.min(64, activeEffects.size()); i++)
		{
			ComponentEffect effect = activeEffects.get(i);
			if(effect.location().distanceTo(playerPos) < (64D * 64D))
				effect.tick(mc.level, event);
		}
	}
	
	public static void addNewEffect(ResourceLocation nameIn, Vec3 pos, CompoundTag dataIn, int ticks)
	{
		if(getRenderer(nameIn).isPresent() && ticks > 0)
			activeEffects.add(new ComponentEffect(nameIn, pos, dataIn, ticks));
	}
	
	/** Visuals are cleared when the player changes dimension to save rendering things the player will never see */
	public static void clear() { activeEffects.clear(); }
	
	/** An instance of a component effect being displayed in the world */
	private static class ComponentEffect
	{
		private final ResourceLocation registryName;
		private final Vec3 origin;
		private final CompoundTag data;
		private int duration;
		private int ticksActive;
		
		public ComponentEffect(ResourceLocation nameIn, Vec3 pos, CompoundTag dataIn, int ticks)
		{
			this.registryName = nameIn;
			this.origin = pos;
			this.data = dataIn;
			
			/** Visual effects are not permitted to last longer than one minute for any reason */
			this.duration = Math.min(Math.abs(ticks), Reference.Values.TICKS_PER_MINUTE);
		}
		
		public Vec3 location() { return this.origin; }
		
		public void tick(Level world, final RenderLevelStageEvent event)
		{
			duration--;
			ComponentEffectsRegistry.getRenderer(registryName).ifPresent((renderer) -> renderer.render(world, origin, data, ticksActive++, event));
		}
		
		public boolean isAlive() { return duration >= 0; }
	}
}
