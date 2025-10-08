package io.github.xrickastley.sevenelements.mixin;

import com.mojang.serialization.Decoder;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.registry.dynamic.SevenElementsRegistryLoader;

import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps.RegistryInfoGetter;
import net.minecraft.resource.ResourceManager;

@Mixin(RegistryLoader.class)
public class RegistryLoaderMixin {
	@Inject(
		method = "load(Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static <E> void doSevenElementsLoadFromResource(
		RegistryInfoGetter registryInfoGetter,
		ResourceManager resourceManager,
		RegistryKey<? extends Registry<E>> registryRef,
		MutableRegistry<E> newRegistry,
		Decoder<E> decoder,
		Map<RegistryKey<?>, Exception> exceptions,
		CallbackInfo ci
	) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(newRegistry)) return;
		SevenElementsRegistryLoader.load(registryInfoGetter, resourceManager, registryRef, newRegistry, decoder, exceptions);
		ci.cancel();
	}
}
