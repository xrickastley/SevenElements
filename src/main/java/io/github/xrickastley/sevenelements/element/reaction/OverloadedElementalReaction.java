package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.factory.SevenElementsGameRules;
import io.github.xrickastley.sevenelements.registry.SevenElementsDamageTypes;
import io.github.xrickastley.sevenelements.util.NonEntityDamagingExplosion;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.world.explosion.ExplosionBehavior;

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
		final World world = entity.getWorld();

		if (world.isClient) return;

		final double x = entity.getX();
		final double y = entity.getY();
		final double z = entity.getZ();
		final float power = 3;

		final NonEntityDamagingExplosion explosion = new NonEntityDamagingExplosion(
			world,
			null,
			new ExplosionBehavior(),
			x,
			y,
			z,
			power,
			true,
			world.getGameRules().getBoolean(SevenElementsGameRules.OVERLOADED_EXPLOSIONS_DAMAGE_BLOCKS)
				? DestructionType.DESTROY
				: DestructionType.KEEP
		);

		explosion.collectBlocksAndPushEntities();
		explosion.affectWorld(world.isClient);
		explosion
			.getAffectedEntities()
			.forEach(e -> damage(e, origin));

		//  Sync the explosion effect to the client if the explosion is created on the server
		if (!(world instanceof ServerWorld serverWorld)) return;

		if (!explosion.shouldDestroy()) explosion.clearAffectedBlocks();

		for (ServerPlayerEntity serverPlayerEntity : serverWorld.getPlayers()) {
			if (serverPlayerEntity.squaredDistanceTo(x, y, z) >= 4096.0) continue;

			serverPlayerEntity.networkHandler.sendPacket(
				new ExplosionS2CPacket(
					x,
					y,
					z,
					power,
					explosion.getAffectedBlocks(),
					explosion.getAffectedPlayers().get(serverPlayerEntity),
					explosion.getDestructionType(),
					explosion.getParticle(),
					explosion.getEmitterParticle(),
					explosion.getSoundEvent()
				)
			);
		}
	}

	private void damage(Entity entity, @Nullable Entity origin) {
		if (!(entity instanceof final LivingEntity living)) return;

		final ElementalApplication application = ElementalApplications.gaugeUnits(living, Element.PYRO, 0);
		final ElementalDamageSource source = new ElementalDamageSource(
			entity
				.getDamageSources()
				.create(SevenElementsDamageTypes.OVERLOADED, origin),
			application,
			InternalCooldownContext.ofNone(entity)
		).shouldApplyDMGBonus(false);

		float amount = ElementalReaction.getReactionDamage(entity, 2.75);

		if (entity == origin) amount = 0;

		entity.damage(source, amount);
	}
}
