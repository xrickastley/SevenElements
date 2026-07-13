package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

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

import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

@Mixin(RenderBuffers.class)
public class RenderBuffersMixin {
	@Shadow
	private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> builderStorage, RenderType layer) {
		throw new AssertionError();
	}

	@Inject(
		method = "lambda$new$0",
		at = @At("TAIL")
	)
	private void addSevenElementsRenderLayers(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, CallbackInfo ci) {
		put(map, SevenElementsRenderLayers.getCrystallizeShield());

		Stream.of(Element.values())
			.map(Element::getTexture)
			.distinct()
			.filter(Objects::nonNull)
			.map(SevenElementsRenderLayers.getElements())
			.forEach(layer -> put(map, layer));

		put(map, SevenElementsRenderLayers.getGaugeDisplay());
		put(map, SevenElementsRenderLayers.getLines());
	}

	@Inject(
		method = "put",
		at = @At("TAIL")
	)
	private static void addCustomGlintLayers(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> builderStorage, RenderType layer, CallbackInfo ci) {
		// could technically make this an event, but I don't really need it right now
		if (layer == RenderTypes.armorEntityGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_armor.png")
				.map(SevenElementsRenderLayers.getArmorEntityElementGlint())
				.forEach(layer2 -> put(builderStorage, layer2));
		} else if (layer == RenderTypes.glint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_item.png")
				.mapMulti(sevenelements$mapMultipleRenderLayers(
					SevenElementsRenderLayers.getStaticElementGlint(),
					SevenElementsRenderLayers.getElementGlint()
				))
				.forEach(layer2 -> put(builderStorage, layer2));
		} else if (layer == RenderTypes.entityGlint()) {
			sevenelements$getAllElementGlintPaths("_enchanted_glint_armor.png")
				.mapMulti(sevenelements$mapMultipleRenderLayers(
					SevenElementsRenderLayers.getStaticEntityElementGlint(),
					SevenElementsRenderLayers.getEntityElementGlint()
				))
				.forEach(layer2 -> put(builderStorage, layer2));
		}
	}

	@Unique
	private static Stream<Identifier> sevenelements$getAllElementGlintPaths(final String prefix) {
		return Stream.of(Element.values())
			.map(element -> SevenElements.identifier("textures/misc/" + element.getId().getPath() + prefix));
	}

	@Unique
	@SafeVarargs
	private static BiConsumer<Identifier, Consumer<RenderType>> sevenelements$mapMultipleRenderLayers(final Function<Identifier, RenderType>... renderLayerFunctions) {
		return (id, consumer) -> {
			for (final Function<Identifier, RenderType> fn : renderLayerFunctions)
				consumer.accept(fn.apply(id));
		};
	}
}
