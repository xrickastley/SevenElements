package io.github.xrickastley.sevenelements.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.item.SevenElementsSmithingTemplateItem;
import io.github.xrickastley.sevenelements.recipe.display.SevenElementsSlotDisplay;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;

public class SmithingAttunementRecipe implements SmithingRecipe {
	final Optional<Ingredient> template;
	final Ingredient base;
	final Optional<Ingredient> addition;
	private @Nullable IngredientPlacement ingredientPlacement;

	public SmithingAttunementRecipe(Optional<Ingredient> template, Ingredient base, Optional<Ingredient> addition) {
		this.template = template;
		this.base = base;
		this.addition = addition;
	}

	public ItemStack craft(SmithingRecipeInput smithingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
		return SmithingAttunementRecipe.craft(wrapperLookup, smithingRecipeInput.base(), smithingRecipeInput.template());
	}

	public static ItemStack craft(RegistryWrapper.WrapperLookup registries, ItemStack base, ItemStack template) {
		if (!(template.getItem() instanceof final SevenElementsSmithingTemplateItem template2))
			return ItemStack.EMPTY;

		final ElementalAttunementComponent elementalAttunement = base.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);

		if (elementalAttunement != null && elementalAttunement.element() == template2.getElementalAttunement())
			return ItemStack.EMPTY;

		final ItemStack result = base.copy();

		ElementalAttunementComponent.applyAttunement(result, template2.getElementalAttunement());

		return result;
	}

	@Override
	public Optional<Ingredient> template() {
		return this.template;
	}

	@Override
	public Ingredient base() {
		return this.base;
	}

	@Override
	public Optional<Ingredient> addition() {
		return this.addition;
	}

	@Override
	public RecipeSerializer<SmithingAttunementRecipe> getSerializer() {
		return SevenElementsRecipeSerializer.SMITHING_ATTUNEMENT;
	}

	@Override
	public IngredientPlacement getIngredientPlacement() {
		if (this.ingredientPlacement == null) {
			this.ingredientPlacement = IngredientPlacement.forMultipleSlots(List.of(this.template, Optional.of(this.base), this.addition));
		}

		return this.ingredientPlacement;
	}

	@Override
	public List<RecipeDisplay> getDisplays() {
		SlotDisplay slotDisplay = Ingredient.toDisplay(Optional.of(this.base));
		SlotDisplay slotDisplay2 = Ingredient.toDisplay(this.addition);
		SlotDisplay slotDisplay3 = Ingredient.toDisplay(this.template);
		return List.of(
			new SmithingRecipeDisplay(
				slotDisplay3,
				slotDisplay,
				slotDisplay2,
				new SevenElementsSlotDisplay.SmithingAttunementSlotDisplay(slotDisplay, slotDisplay3),
				new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
			)
		);
	}

	public static class Serializer implements RecipeSerializer<SmithingAttunementRecipe> {
		private static final MapCodec<SmithingAttunementRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					Ingredient.CODEC.optionalFieldOf("template").forGetter(recipe -> recipe.template),
					Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
					Ingredient.CODEC.optionalFieldOf("addition").forGetter(recipe -> recipe.addition)
				)
				.apply(instance, SmithingAttunementRecipe::new)
		);

		public static final PacketCodec<RegistryByteBuf, SmithingAttunementRecipe> PACKET_CODEC = PacketCodec.tuple(
			Ingredient.OPTIONAL_PACKET_CODEC, recipe -> recipe.template,
			Ingredient.PACKET_CODEC, recipe -> recipe.base,
			Ingredient.OPTIONAL_PACKET_CODEC, recipe -> recipe.addition,
			SmithingAttunementRecipe::new
		);

		@Override
		public MapCodec<SmithingAttunementRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, SmithingAttunementRecipe> packetCodec() {
			return PACKET_CODEC;
		}
	}
}
