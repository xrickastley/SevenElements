package io.github.xrickastley.sevenelements.mixin.priority;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;

@Mixin(value = { LivingEntity.class, AbstractHorse.class }, priority = Integer.MIN_VALUE)
public abstract class JumpingEntityMixin extends Entity {
	public JumpingEntityMixin(EntityType<?> type, Level world) {
		super(type, world);
		throw new AssertionError();
	}

	@Inject(
		require = 1,
		method = { "jumpFromGround", "executeRidersJump" },
		at = @At("HEAD"),
		cancellable = true,
		remap = false,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsJumping(CallbackInfo ci) {
		final @Nullable LivingEntity entity = ClassInstanceUtil.castOrNull(this, LivingEntity.class);
		final @Nullable LivingEntity controller = this.getControllingPassenger();

		if ((entity != null && entity.hasEffect(SevenElementsStatusEffects.FROZEN))
			|| (controller != null && controller.hasEffect(SevenElementsStatusEffects.FROZEN))
		) ci.cancel();
	}
}
