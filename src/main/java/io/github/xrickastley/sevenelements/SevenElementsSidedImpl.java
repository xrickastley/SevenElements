package io.github.xrickastley.sevenelements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import io.github.xrickastley.sevenelements.annotation.ExpectedEnvironment;

import net.fabricmc.api.EnvType;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class SevenElementsSidedImpl {
	static BiFunction<Text, Integer, List<Text>> WRAP_LINES = (text, width) -> {
		final LineBreakingVisistor<Object> visitor = new LineBreakingVisistor<>(width / 3);
		text.visit(visitor, Style.EMPTY);
		return visitor.getTexts();
	};

	private static class LineBreakingVisistor<T> implements StringVisitable.StyledVisitor<T> {
		private final int maxLength;
		private final List<Text> texts = new ArrayList<>();
		private MutableText currentText = Text.empty();

		LineBreakingVisistor(int maxLength) {
			this.maxLength = maxLength;
		}

		@Override
		public Optional<T> accept(Style style, String asString) {
			Stream.of(asString.split("\n"))
				.forEachOrdered(line -> acceptLine(style, line));

			return Optional.empty();
		}

		public void acceptLine(Style style, String line) {
			Stream.of(line.split(" "))
				.forEachOrdered(word -> acceptWord(style, word));

			this.pushText(true);
		}

		public Optional<T> acceptWord(Style style, String word) {
			if (this.currentText.getString().length() + word.length() > this.maxLength) {
				this.texts.add(this.currentText);

				this.currentText = Text.literal(word).setStyle(style);
			} else {
				if (!this.currentText.getString().isEmpty())
					this.currentText.append(" ");

				if (!word.isEmpty())
					this.currentText.append(Text.literal(word).setStyle(style));
			}

			return Optional.empty();
		}

		public void pushText(boolean force) {
			if (force || this.currentText.getString().length() > 0)
				this.texts.add(this.currentText);

			this.currentText = Text.empty();
		}

		List<Text> getTexts() {
			this.pushText(false);
			this.currentText = null;

			return this.texts;
		}
	}

	@ExpectedEnvironment(EnvType.CLIENT)
	public static BiFunction<Text, Integer, List<Text>> getWrapLinesFunction() {
		return SevenElementsSidedImpl.WRAP_LINES;
	}
}
