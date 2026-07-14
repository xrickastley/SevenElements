package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
	@Accessor("building")
	public boolean sevenelements$isBuilding();
	@Accessor("format")
	public VertexFormat sevenelements$getVertexFormat();
	@Accessor("primitiveTopology")
	public PrimitiveTopology sevenelements$getPrimitiveTopology();
}
