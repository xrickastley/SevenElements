package io.github.xrickastley.sevenelements.events;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.annotation.ExpectedEnvironment;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

@ExpectedEnvironment(EnvType.SERVER)
@FunctionalInterface
public interface ReactionTriggered {
	public static Event<ReactionTriggered> EVENT = EventFactory.createArrayBacked(ReactionTriggered.class,
		listeners -> (reaction, reducedGauge, target, origin) -> {
			for (final ReactionTriggered listener : listeners) listener.onReactionTriggered(reaction, reducedGauge, target, origin);
		}
	);

	void onReactionTriggered(ElementalReaction reaction, double reducedGauge, LivingEntity target, @Nullable LivingEntity origin);
}
