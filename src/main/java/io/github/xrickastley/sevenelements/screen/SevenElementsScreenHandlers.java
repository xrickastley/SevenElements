package io.github.xrickastley.sevenelements.screen;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class SevenElementsScreenHandlers {
	public static final ScreenHandlerType<ElementalInfusionScreenHandler> ELEMENTAL_INFUSION_SCREEN_HANDLER = new ScreenHandlerType<>(ElementalInfusionScreenHandler::new, FeatureSet.empty());

	public static void register() {
		register("elemental_infusion", ELEMENTAL_INFUSION_SCREEN_HANDLER);
	}

	private static <T extends ScreenHandler> void register(String id, ScreenHandlerType<T> type) {
		Registry.register(Registries.SCREEN_HANDLER, SevenElements.identifier(id), type);
	}
}
