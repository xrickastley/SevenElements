package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Holder;

public class InternalCooldownTagType implements ArgumentType<Holder<String>> {
	public static InternalCooldownTagType tag() {
		return new InternalCooldownTagType();
	}

	@SuppressWarnings("unchecked")
	public static InternalCooldownTag getTag(final CommandContext<?> context, final String name) {
		return context
			.getArgument(name, (Class<Holder<String>>) ClassInstanceUtil.cast(Holder.class))
			.map(InternalCooldownTag::of);
	}

	public static InternalCooldownTag getTagOrDefault(final CommandContext<?> context, final String name, final InternalCooldownTag fallback) {
		try {
			return InternalCooldownTagType.getTag(context, name);
		} catch (IllegalArgumentException e) {
			return fallback;
		}
	}

	@Override
	public Holder<String> parse(final StringReader reader) throws CommandSyntaxException {
		return Holder.of(this.getString(reader));
	}

	private String getString(final StringReader reader) throws CommandSyntaxException {
		return StringReader.isQuotedStringStart(reader.peek())
			? reader.readQuotedString()
			: this.lenientReadUnquotedString(reader);
	}

	public String lenientReadUnquotedString(final StringReader reader) {
		final int start = reader.getCursor();

		while (reader.canRead() && reader.peek() != ' ') reader.skip();

		return reader.getString().substring(start, reader.getCursor());
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		InternalCooldownTag.applySuggestions(builder);

		return builder.buildFuture();
	}
}
