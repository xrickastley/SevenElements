package io.github.xrickastley.sevenelements.recipe;

import io.github.xrickastley.sevenelements.SevenElements;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SevenElementsRecipeSerializer {
	public static void register() {
		register("smithing_attunement", SmithingAttunementRecipe.SERIALIZER);
	}

	public static <T extends Recipe<?>> void register(String id, RecipeSerializer<T> serializer) {
		RecipeSynchronization.synchronizeRecipeSerializer(serializer);

		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, SevenElements.identifier(id), serializer);
	}
}
