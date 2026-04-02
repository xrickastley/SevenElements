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

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;

public final class WorldRenderContextImpl implements WorldRenderContext {
	private LevelRenderer worldRenderer;
	private DeltaTracker tickCounter;
	private Camera camera;
	private ClientLevel world;

	public void prepare(
		LevelRenderer worldRenderer,
		DeltaTracker tickCounter,
		Camera camera,
		ClientLevel world
	) {
		this.worldRenderer = worldRenderer;
		this.tickCounter = tickCounter;
		this.camera = camera;
		this.world = world;
	}

	@Override
	public LevelRenderer worldRenderer() {
		return worldRenderer;
	}

	@Override
	public DeltaTracker tickCounter() {
		return this.tickCounter;
	}

	@Override
	public Camera camera() {
		return camera;
	}

	@Override
	public ClientLevel world() {
		return world;
	}
}
