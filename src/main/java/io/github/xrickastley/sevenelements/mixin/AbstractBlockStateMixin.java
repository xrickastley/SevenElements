package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

// Prioritized since Frozen **MUST** disable block placements.
@Mixin(value = AbstractBlockState.class, priority = Integer.MIN_VALUE)
public class AbstractBlockStateMixin {
	@ModifyReturnValue(
		method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
		at = @At("RETURN")
	)
	private VoxelShape frozenPreventsBlockPlace(VoxelShape original, @Local(argsOnly = true) ShapeContext context) {
		return context instanceof final EntityShapeContext esc
			&& esc.getEntity() instanceof final PlayerEntity player
			&& player.hasStatusEffect(SevenElementsStatusEffects.FROZEN)
			? VoxelShapes.empty()
			: original;
	}
}
