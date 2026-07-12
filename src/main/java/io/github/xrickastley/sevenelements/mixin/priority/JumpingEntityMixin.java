package io.github.xrickastley.sevenelements.mixin.priority;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.world.World;

@Mixin(value = { LivingEntity.class, AbstractHorseEntity.class }, priority = Integer.MIN_VALUE)
public abstract class JumpingEntityMixin extends Entity {
	public JumpingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
		throw new AssertionError();
	}

	@Inject(
		require = 1,
		method = { "jump", "method_6043", "method_45343" },
		at = @At("HEAD"),
		cancellable = true,
		remap = false,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsJumping(CallbackInfo ci) {
		final @Nullable LivingEntity entity = ClassInstanceUtil.castOrNull(this, LivingEntity.class);
		final @Nullable LivingEntity controller = this.getControllingPassenger();

		if ((entity != null && entity.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			|| (controller != null && controller.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
		) ci.cancel();
	}
}
