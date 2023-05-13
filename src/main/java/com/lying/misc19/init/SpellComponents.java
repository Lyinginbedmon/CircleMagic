package com.lying.misc19.init;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.commons.compress.utils.Lists;

import com.lying.misc19.Misc19;
import com.lying.misc19.magic.*;
import com.lying.misc19.magic.ISpellComponent;
import com.lying.misc19.magic.ISpellComponent.Category;
import com.lying.misc19.magic.ISpellComponentBuilder;
import com.lying.misc19.magic.component.*;
import com.lying.misc19.magic.variable.IVariable;
import com.lying.misc19.magic.variable.*;
import com.lying.misc19.magic.variable.VariableSet;
import com.lying.misc19.magic.variable.VariableSet.Slot;
import com.lying.misc19.reference.Reference;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class SpellComponents
{
	public static final ResourceKey<Registry<ISpellComponentBuilder>> REGISTRY_KEY				= ResourceKey.createRegistryKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "components"));
	public static final DeferredRegister<ISpellComponentBuilder> COMPONENTS					= DeferredRegister.create(REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	public static final Supplier<IForgeRegistry<ISpellComponentBuilder>> COMPONENTS_REGISTRY	= COMPONENTS.makeRegistry(() -> (new RegistryBuilder<ISpellComponentBuilder>()).hasTags());
	
	/*
	 * Every arrangement starts at a ROOT glyph<br>
	 * The ROOT populates the variables with specific constant values, according to its type.<br>
	 * The ROOT is then surrounded by a CIRCLE, which contains other GLYPHS and CIRCLES to perform functions.<br>
	 * The ROOT is what is called by spell objects during execution, and extracts the needed mana from the caster.<br>
	 * Arrangements have a hard limit on how many glyphs can be executed per execution call, after which no more glyphs will be run.<br>
	 */
	
	// Root glyphs
	public static final ResourceLocation ROOT_DUMMY = make("dummy_root");
	public static final ResourceLocation ROOT_CASTER = make("caster_root");
	public static final ResourceLocation ROOT_TARGET = make("target_root");
	public static final ResourceLocation ROOT_POSITION = make("position_root");
	
	// Hertz augments
	public static final ResourceLocation HERTZ_SECOND = make("second_hertz");
	public static final ResourceLocation HERTZ_MINUTE = make("minute_hertz");
	
	// Circles
	public static final ResourceLocation CIRCLE_BASIC = make("basic_circle");
	public static final ResourceLocation CIRCLE_STEP = make("step_circle");
	
	// Constants
	public static final ResourceLocation SIGIL_FALSE = make("false_sigil");
	public static final ResourceLocation SIGIL_TRUE = make("true_sigil");
	public static final ResourceLocation SIGIL_2 = make("two_sigil");
	public static final ResourceLocation SIGIL_4 = make("four_sigil");
	public static final ResourceLocation SIGIL_8 = make("eight_sigil");
	public static final ResourceLocation SIGIL_16 = make("sixteen_sigil");
	public static final ResourceLocation SIGIL_PI = make("pi_sigil");
	public static final ResourceLocation SIGIL_XYZ = make("xyz_sigil");
	
	public static final ResourceLocation GLYPH_DUMMY = make("dummy_glyph");
	
	// Arithmetic operations
	public static final ResourceLocation GLYPH_SET = make("set_glyph");
	public static final ResourceLocation GLYPH_ADD = make("add_glyph");
	public static final ResourceLocation GLYPH_SUB = make("sub_glyph");
	public static final ResourceLocation GLYPH_MUL = make("mul_glyph");
	public static final ResourceLocation GLYPH_DIV = make("div_glyph");
	public static final ResourceLocation GLYPH_MOD = make("mod_glyph");
	public static final ResourceLocation GLYPH_RAND = make("random_glyph");
	/**
	 * Signum
	 * Absolute value
	 * Negate
	 */
	
	// Boolean operations
	public static final ResourceLocation GLYPH_EQU = make("equals_glyph");
	public static final ResourceLocation GLYPH_AND = make("and_glyph");
	public static final ResourceLocation GLYPH_OR = make("or_glyph");
	public static final ResourceLocation GLYPH_XOR = make("xor_glyph");
	public static final ResourceLocation GLYPH_NAND = make("not_glyph");
	public static final ResourceLocation GLYPH_GRE = make("greater_glyph");
	public static final ResourceLocation GLYPH_LES = make("lesser_glyph");
	
	// Vector operations
	public static final ResourceLocation GLYPH_MAKEVEC = make("make_vec_glyph");
	public static final ResourceLocation GLYPH_DOT = make("dot_glyph");
	public static final ResourceLocation GLYPH_CROSS = make("cross_glyph");
	public static final ResourceLocation GLYPH_NORMALISE = make("normalize_glyph");
	public static final ResourceLocation GLYPH_LENGTH = make("length_glyph");
	/**
	 * Vector rotation
	 */
	
	// World operations
	public static final ResourceLocation GLYPH_TRACE = make("ray_trace_glyph");
	public static final ResourceLocation GLYPH_IS_EMPTY = make("block_empty_glyph");
	public static final ResourceLocation GLYPH_ENTITIES = make("get_entities_glyph");
	
	// Entity operations
	public static final ResourceLocation GLYPH_ENT_POS = make("entity_position_glyph");
	public static final ResourceLocation GLYPH_ENT_LOOK = make("entity_look_glyph");
	public static final ResourceLocation GLYPH_ENT_VEL = make("entity_velocity_glyph");
	public static final ResourceLocation GLYPH_ENT_HEALTH = make("entity_health_glyph");
	public static final ResourceLocation GLYPH_ENT_TARGET = make("entity_target_glyph");
	/**
	 * Get creature type?
	 */
	
	// Stack operations
	public static final ResourceLocation GLYPH_STACK_GET = make("stack_get_glyph");
	public static final ResourceLocation GLYPH_STACK_ADD = make("stack_add_glyph");
	public static final ResourceLocation GLYPH_STACK_SUB = make("stack_sub_glyph");
	/**
	 * Sort by distance to a point
	 */
	
	// Functions
	public static final ResourceLocation FUNCTION_DEBUG = make("debug_function");
	public static final ResourceLocation FUNCTION_MOVE = make("motion_function");
	public static final ResourceLocation FUNCTION_CREATE = make("creation_function");
	public static final ResourceLocation FUNCTION_DISPEL = make("dispel_function");
	public static final ResourceLocation FUNCTION_STATUS = make("potion_function");
	
	public static ResourceLocation make(String path) { return new ResourceLocation(Reference.ModInfo.MOD_ID, path); }
	
	static
	{
		register(ROOT_DUMMY, () -> () -> new RootGlyph.Dummy());
		register(ROOT_CASTER, () -> () -> new RootGlyph.Self());
		register(ROOT_TARGET, () -> () -> new RootGlyph.Target());
		register(ROOT_POSITION, () -> () -> new RootGlyph.Position());
		
		register(HERTZ_SECOND, () -> () -> new HertzGlyph(Reference.Values.TICKS_PER_SECOND));
		register(HERTZ_MINUTE, () -> () -> new HertzGlyph(Reference.Values.TICKS_PER_MINUTE));
		
		register(CIRCLE_BASIC, () -> () -> new ComponentCircle.Basic());
		register(CIRCLE_STEP, () -> () -> new ComponentCircle.Step());
		
		register(SIGIL_FALSE, () -> () -> new VariableSigil.Constant(VarBool.FALSE));
		register(SIGIL_TRUE, () -> () -> new VariableSigil.Constant(VarBool.TRUE));
		register(SIGIL_2, () -> () -> VariableSigil.Constant.doubleConst(2D));
		register(SIGIL_4, () -> () -> VariableSigil.Constant.doubleConst(4D));
		register(SIGIL_8, () -> () -> VariableSigil.Constant.doubleConst(8D));
		register(SIGIL_16, () -> () -> VariableSigil.Constant.doubleConst(16D));
		register(SIGIL_PI, () -> () -> VariableSigil.Constant.doubleConst(Math.PI));
		registerLocalVariables();
		registerVectorConstants();
		registerElementConstants();
		
		register(GLYPH_DUMMY, () -> () -> new ComponentGlyph.Dummy());
		
		register(GLYPH_SET, () -> () -> new OperationGlyph.Set());
		register(GLYPH_ADD, () -> () -> new OperationGlyph.Add());
		register(GLYPH_SUB, () -> () -> new OperationGlyph.Subtract());
		register(GLYPH_MUL, () -> () -> new OperationGlyph.Multiply());
		register(GLYPH_DIV, () -> () -> new OperationGlyph.Divide());
		register(GLYPH_MOD, () -> () -> new OperationGlyph.Modulus());
		register(GLYPH_RAND, () -> () -> new RandomGlyph());
		
		register(GLYPH_EQU, () -> () -> new ComparisonGlyph.Equals());
		register(GLYPH_GRE, () -> () -> new ComparisonGlyph.Greater());
		register(GLYPH_LES, () -> () -> new ComparisonGlyph.Less());
		register(GLYPH_AND, () -> () -> new ComparisonGlyph.And());
		register(GLYPH_OR, () -> () -> new ComparisonGlyph.Or());
		register(GLYPH_NAND, () -> () -> new ComparisonGlyph.NAnd());
		register(GLYPH_XOR, () -> () -> new ComparisonGlyph.XOR());
		
		register(GLYPH_MAKEVEC, () -> () -> new VectorGlyph.Compose());
		register(GLYPH_DOT, () -> () -> new VectorGlyph.Dot());
		register(GLYPH_CROSS, () -> () -> new VectorGlyph.Cross());
		register(GLYPH_NORMALISE, () -> () -> new VectorGlyph.Normalise());
		register(GLYPH_LENGTH, () -> () -> new VectorGlyph.Length());
		
		register(GLYPH_ENTITIES, () -> () -> new WorldGlyph.EntitiesWithin());
		register(GLYPH_TRACE, () -> () -> new WorldGlyph.RayTrace());
		register(GLYPH_IS_EMPTY, () -> () -> new WorldGlyph.IsBlockEmpty());
		
		register(GLYPH_ENT_POS, () -> () -> new EntityGlyph.Position());
		register(GLYPH_ENT_LOOK, () -> () -> new EntityGlyph.Look());
		register(GLYPH_ENT_VEL, () -> () -> new EntityGlyph.Motion());
		register(GLYPH_ENT_HEALTH, () -> () -> new EntityGlyph.Health());
		register(GLYPH_ENT_TARGET, () -> () -> new EntityGlyph.Target());
		
		register(GLYPH_STACK_GET, () -> () -> new StackGlyph.StackGet());
		register(GLYPH_STACK_ADD, () -> () -> new StackGlyph.StackAdd());
		register(GLYPH_STACK_SUB, () -> () -> new StackGlyph.StackSub());
		
		register(FUNCTION_DEBUG, () -> () -> new FunctionGlyph.Debug());
		register(FUNCTION_MOVE, () -> () -> new FunctionGlyph.AddMotion());
		register(FUNCTION_CREATE, () -> () -> new FunctionGlyph.Create());
		register(FUNCTION_DISPEL, () -> () -> new FunctionGlyph.Dispel());
		register(FUNCTION_STATUS, () -> () -> new FunctionGlyph.StatusEffect());
	}
	
	private static RegistryObject<ISpellComponentBuilder> register(ResourceLocation nameIn, Supplier<ISpellComponentBuilder> miracleIn)
	{
		return COMPONENTS.register(nameIn.getPath(), miracleIn);
	}
	
	/** Returns a list of fresh components within the given category */
	public static List<ISpellComponent> byCategory(Category cat, boolean placeableOnly)
	{
		List<ISpellComponent> components = Lists.newArrayList();
		for(RegistryObject<ISpellComponentBuilder> entry : COMPONENTS.getEntries())
		{
			ISpellComponent comp = entry.get().create();
			if(comp.category() == cat && (!placeableOnly || placeableOnly && comp.playerPlaceable()))
				components.add(create(entry.getId()));
		}
		
		return components;
	}
	
	private static void registerLocalVariables()
	{
		for(VariableSet.Slot slot : VariableSet.Slot.values())
			register(slot.glyph(), () -> () -> Slot.makeGlyph(slot));
	}
	
	private static void registerElementConstants()
	{
		for(Element element : Element.values())
			register(make(element.getSerializedName()+"_element"), () -> () -> new VariableSigil.Constant(new VarElement(element), Category.ELEMENT));
	}
	
	private static void registerVectorConstants()
	{
		List<IVariable> dirVariables = Lists.newArrayList();
		for(Direction dir : Direction.values())
		{
			Vec3i normal = dir.getNormal();
			IVariable dirVar = new VarVec(new Vec3(normal.getX(), normal.getY(), normal.getZ()));
			register(make(dir.getSerializedName()+"_sigil"), () -> () -> new VariableSigil.Constant(dirVar));
			dirVariables.add(dirVar);
		}
		
		register(SIGIL_XYZ, () -> () -> new VariableSigil.Constant(new VarStack(dirVariables.toArray(new IVariable[0]))));
	}
	
	public static ISpellComponent create(ResourceLocation registryName)
	{
		for(RegistryObject<ISpellComponentBuilder> entry : COMPONENTS.getEntries())
			if(entry.getId().equals(registryName))
			{
				ISpellComponent component = entry.get().create();
				component.setRegistryName(entry.getId());
				return component;
			}
		
		return create(GLYPH_DUMMY);
	}
	
	public static void reportInit(final FMLLoadCompleteEvent event)
	{
		Misc19.LOG.info("# Reporting registered spell components #");
			for(Category cat : Category.values())
			{
				List<ResourceLocation> components = Lists.newArrayList();
				for(RegistryObject<ISpellComponentBuilder> entry : COMPONENTS.getEntries())
					if(entry.get().create().category() == cat)
						components.add(entry.getId());
				
				if(!components.isEmpty())
				{
					Misc19.LOG.info("# Added "+components.size()+" "+cat.name());
					components.forEach((id) -> Misc19.LOG.info("# * "+id.toString()));
				}
			}
		Misc19.LOG.info("# "+COMPONENTS.getEntries().size()+" total components #");
		
//		runComponentTests();
	}
	
	@SuppressWarnings("unused")
	private static void runComponentTests()
	{
		Misc19.LOG.info("Running component tests");
		
		ISpellComponent testIndex = create(CIRCLE_BASIC).addInputs(create(SIGIL_XYZ)).addOutputs(create(GLYPH_SET).addInputs(create(Slot.INDEX.glyph())).addOutputs(create(Slot.BAST.glyph())));
		Misc19.LOG.info("Circle index test: "+((VariableSigil)create(SIGIL_XYZ)).get(null).asDouble()+" runs = index "+testIndex.execute(new VariableSet()).get(Slot.BAST).asDouble());
		
		runArithmeticTests();
		runLogicTests();
		runVectorTests();
	}
	
	private static void runArithmeticTests()
	{
		ISpellComponent testAdd = create(GLYPH_ADD).addInputs(create(SIGIL_4), create(SIGIL_2)).addOutputs(create(Slot.BAST.glyph()));
		Misc19.LOG.info("Add 4 + 2 test: "+testAdd.execute(new VariableSet()).get(Slot.BAST).asDouble());
		
		ISpellComponent testSub = create(GLYPH_SUB).addInputs(create(SIGIL_4), create(SIGIL_2)).addOutputs(create(Slot.BAST.glyph()));
		Misc19.LOG.info("Subtract 4 - 2 test: "+testSub.execute(new VariableSet()).get(Slot.BAST).asDouble());
		
		ISpellComponent testMul = create(GLYPH_MUL).addInputs(create(SIGIL_4), create(SIGIL_2)).addOutputs(create(Slot.BAST.glyph()));
		Misc19.LOG.info("Multiply 4 * 2 test: "+testMul.execute(new VariableSet()).get(Slot.BAST).asDouble());
		
		ISpellComponent testDiv = create(GLYPH_DIV).addInputs(create(SIGIL_4), create(SIGIL_2)).addOutputs(create(Slot.BAST.glyph()));
		Misc19.LOG.info("Divide 4 / 2 test: "+testDiv.execute(new VariableSet()).get(Slot.BAST).asDouble());
		
		ISpellComponent testMod = create(GLYPH_MOD).addInputs(create(GLYPH_ADD).addInputs(create(SIGIL_TRUE),create(SIGIL_XYZ)), create(SIGIL_2)).addOutputs(create(Slot.BAST.glyph()));
		Misc19.LOG.info("Modulus 7 % 2 test: "+testMod.execute(new VariableSet()).get(Slot.BAST).asDouble());
	}
	
	private static void runLogicTests()
	{
		Misc19.LOG.info("2-bit adder tests:");
		runAdderTest(SIGIL_FALSE, SIGIL_FALSE, SIGIL_FALSE);
		runAdderTest(SIGIL_TRUE, SIGIL_FALSE, SIGIL_FALSE);
		runAdderTest(SIGIL_FALSE, SIGIL_TRUE, SIGIL_FALSE);
		runAdderTest(SIGIL_TRUE, SIGIL_TRUE, SIGIL_FALSE);
		runAdderTest(SIGIL_FALSE, SIGIL_FALSE, SIGIL_TRUE);
		runAdderTest(SIGIL_TRUE, SIGIL_FALSE, SIGIL_TRUE);
		runAdderTest(SIGIL_FALSE, SIGIL_TRUE, SIGIL_TRUE);
	}
	
	private static void runAdderTest(ResourceLocation bit0, ResourceLocation bit1, ResourceLocation bit2)
	{
		ISpellComponent circle = create(ROOT_DUMMY).addOutputs(create(CIRCLE_BASIC).addOutputs(
				create(GLYPH_SET).addInputs(create(bit0)).addOutputs(create(Slot.BAST.glyph())),
				create(GLYPH_SET).addInputs(create(bit1)).addOutputs(create(Slot.THOTH.glyph())),
				create(GLYPH_SET).addInputs(create(bit2)).addOutputs(create(Slot.SUTEKH.glyph())),
				create(GLYPH_XOR).addInputs(create(Slot.BAST.glyph()), create(Slot.THOTH.glyph())).addOutputs(create(Slot.ANUBIS.glyph())),
				create(GLYPH_AND).addInputs(create(Slot.ANUBIS.glyph()), create(Slot.SUTEKH.glyph())).addOutputs(create(Slot.HORUS.glyph())),
				create(GLYPH_AND).addInputs(create(Slot.BAST.glyph()), create(Slot.THOTH.glyph())).addOutputs(create(Slot.ISIS.glyph())),
				create(GLYPH_OR).addInputs(create(Slot.HORUS.glyph()), create(Slot.ISIS.glyph())).addOutputs(create(Slot.RA.glyph())),
				create(GLYPH_XOR).addInputs(create(Slot.ANUBIS.glyph()), create(Slot.SUTEKH.glyph())).addOutputs(create(Slot.OSIRIS.glyph()))));
		
		CompoundTag circleData = ISpellComponent.saveToNBT(circle);
		ISpellComponent circle2 = readFromNBT(circleData);
		
		VariableSet variables = new VariableSet();
		variables = circle2.execute(variables);
		
		boolean var0 = ((VariableSigil)create(bit0)).get(null).asBoolean();
		boolean var1 = ((VariableSigil)create(bit1)).get(null).asBoolean();
		boolean var2 = ((VariableSigil)create(bit2)).get(null).asBoolean();
		
		boolean anubis = (var0 || var1) && var0 != var1;
		boolean osiris = (anubis || var2) && anubis != var2;
		boolean ra = (anubis && var2) || (var0 && var1);
		
		boolean testPassed = osiris == variables.get(Slot.OSIRIS).asBoolean() && ra == variables.get(Slot.RA).asBoolean();
		
		Misc19.LOG.info(" * "+(var0 ? 1 : 0)+" + "+(var1 ? 1 : 0)+" ("+(var2 ? 1 : 0)+") = "+(int)variables.get(Slot.OSIRIS).asDouble()+" ("+(int)variables.get(Slot.RA).asDouble()+") "+(testPassed ? "PASSED" : "FAILED"));
	}
	
	private static void runVectorTests()
	{
		ISpellComponent dotTest = create(ROOT_DUMMY).addOutputs(create(CIRCLE_BASIC).addOutputs(
				create(GLYPH_DOT).addInputs(create(make(Direction.NORTH.getSerializedName()+"_glyph")), create(make(Direction.SOUTH.getSerializedName()+"_glyph"))).addOutputs(create(VariableSet.Slot.SOBEK.glyph()))
				));
		Misc19.LOG.info("Dot product of "+Direction.NORTH.getNormal().toShortString()+" and "+Direction.SOUTH.getNormal().toShortString()+": "+dotTest.execute(new VariableSet()).get(Slot.SOBEK).asDouble());
		
	}
	
	@Nonnull
	public static ISpellComponent readFromNBT(CompoundTag nbt)
	{
		ResourceLocation registryName = nbt.contains("ID", Tag.TAG_STRING) ? new ResourceLocation(Reference.ModInfo.MOD_ID, nbt.getString("ID")) : GLYPH_DUMMY;
		ISpellComponent component = create(registryName);
		
		if(nbt.contains("Data", Tag.TAG_COMPOUND))
			component.deserialiseNBT(nbt.getCompound("Data"));
		
		if(nbt.contains("Input", Tag.TAG_LIST))
			nbt.getList("Input", Tag.TAG_COMPOUND).forEach((tag) -> component.addInput(readFromNBT((CompoundTag)tag)));
		
		if(nbt.contains("Output", Tag.TAG_LIST))
			nbt.getList("Output", Tag.TAG_COMPOUND).forEach((tag) -> component.addOutput(readFromNBT((CompoundTag)tag)));
		
		return component;
	}
}
