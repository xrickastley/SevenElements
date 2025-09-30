package io.github.xrickastley.sevenelements.element.reaction;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.annotation.mixin.At;
import io.github.xrickastley.sevenelements.annotation.mixin.Inject;
import io.github.xrickastley.sevenelements.annotation.mixin.Local;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.networking.ShowElectroChargeS2CPayload;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class ElectroChargedElementalReaction extends ElementalReaction {
	ElectroChargedElementalReaction() {
		super(
			new Settings("Electro-Charged", SevenElements.identifier("electro-charged"), TextHelper.reaction("reaction.seven-elements.electro-charged", "#d691fc"))
				.setReactionCoefficient(0)
				.setAuraElement(Element.ELECTRO, 5)
				.setTriggeringElement(Element.HYDRO, 6)
				.applyResultAsAura(true)
				.reversable(true)
		);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		final ElementalApplication applicationAE = component.getElementalApplication(auraElement.getLeft());
		final ElementalApplication applicationTE = component.getElementalApplication(triggeringElement.getLeft());

		// We need both Elements to exist for Electro-Charged.
		return applicationAE != null && !applicationAE.isEmpty()
			&& applicationTE != null && !applicationTE.isEmpty()
			&& !component.isElectroChargedOnCD()
			&& !component.hasElementalApplication(Element.FREEZE);
	}

	@Override
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!isTriggerable(entity) || entity.getWorld().isClient) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication triggeringElement = component.getElementalApplication(this.triggeringElement.getLeft());

		final double reducedGauge = auraElement.reduceGauge(0.4);
		triggeringElement.reduceGauge(reducedGauge);

		this.onTrigger(entity, auraElement, triggeringElement, reducedGauge, origin);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final ElementComponent entityComponent = ElementComponent.KEY.get(entity);

		entityComponent.setOrRetainElectroChargedOrigin(origin);

		final Predicate<LivingEntity> predicate = e -> {
			final ElementComponent c = ElementComponent.KEY.get(e);

			return (e == entity || c.hasElementalApplication(Element.HYDRO)) && !c.isElectroChargedOnCD();
		};

		final List<LivingEntity> targets = ElementalReaction.getEntitiesInAoE(entity, 2.5, predicate);

		for (final LivingEntity target : targets) {
			final float damage = ElementalReaction.getReactionDamage(entity, 2.0);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(SevenElementsDamageTypes.ELECTRO_CHARGED, entity, origin),
				ElementalApplications.gaugeUnits(target, Element.ELECTRO, 0),
				InternalCooldownContext.ofNone(origin)
			).shouldApplyDMGBonus(false);

			target.damage(source, damage);

			ElementComponent.KEY
				.get(target)
				.resetElectroChargedCD();
		}

		this.sendDisplayPacket(entity, targets);
	}

	private void sendDisplayPacket(LivingEntity mainTarget, List<LivingEntity> otherTargets) {
		if (otherTargets.isEmpty()) return;

		final ShowElectroChargeS2CPayload packet = new ShowElectroChargeS2CPayload(mainTarget, otherTargets);

		if (mainTarget instanceof final ServerPlayerEntity serverPlayer) ServerPlayNetworking.send(serverPlayer, packet);

		for (final ServerPlayerEntity otherPlayer : PlayerLookup.tracking(mainTarget)) {
			if (otherPlayer.getId() == mainTarget.getId()) continue;

			ServerPlayNetworking.send(otherPlayer, packet);
		}
	}

	// These "mixins" are injected pieces of code that allow Electro-Charged to work properly, and allow code readers to easily see the way it was hardcoded.
	@Inject(
		method = "Lio/github/xrickastley/sevenelements/component/ElementComponentImpl;tick()V",
		at = @At("HEAD")
	)
	public static void mixin$tick(@Local(field = "owner:Lnet/minecraft/entity/LivingEntity;") LivingEntity entity) {
		if (!ElementalReactions.ELECTRO_CHARGED.isTriggerable(entity) || entity.getWorld().isClient || entity.isDead()) return;

		ElementalReactions.ELECTRO_CHARGED.trigger(entity);

		ElementComponent.sync(entity);
	}
}
