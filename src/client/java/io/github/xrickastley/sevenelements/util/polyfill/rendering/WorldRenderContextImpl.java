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

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;

public final class WorldRenderContextImpl implements WorldRenderContext {
	private WorldRenderer worldRenderer;
	private RenderTickCounter tickCounter;
	private Camera camera;
	private ClientWorld world;

	public void prepare(
			WorldRenderer worldRenderer,
			RenderTickCounter tickCounter,
			Camera camera,
			ClientWorld world
	) {
		this.worldRenderer = worldRenderer;
		this.tickCounter = tickCounter;
		this.camera = camera;
		this.world = world;
	}

	@Override
	public WorldRenderer worldRenderer() {
		return worldRenderer;
	}

	@Override
	public RenderTickCounter tickCounter() {
		return this.tickCounter;
	}

	@Override
	public Camera camera() {
		return camera;
	}

	@Override
	public ClientWorld world() {
		return world;
	}
}