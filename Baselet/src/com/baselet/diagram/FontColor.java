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
		else if (style.getFormat().contains(FormatLabels.BLUE))
		{
			color = Color.BLUE;
		}
		else if (style.getFormat().contains(FormatLabels.YELLOW))
		{
			color = Color.YELLOW;
		}
		else if (style.getFormat().contains(FormatLabels.MAGENTA))
		{
			color = Color.MAGENTA;
		}
		else if (style.getFormat().contains(FormatLabels.WHITE))
		{
			color = Color.WHITE;
		}
		else if (style.getFormat().contains(FormatLabels.BLACK))
		{
			color = Color.BLACK;
		}
		else if (style.getFormat().contains(FormatLabels.ORANGE))
		{
			color = Color.ORANGE;
		}
		else if (style.getFormat().contains(FormatLabels.CYAN))
		{
			color = Color.CYAN;
		}
		else if (style.getFormat().contains(FormatLabels.DARK_GRAY))
		{
			color = Color.DARK_GRAY;
		}
		else if (style.getFormat().contains(FormatLabels.GRAY))
		{
			color = Color.GRAY;
		}
		else if (style.getFormat().contains(FormatLabels.LIGHT_GRAY))
		{
			color = Color.LIGHT_GRAY;
		}
		else if (style.getFormat().contains(FormatLabels.PINK))
		{
			color = Color.PINK;
		}
		else {
			color = Color.BLACK;
		}
	}

	public static Color getColor() {
		return color;
	}
}
