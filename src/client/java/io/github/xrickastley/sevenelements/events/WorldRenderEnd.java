package io.github.xrickastley.sevenelements.events;

import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRenderContext;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface WorldRenderEnd {
	public static final Event<WorldRenderEnd> EVENT = EventFactory.createArrayBacked(WorldRenderEnd.class,
		listeners -> context -> {
			for (final WorldRenderEnd listener : listeners) {
				listener.onWorldRenderEnd(context);
			}
		}
	);

	void onWorldRenderEnd(WorldRenderContext context);
}
