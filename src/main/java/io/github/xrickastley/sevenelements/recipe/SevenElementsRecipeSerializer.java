package io.github.xrickastley.sevenelements.recipe;

import io.github.xrickastley.sevenelements.SevenElements;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SevenElementsRecipeSerializer {
	public static final RecipeSerializer<SmithingAttunementRecipe> SMITHING_ATTUNEMENT = new SmithingAttunementRecipe.Serializer();

	public static void register() {
		register("smithing_attunement", SevenElementsRecipeSerializer.SMITHING_ATTUNEMENT);
	}

	public static <S extends RecipeSerializer<T>, T extends Recipe<?>> void register(String id, S serializer) {
		Registry.register(Registries.RECIPE_SERIALIZER, SevenElements.identifier(id), serializer);
	}
}
