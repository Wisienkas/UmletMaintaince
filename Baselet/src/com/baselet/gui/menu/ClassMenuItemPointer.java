package com.baselet.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.command.ChangeElementSetting;
import com.baselet.diagram.command.Command;
import com.baselet.diagram.command.Relation;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.diagram.draw.helper.ColorOwn;
import com.baselet.element.GridElement;
import com.umlet.elementnew.ComponentSwing;

public class ClassMenuItemPointer extends JMenuItem {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ClassMenuItemPointer.class);

	private GridElement parent;

	public ClassMenuItemPointer(GridElement parent, GridElement child, DiagramHandler handler) {
		super(parent.getId().toString());
		this.parent = parent;
		
		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {
				handler.getController().undo();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				handler.getController().executeCommand(getColorSetting("blue", parent));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(
					() -> {
						// Reset indication color
						handler.getController().undo();
						handler.getController()
						.executeCommand(new Relation(Optional.ofNullable(parent), child));						
					}
				);
			}
		});
	}

	private Command getColorSetting(String color, GridElement element) {
		
		Map<GridElement, String> valueMap = new HashMap<>();
		valueMap.put(element, color);
		return new ChangeElementSetting("bg", valueMap);
	}
	
	public Rectangle getRect() {
		return parent.getComponent().getBoundsRect();
	}

}
