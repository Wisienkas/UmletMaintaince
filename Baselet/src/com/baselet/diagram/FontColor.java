package com.baselet.diagram;

import java.awt.Color;

import com.baselet.control.StringStyle;
import com.baselet.control.enumerations.FormatLabels;

public class FontColor {

	private static Color color;

	public FontColor(String text) {
		findColor(text);
	}

	private static void findColor(String text) {
		StringStyle style = StringStyle.analyzeFormatLabels(text);

		if (style.getFormat().contains(FormatLabels.RED)) {
			color = Color.RED;
		}

		else if (style.getFormat().contains(FormatLabels.GREEN)) {
			color = Color.GREEN;
		}

		else {
			color = Color.BLACK;
		}
	}

	public static Color getColor() {
		return color;
	}
}
