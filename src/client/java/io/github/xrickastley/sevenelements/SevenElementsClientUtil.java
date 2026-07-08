package io.github.xrickastley.sevenelements;

import java.util.ArrayList;
import java.util.List;

import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class SevenElementsClientUtil {
	static class TextRebuilder implements CharacterVisitor {
		private final List<MutableText> texts = new ArrayList<>();
		private Style currentStyle = null;
		private StringBuffer currentText = new StringBuffer();

		public boolean accept(int index, Style style, int codePoint) {
			if (currentStyle == null)
				currentStyle = style;

			if (!currentStyle.equals(style)) {
				texts.add(Text.literal(currentText.toString()).setStyle(currentStyle));

				currentText = new StringBuffer();
				currentStyle = style;
			}

			currentText.appendCodePoint(codePoint);

			return true;
		}

		public MutableText getText() {
			final MutableText result = Text.empty();

			if (!currentText.isEmpty())
				texts.add(
					Text.literal(currentText.toString())
						.setStyle(JavaScriptUtil.nullishCoalesing(currentStyle, Style.EMPTY))
				);

			texts.forEach(result::append);

			currentText = new StringBuffer();
			currentStyle = null;
			texts.clear();

			return result;
		}

		public static MutableText rebuild(OrderedText text) {
			final TextRebuilder rebuilder = new TextRebuilder();
			text.accept(rebuilder);
			return rebuilder.getText();
		}
	}
}
