package com.lying.misc19.blocks;

import com.lying.misc19.world.GenericTreeHolder;
import com.lying.misc19.world.M19WorldEvents;

import net.minecraft.world.level.block.SaplingBlock;

public class MagicSapling extends SaplingBlock
{
	public MagicSapling(Properties propertiesIn)
	{
		super(new GenericTreeHolder(() -> M19WorldEvents.MAGIC_TREE), propertiesIn);
	}
}
