package io.github.xrickastley.sevenelements.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import io.github.xrickastley.sevenelements.interfaces.EntityAwareRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityAwareRenderState {
	@Unique
	private Entity sevenelements$entity;

	@Override
	public @Nullable Entity sevenelements$getEntity() {
		return this.sevenelements$entity;
	}

	@Override
	public void sevenelements$setEntity(Entity entity) {
		this.sevenelements$entity = entity;
	}
}
