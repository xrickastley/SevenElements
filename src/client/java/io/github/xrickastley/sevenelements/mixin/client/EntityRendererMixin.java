package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
	@Inject(
		method = "updateRenderState",
		at = @At("HEAD")
	)
	private void setEntity(T entity, S state, float tickDelta, CallbackInfo ci) {
		state.sevenelements$setEntity(entity);
	}
}
