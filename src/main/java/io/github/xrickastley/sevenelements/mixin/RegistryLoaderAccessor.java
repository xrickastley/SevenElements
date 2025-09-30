package io.github.xrickastley.sevenelements.mixin;

import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.registry.entry.RegistryEntryInfo;

@Mixin(RegistryLoader.class)
public interface RegistryLoaderAccessor {
	@Accessor("RESOURCE_ENTRY_INFO_GETTER")
	public static Function<Optional<VersionedIdentifier>, RegistryEntryInfo> getResourceEntryInfoGetter() {
		throw new AssertionError();
	}

	@Accessor("EXPERIMENTAL_ENTRY_INFO")
	public static RegistryEntryInfo getExperimentalEntryInfo() {
		throw new AssertionError();
	}
}
