package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.BufferBuilder;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
	@Accessor("building")
	public boolean isBuilding();
}
