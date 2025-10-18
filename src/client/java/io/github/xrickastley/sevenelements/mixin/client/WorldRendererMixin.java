
package io.github.xrickastley.sevenelements.mixin.client;

import io.github.xrickastley.sevenelements.events.WorldRenderEnd;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBufferSlice;

import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRenderContext;
import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRenderContextImpl;
import io.github.xrickastley.sevenelements.util.polyfill.rendering.WorldRendererHooks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.world.ClientWorld;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements WorldRendererHooks {
	@Shadow
	@Nullable
	private ClientWorld world;

	@Unique
	private final WorldRenderContextImpl sevenelements$worldRenderContext = new WorldRenderContextImpl();

	@Override
	@Unique
	public WorldRenderContext sevenelements$getWorldRenderContext() {
		return this.sevenelements$worldRenderContext;
	}

	@Inject(
		method = "render",
		at = @At("HEAD")
	)
	private void beforeRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		this.sevenelements$worldRenderContext
			.prepare(((WorldRenderer)(Object) this), tickCounter, camera, world);
	}

	@Inject(
		method = "render",
		at = @At("RETURN")
	)
	private void afterRender(CallbackInfo ci) {
		WorldRenderEnd.EVENT.invoker().onWorldRenderEnd(this.sevenelements$worldRenderContext);
	}
}
