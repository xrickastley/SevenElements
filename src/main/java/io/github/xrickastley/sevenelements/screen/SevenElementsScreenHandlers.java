package io.github.xrickastley.sevenelements.screen;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class SevenElementsScreenHandlers {
	public static final MenuType<ElementalInfusionScreenHandler> ELEMENTAL_INFUSION_SCREEN_HANDLER = new MenuType<>(ElementalInfusionScreenHandler::new, FeatureFlagSet.of());

	public static void register() {
		register("elemental_infusion", ELEMENTAL_INFUSION_SCREEN_HANDLER);
	}

	private static <T extends AbstractContainerMenu> void register(String id, MenuType<T> type) {
		Registry.register(BuiltInRegistries.MENU, SevenElements.identifier(id), type);
	}
}
