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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

public class SmithingAttunementRecipe extends SimpleSmithingRecipe {
	public static final MapCodec<SmithingAttunementRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
				Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
				Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
				Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition)
			)
			.apply(instance, SmithingAttunementRecipe::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, SmithingAttunementRecipe> PACKET_CODEC = StreamCodec.composite(
		Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.template,
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.base,
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.addition,
		SmithingAttunementRecipe::new
	);

	public static final RecipeSerializer<SmithingAttunementRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, PACKET_CODEC);

	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	private @Nullable PlacementInfo ingredientPlacement;

	public SmithingAttunementRecipe(final Recipe.CommonInfo commonInfo, Ingredient template, Ingredient base, Ingredient addition) {
		super(commonInfo);

		this.template = template;
		this.base = base;
		this.addition = addition;
	}

	@Override
	public ItemStack assemble(SmithingRecipeInput input) {
		return SmithingAttunementRecipe.craft(input.base(), input.template());
	}

	public static ItemStack craft(ItemStack base, ItemStack template) {
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
	public Optional<Ingredient> templateIngredient() {
		return Optional.of(this.template);
	}

	@Override
	public Ingredient baseIngredient() {
		return this.base;
	}

	@Override
	public Optional<Ingredient> additionIngredient() {
		return Optional.of(this.addition);
	}

	@Override
	public RecipeSerializer<SmithingAttunementRecipe> getSerializer() {
		return SmithingAttunementRecipe.SERIALIZER;
	}

	@Override
	protected PlacementInfo createPlacementInfo() {
		return PlacementInfo.create(List.of(this.template, this.base, this.addition));
	}

	@Override
	public List<RecipeDisplay> display() {
		SlotDisplay slotDisplay = this.base.display();
		SlotDisplay slotDisplay2 = this.addition.display();
		SlotDisplay slotDisplay3 = this.template.display();
		return List.of(
			new SmithingRecipeDisplay(
				slotDisplay3,
				slotDisplay,
				slotDisplay2,
				new SevenElementsSlotDisplay.SmithingAttunementDemoSlotDisplay(slotDisplay, slotDisplay3),
				new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
			)
		);
	}
}
