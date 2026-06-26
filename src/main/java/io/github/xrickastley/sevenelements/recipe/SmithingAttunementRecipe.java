package io.github.xrickastley.sevenelements.recipe;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.item.SevenElementsSmithingTemplateItem;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
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

	public boolean matches(SmithingRecipeInput smithingRecipeInput, World world) {
		return this.template.test(smithingRecipeInput.template()) && this.base.test(smithingRecipeInput.base()) && this.addition.test(smithingRecipeInput.addition());
	}

	public ItemStack craft(SmithingRecipeInput smithingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
		ItemStack itemStack = smithingRecipeInput.base();

		if (!this.base.test(itemStack)) return ItemStack.EMPTY;

		if (!(smithingRecipeInput.template().getItem() instanceof final SevenElementsSmithingTemplateItem template)) 
			return ItemStack.EMPTY;

		final ElementalAttunementComponent elementalAttunement = itemStack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT); 

		if (elementalAttunement != null && elementalAttunement.element() == template.getElementalAttunement())
			return ItemStack.EMPTY;

		final ItemStack result = itemStack.copy();

		ElementalAttunementComponent.applyAttunement(result, template.getElementalAttunement());

		return result;
	}

	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
		final ItemStack itemStack = new ItemStack(Items.IRON_SWORD);
		
		itemStack.set(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT, new ElementalAttunementComponent(Element.PYRO));

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
		private static final MapCodec<SmithingAttunementRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					Ingredient.ALLOW_EMPTY_CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
					Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
					Ingredient.ALLOW_EMPTY_CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition)
				)
				.apply(instance, SmithingAttunementRecipe::new)
		);
		public static final PacketCodec<RegistryByteBuf, SmithingAttunementRecipe> PACKET_CODEC = PacketCodec.ofStatic(
			SmithingAttunementRecipe.Serializer::write, SmithingAttunementRecipe.Serializer::read
		);

		@Override
		public MapCodec<SmithingAttunementRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, SmithingAttunementRecipe> packetCodec() {
			return PACKET_CODEC;
		}

		private static SmithingAttunementRecipe read(RegistryByteBuf buf) {
			Ingredient ingredient = Ingredient.PACKET_CODEC.decode(buf);
			Ingredient ingredient2 = Ingredient.PACKET_CODEC.decode(buf);
			Ingredient ingredient3 = Ingredient.PACKET_CODEC.decode(buf);
			return new SmithingAttunementRecipe(ingredient, ingredient2, ingredient3);
		}

		private static void write(RegistryByteBuf buf, SmithingAttunementRecipe recipe) {
			Ingredient.PACKET_CODEC.encode(buf, recipe.template);
			Ingredient.PACKET_CODEC.encode(buf, recipe.base);
			Ingredient.PACKET_CODEC.encode(buf, recipe.addition);
		}
	}
}
