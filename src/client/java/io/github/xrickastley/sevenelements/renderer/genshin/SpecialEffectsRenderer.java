package io.github.xrickastley.sevenelements.renderer.genshin;

import io.github.xrickastley.sevenelements.interfaces.SevenElementsLivingEntityRenderState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public final class SpecialEffectsRenderer {
	/**
	 * Returns whether effects should be rendered for the provided entity.
	 * @param entity The entity planned to render effects for.
	 */
	public static boolean shouldRender(Entity entity) {
		final MinecraftClient client = MinecraftClient.getInstance();

		return entity.isAlive()
			&& (entity != client.player || client.gameRenderer.getCamera().isThirdPerson());
	}

	/**
	 * Returns whether effects should be rendered for the provided entity state.
	 *
	 * @param state The entity state to be rendered effects for.
	 */
	public static boolean shouldRender(SevenElementsLivingEntityRenderState state) {
		final MinecraftClient client = MinecraftClient.getInstance();

		return !state.sevenelements$isDead()
			&& (!state.sevenelements$isClientPlayer() || client.gameRenderer.getCamera().isThirdPerson());
	}
}
