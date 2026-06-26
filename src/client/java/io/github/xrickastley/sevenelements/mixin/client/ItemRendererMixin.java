package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;

import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
	@WrapOperation(
		method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;getDynamicDisplayGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack$Entry;)Lnet/minecraft/client/render/VertexConsumer;"
		)
	)
	private VertexConsumer renderAttunementGlint$1(VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemStack itemStack) {
		if (!itemStack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)) return original.call(provider, layer, entry);

		return VertexConsumers.union(
			new OverlayVertexConsumer(
				provider.getBuffer(SevenElementsRenderLayer.getElementGlint(this.sevenelements$getElementGlintPath(itemStack))),
				entry,
				0.0078125F
			),
			provider.getBuffer(layer)
		);
	}

	@WrapOperation(
		method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;getDirectItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"
		)
	)
	private VertexConsumer renderAttunementGlint$2(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemStack itemStack) {
		if (!itemStack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)) return original.call(provider, layer, solid, glint);

		return VertexConsumers.union(
			provider.getBuffer(solid ? SevenElementsRenderLayer.getElementGlint(this.sevenelements$getElementGlintPath(itemStack)) : RenderLayer.getDirectEntityGlint()),
			provider.getBuffer(layer)
		);
	}

	@WrapOperation(
		method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;getItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"
		)
	)
	private VertexConsumer renderAttunementGlint$3(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemStack itemStack) {
		if (!itemStack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)) return original.call(vertexConsumers, layer, solid, glint);

		return VertexConsumers.union(
			vertexConsumers.getBuffer(SevenElementsRenderLayer.getElementGlint(this.sevenelements$getElementGlintPath(itemStack))),
			vertexConsumers.getBuffer(layer)
		);
	}

	@Unique
	private Identifier sevenelements$getElementGlintPath(ItemStack stack) {
		if (!stack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT))
			throw new IllegalArgumentException();

		final ElementalAttunementComponent elementalAttunement = stack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);
		return SevenElements.identifier("textures/misc/" + elementalAttunement.element().getId().getPath() + "_enchanted_glint_item.png");
	}
}
