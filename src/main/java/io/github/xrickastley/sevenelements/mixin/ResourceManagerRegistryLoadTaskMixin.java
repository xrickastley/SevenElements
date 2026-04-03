package io.github.xrickastley.sevenelements.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

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
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader.ElementLookup;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.thread.ParallelMapTransform;

@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class ResourceManagerRegistryLoadTaskMixin<T> extends RegistryLoadTask<T> {
	protected ResourceManagerRegistryLoadTaskMixin(RegistryDataLoader.RegistryData<T> data, Lifecycle lifecycle, Map<ResourceKey<?>, Exception> loadingErrors) {
		super(data, lifecycle, loadingErrors);

		throw new AssertionError();
	}

	@Shadow
	@Final
	private static Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE;
	@Shadow
	@Final
	private ResourceManager resourceManager;

	@Inject(
		method = "load",
		at = @At("HEAD"),
		cancellable = true
	)
	private void doSevenElementsLoadFromResource(
		RegistryOps.RegistryInfoLookup context, Executor executor, CallbackInfoReturnable<CompletableFuture<?>> cir
	) {
		if (!SevenElementsRegistryLoader.isDynamicRegistry(this.registryKey())) return;

		cir.setReturnValue(this.sevenelements$loadFromResource(context, executor));
	}

	@Unique
	private CompletableFuture<?> sevenelements$loadFromResource(RegistryOps.RegistryInfoLookup context, Executor executor) {
		final @Nullable RegistryEntry<? extends T, ?> dynRegEntry = SevenElementsRegistryLoader.getDynamicRegistry(this.registryKey());

		@SuppressWarnings("unchecked")
		final WritableRegistry<T> registry = ((RegistryLoadTaskAccessor<T>) this).getRegistry();

		if (dynRegEntry == null)
			throw new IllegalArgumentException("You may only pass a dynamic registry registered to the SevenElementsRegistryLoader!");

		DynamicRegistryLoadEvents.BEFORE_LOAD.invoker().onBeforeLoad(new RegistryContextImpl<T>(this.registryKey(), registry));

		dynRegEntry.requireUnmodifiableEntries(registry);

		final String path = dynRegEntry.getPath();
		final FileToIdConverter lister = FileToIdConverter.json(path);

		return CompletableFuture
			.supplyAsync(() -> lister.listMatchingResources(this.resourceManager), executor)
			.thenCompose(registryResources -> {
				final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, context);

				return ParallelMapTransform.schedule(registryResources, (identifier, thunk) -> {
					Identifier resourceId = lister.fileToId(identifier);
					ResourceKey<T> elementKey = ResourceKey.create(this.registryKey(), resourceId);
					RegistrationInfo registrationInfo = REGISTRATION_INFO_CACHE.apply(thunk.knownPackInfo());

					return new PendingRegistration<T>(
						elementKey,
						SevenElementsRegistryLoader
							.loadFromResource(ClassInstanceUtil.cast(dynRegEntry), ops, elementKey, identifier, thunk)
							.ifLeft(value -> DynamicRegistryLoadEvents.ENTRY_LOAD.invoker().onEntryLoad(new RegistryEntryContextImpl<>(value, registry.key(), registry))),
						registrationInfo
					);
				}, executor);
			})
			.thenAcceptAsync(loadedEntries -> {
				this.registerElements(
					loadedEntries
						.entrySet()
						.stream()
						.sorted(Map.Entry.comparingByKey())
						.map(Map.Entry::getValue)
				);

				TagLoader.ElementLookup<Holder<T>> tagElementLookup = ElementLookup.fromGetters(this.registryKey(), this.concurrentRegistrationGetter, this.readOnlyRegistry());
				Map<TagKey<T>, List<Holder<T>>> pendingTags = TagLoader.loadTagsForRegistry(this.resourceManager, this.registryKey(), tagElementLookup);

				this.registerTags(pendingTags);

				DynamicRegistryLoadEvents.AFTER_LOAD.invoker().onAfterLoad(new RegistryContextImpl<T>(this.registryKey(), registry));
			}, executor);
	}
}
