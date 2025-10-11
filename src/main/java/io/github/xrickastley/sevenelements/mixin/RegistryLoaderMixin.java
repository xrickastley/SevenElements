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
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;

@Mixin(RegistryLoader.class)
public class RegistryLoaderMixin {
	@Inject(
		method = "loadFromResource(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static <E> void doSevenElementsLoadFromResource(
		ResourceManager resourceManager,
		RegistryOps.RegistryInfoGetter infoGetter,
		MutableRegistry<E> registry,
		Decoder<E> elementDecoder,
		Map<RegistryKey<?>, Exception> errors,
		CallbackInfo ci
	) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(registry)) return;
		SevenElementsRegistryLoader.loadFromResource(resourceManager, infoGetter, registry, elementDecoder, errors);
		ci.cancel();
	}

	@Inject(
		method = "loadFromNetwork(Ljava/util/Map;Lnet/minecraft/resource/ResourceFactory;Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static <E> void doSevenElementsLoadFromNetwork(
		Map<RegistryKey<? extends Registry<?>>, RegistryLoader.ElementsAndTags> data,
		ResourceFactory factory,
		RegistryOps.RegistryInfoGetter infoGetter,
		MutableRegistry<E> registry,
		Decoder<E> decoder,
		Map<RegistryKey<?>, Exception> loadingErrors,
		CallbackInfo ci
	) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(registry)) return;
		SevenElementsRegistryLoader.loadFromNetwork(data, factory, infoGetter, registry, decoder, loadingErrors);
		ci.cancel();
	}
}
