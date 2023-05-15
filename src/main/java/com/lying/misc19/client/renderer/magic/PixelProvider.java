package com.lying.misc19.client.renderer.magic;

import java.util.List;

import com.lying.misc19.client.SpellTexture;

public interface PixelProvider
{
	public void applyTo(SpellTexture texture, List<PixelProvider> conflictors);
	
	public default boolean shouldExclude(int x, int y) { return false; }
}
