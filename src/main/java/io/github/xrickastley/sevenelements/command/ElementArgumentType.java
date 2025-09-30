package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

public class ElementArgumentType implements ArgumentType<Element> {
	public static ElementArgumentType element() {
		return new ElementArgumentType();
	}

	public static Element getElement(final CommandContext<ServerCommandSource> context, final String name) throws CommandSyntaxException {
		return context.getArgument(name, Element.class);
	}

	@Override
	public Element parse(final StringReader stringReader) throws CommandSyntaxException {
		final String string = stringReader.readUnquotedString();

		return Element.valueOf(string.toUpperCase());
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(
			ElementArgumentType.map(Element.values(), Functions.compose(Element::toString, String::toLowerCase)),
			builder
		);
	}

	public Collection<String> getExamples() {
		return ElementArgumentType.map(Element.values(), Functions.compose(Element::toString, String::toLowerCase));
	}

	private static <T, R> List<R> map(T[] array, Function<T, R> mapper) {
		final List<R> result = new ArrayList<>();

		for (final T element : array) result.add(mapper.apply(element));

		return result;
	}
}
