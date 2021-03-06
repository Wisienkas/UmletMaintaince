package com.baselet.gui;

import static com.baselet.control.MenuConstants.ABOUT_PROGRAM;
import static com.baselet.control.MenuConstants.ALIGN;
import static com.baselet.control.MenuConstants.COPY;
import static com.baselet.control.MenuConstants.CUSTOM_ELEMENTS_TUTORIAL;
import static com.baselet.control.MenuConstants.CUT;
import static com.baselet.control.MenuConstants.DELETE;
import static com.baselet.control.MenuConstants.EDIT_CURRENT_PALETTE;
import static com.baselet.control.MenuConstants.EDIT_SELECTED;
import static com.baselet.control.MenuConstants.EXIT;
import static com.baselet.control.MenuConstants.EXPORT_AS;
import static com.baselet.control.MenuConstants.GENERATE_CLASS;
import static com.baselet.control.MenuConstants.GENERATE_CLASS_OPTIONS;
import static com.baselet.control.MenuConstants.GENERATE_PACKAGE_DIAGRAM;
import static com.baselet.control.MenuConstants.GENERATE_CODE;
import static com.baselet.control.MenuConstants.GROUP;
import static com.baselet.control.MenuConstants.LAYER;
import static com.baselet.control.MenuConstants.LAYER_DOWN;
import static com.baselet.control.MenuConstants.LAYER_UP;
import static com.baselet.control.MenuConstants.MAIL_TO;
import static com.baselet.control.MenuConstants.NEW;
import static com.baselet.control.MenuConstants.NEW_CE;
import static com.baselet.control.MenuConstants.NEW_FROM_TEMPLATE;
import static com.baselet.control.MenuConstants.ONLINE_HELP;
import static com.baselet.control.MenuConstants.ONLINE_SAMPLE_DIAGRAMS;
import static com.baselet.control.MenuConstants.OPEN;
import static com.baselet.control.MenuConstants.OPTIONS;
import static com.baselet.control.MenuConstants.PASTE;
import static com.baselet.control.MenuConstants.PRINT;
import static com.baselet.control.MenuConstants.PROGRAM_HOMEPAGE;
import static com.baselet.control.MenuConstants.RATE_PROGRAM;
import static com.baselet.control.MenuConstants.RECENT_FILES;
import static com.baselet.control.MenuConstants.REDO;
import static com.baselet.control.MenuConstants.RELATE_AROUND;
import static com.baselet.control.MenuConstants.SAVE;
import static com.baselet.control.MenuConstants.SAVE_AS;
import static com.baselet.control.MenuConstants.SELECT_ALL;
import static com.baselet.control.MenuConstants.SET_BACKGROUND_COLOR;
import static com.baselet.control.MenuConstants.SET_FOREGROUND_COLOR;
import static com.baselet.control.MenuConstants.UNDO;
import static com.baselet.control.MenuConstants.UNGROUP;
import static com.baselet.control.MenuConstants.VIDEO_TUTORIAL;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Logger;

import com.baselet.control.Constants;
import com.baselet.control.Constants.Os;
import com.baselet.control.Constants.SystemInfo;
import com.baselet.control.Main;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.command.Relation;
import com.baselet.diagram.draw.helper.ColorOwn;
import com.baselet.element.GridElement;
import com.baselet.gui.menu.ClassMenuItemPointer;
import com.umlet.elementnew.ComponentSwing;

public class MenuFactorySwing extends MenuFactory {

	private static Logger log = Logger.getLogger(MenuFactorySwing.class);

	private static MenuFactorySwing instance = null;

	public static MenuFactorySwing getInstance() {
		if (instance == null) {
			instance = new MenuFactorySwing();
		}
		return instance;
	}

	public JMenuItem createNew() {
		return createJMenuItem(false, NEW, KeyEvent.VK_N, true, null);
	}

	public JMenuItem createOpen() {
		return createJMenuItem(false, OPEN, KeyEvent.VK_O, true, null);
	}

	public JMenu createRecentFiles() {
		final JMenu recentFiles = new JMenu();
		recentFiles.setText(RECENT_FILES);
		recentFiles.addMenuListener(new MenuListener() {
			@Override
			public void menuDeselected(MenuEvent e) {}

			@Override
			public void menuCanceled(MenuEvent e) {}

			@Override
			public void menuSelected(MenuEvent e) {
				recentFiles.removeAll();
				for (String file : Constants.recentlyUsedFilesList) {
					recentFiles.add(createJMenuItem(false, file, RECENT_FILES, file));
				}
			}
		});
		return recentFiles;
	}

	public JMenuItem createGenerate() {
		return createJMenuItem(false, GENERATE_CLASS, null);
	}

	public JMenuItem createGenerateCode() {
		return createJMenuItem(false, GENERATE_CODE, null);
	}

	public JMenuItem createGenerateOptions() {
		return createJMenuItem(false, GENERATE_CLASS_OPTIONS, null);
	}

	// PACKAGE DIAGRAM CHANGE REQUEST
	public JMenuItem createGeneratePackageDiagram() {
		return createJMenuItem(false, GENERATE_PACKAGE_DIAGRAM, null);
	}

	public JMenuItem createSave() {
		return createJMenuItem(true, SAVE, KeyEvent.VK_S, true, null);
	}

	public JMenuItem createSaveAs() {
		return createJMenuItem(true, SAVE_AS, null);
	}

	public JMenu createExportAs() {
		final JMenu export = new JMenu();
		export.setText(EXPORT_AS);
		diagramDependendComponents.add(export);
		for (final String format : Constants.exportFormatList) {
			export.add(createJMenuItem(true, format.toUpperCase() + "...", EXPORT_AS, format));
		}
		return export;
	}

	public JMenuItem createMailTo() {
		return createJMenuItem(true, MAIL_TO, KeyEvent.VK_M, true, null);
	}

	public JMenuItem createEditCurrentPalette() {
		return createJMenuItem(false, EDIT_CURRENT_PALETTE, null);
	}

	public JMenuItem createOptions() {
		return createJMenuItem(false, OPTIONS, null);
	}

	public JMenuItem createPrint() {
		return createJMenuItem(true, PRINT, KeyEvent.VK_P, true, null);
	}

	public JMenuItem createExit() {
		return createJMenuItem(false, EXIT, null);
	}

	public JMenuItem createUndo() {
		return createJMenuItem(false, UNDO, KeyEvent.VK_Z, true, null);
	}

	public JMenuItem createRedo() {
		return createJMenuItem(false, REDO, KeyEvent.VK_Y, true, null);
	}

	public JMenuItem createDelete() {
		int[] keys = new int[] { KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE }; // backspace AND delete both work for deleting elements
		if (SystemInfo.OS == Os.MAC) { // MacOS shows the backspace key mapping because it's the only one working - see http://stackoverflow.com/questions/4881262/java-keystroke-for-delete/4881606#4881606
			return createJMenuItem(false, DELETE, keys, KeyEvent.VK_BACK_SPACE);
		}
		else {
			return createJMenuItem(false, DELETE, keys, KeyEvent.VK_DELETE);
		}
	}

	public JMenuItem createSelectAll() {
		return createJMenuItem(false, SELECT_ALL, KeyEvent.VK_A, true, null);
	}

	public JMenuItem createGroup() {
		return createJMenuItem(false, GROUP, KeyEvent.VK_G, true, null);
	}

	public JMenuItem createUngroup() {
		return createJMenuItem(false, UNGROUP, KeyEvent.VK_U, true, null);
	}

	public JMenuItem createCut() {
		return createJMenuItem(false, CUT, KeyEvent.VK_X, true, null);
	}

	public JMenuItem createCopy() {
		return createJMenuItem(false, COPY, KeyEvent.VK_C, true, null);
	}

	public JMenuItem createPaste() {
		return createJMenuItem(false, PASTE, KeyEvent.VK_V, true, null);
	}

	public JMenuItem createNewCustomElement() {
		return createJMenuItem(false, NEW_CE, null);
	}

	public JMenu createNewCustomElementFromTemplate() {
		JMenu menu = new JMenu(NEW_FROM_TEMPLATE);
		for (String template : Main.getInstance().getTemplateNames()) {
			menu.add(createJMenuItem(false, template, NEW_FROM_TEMPLATE, template));
		}
		return menu;
	}

	public JMenuItem createEditSelected() {
		return createJMenuItem(false, EDIT_SELECTED, null);
	}

	public JMenuItem createCustomElementTutorial() {
		return createJMenuItem(false, CUSTOM_ELEMENTS_TUTORIAL, null);
	}

	public JMenuItem createOnlineHelp() {
		return createJMenuItem(false, ONLINE_HELP, null);
	}

	public JMenuItem createOnlineSampleDiagrams() {
		return createJMenuItem(false, ONLINE_SAMPLE_DIAGRAMS, null);
	}

	public JMenuItem createVideoTutorials() {
		return createJMenuItem(false, VIDEO_TUTORIAL, null);
	}

	public JMenuItem createProgramHomepage() {
		return createJMenuItem(false, PROGRAM_HOMEPAGE, null);
	}

	public JMenuItem createRateProgram() {
		return createJMenuItem(false, RATE_PROGRAM, null);
	}

	public JMenu createSetColor(boolean fg) {
		String name = fg ? SET_FOREGROUND_COLOR : SET_BACKGROUND_COLOR;
		JMenu menu = new JMenu(name);
		menu.add(createJMenuItem(false, "default", name, null));
		for (String color : ColorOwn.COLOR_MAP.keySet()) {
			JMenuItem item = createJMenuItem(false, color, name, color);
			menu.add(item);
			item.setIcon(new PlainColorIcon(color));
		}
		return menu;
	}

	public JMenuItem createAboutProgram() {
		return createJMenuItem(false, ABOUT_PROGRAM, null);
	}

	public JMenu createAlign() {
		JMenu alignMenu = new JMenu(ALIGN);
		for (String direction : new String[] { "Left", "Right", "Top", "Bottom" }) {
			alignMenu.add(createJMenuItem(false, direction, ALIGN, direction));
		}
		return alignMenu;
	}

	public JMenu createLayerUp() {
		JMenu alignMenu = new JMenu(LAYER);
		for (String direction : new String[] { LAYER_DOWN, LAYER_UP }) {
			alignMenu.add(createJMenuItem(false, direction, LAYER, direction));
		}
		return alignMenu;
	}

	public JMenu createRelateAround(GridElement el, DiagramHandler handler) {

		Set<ComponentSwing> relevant = new HashSet<ComponentSwing>();
		
		for (Component c : handler.getDrawPanel().getComponents()) {
			try {
				if (!(c instanceof com.baselet.gui.StartUpHelpText)) {
					ComponentSwing cs = (ComponentSwing) c; // Casting because ComponentSwing extends JComponent which extends Container which extends Component

					if (cs != el.getComponent() && cs.getBoundsRect().contains(el.getRectangle())) {
						relevant.add(cs);
					}
				}
			} catch (Exception ex) {
				log.error(ex);
			}
		}

		JMenu menu = new JMenu(RELATE_AROUND);
		for (ComponentSwing cs : relevant) {
			menu.add(new ClassMenuItemPointer(cs.getGridElement(), el, handler));
		}

		return menu;
	}
	
	public JMenuItem createRemoveParent(GridElement child, DiagramHandler handler) {
		JMenuItem item = new JMenu("Remove parent");
		if(handler.getRelationManager().hasParent(child)) {
			item.addActionListener(e -> {
				handler.getController().executeCommand(new Relation(Optional.empty(), child));
			});
		} else {
			item.setEnabled(false);
		}
		
		return item;
	}

	private JMenuItem createJMenuItem(boolean grayWithoutDiagram, final String name, Object param) {
		return createJMenuItem(grayWithoutDiagram, name, name, null, null, param);
	}

	private JMenuItem createJMenuItem(boolean grayWithoutDiagram, final String name, Integer mnemonic, Boolean meta, Object param) {
		return createJMenuItem(grayWithoutDiagram, name, name, mnemonic, meta, param);
	}

	private JMenuItem createJMenuItem(boolean grayWithoutDiagram, final String menuName, final String actionName, final Object param) {
		return createJMenuItem(grayWithoutDiagram, menuName, actionName, null, null, param);
	}

	private JMenuItem createJMenuItem(boolean grayWithoutDiagram, final String menuName, final String actionName, Integer mnemonic, Boolean meta, final Object param) {
		JMenuItem menuItem = new JMenuItem(menuName);
		if (mnemonic != null) {
			menuItem.setMnemonic(mnemonic);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(mnemonic, !meta ? 0 : SystemInfo.META_KEY.getMask()));
		}
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAction(actionName, param);
			}
		});
		if (grayWithoutDiagram) {
			diagramDependendComponents.add(menuItem);
		}
		return menuItem;
	}

	/**
	 * Create a JMenuItem with multiple key bindings (only one mnemonic can be set at any time).
	 * @see "http://docs.oracle.com/javase/tutorial/uiswing/misc/action.html"
	 */
	private JMenuItem createJMenuItem(boolean grayWithoutDiagram, final String name, int[] keyEvents, int preferredMnemonic) {
		JMenuItem menuItem = new JMenuItem(name);

		MultipleKeyBindingsAction action = new MultipleKeyBindingsAction(name, preferredMnemonic);
		for (int keyEvent : keyEvents) {
			addKeyBinding(menuItem, keyEvent, name);
		}
		menuItem.getActionMap().put(name, action);
		menuItem.setAction(action);

		if (grayWithoutDiagram) {
			diagramDependendComponents.add(menuItem);
		}
		return menuItem;
	}

	private void addKeyBinding(JMenuItem menuItem, int keyEvent, String actionName) {
		menuItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyEvent, 0), actionName);
	}

	@SuppressWarnings("serial")
	private class MultipleKeyBindingsAction extends AbstractAction {

		public MultipleKeyBindingsAction(String menuName, int preferredMnemonic) {
			super(menuName);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(preferredMnemonic, 0));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			doAction(getValue(NAME).toString(), null);
		}
	}
}
