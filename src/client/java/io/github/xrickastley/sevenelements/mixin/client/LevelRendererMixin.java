package io.github.xrickastley.sevenelements.mixin.client;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.events.WorldRenderEnd;
import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRenderContext;
import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRenderContextImpl;
import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRendererHooks;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin implements WorldRendererHooks {
	@Shadow
	@Nullable
	private ClientLevel level;

	@Unique
	private final WorldRenderContextImpl sevenelements$worldRenderContext = new WorldRenderContextImpl();

	@Override
	@Unique
	public WorldRenderContext sevenelements$getWorldRenderContext() {
		return this.sevenelements$worldRenderContext;
	}

	@Inject(
		method = "renderLevel",
		at = @At("HEAD")
	)
	private void beforeRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		this.sevenelements$worldRenderContext
			.prepare(((LevelRenderer)(Object) this), tickCounter, camera, level);
	}

	@Inject(
		method = "renderLevel",
		at = @At("RETURN")
	)
	private void afterRender(CallbackInfo ci) {
		WorldRenderEnd.EVENT.invoker().onWorldRenderEnd(this.sevenelements$worldRenderContext);
	}
}
