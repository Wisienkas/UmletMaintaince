package com.baselet.gwt.client.element;

import org.apache.log4j.Logger;

import com.baselet.control.StringStyle;
import com.baselet.control.enumerations.AlignHorizontal;
import com.baselet.control.enumerations.FormatLabels;
import com.baselet.control.enumerations.LineType;
import com.baselet.diagram.draw.DrawFunction;
import com.baselet.diagram.draw.DrawHandler;
import com.baselet.diagram.draw.geom.DimensionDouble;
import com.baselet.diagram.draw.geom.PointDouble;
import com.baselet.diagram.draw.helper.ColorOwn;
import com.baselet.diagram.draw.helper.Style;
import com.baselet.gwt.client.Converter;
import com.baselet.gwt.client.Notification;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;

public class DrawHandlerGwt extends DrawHandler {

	private static final Logger log = Logger.getLogger(DrawHandlerGwt.class);

	private final Canvas canvas;
	private final Context2d ctx;

	public DrawHandlerGwt(Canvas canvas) {
		this.canvas = canvas;
		ctx = canvas.getContext2d();
	}

	@Override
	protected DimensionDouble textDimensionHelper(String string) {
		StringStyle stringStyle = StringStyle.analyzeFormatLabels(string);
		ctxSetFont(style.getFontSize(), stringStyle);
		DimensionDouble dim = new DimensionDouble(ctx.measureText(stringStyle.getStringWithoutMarkup()).getWidth(), style.getFontSize()); // unfortunately a html canvas offers no method to get the exakt height, therefore just use the fontsize
		return dim;
	}

	@Override
	protected double getDefaultFontSize() {
		return 12;
	}

	@Override
	public PseudoDrawHandlerGwt getPseudoDrawHandler() {
		PseudoDrawHandlerGwt pseudo = new PseudoDrawHandlerGwt(canvas);
		pseudo.setStyle(style.cloneFromMe()); // set style to make sure fontsize (and therefore calls like this.textHeight()) work as intended
		return pseudo;
	}

	@Override
	public void drawArc(final double x, final double y, final double width, final double height, final double start, final double extent, final boolean open) {
		final Style styleAtDrawingCall = style.cloneFromMe();
		addDrawable(new DrawFunction() {
			@Override
			public void run() {
				setStyle(ctx, styleAtDrawingCall);

				double centerX = (int) (x + width / 2) + HALF_PX;
				double centerY = (int) (y + height / 2) + HALF_PX;
				if (open) { // if arc should be open, move before the path begins
					ctx.moveTo(centerX, centerY);
					ctx.beginPath();
				}
				else { // otherwise the move is part of the path
					ctx.beginPath();
					ctx.moveTo(centerX, centerY);
				}
				ctx.arc(centerX, centerY, width / 2, -Math.toRadians(start), -Math.toRadians(start + extent), true);
				if (!open) { // close path only if arc is not open
					ctx.closePath();
				}
				ctx.fill();
				ctx.stroke();
			}
		});
	}

	@Override
	public void drawCircle(final double x, final double y, final double radius) {
		final Style styleAtDrawingCall = style.cloneFromMe();
		addDrawable(new DrawFunction() {
			@Override
			public void run() {
				setStyle(ctx, styleAtDrawingCall);
				ctx.beginPath();
				ctx.arc((int) x + HALF_PX, (int) y + HALF_PX, radius, 0, 2 * Math.PI);
				ctx.fill();
				ctx.stroke();
			}
		});
	}

	@Override
	public void drawEllipse(final double x, final double y, final double width, final double height) {
		final Style styleAtDrawingCall = style.cloneFromMe();
		addDrawable(new DrawFunction() {
			@Override
			public void run() {
				setStyle(ctx, styleAtDrawingCall);
				drawEllipseHelper(ctx, (int) x + HALF_PX, (int) y + HALF_PX, width, height);
			}
		});
	}

	@Override
	public void drawLines(final PointDouble... points) {
		if (points.length > 1) {
			final Style styleAtDrawingCall = style.cloneFromMe();
			addDrawable(new DrawFunction() {
				@Override
				public void run() {
					setStyle(ctx, styleAtDrawingCall);
					drawLineHelper(points);
				}
			});
		}
	}

	@Override
	public void drawRectangle(final double x, final double y, final double width, final double height) {
		final Style styleAtDrawingCall = style.cloneFromMe();
		addDrawable(new DrawFunction() {
			@Override
			public void run() {
				setStyle(ctx, styleAtDrawingCall);
				// int cast on x/y + HALF_PX and int cast on width/height is important to make sure it never draws between pixels
				ctx.fillRect((int) x + HALF_PX, (int) y + HALF_PX, (int) width, (int) height);
				ctx.beginPath();
				ctx.rect((int) x + HALF_PX, (int) y + HALF_PX, (int) width, (int) height);
				ctx.stroke();
			}
		});
	}

	@Override
	public void drawRectangleRound(final double x, final double y, final double width, final double height, final double radius) {
		final Style styleAtDrawingCall = style.cloneFromMe();
		addDrawable(new DrawFunction() {
			@Override
			public void run() {
				setStyle(ctx, styleAtDrawingCall);
				drawRoundRectHelper(ctx, (int) x + HALF_PX, (int) y + HALF_PX, (int) width, (int) height, radius);
			}
		});
	}

	@Override
	public void printHelper(final String text, final PointDouble point, final AlignHorizontal align) {
		final Style styleAtDrawingCall = style.cloneFromMe();
		addDrawable(new DrawFunction() {
			@Override
			public void run() {
				PointDouble pToDraw = point;
				ColorOwn fgColor = getOverlay().getForegroundColor() != null ? getOverlay().getForegroundColor() : styleAtDrawingCall.getForegroundColor();
				ctx.setFillStyle(Converter.convert(fgColor));
				for (String line : text.split("\n")) {
					drawTextHelper(line, pToDraw, align, styleAtDrawingCall.getFontSize());
					pToDraw = new PointDouble(pToDraw.getX(), pToDraw.getY() + textHeightMax());
				}
			}
		});
	}

	private void drawTextHelper(final String text, PointDouble p, AlignHorizontal align, double fontSize) {
		StringStyle stringStyle = StringStyle.analyzeFormatLabels(text);

		ctxSetFont(fontSize, stringStyle);

		String textToDraw = stringStyle.getStringWithoutMarkup();
		if (textToDraw == null || textToDraw.isEmpty()) {
			return; // if nothing should be drawn return (some browsers like Opera have problems with ctx.fillText calls on empty strings)
		}

		if (stringStyle.getFormat().contains(FormatLabels.UNDERLINE)) {
			ctx.setLineWidth(1.0f);
			setLineDash(ctx, LineType.SOLID, 1.0f);
			double textWidth = textWidth(textToDraw);
			int vDist = 1;
			switch (align) {
				case LEFT:
					drawLineHelper(new PointDouble(p.x, p.y + vDist), new PointDouble(p.x + textWidth, p.y + vDist));
					break;
				case CENTER:
					drawLineHelper(new PointDouble(p.x - textWidth / 2, p.y + vDist), new PointDouble(p.x + textWidth / 2, p.y + vDist));
					break;
				case RIGHT:
					drawLineHelper(new PointDouble(p.x - textWidth, p.y + vDist), new PointDouble(p.x, p.y + vDist));
					break;
			}
		}

		ctxSetTextAlign(align);
		ctx.fillText(textToDraw, p.x, p.y);
	}

	private void ctxSetFont(double fontSize, StringStyle stringStyle) {
		String htmlStyle = "";
		if (stringStyle.getFormat().contains(FormatLabels.BOLD)) {
			htmlStyle += " bold";
		}
		if (stringStyle.getFormat().contains(FormatLabels.ITALIC)) {
			htmlStyle += " italic";
		}
		ctx.setFont(htmlStyle + " " + fontSize + "px sans-serif");
	}

	private void ctxSetTextAlign(AlignHorizontal align) {
		TextAlign ctxAlign = null;
		switch (align) {
			case LEFT:
				ctxAlign = TextAlign.LEFT;
				break;
			case CENTER:
				ctxAlign = TextAlign.CENTER;
				break;
			case RIGHT:
				ctxAlign = TextAlign.RIGHT;
				break;
		}
		ctx.setTextAlign(ctxAlign);
	}

	/**
	 * based on http://stackoverflow.com/questions/2172798/how-to-draw-an-oval-in-html5-canvas/2173084#2173084
	 */
	private static void drawEllipseHelper(Context2d ctx, double x, double y, double w, double h) {
		double kappa = .5522848f;
		double ox = w / 2 * kappa; // control point offset horizontal
		double oy = h / 2 * kappa; // control point offset vertical
		double xe = x + w; // x-end
		double ye = y + h; // y-end
		double xm = x + w / 2; // x-middle
		double ym = y + h / 2; // y-middle

		ctx.beginPath();
		ctx.moveTo(x, ym);
		ctx.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y);
		ctx.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym);
		ctx.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye);
		ctx.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym);
		ctx.fill();
		ctx.stroke();
	}

	/**
	 * based on http://js-bits.blogspot.co.at/2010/07/canvas-rounded-corner-rectangles.html
	 */
	private static void drawRoundRectHelper(Context2d ctx, final double x, final double y, final double width, final double height, final double radius) {
		ctx.beginPath();
		ctx.moveTo(x + radius, y);
		ctx.lineTo(x + width - radius, y);
		ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
		ctx.lineTo(x + width, y + height - radius);
		ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
		ctx.lineTo(x + radius, y + height);
		ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
		ctx.lineTo(x, y + radius);
		ctx.quadraticCurveTo(x, y, x + radius, y);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();
	}

	private void drawLineHelper(PointDouble... points) {
		ctx.beginPath();
		boolean first = true;
		for (PointDouble point : points) {
			if (first) {
				ctx.moveTo(point.x.intValue() + HALF_PX, point.y.intValue() + HALF_PX); // +0.5 because a line of thickness 1.0 spans 50% left and 50% right (therefore it would not be on the 1 pixel - see https://developer.mozilla.org/en-US/docs/HTML/Canvas/Tutorial/Applying_styles_and_colors)
				first = false;
			}
			ctx.lineTo(point.x.intValue() + HALF_PX, point.y.intValue() + HALF_PX);
		}
		if (points[0].equals(points[points.length - 1])) {
			ctx.fill(); // only fill if first point == lastpoint
		}
		ctx.stroke();
	}

	private void setStyle(Context2d ctx, Style style) {
		if (style.getBackgroundColor() != null) {
			ctx.setFillStyle(Converter.convert(style.getBackgroundColor()));
		}
		ColorOwn fgColor = getOverlay().getForegroundColor() != null ? getOverlay().getForegroundColor() : style.getForegroundColor();
		if (fgColor != null) {
			ctx.setStrokeStyle(Converter.convert(fgColor));
		}
		ctx.setLineWidth(style.getLineWidth());
		setLineDash(ctx, style.getLineType(), style.getLineWidth());
	}

	private void setLineDash(Context2d ctx, LineType lineType, double lineThickness) {
		try {
			switch (lineType) {
				case DASHED: // large linethickness values need longer dashes
					setLineDash(ctx, 6 * Math.max(1, lineThickness / 2));
					break;
				case DOTTED: // minimum must be 2, otherwise the dotting is not really visible
					setLineDash(ctx, Math.max(2, lineThickness));
					break;
				default: // default is a solid line
					setLineDash(ctx, 0);
			}
		} catch (Exception e) {
			log.debug("No browser support for dashed lines", e);
			Notification.showFeatureNotSupported("Dashed and dotted lines are shown as solid lines<br/>To correctly display them, please use Firefox or Chrome", true);
		}
	}

	/**
	 * Chrome supports setLineDash()
	 * Firefox supports mozDash()
	 */
	public final native void setLineDash(Context2d ctx, double dash) /*-{
		if (ctx.setLineDash !== undefined) {
			ctx.setLineDash([ dash ]);
		} else if (ctx.mozDash !== undefined) {
			if (dash != 0) {
				ctx.mozDash = [ dash ];
			} else { // default is null
				ctx.mozDash = null;
			}
		} else if (dash != 0) { // if another line than a solid one should be set and the browser doesn't support it throw an Exception
			throw new Exception();
		}
	}-*/;

}
