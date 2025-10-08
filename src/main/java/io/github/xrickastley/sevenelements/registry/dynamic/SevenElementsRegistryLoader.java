package io.github.xrickastley.sevenelements.registry.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents.RegistryContextImpl;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents.RegistryEntryContextImpl;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public final class SevenElementsRegistryLoader {
	private static final List<Entry<?, ?>> DYNAMIC_REGISTRIES = new ArrayList<>();
	private static final Multimap<RegistryKey<?>, Identifier> UNMODIFIABLE_ENTRIES = HashMultimap.create();

	static void add(Entry<?, ?> entry) {
		SevenElementsRegistryLoader.DYNAMIC_REGISTRIES.add(entry);
	}

	static void addUnmodifiableEntries(RegistryKey<? extends Registry<?>> key, Identifier... ids) {
		SevenElementsRegistryLoader.addUnmodifiableEntries(key, List.of(ids));
	}

	static void addUnmodifiableEntries(RegistryKey<? extends Registry<?>> key, List<Identifier> ids) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(key))
			throw new IllegalArgumentException("You may only pass a dynamic registry registered to the SevenElementsRegistryLoader!");

		SevenElementsRegistryLoader.UNMODIFIABLE_ENTRIES.putAll(key, ids);
	}

	public static <E> void load(RegistryOps.RegistryInfoGetter registryInfoGetter, ResourceManager resourceManager, RegistryKey<? extends Registry<E>> registryRef, MutableRegistry<E> newRegistry, Decoder<E> decoder, Map<RegistryKey<?>, Exception> exceptions) {
		final @Nullable Entry<? extends E, ?> dynRegEntry = SevenElementsRegistryLoader.getDynamicRegistry(newRegistry);

		if (dynRegEntry == null)
			throw new IllegalArgumentException("You may only pass a dynamic registry registered to the SevenElementsRegistryLoader!");

		DynamicRegistryLoadEvents.BEFORE_LOAD.invoker().onBeforeLoad(new RegistryContextImpl<>(registryRef, newRegistry));

		dynRegEntry.requireUnmodifiableEntries(newRegistry);

		final String path = dynRegEntry.getPath();
		final ResourceFinder resourceFinder = ResourceFinder.json(path);
		final RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, registryInfoGetter);
		final Iterator<Map.Entry<Identifier, Resource>> var9 = resourceFinder.findResources(resourceManager).entrySet().iterator();

		while (var9.hasNext()) {
			Map.Entry<Identifier, Resource> entry = var9.next();
			final Identifier identifier = entry.getKey();
			final Identifier resourceId = resourceFinder.toResourceId(identifier);

			if (dynRegEntry.isUnmodifiable(resourceId)) {
				SevenElements
					.sublogger()
					.warn("The data pack (\"{}\") with file at path ({}/{}) attempted to overwrite the preloaded entry {}, ignoring!", entry.getValue().getResourcePackName(), identifier.getNamespace(), identifier.getPath(), resourceId);

				continue;
			}

			final RegistryKey<E> registryKey = RegistryKey.of(registryRef, resourceId);
			final Resource resource = entry.getValue();

			try {
				parseAndAdd(newRegistry, ClassInstanceUtil.cast(dynRegEntry), registryOps, registryKey, resourceId, resource);
			} catch (Exception var15) {
				exceptions.put(registryKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", identifier, resource.getResourcePackName()), var15));
			}
		}

		DynamicRegistryLoadEvents.AFTER_LOAD.invoker().onAfterLoad(new RegistryContextImpl<>(registryRef, newRegistry));
	}

	public static <E> void parseAndAdd(MutableRegistry<E> registry, Entry<E, ?> entry, RegistryOps<JsonElement> ops, RegistryKey<E> key, Identifier identifier, Resource resource) throws IOException {
		Reader reader = resource.getReader();

		try {
			JsonElement jsonElement = JsonParser.parseReader(reader);
			Pair<E, ? extends DataResult<?>> pair = entry.parse(ops, jsonElement, identifier);
			E object = pair.getLeft();
			DataResult<?> dataResult = pair.getRight();
			registry.add(key, object, resource.isAlwaysStable() ? Lifecycle.stable() : dataResult.lifecycle());

			DynamicRegistryLoadEvents.ENTRY_LOAD.invoker().onEntryLoad(new RegistryEntryContextImpl<>(object, registry.getKey(), registry));
		} catch (Throwable var11) {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable var10) {
					var11.addSuppressed(var10);
				}
			}
			throw var11;
		}

		if (reader != null)
			reader.close();
	}

	public static boolean isDynamicRegistry(Registry<?> registry) {
		return SevenElementsRegistryLoader.isDynamicRegistry(registry.getKey());
	}

	public static boolean isDynamicRegistry(RegistryKey<? extends Registry<?>> registryKey) {
		return SevenElementsRegistryLoader.DYNAMIC_REGISTRIES
			.stream()
			.anyMatch(entry -> entry.key == registryKey);
	}

	private static <T, C> @Nullable Entry<T, C> getDynamicRegistry(Registry<T> registry) {
		return SevenElementsRegistryLoader.getDynamicRegistry(registry.getKey());
	}

	private static <T, C> @Nullable Entry<T, C> getDynamicRegistry(RegistryKey<? extends Registry<T>> registryKey) {
		for (final Entry<?, ?> entry : SevenElementsRegistryLoader.DYNAMIC_REGISTRIES) {
			if (entry.key != registryKey) continue;

			return ClassInstanceUtil.cast(entry);
		}

		return null;
	}

	/**
	 * A dynamic registry entry. <br> <br>
	 *
	 * Here, {@code C} must equal {@code T}.
	 */
	public static class Entry<T, C> {
		private final Class<T> entryClass;
		private final RegistryKey<? extends Registry<T>> key;
		private final Codec<C> elementCodec;
		private boolean useNamespace = false;

		public Entry(Class<T> entryClass, RegistryKey<? extends Registry<T>> registryKey, Codec<C> codec) {
			this.entryClass = entryClass;
			this.key = registryKey;
			this.elementCodec = codec;
		}

		/**
		 * Whether the namespace should be used in the data pack entry path. <br> <br>
		 *
		 * This avoids conflict with other mods that may use the same folder path.
		 */
		public void shouldUseNamespace(boolean useNamespace) {
			this.useNamespace = useNamespace;
		}

		/**
		 * The expected path of data pack entries. <br> <br>
		 *
		 * When using an {@code SevenElementsRegistryLoader.Entry}, <b>always</b> prefer this
		 * method over {@link RegistryKeys#getPath(RegistryKey)}.
		 *
		 * @return The expected path of data pack entries.
		 */
		public String getPath() {
			final String path = key.getValue().getPath();

			return this.useNamespace
				? key.getValue().getNamespace() + "/" + path
				: path;
		}

		public RegistryLoader.Entry<T> asRegistryLoaderEntry() {
			return new RegistryLoader.Entry<>(key, ClassInstanceUtil.cast(elementCodec));
		}

		public Pair<T, DataResult<C>> parse(RegistryOps<JsonElement> ops, JsonElement jsonElement, Identifier identifier) {
			DataResult<C> dataResult = this.elementCodec.parse(ops, jsonElement);

			return new Pair<>(
				entryClass.cast(dataResult.getOrThrow(false, error -> {})),
				dataResult
			);
		}

		public Pair<T, DataResult<C>> parse(RegistryOps<NbtElement> ops, NbtElement nbt, Identifier identifier) {
			DataResult<C> dataResult = this.elementCodec.parse(ops, nbt);

			return new Pair<>(
				entryClass.cast(dataResult.getOrThrow(false, error -> {})),
				dataResult
			);
		}

		public boolean isUnmodifiable(Identifier id) {
			final @Nullable Collection<Identifier> entries = SevenElementsRegistryLoader.UNMODIFIABLE_ENTRIES.get(key);

			return entries != null && entries.contains(id);
		}

		public void requireUnmodifiableEntries(MutableRegistry<?> registry) {
			final @Nullable Collection<Identifier> entries = SevenElementsRegistryLoader.UNMODIFIABLE_ENTRIES.get(key);

			if (entries == null) return;

			final List<Identifier> unregistered = entries
				.stream()
				.filter(Predicate.not(registry::containsId))
				.toList();

			if (unregistered.isEmpty()) return;

			throw new IllegalStateException("Some unmodifiable holders were not registered: " + unregistered);
		}
	}

	/**
	 * Variant of Entry that creates a "builder" object, then passes an Identifier to create the
	 * target object. <br> <br>
	 *
	 * Here, {@code T} is the "builder" for the serialized data and {@code T} is the result object of the builder.
	 */
	public static class IdentifiedEntry<T, R> extends Entry<T, R> {
		private final BiFunction<R, Identifier, T> resultFn;

		public IdentifiedEntry(Class<T> resultClass, RegistryKey<? extends Registry<T>> registryKey, Codec<R> resultCodec, BiFunction<R, Identifier, T> resultFn) {
			super(resultClass, registryKey, resultCodec);

			this.resultFn = resultFn;
		}

		@Override
		public Pair<T, DataResult<R>> parse(RegistryOps<JsonElement> ops, JsonElement jsonElement, Identifier identifier) {
			DataResult<R> dataResult = super.elementCodec.parse(ops, jsonElement);

			return new Pair<>(
				this.resultFn.apply(dataResult.getOrThrow(false, error -> {}), identifier),
				dataResult
			);
		}

		@Override
		public Pair<T, DataResult<R>> parse(RegistryOps<NbtElement> ops, NbtElement nbt, Identifier identifier) {
			DataResult<R> dataResult = super.elementCodec.parse(ops, nbt);

			return new Pair<>(
				this.resultFn.apply(dataResult.getOrThrow(false, error -> {}), identifier),
				dataResult
			);
		}
	}

	static {
		new SevenElementsRegistryLoader.IdentifiedEntry<>(
			InternalCooldownType.class,
			SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE,
			InternalCooldownType.Builder.CODEC,
			InternalCooldownType.Builder::getInstance
		);
	}
}
