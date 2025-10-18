/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Polyfilled in SevenElements and Modified by xrickastley.

package io.github.xrickastley.sevenelements.util.polyfill.rendering;

import com.google.common.base.Preconditions;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;

/**
 * Except as noted below, the properties exposed here match the parameters passed to
 * {@link WorldRenderer#render}.
 */
public interface WorldRenderContext {
	/**
	 * Returns the {@code WorldRenderContext} for the given {@code WorldRenderer} instance, for use in cases where you
	 * have access to the world renderer but not the world render context. World render events always pass the world
	 * render context as a parameter, so always prefer to use that over this method.
	 *
	 * @param worldRenderer The world renderer
	 * @return The world render context for the world renderer
	 * @throws IllegalStateException If not currently rendering the world
	 */
	static WorldRenderContext getInstance(WorldRenderer worldRenderer) {
		Preconditions.checkNotNull(worldRenderer, "worldRenderer");
		return ((WorldRendererHooks) worldRenderer).sevenelements$getWorldRenderContext();
	}

	/**
	 * The world renderer instance doing the rendering and invoking the event.
	 *
	 * @return WorldRenderer instance invoking the event
	 */
	WorldRenderer worldRenderer();

	RenderTickCounter tickCounter();

	Camera camera();

	/**
	 * Convenient access to {WorldRenderer.world}.
	 *
	 * @return world renderer's client world instance
	 */
	ClientWorld world();
}