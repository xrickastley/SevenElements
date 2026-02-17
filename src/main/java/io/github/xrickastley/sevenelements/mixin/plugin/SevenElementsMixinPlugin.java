package io.github.xrickastley.sevenelements.mixin.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class SevenElementsMixinPlugin implements IMixinConfigPlugin {
	private static final Map<String, String> MOD_CONDITIONAL_MIXINS = new HashMap<>();

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return SevenElementsMixinPlugin.MOD_CONDITIONAL_MIXINS.containsKey(mixinClassName)
			? FabricLoader.getInstance().isModLoaded(SevenElementsMixinPlugin.MOD_CONDITIONAL_MIXINS.get(mixinClassName))
			: true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	/**
	 * Adds an arbitrary amount of conditional mixins that are only loaded when the provided mod
	 * exists.
	 *
	 * @param modId The required mod for all specified mixins to be loaded.
	 * @param mixins The conditional mixins to load. This must be {@code Strings} containing the
	 * full package name and the class name, i.e. {@code io.github.xrickastley.sevenelements.mixin.ExampleOptionalMixin}
	 */
	public static void addConditionalMixins(String modId, String... mixins) {
		SevenElementsMixinPlugin.addConditionalMixins(modId, List.of(mixins));
	}

	/**
	 * Adds an arbitrary amount of conditional mixins that are only loaded when the provided mod
	 * exists.
	 *
	 * @param modId The required mod for all specified mixins to be loaded.
	 * @param mixins The conditional mixins to load. This must be a {@code List} of {@code Strings}
	 * containing the full package name and the class name, i.e.
	 * {@code io.github.xrickastley.sevenelements.mixin.ExampleOptionalMixin}
	 */
	public static void addConditionalMixins(String modId, List<String> mixins) {
		mixins.forEach(mixin ->
			SevenElementsMixinPlugin.MOD_CONDITIONAL_MIXINS.put(mixin, modId)
		);
	}

	static {
		SevenElementsMixinPlugin.addConditionalMixins(
			"taxfreelevels",
			"io.github.xrickastley.sevenelements.mixin.compat.taxfreelevels.ElementalInfusionScreenHandlerMixin"
		);

		SevenElementsMixinPlugin.addConditionalMixins(
			"taxfreelevels",
			"io.github.xrickastley.sevenelements.mixin.compat.taxfreelevels.ElementalInfusionScreenHandlerMixin"
		);
	}
}
