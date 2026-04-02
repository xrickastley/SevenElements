package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import java.util.SequencedMap;
import java.util.function.Function;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public class SevenElementsRenderLayer {
	private static final RenderType TRIANGLES = RenderType.create(
		"seven-elements:triangles",
		RenderSetup
			.builder(SevenElementsRenderPipelines.TRIANGLES)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType QUADS = RenderType.create(
		"seven-elements:quads",
		RenderSetup
			.builder(SevenElementsRenderPipelines.QUADS)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType RULER_LINES = RenderType.create(
		"seven-elements:ruler_lines",
		RenderSetup
			.builder(SevenElementsRenderPipelines.LINES)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final Function<Identifier, RenderType> ELEMENTS = Util.memoize(
		texture -> {
			return RenderType.create(
				"seven-elements:elements",
				RenderSetup
					.builder(SevenElementsRenderPipelines.ELEMENTS)
					.setOutline(OutlineProperty.NONE)
					.withTexture("Sampler0", texture)
					.createRenderSetup()
			);
		}
	);

	private static final RenderType WORLD_TEXT = RenderType.create(
		"seven-elements:world/text",
		RenderSetup
			.builder(SevenElementsRenderPipelines.WORLD_TEXT)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType CHARGE_LINE = RenderType.create(
		"seven-elements:world/charge_line",
		RenderSetup
			.builder(SevenElementsRenderPipelines.CHARGE_LINE)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType SPHERE = RenderType.create(
		"seven-elements:sphere",
		RenderSetup
			.builder(SevenElementsRenderPipelines.SPHERE)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final SequencedMap<RenderType, ByteBufferBuilder> WORLD_TEXT_SEQUENCED_MAP = Util.make(
		new Object2ObjectLinkedOpenHashMap<>(), map -> {
			map.put(SevenElementsRenderLayer.WORLD_TEXT, SevenElementsRenderer.createAllocator(786432));
		}
	);

	private static final MultiBufferSource.BufferSource WORLD_TEXT_IMMEDIATE = MultiBufferSource.immediateWithBuffers(WORLD_TEXT_SEQUENCED_MAP, SevenElementsRenderer.createAllocator(1536));

	public static RenderType getTriangles() {
		return SevenElementsRenderLayer.TRIANGLES;
	}

	public static RenderType getQuads() {
		return SevenElementsRenderLayer.QUADS;
	}

	public static RenderType getLines() {
		return SevenElementsRenderLayer.RULER_LINES;
	}

	public static Function<Identifier, RenderType> getElements() {
		return SevenElementsRenderLayer.ELEMENTS;
	}

	public static RenderType getElements(Identifier texture) {
		return SevenElementsRenderLayer.ELEMENTS.apply(texture);
	}

	public static RenderType getWorldText() {
		return SevenElementsRenderLayer.WORLD_TEXT;
	}

	public static RenderType getChargeLine() {
		return SevenElementsRenderLayer.CHARGE_LINE;
	}

	public static RenderType getSphere() {
		return SevenElementsRenderLayer.SPHERE;
	}

	public static MultiBufferSource.BufferSource getWorldTextImmediate() {
		return WORLD_TEXT_IMMEDIATE;
	}
}
