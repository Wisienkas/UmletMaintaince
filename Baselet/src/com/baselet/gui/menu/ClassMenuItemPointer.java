package com.baselet.gui.menu;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.diagram.draw.helper.ColorOwn;
import com.umlet.elementnew.ComponentSwing;

public class ClassMenuItemPointer extends JMenuItem {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ClassMenuItemPointer.class);

	private final ComponentSwing comp;

	private final ColorOwn color;

	public ClassMenuItemPointer(final ComponentSwing cs) {
		super(cs.getClass().getSimpleName());
		comp = cs;
		color = cs.getDrawHandler().getStyle().getBackgroundColor();
		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {
				log.info("exited");
				cs.getDrawHandler().setBackgroundColor(color);
				cs.getDrawHandler().drawAll();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				log.info("Entered");
				cs.getDrawHandler().setBackgroundColor(ColorOwn.BLUE);
				cs.getDrawHandler().drawAll();
			}

			@Override
			public void mouseClicked(MouseEvent e) {}
		});
	}

	public Rectangle getRect() {
		return comp.getBoundsRect();
	}

}
