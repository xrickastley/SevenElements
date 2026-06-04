package io.github.xrickastley.sevenelements.component.interfaces;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;

public interface TooltipProvider {
	public static void appendTooltip(ComponentKey<? extends TooltipProvider> component, ItemStack stack, @Nullable PlayerEntity player, TooltipContext context, Consumer<Text> textConsumer) {
		final TooltipProvider provider = component.get(stack);

		if (provider == null) return;

		provider.appendTooltip(player, context, textConsumer);
	}

	public void appendTooltip(@Nullable PlayerEntity player, TooltipContext context, Consumer<Text> textConsumer);
}
