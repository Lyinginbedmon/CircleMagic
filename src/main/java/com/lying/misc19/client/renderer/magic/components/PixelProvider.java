package com.lying.misc19.client.renderer.magic.components;

import java.util.List;

import com.lying.misc19.client.SpellTexture;

public interface PixelProvider
{
	public void applyTo(SpellTexture texture, List<PixelProvider> conflictors);
	
	/** Returns true if this provider should prevent anything being drawn at the given coordinates */
	public default boolean shouldExclude(int x, int y, int texWidth, int texHeight, int resolution) { return false; }
}
