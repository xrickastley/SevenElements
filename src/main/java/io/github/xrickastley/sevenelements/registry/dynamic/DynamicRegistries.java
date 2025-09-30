package io.github.xrickastley.sevenelements.registry.dynamic;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.function.BiFunction;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class DynamicRegistries {
	public static <T> void register(Class<T> entryClass, RegistryKey<? extends Registry<T>> key, Codec<T> elementCodec) {
		DynamicRegistries.register(entryClass, key, elementCodec, false);
	}

	public static <T> void register(Class<T> entryClass, RegistryKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty) {
		SevenElementsRegistryLoader.add(
			new SevenElementsRegistryLoader.Entry<>(entryClass, key, elementCodec, requiredNonEmpty)
		);

		DynamicRegistriesImpl.register(key, elementCodec);
		DynamicRegistriesImpl.addSyncedRegistry(key, elementCodec);
		DynamicRegistriesImpl.FABRIC_DYNAMIC_REGISTRY_KEYS.remove(key);
	}

	public static <T, R> void registerIdentified(Class<T> resultClass, RegistryKey<? extends Registry<T>> key, Codec<R> builderCodec, Codec<T> elementCodec, BiFunction<R, Identifier, T> resultFn) {
		DynamicRegistries.registerIdentified(resultClass, key, builderCodec, elementCodec, resultFn, false);
	}

	public static <T, R> void registerIdentified(Class<T> resultClass, RegistryKey<? extends Registry<T>> key, Codec<R> builderCodec, Codec<T> elementCodec, BiFunction<R, Identifier, T> resultFn, boolean requiredNonEmpty) {
		SevenElementsRegistryLoader.add(
			new SevenElementsRegistryLoader.IdentifiedEntry<>(resultClass, key, builderCodec, resultFn, requiredNonEmpty)
		);

		DynamicRegistriesImpl.register(key, elementCodec);
		DynamicRegistriesImpl.addSyncedRegistry(key, elementCodec);
		DynamicRegistriesImpl.FABRIC_DYNAMIC_REGISTRY_KEYS.remove(key);
	}

	public static void addUnmodifiableEntries(RegistryKey<? extends Registry<?>> key, Identifier... ids) {
		SevenElementsRegistryLoader.addUnmodifiableEntries(key, ids);
	}

	public static void addUnmodifiableEntries(RegistryKey<? extends Registry<?>> key, List<Identifier> ids) {
		SevenElementsRegistryLoader.addUnmodifiableEntries(key, ids);
	}
}
