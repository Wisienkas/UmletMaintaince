package com.baselet.diagram;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;

import com.baselet.control.Constants;
import com.baselet.control.Utils;
import com.baselet.control.enumerations.AlignHorizontal;
import com.baselet.diagram.draw.geom.DimensionDouble;

public class FontHandler {

	private final DiagramHandler handler;
	private Double fontSize;
	private Double diagramDefaultSize = null; // if "fontsize=..." is uncommented this variable is set

	private String diagramDefaultFontFamily = null;
	private FontRenderContext fontrenderContext = new FontRenderContext(null, true, true);

	public FontHandler(DiagramHandler handler) {
		this.handler = handler;
	}

	public void setFontSize(Double fontsize) {
		fontSize = fontsize;
	}

	public void setDiagramDefaultFontFamily(String fontfamily) {
		if (Constants.fontFamilyList.contains(fontfamily)) {
			diagramDefaultFontFamily = fontfamily;
		}
	}

	public void resetDiagramDefaultFontFamily() {
		diagramDefaultFontFamily = null;
	}

	private String getDiagramDefaultFontFamily() {
		String returnFontFamily;
		if (diagramDefaultFontFamily != null) {
			returnFontFamily = diagramDefaultFontFamily;
		}
		else {
			returnFontFamily = Constants.defaultFontFamily;
		}

		return returnFontFamily;
	}

	public void setDiagramDefaultFontSize(Double diagramDefaultSize) {
		this.diagramDefaultSize = diagramDefaultSize;
	}

	public void resetFontSize() {
		fontSize = null;
	}

	public void resetDiagramDefaultFontSize() {
		diagramDefaultSize = null;
	}

	public double getFontSize() {
		return getFontSize(true);
	}

	public double getFontSize(boolean applyZoom) {
		Double returnFontSize;
		if (fontSize != null) {
			returnFontSize = fontSize;
		}
		else if (diagramDefaultSize != null) {
			returnFontSize = diagramDefaultSize;
		}
		else {
			returnFontSize = Double.valueOf(Constants.defaultFontsize);
		}

		if (applyZoom) {
			return returnFontSize * handler.getGridSize() / Constants.DEFAULTGRIDSIZE;
		}
		else {
			return returnFontSize;
		}
	}

	public Font getFont() {
		return getFont(true);
	}

	public Font getFont(boolean applyZoom) {
		return new Font(getDiagramDefaultFontFamily(), Font.PLAIN, (int) getFontSize(applyZoom));
	}

	public double getDistanceBetweenTexts() {
		return getDistanceBetweenTexts(true);
	}

	public double getDistanceBetweenTexts(boolean applyZoom) {
		return getFontSize(applyZoom) / 4;
	}

	public DimensionDouble getTextSize(String stringWithFormatLabels) {
		return getTextSize(stringWithFormatLabels, true);
	}

	public DimensionDouble getTextSize(String stringWithFormatLabels, boolean applyZoom) {
		if (stringWithFormatLabels.isEmpty()) {
			return new DimensionDouble(0, 0);
		}
		return Utils.getTextSize(new FormattedFont(stringWithFormatLabels, getFontSize(applyZoom), getFont(applyZoom), fontrenderContext));
	}

	public double getTextWidth(String s) {
		return getTextWidth(s, true);
	}

	public double getTextWidth(String s, boolean applyZoom) {
		if (s == null) {
			return 0;
		}
		return this.getTextSize(s, applyZoom).getWidth();
	}

	public void writeText(Graphics2D g2, String s, double x, double y, AlignHorizontal align) {
		writeText(g2, s, x, y, align, true);
	}

	public void writeText(Graphics2D g2, String s, double x, double y, AlignHorizontal align, boolean applyZoom) {
		for (String line : s.split("\n", -1)) {
			write(g2, line, x, y, align, applyZoom);
			y += g2.getFontMetrics().getHeight();
		}
	}

	private void write(Graphics2D g2, String stringWithFormatLabels, double x, double y, AlignHorizontal align, boolean applyZoom) {
		if (stringWithFormatLabels == null || stringWithFormatLabels.isEmpty()) {
			return;
		}
		double fontSize = getFontSize(applyZoom);
		FormattedFont formattedFont = new FormattedFont(stringWithFormatLabels, fontSize, getFont(applyZoom), g2.getFontRenderContext());
		FontColor color = new FontColor(stringWithFormatLabels);
		fontrenderContext = g2.getFontRenderContext(); // TODO workaround to make sure getTextSize works without a graphics object

		if (align == AlignHorizontal.CENTER) {
			x = (int) (x - formattedFont.getWidth() / 2);
		}
		else if (align == AlignHorizontal.RIGHT) {
			x = (int) (x - formattedFont.getWidth());
		}
		g2.setColor(FontColor.getColor());
		g2.drawString(formattedFont.getAttributedCharacterIterator(), (float) x, (float) y);
	}

}
