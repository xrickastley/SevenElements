package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.renderer.genshin.SpecialEffectsRenderer;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Inject(
		method = "tick",
		at = @At("TAIL")
	)
	public void addEffectRendering(CallbackInfo ci) {
		// Sanity check for isClient in case
		if (!this.getWorld().isClient || !ClientConfig.getEffectRenderType().allowsNormalEffects() || !SpecialEffectsRenderer.shouldRender(this)) return;

		final ElementComponent component = ElementComponent.KEY.get(this);

		component
			.getPrioritizedElements()
			.forEach(Functions.composeConsumer(ElementalApplication::getElement, Functions.withArgument(Element::renderEffects, (LivingEntity)(Entity) this)));
	}
}
