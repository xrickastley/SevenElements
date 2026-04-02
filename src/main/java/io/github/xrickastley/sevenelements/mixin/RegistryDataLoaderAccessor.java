package io.github.xrickastley.sevenelements.mixin;

import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.repository.KnownPack;

@Mixin(RegistryDataLoader.class)
public interface RegistryDataLoaderAccessor {
	@Accessor("REGISTRATION_INFO_CACHE")
	public static Function<Optional<KnownPack>, RegistrationInfo> getResourceEntryInfoGetter() {
		throw new AssertionError();
	}

	@Accessor("NETWORK_REGISTRATION_INFO")
	public static RegistrationInfo getExperimentalEntryInfo() {
		throw new AssertionError();
	}
}
