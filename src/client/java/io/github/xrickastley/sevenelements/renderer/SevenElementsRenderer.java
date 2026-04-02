package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.mixin.client.BufferBuilderAccessor;

import net.minecraft.client.renderer.rendertype.RenderType;

public class SevenElementsRenderer {
	private static final List<ByteBufferBuilder> ALLOCATORS = new ArrayList<>();

	public static BufferBuilder createBuffer(final ByteBufferBuilder allocator, final RenderPipeline pipeline) {
		return SevenElementsRenderer.createBuffer((BufferBuilder) null, allocator, pipeline);
	}

	public static BufferBuilder createBuffer(final @Nullable BufferBuilder buffer, final ByteBufferBuilder allocator, final RenderPipeline pipeline) {
		return buffer == null || !((BufferBuilderAccessor) buffer).isBuilding()
			? new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat())
			: buffer;
	}

	public static ByteBufferBuilder createAllocator(final Supplier<RenderType> layer) {
		return SevenElementsRenderer.createAllocator(layer.get());
	}

	public static ByteBufferBuilder createAllocator(final RenderType layer) {
		return SevenElementsRenderer.createAllocator(layer.bufferSize());
	}

	public static ByteBufferBuilder createAllocator(final int size) {
		final ByteBufferBuilder allocator = new ByteBufferBuilder(size);

		SevenElementsRenderer.ALLOCATORS.add(allocator);

		return allocator;
	}

	public static void close() {
		SevenElementsRenderer.ALLOCATORS.forEach(ByteBufferBuilder::close);
	}
}
