package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
	public ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
		super(context);

		throw new AssertionError();
	}

	@WrapOperation(
		method = "renderArmor",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderGlint(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V"
		)
	)
	private void renderAttunementGlint(ArmorFeatureRenderer<T, M, A> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model, Operation<Void> original, @Local ItemStack itemStack) {
		if (!itemStack.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)) {
			original.call(instance, matrices, vertexConsumers, light, model);

			return;
		}

		final ElementalAttunementComponent elementalAttunement = itemStack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);
		final Identifier glintPath = SevenElements.identifier("textures/misc/" + elementalAttunement.element().getId().getPath() + "_enchanted_glint_entity.png");

		model.render(matrices, vertexConsumers.getBuffer(SevenElementsRenderLayer.getElementArmorEntityGlint(glintPath)), light, OverlayTexture.DEFAULT_UV);
	}
}
