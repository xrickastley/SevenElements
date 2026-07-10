package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.ItemStack;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {
	@WrapOperation(
		method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/item/ItemRenderer;getArmorGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;Z)Lnet/minecraft/client/render/VertexConsumer;"
		)
	)
	private VertexConsumer renderAttunementGlint(VertexConsumerProvider provider, RenderLayer layer, boolean glint, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemStack itemStack) {
		return ElementGlintRenderer.getArmorGlintConsumer(original.call(provider, layer, glint), provider, layer, itemStack, glint);
	}
}
