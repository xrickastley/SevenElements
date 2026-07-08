package io.github.xrickastley.sevenelements.recipe.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.stream.Stream;

import io.github.xrickastley.sevenelements.recipe.SmithingAttunementRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.display.DisplayedItemFactory;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.math.random.Random;

public class SevenElementsSlotDisplay {
	public record SmithingAttunementSlotDisplay(SlotDisplay base, SlotDisplay material, SlotDisplay pattern) implements SlotDisplay {
		public static final MapCodec<SmithingAttunementSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					SlotDisplay.CODEC.fieldOf("base").forGetter(SmithingAttunementSlotDisplay::base),
					SlotDisplay.CODEC.fieldOf("material").forGetter(SmithingAttunementSlotDisplay::material),
					SlotDisplay.CODEC.fieldOf("pattern").forGetter(SmithingAttunementSlotDisplay::pattern)
				)
				.apply(instance, SmithingAttunementSlotDisplay::new)
		);

		public static final PacketCodec<RegistryByteBuf, SmithingAttunementSlotDisplay> PACKET_CODEC = PacketCodec.tuple(
			SlotDisplay.PACKET_CODEC, SmithingAttunementSlotDisplay::base,
			SlotDisplay.PACKET_CODEC, SmithingAttunementSlotDisplay::material,
			SlotDisplay.PACKET_CODEC, SmithingAttunementSlotDisplay::pattern,
			SmithingAttunementSlotDisplay::new
		);

		public static final SlotDisplay.Serializer<SmithingAttunementSlotDisplay> SERIALIZER = new SlotDisplay.Serializer<>(CODEC, PACKET_CODEC);

		@Override
		public SlotDisplay.Serializer<SmithingAttunementSlotDisplay> serializer() {
			return SERIALIZER;
		}

		@Override
		public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
			if (factory instanceof DisplayedItemFactory.FromStack<T> fromStack) {
				RegistryWrapper.WrapperLookup wrapperLookup = parameters.getNullable(SlotDisplayContexts.REGISTRIES);

				if (wrapperLookup != null) {
					Random random = Random.create(System.identityHashCode(this));
					List<ItemStack> list = this.base.getStacks(parameters);
					if (list.isEmpty()) return Stream.empty();

					List<ItemStack> list2 = this.material.getStacks(parameters);
					if (list2.isEmpty()) return Stream.empty();

					List<ItemStack> list3 = this.pattern.getStacks(parameters);
					if (list3.isEmpty()) return Stream.empty();

					return Stream.generate(() -> {
						ItemStack itemStack = Util.getRandom(list, random);
						ItemStack itemStack2 = Util.getRandom(list2, random);
						ItemStack itemStack3 = Util.getRandom(list3, random);
						return SmithingAttunementRecipe.craft(wrapperLookup, itemStack, itemStack2, itemStack3);
					}).limit(256L).filter(stack -> !stack.isEmpty()).limit(16L).map(fromStack::toDisplayed);
				}
			}

			return Stream.empty();
		}
	}
}
