package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.entity.SevenElementsEntityTypes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public abstract sealed class AbstractBloomElementalReaction
	extends ElementalReaction
	permits DendroBloomElementalReaction, HydroBloomElementalReaction, QuickenBloomElementalReaction
{
	AbstractBloomElementalReaction(Settings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final Level world = entity.level();

		if (!(world instanceof final ServerLevel serverWorld)) return;

		final DendroCoreEntity dendroCore = SevenElementsEntityTypes.DENDRO_CORE.create(serverWorld, EntitySpawnReason.TRIGGERED);
		dendroCore.addOwner(origin);
		dendroCore.setPos(entity.position());

		serverWorld.tryAddFreshEntityWithPassengers(dendroCore);
	}
}
