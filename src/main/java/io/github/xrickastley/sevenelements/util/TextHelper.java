package io.github.xrickastley.sevenelements.util;

import java.util.List;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.SevenElementsSidedImpl;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class TextHelper {
	public static final Identifier GENSHIN_FONT = SevenElements.identifier("genshin");

	public static MutableComponent font(String text, Identifier font) {
		return font(Component.literal(text), font);
	}

	public static MutableComponent font(MutableComponent text, Identifier font) {
		return text.setStyle(text.getStyle().withFont(new FontDescription.Resource(font)));
	}

	public static MutableComponent gradient(String text, int start, int end) {
		final MutableComponent result = Component.empty();

		final int startR = (start >>> 16) & 0xFF;
		final int startG = (start >>> 8) & 0xFF;
		final int startB = start & 0xFF;

		final int endR = (end >>> 16) & 0xFF;
		final int endG = (end >>> 8) & 0xFF;
		final int endB = end & 0xFF;

		for (int i = 0; i < text.length(); i++) {
			final double step = i / ((double) text.length() - 1);

			final int r = (int) Math.round(startR + (endR - startR) * step);
			final int g = (int) Math.round(startG + (endG - startG) * step);
			final int b = (int) Math.round(startB + (endB - startB) * step);

			final int color = (r << 16) | (g << 8) | b;

			result.append(
				Component.literal(String.valueOf(text.charAt(i)))
					.withStyle(Style.EMPTY.withColor(color))
			);
		}

		return result;
	}

	public static MutableComponent reaction(String translationKey, String color) {
		return TextHelper.reaction(translationKey, Color.fromRGBAHex(color));
	}

	public static MutableComponent reaction(String translationKey, Color color) {
		return TextHelper
			.font(Component.translatable(translationKey), TextHelper.GENSHIN_FONT)
			.withStyle(Style.EMPTY.withColor(color.asRGB()));
	}

	public static MutableComponent color(String text, Color color) {
		return TextHelper.color(text, color.asRGB());
	}

	public static MutableComponent color(String text, int rgbColor) {
		return Component.literal(text).withStyle(Style.EMPTY.withColor(rgbColor));
	}

	public static MutableComponent color(MutableComponent text, Color color) {
		return TextHelper.color(text, color.asRGB());
	}

	public static MutableComponent color(MutableComponent text, int rgbColor) {
		return text.withStyle(text.getStyle().withColor(rgbColor));
	}

	public static MutableComponent noModifiers(MutableComponent text) {
		return text.setStyle(
			text.getStyle()
				.withBold(false)
				.withItalic(false)
				.withStrikethrough(false)
				.withObfuscated(false)
				.withUnderlined(false)
		);
	}

	public static List<Component> wrapLines(Component text, int width) {
		return SevenElementsSidedImpl.getWrapLinesFunction().apply(text, width);
	}
}
