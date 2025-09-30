package io.github.xrickastley.sevenelements.component;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;

public interface FrozenEffectComponent extends AutoSyncedComponent, ClientTickingComponent, ServerTickingComponent {
	public static final ComponentKey<FrozenEffectComponent> KEY = ComponentRegistry.getOrCreate(SevenElements.identifier("frozen_effect"), FrozenEffectComponent.class);

	public boolean isFrozen();

	public EntityPose getForcePose();

	public float getForceHeadYaw();

	public float getForceBodyYaw();

	public float getForcePitch();

	public float getForceLimbAngle();

	public float getForceLimbDistance();

	public void freeze();

	public void unfreeze();

	public static void sync(Entity entity) {
		KEY.sync(entity);
	}
}
