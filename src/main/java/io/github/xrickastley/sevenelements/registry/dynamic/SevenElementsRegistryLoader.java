package io.github.xrickastley.sevenelements.registry.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.mixin.RegistryLoaderAccessor;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents.RegistryContextImpl;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents.RegistryEntryContextImpl;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps.RegistryInfoGetter;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.SerializableRegistries.SerializedRegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

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

	public static <E> void loadFromResource(ResourceManager resourceManager, RegistryInfoGetter infoGetter, MutableRegistry<E> registry, Decoder<E> elementDecoder, Map<RegistryKey<?>, Exception> errors) {
		final @Nullable Entry<? extends E, ?> dynRegEntry = SevenElementsRegistryLoader.getDynamicRegistry(registry);

		if (dynRegEntry == null)
			throw new IllegalArgumentException("You may only pass a dynamic registry registered to the SevenElementsRegistryLoader!");

		DynamicRegistryLoadEvents.BEFORE_LOAD.invoker().onBeforeLoad(new RegistryContextImpl<>(registry.getKey(), registry));

		dynRegEntry.requireUnmodifiableEntries(registry);

		final String path = dynRegEntry.getPath();
		final ResourceFinder resourceFinder = ResourceFinder.json(path);
		final RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, infoGetter);

		for (java.util.Map.Entry<Identifier, Resource> entry : resourceFinder.findResources(resourceManager).entrySet()) {
			final Identifier identifier = entry.getKey();
			final Identifier resourceId = resourceFinder.toResourceId(identifier);

			if (dynRegEntry.isUnmodifiable(resourceId)) {
				SevenElements
					.sublogger()
					.warn("The data pack (\"{}\") with file at path ({}/{}) attempted to overwrite the preloaded entry {}, ignoring!", entry.getValue().getPackId(), identifier.getNamespace(), identifier.getPath(), resourceId);

				continue;
			}

			final RegistryKey<E> registryKey = RegistryKey.of(registry.getKey(), resourceId);
			final Resource resource = entry.getValue();
			final RegistryEntryInfo registryEntryInfo = RegistryLoaderAccessor.getResourceEntryInfoGetter().apply(resource.getKnownPackInfo());

			try {
				parseAndAdd(registry, ClassInstanceUtil.cast(dynRegEntry), registryOps, registryKey, resourceId, resource, registryEntryInfo);
			} catch (Exception var15) {
				errors.put(registryKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", identifier, resource.getPackId()), var15));
			}
		}

		TagGroupLoader.loadInitial(resourceManager, registry);

		DynamicRegistryLoadEvents.AFTER_LOAD.invoker().onAfterLoad(new RegistryContextImpl<>(registry.getKey(), registry));
	}

	public static <E> void loadFromNetwork(
		Map<RegistryKey<? extends Registry<?>>, RegistryLoader.ElementsAndTags> data,
		ResourceFactory factory,
		RegistryInfoGetter infoGetter,
		MutableRegistry<E> registry,
		Decoder<E> decoder,
		Map<RegistryKey<?>, Exception> loadingErrors
	) {
		final @Nullable Entry<? extends E, ?> dynRegEntry = SevenElementsRegistryLoader.getDynamicRegistry(registry);

		if (dynRegEntry == null)
			throw new IllegalArgumentException("You may only pass a dynamic registry registered to the SevenElementsRegistryLoader!");

		DynamicRegistryLoadEvents.BEFORE_LOAD.invoker().onBeforeLoad(new RegistryContextImpl<>(registry.getKey(), registry));

		dynRegEntry.requireUnmodifiableEntries(registry);

		RegistryLoader.ElementsAndTags elementsAndTags = data.get(registry.getKey());
		if (elementsAndTags != null) {
			RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, infoGetter);
			RegistryOps<JsonElement> registryOps2 = RegistryOps.of(JsonOps.INSTANCE, infoGetter);
			String string = RegistryKeys.getPath(registry.getKey());
			ResourceFinder resourceFinder = ResourceFinder.json(string);

			for (SerializedRegistryEntry serializedRegistryEntry : elementsAndTags.elements()) {
				if (dynRegEntry.isUnmodifiable(serializedRegistryEntry.id())) continue;

				RegistryKey<E> registryKey = RegistryKey.of(registry.getKey(), serializedRegistryEntry.id());
				Optional<NbtElement> optional = serializedRegistryEntry.data();
				if (optional.isPresent()) {
					try {
						DataResult<E> dataResult = decoder.parse(registryOps, optional.get());
						E object = dataResult.getOrThrow();
						registry.add(registryKey, object, RegistryLoaderAccessor.getExperimentalEntryInfo());

						DynamicRegistryLoadEvents.ENTRY_LOAD.invoker().onEntryLoad(new RegistryEntryContextImpl<>(object, registry.getKey(), registry));
					} catch (Exception var17) {
						loadingErrors.put(registryKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), var17));
					}
				} else {
					Identifier identifier = resourceFinder.toResourcePath(serializedRegistryEntry.id());

					try {
						Resource resource = factory.getResourceOrThrow(identifier);
						final Identifier resourceId = resourceFinder.toResourceId(identifier);

						if (dynRegEntry.isUnmodifiable(resourceId)) {
							SevenElements
								.sublogger()
								.warn("The data pack (\"{}\") with file at path ({}/{}) attempted to overwrite the preloaded entry {}, ignoring!", resource.getPackId(), identifier.getNamespace(), identifier.getPath(), resourceId);

							continue;
						}

						parseAndAdd(registry, ClassInstanceUtil.cast(dynRegEntry), registryOps2, registryKey, resourceFinder.toResourceId(identifier), resource, RegistryLoaderAccessor.getExperimentalEntryInfo());
					} catch (Exception var18) {
						loadingErrors.put(registryKey, new IllegalStateException("Failed to parse local data", var18));
					}
				}
			}

			TagGroupLoader.loadFromNetwork(elementsAndTags.tags(), registry);
			DynamicRegistryLoadEvents.AFTER_LOAD.invoker().onAfterLoad(new RegistryContextImpl<>(registry.getKey(), registry));
		}
	}

	public static <E> void parseAndAdd(MutableRegistry<E> registry, Entry<E, ?> entry, RegistryOps<JsonElement> ops, RegistryKey<E> key, Identifier identifier, Resource resource, RegistryEntryInfo entryInfo) throws IOException {
		Reader reader = resource.getReader();

		try {
			JsonElement jsonElement = JsonParser.parseReader(reader);
			E object = entry.parse(ops, jsonElement, identifier);
			registry.add(key, object, entryInfo);

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
		private final boolean requiredNonEmpty;
		private boolean useNamespace = false;

		public Entry(Class<T> entryClass, RegistryKey<? extends Registry<T>> registryKey, Codec<C> codec) {
			this(entryClass, registryKey, codec, false);
		}

		public Entry(Class<T> entryClass, RegistryKey<? extends Registry<T>> key, Codec<C> elementCodec, boolean requiredNonEmpty) {
			this.entryClass = entryClass;
			this.key = key;
			this.elementCodec = elementCodec;
			this.requiredNonEmpty = requiredNonEmpty;
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
			final String path = RegistryKeys.getPath(key);

			return this.useNamespace
				? key.getValue().getNamespace() + "/" + path
				: path;
		}

		public RegistryLoader.Entry<T> asRegistryLoaderEntry() {
			return new RegistryLoader.Entry<>(key, ClassInstanceUtil.cast(elementCodec), requiredNonEmpty);
		}

		public T parse(RegistryOps<JsonElement> ops, JsonElement jsonElement, Identifier identifier) {
			DataResult<C> dataResult = this.elementCodec.parse(ops, jsonElement);

			return entryClass.cast(dataResult.getOrThrow());
		}

		public T parse(RegistryOps<NbtElement> ops, NbtElement nbt, Identifier identifier) {
			DataResult<C> dataResult = this.elementCodec.parse(ops, nbt);

			return entryClass.cast(dataResult.getOrThrow());
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
			this(resultClass, registryKey, resultCodec, resultFn, false);
		}

		public IdentifiedEntry(Class<T> resultClass, RegistryKey<? extends Registry<T>> registryKey, Codec<R> resultCodec, BiFunction<R, Identifier, T> resultFn, boolean requiredNonEmpty) {
			// T is a generic anyway, just ensure transformation before setting.
			super(resultClass, ClassInstanceUtil.cast(registryKey), resultCodec, requiredNonEmpty);

			this.resultFn = resultFn;
		}

		@Override
		public T parse(RegistryOps<JsonElement> ops, JsonElement jsonElement, Identifier identifier) {
			DataResult<R> dataResult = super.elementCodec.parse(ops, jsonElement);

			return this.resultFn.apply(dataResult.getOrThrow(), identifier);
		}

		@Override
		public T parse(RegistryOps<NbtElement> ops, NbtElement nbt, Identifier identifier) {
			DataResult<R> dataResult = super.elementCodec.parse(ops, nbt);

			return this.resultFn.apply(dataResult.getOrThrow(), identifier);
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
