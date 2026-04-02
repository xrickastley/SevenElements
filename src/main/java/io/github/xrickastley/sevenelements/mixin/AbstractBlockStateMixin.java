package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Prioritized since Frozen **MUST** disable block placements.
@Mixin(value = BlockStateBase.class, priority = Integer.MIN_VALUE)
public class AbstractBlockStateMixin {
	@ModifyReturnValue(
		method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At("RETURN")
	)
	private VoxelShape frozenPreventsBlockPlace(VoxelShape original, @Local(argsOnly = true) CollisionContext context) {
		return context instanceof final EntityCollisionContext esc
			&& esc.getEntity() instanceof final Player player
			&& player.hasEffect(SevenElementsStatusEffects.FROZEN)
			? Shapes.empty()
			: original;
	}
}
