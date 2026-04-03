package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryLoadTask;

@Mixin(RegistryLoadTask.class)
public interface RegistryLoadTaskAccessor<T> {
	@Accessor("registry")
	public WritableRegistry<T> getRegistry();
}
