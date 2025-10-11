package io.github.xrickastley.sevenelements.element.reaction;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;
import io.github.xrickastley.sevenelements.entity.SevenElementsEntityTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public abstract sealed class AbstractBloomElementalReaction
	extends ElementalReaction
	permits DendroBloomElementalReaction, HydroBloomElementalReaction, QuickenBloomElementalReaction
{
	AbstractBloomElementalReaction(Settings settings) {
		super(settings);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final World world = entity.getWorld();

		if (!(world instanceof final ServerWorld serverWorld)) return;

		final DendroCoreEntity dendroCore = SevenElementsEntityTypes.DENDRO_CORE.create(serverWorld, SpawnReason.TRIGGERED);
		dendroCore.addOwner(origin);
		dendroCore.setPosition(entity.getPos());

		serverWorld.spawnNewEntityAndPassengers(dendroCore);
	}
}
