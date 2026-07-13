package io.github.xrickastley.sevenelements;

import java.util.ArrayList;
import java.util.List;

import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

public class SevenElementsClientUtil {
	static class TextRebuilder implements FormattedCharSink {
		private final List<MutableComponent> texts = new ArrayList<>();
		private Style currentStyle = null;
		private StringBuffer currentText = new StringBuffer();

		public boolean accept(int index, Style style, int codePoint) {
			if (currentStyle == null)
				currentStyle = style;

			if (!currentStyle.equals(style)) {
				texts.add(Component.literal(currentText.toString()).setStyle(currentStyle));

				currentText = new StringBuffer();
				currentStyle = style;
			}

			currentText.appendCodePoint(codePoint);

			return true;
		}

		public MutableComponent getText() {
			final MutableComponent result = Component.empty();

			if (!currentText.isEmpty())
				texts.add(
					Component.literal(currentText.toString())
						.setStyle(JavaScriptUtil.nullishCoalesing(currentStyle, Style.EMPTY))
				);

			texts.forEach(result::append);

			currentText = new StringBuffer();
			currentStyle = null;
			texts.clear();

			return result;
		}

		public static MutableComponent rebuild(FormattedCharSequence text) {
			final TextRebuilder rebuilder = new TextRebuilder();
			text.accept(rebuilder);
			return rebuilder.getText();
		}
	}
}
