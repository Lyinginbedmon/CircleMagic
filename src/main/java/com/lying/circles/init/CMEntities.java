package com.lying.circles.init;

import com.lying.circles.entities.SpellEntity;
import com.lying.circles.entities.StatueEntity;
import com.lying.circles.reference.Reference;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CMEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.ModInfo.MOD_ID);
    
    public static final RegistryObject<EntityType<SpellEntity>> SPELL = register("spell", EntityType.Builder.<SpellEntity>of(SpellEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(16));
    public static final RegistryObject<EntityType<StatueEntity>> STATUE = register("statue", EntityType.Builder.<StatueEntity>of(StatueEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(16));
    
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder)
	{
		return ENTITIES.register(name, () -> builder.build(Reference.ModInfo.MOD_PREFIX + name));
	}
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event)
    {
//    	event.put(HEARTH_LIGHT.get(), EntityHearthLight.createLivingAttributes().build());
    	event.put(STATUE.get(), StatueEntity.createLivingAttributes().build());
    }
}
