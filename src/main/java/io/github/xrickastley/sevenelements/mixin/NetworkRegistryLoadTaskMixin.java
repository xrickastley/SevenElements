package io.github.xrickastley.sevenelements.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents.RegistryContextImpl;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents.RegistryEntryContextImpl;
import io.github.xrickastley.sevenelements.registry.dynamic.DynamicRegistryLoadEvents;
import io.github.xrickastley.sevenelements.registry.dynamic.SevenElementsRegistryLoader.RegistryEntry;
import io.github.xrickastley.sevenelements.registry.dynamic.SevenElementsRegistryLoader;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.WritableRegistry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.NetworkRegistryLoadTask;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Util;

@Mixin(NetworkRegistryLoadTask.class)
public abstract class NetworkRegistryLoadTaskMixin<T> extends RegistryLoadTask<T> {
	protected NetworkRegistryLoadTaskMixin(RegistryDataLoader.RegistryData<T> data, Lifecycle lifecycle, Map<ResourceKey<?>, Exception> loadingErrors) {
		super(data, lifecycle, loadingErrors);

		throw new AssertionError();
	}

	@Shadow
	@Final
	private static RegistrationInfo NETWORK_REGISTRATION_INFO;
	@Shadow
	@Final
	private Map<ResourceKey<? extends Registry<?>>, RegistryDataLoader.NetworkedRegistryData> entries;
	@Shadow
	@Final
	private ResourceProvider knownDataSource;

	@Inject(
		method = "load",
		at = @At("HEAD"),
		cancellable = true
	)
	private void doSevenElementsLoadFromNetwork(
		RegistryOps.RegistryInfoLookup context, Executor executor, CallbackInfoReturnable<CompletableFuture<?>> cir
	) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(this.registryKey())) return;

		cir.setReturnValue(this.sevenelements$loadFromNetwork(context, executor));
	}

	@Unique
	private CompletableFuture<?> sevenelements$loadFromNetwork(RegistryOps.RegistryInfoLookup context, Executor executor) {
		final @Nullable RegistryEntry<? extends T, ?> dynRegEntry = SevenElementsRegistryLoader.getDynamicRegistry(this.registryKey());

		@SuppressWarnings("unchecked")
		final WritableRegistry<T> registry = ((RegistryLoadTaskAccessor<T>) this).getRegistry();

		if (dynRegEntry == null)
			throw new IllegalArgumentException("You may only pass a dynamic registry registered to the SevenElementsRegistryLoader!");

		DynamicRegistryLoadEvents.BEFORE_LOAD.invoker().onBeforeLoad(new RegistryContextImpl<T>(this.registryKey(), registry));

		dynRegEntry.requireUnmodifiableEntries(registry);



		RegistryDataLoader.NetworkedRegistryData registryEntries = this.entries.get(this.registryKey());

		if (registryEntries == null)
			return CompletableFuture.completedFuture(null);

		RegistryOps<Tag> nbtOps = RegistryOps.create(NbtOps.INSTANCE, context);
		RegistryOps<JsonElement> jsonOps = RegistryOps.create(JsonOps.INSTANCE, context);
		FileToIdConverter knownDataPathConverter = FileToIdConverter.registry(this.registryKey());
		List<CompletableFuture<RegistryLoadTask.PendingRegistration<T>>> elements = new ArrayList<>(registryEntries.elements().size());

		for (RegistrySynchronization.PackedRegistryEntry entry : registryEntries.elements()) {
			if (dynRegEntry.isUnmodifiable(entry.id())) continue;

			ResourceKey<T> elementKey = ResourceKey.create(this.registryKey(), entry.id());
			Optional<Tag> networkContents = entry.data();
			CompletableFuture<PendingRegistration<T>> fut = networkContents.isPresent()
				? CompletableFuture.supplyAsync(() -> new PendingRegistration<>(elementKey, PendingRegistration.loadFromNetwork(this.data.elementCodec(), nbtOps, elementKey, networkContents.get()), NETWORK_REGISTRATION_INFO), executor)
				: CompletableFuture.supplyAsync(() -> new PendingRegistration<>(elementKey, SevenElementsRegistryLoader.findAndLoadFromResource(ClassInstanceUtil.cast(dynRegEntry), jsonOps, elementKey, knownDataPathConverter, this.knownDataSource), NETWORK_REGISTRATION_INFO), executor);

			fut.thenApply(pr ->
				pr.value().ifLeft(value -> DynamicRegistryLoadEvents.ENTRY_LOAD.invoker().onEntryLoad(new RegistryEntryContextImpl<>(value, registry.key(), registry)))
			);

			elements.add(fut);
		}

		return Util
			.sequence(elements)
			.thenAcceptAsync(pendingRegistrations -> {
				this.registerElements(pendingRegistrations.stream());

				Map<TagKey<T>, List<Holder<T>>> pendingTags = TagLoader.loadTagsFromNetwork(registryEntries.tags(), this.readOnlyRegistry());

				this.registerTags(pendingTags);

				DynamicRegistryLoadEvents.AFTER_LOAD.invoker().onAfterLoad(new RegistryContextImpl<T>(this.registryKey(), registry));
			}, executor);
	}
}
