package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.ItemStack;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {
	@ModifyExpressionValue(
		method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/RenderLayers;armorEntityGlint()Lnet/minecraft/client/render/RenderLayer;"
		)
	)
	private RenderLayer renderAttunementGlint(RenderLayer original, @Local(argsOnly = true) ItemStack itemStack) {
		return ElementGlintRenderer.getArmorEntityGlintLayer(original, itemStack);
	}
}
