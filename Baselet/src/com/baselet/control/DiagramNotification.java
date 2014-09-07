package com.baselet.control;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;

import javax.swing.JComponent;

import com.baselet.diagram.FormattedFont;
import com.baselet.diagram.draw.geom.DimensionDouble;
import com.baselet.diagram.draw.geom.Rectangle;

public class DiagramNotification extends JComponent {

	private static final long serialVersionUID = 1L;
	private final String message;
	private final Rectangle drawPanelSize;

	private static final Font notificationFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
	private static final FontRenderContext frc = new FontRenderContext(null, true, true);

	public DiagramNotification(Rectangle drawPanelSize, String message) {
		this.message = message;
		this.drawPanelSize = drawPanelSize;
		this.setSize(100, 20);
		adaptDimensions();
	}

	@Override
	public final void paint(Graphics g) {

		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		Composite old = g2.getComposite(); // Store non-transparent composite

		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f)); // 40% transparency
		g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		Font drawFont = g2.getFont();
		g2.setFont(notificationFont);
		adaptDimensions();

		int textX = 5;
		int textY = getHeight() / 2 + 3;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); // 70% transparency
		g2.drawString(message, textX, textY);

		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f)); // 5% transparency
		g2.setColor(java.awt.Color.blue);
		g2.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		g2.setComposite(old);
		g2.setFont(drawFont);
	}

	private void adaptDimensions() {
		DimensionDouble textSize = Utils.getTextSize(new FormattedFont(message, notificationFont.getSize(), notificationFont, frc));
		int x = (int) (drawPanelSize.getX2() - textSize.getWidth() - 20);
		int y = drawPanelSize.getY() + 10;
		this.setLocation(x, y);
		this.setSize((int) textSize.getWidth() + 10, (int) textSize.getHeight() + 10);
	}
}
