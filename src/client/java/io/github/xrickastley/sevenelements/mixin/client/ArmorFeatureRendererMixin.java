package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

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
		} else {
			model.render(
				matrices,
				vertexConsumers.getBuffer(
					ElementGlintRenderer.ARMOR_ENTITY_GLINT.getLayer(
						itemStack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element()
					)
				),
				light,
				OverlayTexture.DEFAULT_UV
			);
		}
	}
}
