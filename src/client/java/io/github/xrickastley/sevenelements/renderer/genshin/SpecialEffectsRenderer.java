package io.github.xrickastley.sevenelements.renderer.genshin;

import io.github.xrickastley.sevenelements.interfaces.SevenElementsLivingEntityRenderState;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class SpecialEffectsRenderer {
	/**
	 * Returns whether effects should be rendered for the provided entity.
	 * @param entity The entity planned to render effects for.
	 */
	public static boolean shouldRender(Entity entity) {
		final Minecraft client = Minecraft.getInstance();

		return entity.isAlive()
			&& (entity != client.player || client.gameRenderer.mainCamera().isDetached());
	}

	/**
	 * Returns whether effects should be rendered for the provided entity state.
	 *
	 * @param state The entity state to be rendered effects for.
	 */
	public static boolean shouldRender(SevenElementsLivingEntityRenderState state) {
		final Minecraft client = Minecraft.getInstance();

		return !state.sevenelements$isDead()
			&& (!state.sevenelements$isClientPlayer() || client.gameRenderer.mainCamera().isDetached());
	}
}
