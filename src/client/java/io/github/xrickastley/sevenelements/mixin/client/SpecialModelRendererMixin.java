package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import io.github.xrickastley.sevenelements.interfaces.ElementGlintAccess;

import net.minecraft.client.renderer.special.SpecialModelRenderer;

@Mixin(SpecialModelRenderer.class)
public interface SpecialModelRendererMixin<T> extends ElementGlintAccess {}
