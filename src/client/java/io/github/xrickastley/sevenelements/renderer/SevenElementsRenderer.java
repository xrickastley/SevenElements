package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.mixin.client.BufferBuilderAccessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;

public class SevenElementsRenderer {
	private static final List<BufferAllocator> ALLOCATORS = new ArrayList<>();

	public static BufferBuilder createBuffer(final BufferAllocator allocator, final RenderPipeline pipeline) {
		return SevenElementsRenderer.createBuffer((BufferBuilder) null, allocator, pipeline);
	}

	public static BufferBuilder createBuffer(final @Nullable BufferBuilder buffer, final BufferAllocator allocator, final RenderPipeline pipeline) {
		return buffer == null || !((BufferBuilderAccessor) buffer).isBuilding()
			? new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat())
			: buffer;
	}

	public static BufferAllocator createAllocator(final Supplier<RenderLayer> layer) {
		return SevenElementsRenderer.createAllocator(layer.get());
	}

	public static BufferAllocator createAllocator(final RenderLayer layer) {
		return SevenElementsRenderer.createAllocator(layer.getExpectedBufferSize());
	}

	public static BufferAllocator createAllocator(final int size) {
		final BufferAllocator allocator = new BufferAllocator(size);

		SevenElementsRenderer.ALLOCATORS.add(allocator);

		return allocator;
	}

	public static void close() {
		SevenElementsRenderer.ALLOCATORS.forEach(BufferAllocator::close);
	}
}
