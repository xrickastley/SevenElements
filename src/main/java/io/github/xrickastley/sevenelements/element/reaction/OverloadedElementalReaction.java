package io.github.xrickastley.sevenelements.element.reaction;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.mixin.LevelAccessor;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;
import io.github.xrickastley.sevenelements.util.NonEntityDamagingExplosion;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.ExplosionDamageCalculator;

public class OverloadedElementalReaction extends ElementalReaction {
	OverloadedElementalReaction() {
		super(
			new Settings("Overloaded", SevenElements.identifier("overloaded"), TextHelper.reaction("reaction.seven-elements.overloaded", "#fc7fa4"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.PYRO, 2)
				.setTriggeringElement(Element.ELECTRO, 3)
				.reversable(true)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		if (!(entity.level() instanceof final ServerLevel world)) return;

		final float power = 3;

		final NonEntityDamagingExplosion explosion = new NonEntityDamagingExplosion(
			world,
			null,
			new ExplosionDamageCalculator(),
			entity.position(),
			power,
			world.getGameRules().get(SevenElementsGameRules.OVERLOADED_EXPLOSIONS_CREATE_FIRE),
			world.getGameRules().get(SevenElementsGameRules.OVERLOADED_EXPLOSIONS_DAMAGE_BLOCKS)
				? BlockInteraction.DESTROY
				: BlockInteraction.KEEP
		);

		explosion.explode();
		explosion
			.getAffectedEntities()
			.forEach(e -> damage(e, origin));

		for (ServerPlayer serverPlayerEntity : world.players()) {
			if (serverPlayerEntity.distanceToSqr(entity.position()) >= 4096.0) continue;

			serverPlayerEntity.connection.send(
				new ClientboundExplodePacket(
					entity.position(),
					explosion.radius(),
					0,
					Optional.ofNullable(explosion.getKnockbackByPlayer().get(serverPlayerEntity)),
					explosion.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER,
					SoundEvents.GENERIC_EXPLODE,
					LevelAccessor.getExplosionBlockParticles()
				)
			);
		}
	}

	private void damage(Entity entity, @Nullable Entity origin) {
		if (!(entity instanceof final LivingEntity living)) return;

		if (!(entity.level() instanceof final ServerLevel world)) return;

		final ElementalApplication application = ElementalApplications.gaugeUnits(living, Element.PYRO, 0);
		final ElementalDamageSource source = new ElementalDamageSource(
			entity
				.damageSources()
				.source(SevenElementsDamageTypes.OVERLOADED, origin),
			application,
			InternalCooldownContext.ofNone(entity)
		).shouldApplyDMGBonus(false);

		float amount = ElementalReaction.getReactionDamage(entity, 2.75);

		if (entity == origin) amount = 0;

		entity.hurtServer(world, source, amount);
	}
}
