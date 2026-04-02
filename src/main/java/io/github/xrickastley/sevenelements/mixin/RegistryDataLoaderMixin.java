package io.github.xrickastley.sevenelements.mixin;

import com.mojang.serialization.Decoder;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.registry.dynamic.SevenElementsRegistryLoader;

import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
	@Inject(
		method = "loadContentsFromManager(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static <E> void doSevenElementsLoadFromResource(
		ResourceManager resourceManager,
		RegistryOps.RegistryInfoLookup infoGetter,
		WritableRegistry<E> registry,
		Decoder<E> elementDecoder,
		Map<ResourceKey<?>, Exception> errors,
		CallbackInfo ci
	) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(registry)) return;
		SevenElementsRegistryLoader.loadFromResource(resourceManager, infoGetter, registry, elementDecoder, errors);
		ci.cancel();
	}

	@Inject(
		method = "loadContentsFromNetwork(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static <E> void doSevenElementsLoadFromNetwork(
		Map<ResourceKey<? extends Registry<?>>, RegistryDataLoader.NetworkedRegistryData> data,
		ResourceProvider factory,
		RegistryOps.RegistryInfoLookup infoGetter,
		WritableRegistry<E> registry,
		Decoder<E> decoder,
		Map<ResourceKey<?>, Exception> loadingErrors,
		CallbackInfo ci
	) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(registry)) return;
		SevenElementsRegistryLoader.loadFromNetwork(data, factory, infoGetter, registry, decoder, loadingErrors);
		ci.cancel();
	}
}
