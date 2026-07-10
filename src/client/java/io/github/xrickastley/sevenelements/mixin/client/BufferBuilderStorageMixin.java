package io.github.xrickastley.sevenelements.mixin.client;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

@Mixin(BufferBuilderStorage.class)
public class BufferBuilderStorageMixin {
	@Shadow
	private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> builderStorage, RenderLayer layer) {
		throw new AssertionError();
	}

	@Inject(
		method = "method_54639",
		at = @At("TAIL")
	)
	private void addSevenElementsRenderLayers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map, CallbackInfo ci) {
		assignBufferBuilder(map, SevenElementsRenderLayer.getCrystallizeShield());

		Stream.of(Element.values())
			.map(Element::getTexture)
			.distinct()
			.filter(Objects::nonNull)
			.map(SevenElementsRenderLayer.getElements())
			.forEach(layer -> assignBufferBuilder(map, layer));

		assignBufferBuilder(map, SevenElementsRenderLayer.getGaugeDisplay());
		assignBufferBuilder(map, SevenElementsRenderLayer.getThinLines());
		assignBufferBuilder(map, SevenElementsRenderLayer.getThickLines());
	}

	@Inject(
		method = "assignBufferBuilder",
		at = @At("TAIL")
	)
	private static void addCustomGlintLayers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> builderStorage, RenderLayer layer, CallbackInfo ci) {
		// could technically make this an event, but I don't really need it right now
		if (layer == RenderLayer.getArmorEntityGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_armor.png")
				.map(SevenElementsRenderLayer.getArmorEntityElementGlint())
				.forEach(layer2 -> assignBufferBuilder(builderStorage, layer2));
		} else if (layer == RenderLayer.getGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_item.png")
				.mapMulti(sevenelements$mapMultipleRenderLayers(
					SevenElementsRenderLayer.getStaticElementGlint(),
					SevenElementsRenderLayer.getElementGlint()
				))
				.forEach(layer2 -> assignBufferBuilder(builderStorage, layer2));
		} else if (layer == RenderLayer.getEntityGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_armor.png")
				.mapMulti(sevenelements$mapMultipleRenderLayers(
					SevenElementsRenderLayer.getStaticEntityElementGlint(),
					SevenElementsRenderLayer.getEntityElementGlint()
				))
				.forEach(layer2 -> assignBufferBuilder(builderStorage, layer2));
		}
	}

	@Unique
	private static Stream<Identifier> sevenelements$getAllElementGlintPaths(final String prefix) {
		return Stream.of(Element.values())
			.map(element -> SevenElements.identifier("textures/misc/" + element.getId().getPath() + prefix));
	}

	@Unique
	@SafeVarargs
	private static BiConsumer<Identifier, Consumer<RenderLayer>> sevenelements$mapMultipleRenderLayers(final Function<Identifier, RenderLayer>... renderLayerFunctions) {
		return (id, consumer) -> {
			for (final Function<Identifier, RenderLayer> fn : renderLayerFunctions)
				consumer.accept(fn.apply(id));
		};
	}
}
