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
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayers;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
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
		assignBufferBuilder(map, SevenElementsRenderLayers.getCrystallizeShield());

		Stream.of(Element.values())
			.map(Element::getTexture)
			.distinct()
			.filter(Objects::nonNull)
			.map(SevenElementsRenderLayers.getElements())
			.forEach(layer -> assignBufferBuilder(map, layer));

		assignBufferBuilder(map, SevenElementsRenderLayers.getGaugeDisplay());
		assignBufferBuilder(map, SevenElementsRenderLayers.getLines());
	}

	@Inject(
		method = "assignBufferBuilder",
		at = @At("TAIL")
	)
	private static void addCustomGlintLayers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> builderStorage, RenderLayer layer, CallbackInfo ci) {
		// could technically make this an event, but I don't really need it right now
		if (layer == RenderLayers.armorEntityGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_armor.png")
				.map(SevenElementsRenderLayers.getArmorEntityElementGlint())
				.forEach(layer2 -> assignBufferBuilder(builderStorage, layer2));
		} else if (layer == RenderLayers.glint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_item.png")
				.mapMulti(sevenelements$mapMultipleRenderLayers(
					SevenElementsRenderLayers.getStaticElementGlint(),
					SevenElementsRenderLayers.getElementGlint()
				))
				.forEach(layer2 -> assignBufferBuilder(builderStorage, layer2));
		} else if (layer == RenderLayers.entityGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_armor.png")
				.mapMulti(sevenelements$mapMultipleRenderLayers(
					SevenElementsRenderLayers.getStaticEntityElementGlint(),
					SevenElementsRenderLayers.getEntityElementGlint()
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
