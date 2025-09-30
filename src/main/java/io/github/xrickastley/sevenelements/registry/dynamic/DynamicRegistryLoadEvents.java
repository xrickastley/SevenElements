package io.github.xrickastley.sevenelements.registry.dynamic;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public final class DynamicRegistryLoadEvents {
	public static final Event<BeforeLoad> BEFORE_LOAD = EventFactory.createArrayBacked(BeforeLoad.class,
		listeners -> registryContext -> {
			for (BeforeLoad listener : listeners) listener.onBeforeLoad(registryContext);
		}
	);

	public static final Event<EntryLoad> ENTRY_LOAD = EventFactory.createArrayBacked(EntryLoad.class,
		listeners -> entryContext -> {
			for (EntryLoad listener : listeners) listener.onEntryLoad(entryContext);
		}
	);

	public static final Event<AfterLoad> AFTER_LOAD = EventFactory.createArrayBacked(AfterLoad.class,
		listeners -> registryContext -> {
			for (AfterLoad listener : listeners) listener.onAfterLoad(registryContext);
		}
	);

	@FunctionalInterface
	public interface BeforeLoad {
		void onBeforeLoad(RegistryContext<?> registryContext);
	}

	public interface EntryLoad {
		void onEntryLoad(RegistryEntryContext<?> entryContext);
	}

	@FunctionalInterface
	public interface AfterLoad {
		void onAfterLoad(RegistryContext<?> registryContext);
	}

	public interface RegistryContext<T> {
		public RegistryKey<? extends Registry<T>> registryKey();
		public Registry<T> registry();

		default <R> @Nullable RegistryContext<R> asKey(RegistryKey<? extends Registry<R>> key) {
			return this.registryKey().equals(key)
				? ClassInstanceUtil.cast(this)
				: null;
		}

		default <R> RegistryContext<R> withType(Class<T> entryClazz) {
			return ClassInstanceUtil.cast(this);
		}
	}

	public interface RegistryEntryContext<T> extends RegistryContext<T> {
		public T entry();

		default <R> @Nullable RegistryEntryContext<R> asKey(RegistryKey<? extends Registry<R>> key) {
			return this.registryKey().equals(key)
				? ClassInstanceUtil.cast(this)
				: null;
		}

		default <R> RegistryEntryContext<R> withType(Class<T> entryClazz) {
			return ClassInstanceUtil.cast(this);
		}
	}

	static class RegistryContextImpl<T> implements RegistryContext<T> {
		private final RegistryKey<? extends Registry<T>> registryKey;
		private final Registry<T> registry;

		RegistryContextImpl(RegistryKey<? extends Registry<T>> registryKey, Registry<T> registry) {
			this.registryKey = registryKey;
			this.registry = registry;
		}

		public RegistryKey<? extends Registry<T>> registryKey() {
			return registryKey;
		}

		@Override
		public Registry<T> registry() {
			return registry;
		}
	}

	static class RegistryEntryContextImpl<T> implements RegistryEntryContext<T> {
		private final T entry;
		private final RegistryKey<? extends Registry<T>> registryKey;
		private final Registry<T> registry;

		RegistryEntryContextImpl(T entry, RegistryKey<? extends Registry<T>> registryKey, Registry<T> registry) {
			this.entry = entry;
			this.registryKey = registryKey;
			this.registry = registry;
		}

		@Override
		public T entry() {
			return entry;
		}

		public RegistryKey<? extends Registry<T>> registryKey() {
			return registryKey;
		}

		@Override
		public Registry<T> registry() {
			return registry;
		}
	}
}
