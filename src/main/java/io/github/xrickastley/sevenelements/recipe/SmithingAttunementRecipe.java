package io.github.xrickastley.sevenelements.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.item.SevenElementsSmithingTemplateItem;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;

public class SmithingAttunementRecipe implements SmithingRecipe {
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;

	public SmithingAttunementRecipe(Ingredient template, Ingredient base, Ingredient addition) {
		this.template = template;
		this.base = base;
		this.addition = addition;
	}

	public boolean matches(Inventory inventory, World world) {
		return this.template.test(inventory.getStack(0))
			&& this.base.test(inventory.getStack(1))
			&& this.addition.test(inventory.getStack(2));
	}

	public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
		ItemStack itemStack = inventory.getStack(1);

		if (!this.base.test(itemStack)) return ItemStack.EMPTY;

		if (!(inventory.getStack(0).getItem() instanceof final SevenElementsSmithingTemplateItem template))
			return ItemStack.EMPTY;

		final ElementalAttunementComponent elementalAttunement = ElementalAttunementComponent.get(itemStack);

		if (elementalAttunement != null && elementalAttunement.element() == template.getElementalAttunement())
			return ItemStack.EMPTY;

		final ItemStack result = itemStack.copy();

		ElementalAttunementComponent.applyAttunement(result, template.getElementalAttunement());

		return result;
	}

	@Override
	public ItemStack getResult(DynamicRegistryManager registryManager) {
		final ItemStack itemStack = new ItemStack(Items.IRON_SWORD);

		ElementalAttunementComponent.applyAttunement(itemStack, Element.PYRO);

		return itemStack;
	}

	@Override
	public boolean testTemplate(ItemStack stack) {
		return this.template.test(stack);
	}

	@Override
	public boolean testBase(ItemStack stack) {
		return this.base.test(stack);
	}

	@Override
	public boolean testAddition(ItemStack stack) {
		return this.addition.test(stack);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SevenElementsRecipeSerializer.SMITHING_ATTUNEMENT;
	}

	@Override
	public boolean isEmpty() {
		return Stream.of(this.template, this.base, this.addition)
			.anyMatch(Ingredient::isEmpty);
	}

	public static class Serializer implements RecipeSerializer<SmithingAttunementRecipe> {
		private static final Codec<SmithingAttunementRecipe> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Ingredient.ALLOW_EMPTY_CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
					Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
					Ingredient.ALLOW_EMPTY_CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition)
				)
				.apply(instance, SmithingAttunementRecipe::new)
		);

		@Override
		public Codec<SmithingAttunementRecipe> codec() {
			return CODEC;
		}

		@Override
		public SmithingAttunementRecipe read(PacketByteBuf packetByteBuf) {
			Ingredient ingredient = Ingredient.fromPacket(packetByteBuf);
			Ingredient ingredient2 = Ingredient.fromPacket(packetByteBuf);
			Ingredient ingredient3 = Ingredient.fromPacket(packetByteBuf);
			return new SmithingAttunementRecipe(ingredient, ingredient2, ingredient3);
		}

		@Override
		public void write(PacketByteBuf packetByteBuf, SmithingAttunementRecipe smithingAttunementRecipe) {
			smithingAttunementRecipe.template.write(packetByteBuf);
			smithingAttunementRecipe.base.write(packetByteBuf);
			smithingAttunementRecipe.addition.write(packetByteBuf);
		}
	}
}
