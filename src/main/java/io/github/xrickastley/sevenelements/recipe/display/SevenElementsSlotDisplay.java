package io.github.xrickastley.sevenelements.recipe.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.stream.Stream;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.recipe.SmithingAttunementRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class SevenElementsSlotDisplay {
	public record SmithingAttunementDemoSlotDisplay(SlotDisplay base, SlotDisplay element) implements SlotDisplay {
		public static final MapCodec<SmithingAttunementDemoSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					SlotDisplay.CODEC.fieldOf("base").forGetter(SmithingAttunementDemoSlotDisplay::base),
					SlotDisplay.CODEC.fieldOf("element").forGetter(SmithingAttunementDemoSlotDisplay::element)
				)
				.apply(instance, SmithingAttunementDemoSlotDisplay::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, SmithingAttunementDemoSlotDisplay> PACKET_CODEC = StreamCodec.composite(
			SlotDisplay.STREAM_CODEC, SmithingAttunementDemoSlotDisplay::base,
			SlotDisplay.STREAM_CODEC, SmithingAttunementDemoSlotDisplay::element,
			SmithingAttunementDemoSlotDisplay::new
		);

		public static final SlotDisplay.Type<SmithingAttunementDemoSlotDisplay> SERIALIZER = new SlotDisplay.Type<>(CODEC, PACKET_CODEC);

		@Override
		public SlotDisplay.Type<SmithingAttunementDemoSlotDisplay> type() {
			return SERIALIZER;
		}

		@Override
		public <T> Stream<T> resolve(ContextMap parameters, DisplayContentsFactory<T> factory) {
			if (factory instanceof DisplayContentsFactory.ForStacks<T> fromStack) {
				HolderLookup.Provider wrapperLookup = parameters.getOptional(SlotDisplayContext.REGISTRIES);

				if (wrapperLookup != null) {
					RandomSource random = RandomSource.create(System.identityHashCode(this));
					List<ItemStack> list = this.base.resolveForStacks(parameters);
					if (list.isEmpty()) return Stream.empty();

					List<ItemStack> list3 = this.element.resolveForStacks(parameters);
					if (list3.isEmpty()) return Stream.empty();

					return Stream.generate(() -> {
						ItemStack itemStack = Util.getRandom(list, random);
						ItemStack itemStack3 = Util.getRandom(list3, random);
						return SmithingAttunementRecipe.craft(itemStack, itemStack3);
					}).limit(256L).filter(stack -> !stack.isEmpty()).limit(16L).map(fromStack::forStack);
				}
			}

			return Stream.empty();
		}
	}

	public static void register() {
		register("smithing_attunement", SevenElementsSlotDisplay.SmithingAttunementDemoSlotDisplay.SERIALIZER);
	}

	private static void register(String id, SlotDisplay.Type<?> slotDisplay) {
		Registry.register(BuiltInRegistries.SLOT_DISPLAY, SevenElements.identifier(id), slotDisplay);
	}
}
