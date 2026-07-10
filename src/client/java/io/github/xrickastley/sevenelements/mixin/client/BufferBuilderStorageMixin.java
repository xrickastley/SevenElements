package io.github.xrickastley.sevenelements.mixin.client;

import java.util.Objects;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;

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
}
