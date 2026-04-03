package io.github.xrickastley.sevenelements.registry.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;

public final class SevenElementsRegistryLoader {
	private static final List<RegistryEntry<?, ?>> DYNAMIC_REGISTRIES = new ArrayList<>();
	private static final Multimap<ResourceKey<?>, Identifier> UNMODIFIABLE_ENTRIES = HashMultimap.create();

	static void add(RegistryEntry<?, ?> entry) {
		SevenElementsRegistryLoader.DYNAMIC_REGISTRIES.add(entry);
	}

	static void addUnmodifiableEntries(ResourceKey<? extends Registry<?>> key, Identifier... ids) {
		SevenElementsRegistryLoader.addUnmodifiableEntries(key, List.of(ids));
	}

	static void addUnmodifiableEntries(ResourceKey<? extends Registry<?>> key, List<Identifier> ids) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(key))
			throw new IllegalArgumentException("You may only pass a dynamic registry registered to the SevenElementsRegistryLoader!");

		SevenElementsRegistryLoader.UNMODIFIABLE_ENTRIES.putAll(key, ids);
	}

	@ApiStatus.Internal
	public static <E> Either<E, Exception> loadFromResource(final RegistryEntry<E, ?> entry, final RegistryOps<JsonElement> ops, final ResourceKey<E> key, final Identifier entryPath, final Resource resource) {
		if (entry.isUnmodifiable(key.identifier()))
			return Either.right(new UnmodifiableEntryOverwriteException(resource, entryPath, key.identifier()));

		try {
			Reader reader = resource.openAsReader();

			Either<E, Exception> result;
			try {
				JsonElement jsonElement = JsonParser.parseReader(reader);
				result = entry.parse(ops, jsonElement, key.identifier());
			} catch (Throwable var8) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (reader != null)
				reader.close();

			return result;
		} catch (Exception e) {
			return Either.right(new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", key.identifier(), resource.sourcePackId()), e));
		}
	}

	@ApiStatus.Internal
	public static <E> Either<E, Exception> findAndLoadFromResource(final RegistryEntry<E, ?> entry, final RegistryOps<JsonElement> ops, final ResourceKey<E> elementKey, final FileToIdConverter converter, final ResourceProvider resourceProvider) {
		Identifier resourceId = converter.idToFile(elementKey.identifier());

		return resourceProvider
			.getResource(resourceId)
			.map(resource -> loadFromResource(entry, ops, elementKey, resourceId, resource))
			.orElseGet(() -> Either.right(new IllegalStateException(String.format(Locale.ROOT, "Failed to find resource %s for element %s", resourceId, elementKey.identifier()))));
	}

	public static boolean isDynamicRegistry(Registry<?> registry) {
		return SevenElementsRegistryLoader.isDynamicRegistry(registry.key());
	}

	public static boolean isDynamicRegistry(ResourceKey<? extends Registry<?>> registryKey) {
		return SevenElementsRegistryLoader.DYNAMIC_REGISTRIES
			.stream()
			.anyMatch(entry -> entry.key == registryKey);
	}

	@ApiStatus.Internal
	public static <T, C> @Nullable RegistryEntry<T, C> getDynamicRegistry(Registry<T> registry) {
		return SevenElementsRegistryLoader.getDynamicRegistry(registry.key());
	}

	@ApiStatus.Internal
	public static <T, C> @Nullable RegistryEntry<T, C> getDynamicRegistry(ResourceKey<? extends Registry<T>> registryKey) {
		for (final RegistryEntry<?, ?> entry : SevenElementsRegistryLoader.DYNAMIC_REGISTRIES) {
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
	@ApiStatus.Internal
	public static class RegistryEntry<T, C> {
		private final Class<T> entryClass;
		private final ResourceKey<? extends Registry<T>> key;
		private final Codec<C> elementCodec;
		private final RegistryValidator<T> validator;
		private boolean useNamespace = false;

		public RegistryEntry(Class<T> entryClass, ResourceKey<? extends Registry<T>> registryKey, Codec<C> codec) {
			this(entryClass, registryKey, codec, RegistryValidator.none());
		}

		public RegistryEntry(Class<T> entryClass, ResourceKey<? extends Registry<T>> key, Codec<C> elementCodec, RegistryValidator<T> validator) {
			this.entryClass = entryClass;
			this.key = key;
			this.elementCodec = elementCodec;
			this.validator = validator;
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
		 * method over {@link Registries#elementsDirPath(ResourceKey)}.
		 *
		 * @return The expected path of data pack entries.
		 */
		public String getPath() {
			final String path = Registries.elementsDirPath(key);

			return this.useNamespace
				? key.identifier().getNamespace() + "/" + path
				: path;
		}

		public RegistryDataLoader.RegistryData<T> asRegistryLoaderEntry() {
			return new RegistryDataLoader.RegistryData<>(key, ClassInstanceUtil.cast(elementCodec), validator);
		}

		public Either<T, Exception> parse(RegistryOps<JsonElement> ops, JsonElement jsonElement, Identifier identifier) {
			DataResult<C> dataResult = this.elementCodec.parse(ops, jsonElement);

			return tryGet(Functions.map(dataResult::getOrThrow, entryClass::cast));
		}

		public Either<T, Exception> parse(RegistryOps<Tag> ops, Tag nbt, Identifier identifier) {
			DataResult<C> dataResult = this.elementCodec.parse(ops, nbt);

			return tryGet(Functions.map(dataResult::getOrThrow, entryClass::cast));
		}

		public boolean isUnmodifiable(Identifier id) {
			final @Nullable Collection<Identifier> entries = SevenElementsRegistryLoader.UNMODIFIABLE_ENTRIES.get(key);

			return entries != null && entries.contains(id);
		}

		public void requireUnmodifiableEntries(WritableRegistry<?> registry) {
			final @Nullable Collection<Identifier> entries = SevenElementsRegistryLoader.UNMODIFIABLE_ENTRIES.get(key);

			if (entries == null) return;

			final List<Identifier> unregistered = entries
				.stream()
				.filter(Predicate.not(registry::containsKey))
				.toList();

			if (unregistered.isEmpty()) return;

			throw new IllegalStateException("Some unmodifiable holders were not registered: " + unregistered);
		}
	}

	/**
	 * Variant of Entry that creates a "builder" object, then passes an Identifier to create the
	 * target object. <br> <br>
	 *
	 * Here, {@code R} is the "builder" for the serialized data and {@code T} is the result object of the builder.
	 */
	@ApiStatus.Internal
	public static class IdentifiedRegistryEntry<T, R> extends RegistryEntry<T, R> {
		private final BiFunction<R, Identifier, T> resultFn;

		public IdentifiedRegistryEntry(Class<T> resultClass, ResourceKey<? extends Registry<T>> registryKey, Codec<R> resultCodec, BiFunction<R, Identifier, T> resultFn) {
			this(resultClass, registryKey, resultCodec, resultFn, RegistryValidator.none());
		}

		public IdentifiedRegistryEntry(Class<T> resultClass, ResourceKey<? extends Registry<T>> registryKey, Codec<R> resultCodec, BiFunction<R, Identifier, T> resultFn, RegistryValidator<T> validator) {
			// T is a generic anyway, just ensure transformation before setting.
			super(resultClass, ClassInstanceUtil.cast(registryKey), resultCodec, validator);

			this.resultFn = resultFn;
		}

		@Override
		public Either<T, Exception> parse(RegistryOps<JsonElement> ops, JsonElement jsonElement, Identifier identifier) {
			DataResult<R> dataResult = super.elementCodec.parse(ops, jsonElement);

			return tryGet(Functions.supplier(this.resultFn::apply, dataResult.getOrThrow(), identifier));
		}

		@Override
		public Either<T, Exception> parse(RegistryOps<Tag> ops, Tag nbt, Identifier identifier) {
			DataResult<R> dataResult = super.elementCodec.parse(ops, nbt);

			return tryGet(Functions.supplier(this.resultFn::apply, dataResult.getOrThrow(), identifier));
		}
	}

	private static <T> Either<T, Exception> tryGet(Supplier<T> supplier) {
		try {
			return Either.left(supplier.get());
		} catch (Exception e) {
			return Either.right(e);
		}
	}

	static {
		new SevenElementsRegistryLoader.IdentifiedRegistryEntry<>(
			InternalCooldownType.class,
			SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE,
			InternalCooldownType.Builder.CODEC,
			InternalCooldownType.Builder::getInstance
		);
	}
}
