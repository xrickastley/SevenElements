package io.github.xrickastley.sevenelements.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("level")
	public @Nullable ClientLevel sevenelements$getLevel();
}
