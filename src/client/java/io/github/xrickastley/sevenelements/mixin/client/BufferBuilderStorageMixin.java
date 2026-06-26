package io.github.xrickastley.sevenelements.mixin.client;

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
		method = "assignBufferBuilder",
		at = @At("TAIL")
	)
	private static void addCustomGlintLayers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> builderStorage, RenderLayer layer, CallbackInfo ci) {
		if (layer == RenderLayer.getGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_item.png")
				.map(SevenElementsRenderLayer::getElementGlint)
				.forEach(layer2 -> assignBufferBuilder(builderStorage, layer2));
		} else if (layer == RenderLayer.getArmorEntityGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_entity.png")
				.map(SevenElementsRenderLayer::getElementArmorEntityGlint)
				.forEach(layer2 -> assignBufferBuilder(builderStorage, layer2));
		}
	}

	@Unique
	private static Stream<Identifier> sevenelements$getAllElementGlintPaths(final String prefix) {
		return Stream.of(Element.values())
			.map(element -> SevenElements.identifier("textures/misc/" + element.getId().getPath() + prefix));
	}
}
