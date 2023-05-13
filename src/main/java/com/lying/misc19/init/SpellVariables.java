package com.lying.misc19.init;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.lying.misc19.magic.IVariableBuilder;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.VarBool;
import com.lying.misc19.magic.variable.VarDouble;
import com.lying.misc19.magic.variable.VarEntity;
import com.lying.misc19.magic.variable.VarLevel;
import com.lying.misc19.magic.variable.VarStack;
import com.lying.misc19.magic.variable.VarVec;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class SpellVariables
{
	public static final ResourceKey<Registry<IVariableBuilder>> REGISTRY_KEY				= ResourceKey.createRegistryKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "variables"));
	public static final DeferredRegister<IVariableBuilder> VARIABLES					= DeferredRegister.create(REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	public static final Supplier<IForgeRegistry<IVariableBuilder>> VARIABLES_REGISTRY	= VARIABLES.makeRegistry(() -> (new RegistryBuilder<IVariableBuilder>()).hasTags());
	
	public static final ResourceLocation VEC = make("vector");
	public static final ResourceLocation DOUBLE = make("double");
	public static final ResourceLocation BOOL = make("bool");
	public static final ResourceLocation ENTITY = make("entity");
	public static final ResourceLocation WORLD = make("level");
	public static final ResourceLocation STACK = make("stack");
	
	public static ResourceLocation make(String path) { return new ResourceLocation(Reference.ModInfo.MOD_ID, path); }
	
	static
	{
		register(VEC, () -> () -> new VarVec(Vec3.ZERO));
		register(DOUBLE, () -> () -> new VarDouble(0D));
		register(BOOL, () -> () -> new VarBool(false));
		register(ENTITY, () -> () -> new VarEntity(null));
		register(WORLD, () -> () -> new VarLevel(null));
		register(STACK, () -> () -> new VarStack());
	}
	
	private static RegistryObject<IVariableBuilder> register(ResourceLocation nameIn, Supplier<IVariableBuilder> variableIn)
	{
		return VARIABLES.register(nameIn.getPath(), variableIn);
	}
	
	public static ResourceLocation getRegistryName(IVariable variable)
	{
		if(variable.getRegistryName() != null)
			return variable.getRegistryName();
		
		for(RegistryObject<IVariableBuilder> entry : VARIABLES.getEntries())
			if(entry.get().create().getClass() == variable.getClass())
				return entry.getId();
		return BOOL;
	}
	
	@Nonnull
	public static IVariable create(ResourceLocation registryName)
	{
		for(RegistryObject<IVariableBuilder> entry : VARIABLES.getEntries())
			if(entry.getId().equals(registryName))
			{
				IVariable var = entry.get().create();
				var.setRegistryName(entry.getId());
				return var;
			}
		
		return VariableSet.DEFAULT;
	}
	
	@Nonnull
	public static IVariable readFromNbt(CompoundTag compound)
	{
		if(compound.contains("ID", Tag.TAG_STRING))
		{
			ResourceLocation type = new ResourceLocation(compound.getString("ID"));
			IVariable var = create(type);
			if(compound.contains("Data", Tag.TAG_COMPOUND))
				var.load(compound.getCompound("Data"));
			return var;
		}
		return VariableSet.DEFAULT;
	}
}
