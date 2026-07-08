package io.github.xrickastley.sevenelements.component.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.item.ItemStack;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;

public class ElementModifyingComponentImpl {
	private static final List<ComponentKey<? extends ElementModifyingComponent>> COMPONENTS = new ArrayList<>();

	public static void addComponent(ComponentKey<? extends ElementModifyingComponent> key) {
		ElementModifyingComponentImpl.COMPONENTS.add(key);
	}

	@ApiStatus.Internal
	public static Stream<? extends ElementModifyingComponent> getComponentsOf(ItemStack stack) {
		return ElementModifyingComponentImpl.COMPONENTS
			.stream()
			.mapMulti((key, consumer) -> ClassInstanceUtil.ifPresentMapped(stack, key::get, consumer));
	}
}
