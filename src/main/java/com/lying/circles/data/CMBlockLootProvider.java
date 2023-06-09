package com.lying.circles.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.lying.circles.blocks.Crucible;
import com.lying.circles.blocks.MagicTree;
import com.lying.circles.init.CMBlocks;
import com.lying.circles.init.CMItems;
import com.lying.circles.reference.Reference;
import com.mojang.datafixers.util.Pair;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CMBlockLootProvider extends LootTableProvider
{
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = new ArrayList<>();
    private final ExistingFileHelper existingFileHelper;
    
	public CMBlockLootProvider(DataGenerator generator, ExistingFileHelper fileHelper)
	{
		super(generator);
		this.existingFileHelper = fileHelper;
	}
	
	@Override
	public String getName()
	{
		return "Circle Magic block loot tables";
	}
	
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
    {
        tables.clear();
        
        addBlockLootTable("blocks/sandbox", dropSelf(CMItems.SANDBOX_ITEM.get()));
        addBlockLootTable("blocks/crucible", whenPropertyDropItem(CMBlocks.CRUCIBLE.get(), Crucible.HALF, DoubleBlockHalf.LOWER, CMItems.CRUCIBLE_ITEM.get()));
        
        addBlockLootTable("blocks/magic_sapling", dropSelf(CMItems.MAGIC_SAPLING_ITEM.get()));
        addBlockLootTable("blocks/magic_tree", whenPropertyDropItem(CMBlocks.MAGIC_TREE.get(), MagicTree.HALF, DoubleBlockHalf.LOWER, CMItems.MAGIC_SAPLING_ITEM.get()));
        
        addBlockLootTable("blocks/inscribed_stone", dropSelf(CMItems.INSCRIBED_STONE_ITEM.get()));
        addBlockLootTable("blocks/imbued_stone", dropSelf(CMItems.IMBUED_STONE_ITEM.get()));
        
        addBlockLootTable("blocks/tilled_sand", dropSelf(Blocks.SAND));
        addBlockLootTable("blocks/tilled_red_sand", dropSelf(Blocks.RED_SAND));
        
        addBlockLootTable("blocks/inscribed_acacia", dropSelf(CMItems.INSCRIBED_ACACIA_ITEM.get()));
        addBlockLootTable("blocks/imbued_acacia", dropSelf(CMItems.IMBUED_ACACIA_ITEM.get()));
        addBlockLootTable("blocks/inscribed_birch", dropSelf(CMItems.INSCRIBED_BIRCH_ITEM.get()));
        addBlockLootTable("blocks/imbued_birch", dropSelf(CMItems.IMBUED_BIRCH_ITEM.get()));
        addBlockLootTable("blocks/inscribed_crimson", dropSelf(CMItems.INSCRIBED_CRIMSON_ITEM.get()));
        addBlockLootTable("blocks/imbued_crimson", dropSelf(CMItems.IMBUED_CRIMSON_ITEM.get()));
        addBlockLootTable("blocks/inscribed_dark_oak", dropSelf(CMItems.INSCRIBED_DARK_OAK_ITEM.get()));
        addBlockLootTable("blocks/imbued_dark_oak", dropSelf(CMItems.IMBUED_DARK_OAK_ITEM.get()));
        addBlockLootTable("blocks/inscribed_jungle", dropSelf(CMItems.INSCRIBED_JUNGLE_ITEM.get()));
        addBlockLootTable("blocks/imbued_jungle", dropSelf(CMItems.IMBUED_JUNGLE_ITEM.get()));
        addBlockLootTable("blocks/inscribed_mangrove", dropSelf(CMItems.INSCRIBED_MANGROVE_ITEM.get()));
        addBlockLootTable("blocks/imbued_mangrove", dropSelf(CMItems.IMBUED_MANGROVE_ITEM.get()));
        addBlockLootTable("blocks/inscribed_oak", dropSelf(CMItems.INSCRIBED_OAK_ITEM.get()));
        addBlockLootTable("blocks/imbued_oak", dropSelf(CMItems.IMBUED_OAK_ITEM.get()));
        addBlockLootTable("blocks/inscribed_spruce", dropSelf(CMItems.INSCRIBED_SPRUCE_ITEM.get()));
        addBlockLootTable("blocks/imbued_spruce", dropSelf(CMItems.IMBUED_SPRUCE_ITEM.get()));
        addBlockLootTable("blocks/inscribed_warped", dropSelf(CMItems.INSCRIBED_WARPED_ITEM.get()));
        addBlockLootTable("blocks/imbued_warped", dropSelf(CMItems.IMBUED_WARPED_ITEM.get()));
        
        return tables;
    }
    
    private void addBlockLootTable(String location, LootTable.Builder lootTable)
	{
		addLootTable(location, lootTable, LootContextParamSets.BLOCK);
	}
    
    private static LootTable.Builder dropSelf(ItemLike item)
    {
    	return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item)));
    }
    
    private void addLootTable(String location, LootTable.Builder lootTable, LootContextParamSet lootParameterSet)
    {
        if(location.startsWith("inject/"))
        {
            String actualLocation = location.replace("inject/", "");
            Preconditions.checkArgument(existingFileHelper.exists(new ResourceLocation("loot_tables/" + actualLocation + ".json"), PackType.SERVER_DATA), "Loot table %s does not exist in any known data pack", actualLocation);
        }
        tables.add(Pair.of
        		(
        				() -> lootBuilder -> lootBuilder.accept(new ResourceLocation(Reference.ModInfo.MOD_ID, location), lootTable), 
        				lootParameterSet
        		));
    }
    
    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker)
    {
        map.forEach((loc, table) -> LootTables.validate(validationtracker, loc, table));
    }
    
    public static <T extends Comparable<T> & StringRepresentable> LootTable.Builder whenPropertyDropItem(Block block, Property<T> property, T value, Item item)
    {
    	return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value)))));
    }
    
    protected static <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T value) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value)))));
     }
}
