package io.github.xrickastley.sevenelements.util;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TextHelper {
	public static final Identifier GENSHIN_FONT = SevenElements.identifier("genshin");

	public static MutableText font(String text, Identifier font) {
		return font(Text.literal(text), font);
	}

	public static MutableText font(MutableText text, Identifier font) {
		return text.setStyle(text.getStyle().withFont(font));
	}

	public static MutableText gradient(String text, int start, int end) {
		final MutableText result = Text.empty();

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
				Text.literal(String.valueOf(text.charAt(i)))
					.fillStyle(Style.EMPTY.withColor(color))
			);
	    }

	    return result;
	}

	public static MutableText reaction(String translationKey, String color) {
		return TextHelper.reaction(translationKey, Color.fromRGBAHex(color));
	}

	public static MutableText reaction(String translationKey, Color color) {
		return TextHelper
			.font(Text.translatable(translationKey), TextHelper.GENSHIN_FONT)
			.fillStyle(Style.EMPTY.withColor(color.asRGB()));
	}

	public static MutableText color(String text, Color color) {
		return TextHelper.color(text, color.asRGB());
	}

	public static MutableText color(String text, int rgbColor) {
		return Text.literal(text).fillStyle(Style.EMPTY.withColor(rgbColor));
	}

	public static MutableText color(MutableText text, Color color) {
		return TextHelper.color(text, color.asRGB());
	}

	public static MutableText color(MutableText text, int rgbColor) {
		return text.fillStyle(text.getStyle().withColor(rgbColor));
	}

	public static MutableText noModifiers(MutableText text) {
		return text.setStyle(
			text.getStyle()
				.withBold(false)
				.withItalic(false)
				.withStrikethrough(false)
				.withObfuscated(false)
				.withUnderline(false)
		);
	}
}
