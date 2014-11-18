package com.baselet.diagram;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.baselet.control.Constants;
import com.baselet.control.ErrorMessages;
import com.baselet.control.Main;
import com.baselet.control.Notifier;
import com.baselet.control.RelateManager;
import com.baselet.control.SharedConstants.Program;
import com.baselet.control.SharedUtils;
import com.baselet.diagram.draw.geom.Point;
import com.baselet.diagram.draw.swing.Converter;
import com.baselet.diagram.draw.swing.DrawHandlerSwing;
import com.baselet.diagram.io.DiagramFileHandler;
import com.baselet.element.GridElement;
import com.baselet.elementnew.NewGridElement;
import com.baselet.gui.BaseGUI;
import com.baselet.gui.DiagramPopupMenu;
import com.baselet.gui.listener.DiagramListener;
import com.baselet.gui.listener.GridElementListener;
import com.baselet.gui.listener.OldRelationListener;
import com.baselet.gui.standalone.StandaloneGUI;
import com.umlet.element.Relation;
import com.umlet.element.SequenceDiagram;

public class DiagramHandler {

	private static final Logger log = Logger.getLogger(DiagramHandler.class);

	private boolean isChanged;
	private final DiagramFileHandler fileHandler;
	private final FontHandler fontHandler;

	protected DrawPanel drawpanel;
	private final Controller controller;
	protected DiagramListener listener;
	private RelateManager relationKeeper;
	
	private String helptext;
	private boolean enabled;
	private int gridSize;

	private OldRelationListener relationListener;
	private GridElementListener gridElementListener;

	public DiagramHandler(File diagram) {
		this(diagram, false);
	}
	
	public RelateManager getRelationManager() {
		return relationKeeper;
	}

	protected DiagramHandler(File diagram, boolean nolistener) {
		gridSize = Constants.DEFAULTGRIDSIZE;
		isChanged = false;
		enabled = true;
		drawpanel = new DrawPanel(this);
		controller = new Controller(this);
		fontHandler = new FontHandler(this);
		fileHandler = DiagramFileHandler.createInstance(this, diagram);
		if (!nolistener) {
			setListener(new DiagramListener(this));
		}
		if (diagram != null) {
			fileHandler.doOpen();
		}

		boolean extendedPopupMenu = false;
		BaseGUI gui = Main.getInstance().getGUI();
		if (gui != null) {
			gui.setValueOfZoomDisplay(getGridSize());
			if (gui instanceof StandaloneGUI)
			{
				extendedPopupMenu = true; // AB: use extended popup menu on standalone gui only
			}
		}

		if (!(this instanceof PaletteHandler)) {
			drawpanel.setComponentPopupMenu(new DiagramPopupMenu(extendedPopupMenu));
		}
		relationKeeper = new RelateManager(drawpanel.getGridElements());
	}

	public void setEnabled(boolean en) {
		if (!en && enabled) {
			drawpanel.removeMouseListener(listener);
			drawpanel.removeMouseMotionListener(listener);
			enabled = false;
		}
		else if (en && !enabled) {
			drawpanel.addMouseListener(listener);
			drawpanel.addMouseMotionListener(listener);
			enabled = true;
		}
	}

	protected void setListener(DiagramListener listener) {
		this.listener = listener;
		drawpanel.addMouseListener(this.listener);
		drawpanel.addMouseMotionListener(this.listener);
		drawpanel.addMouseWheelListener(this.listener);
	}

	public DiagramListener getListener() {
		return listener;
	}

	public void setChanged(boolean changed) {
		if (isChanged != changed) {
			isChanged = changed;
			BaseGUI gui = Main.getInstance().getGUI();
			if (gui != null) {
				gui.setDiagramChanged(this, changed);
			}
		}
	}

	public DrawPanel getDrawPanel() {
		return drawpanel;
	}

	public DiagramFileHandler getFileHandler() {
		return fileHandler;
	}

	public FontHandler getFontHandler() {
		return fontHandler;
	}

	public Controller getController() {
		return controller;
	}

	// returnvalue needed for eclipse plugin
	// returns true if the file is saved, else returns false
	public boolean doSave() {
		try {
			fileHandler.doSave();
			reloadPalettes();
			Main.getInstance().getGUI().afterSaving();
			return true;
		} catch (IOException e) {
			log.error(e);
			Main.displayError(ErrorMessages.ERROR_SAVING_FILE + e.getMessage());
			return false;
		}
	}

	public void doSaveAs(String extension) {
		if (drawpanel.getGridElements().isEmpty()) {
			Main.displayError(ErrorMessages.ERROR_SAVING_EMPTY_DIAGRAM);
		}
		else {
			try {
				fileHandler.doSaveAs(extension);
				reloadPalettes();
				Main.getInstance().getGUI().afterSaving();
			} catch (IOException e) {
				log.error(e);
				Main.displayError(ErrorMessages.ERROR_SAVING_FILE + e.getMessage());
			}
		}
	}

	public void doPrint() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(getDrawPanel());
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (PrinterException pe) {
				Main.displayError(ErrorMessages.ERROR_PRINTING);
			}
		}
	}

	// reloads the diagram from file + updates gui
	public void reload() {
		drawpanel.removeAll();
		fileHandler.doOpen();
		drawpanel.updatePanelAndScrollbars();
	}

	// reloads palettes if the palette has been changed.
	private void reloadPalettes() {
		for (DiagramHandler d : Main.getInstance().getPalettes().values()) {
			if (d.getFileHandler().equals(getFileHandler()) && !d.equals(this)) {
				d.reload();
			}
		}
		getDrawPanel().getSelector().updateSelectorInformation(); // Must be updated to remain in the current Property Panel
	}

	public void doClose() {
		if (askSaveIfDirty()) {
			Main.getInstance().getDiagrams().remove(this);
			Main.getInstance().getGUI().close(this);
			drawpanel.getSelector().deselectAll();

			// update property panel to now selected diagram (or to empty if no diagram exists)
			DiagramHandler newhandler = Main.getInstance().getDiagramHandler(); //
			if (newhandler != null) {
				newhandler.getDrawPanel().getSelector().updateSelectorInformation();
			}
			else {
				Main.getInstance().setPropertyPanelToGridElement(null);
			}
		}
	}

	public String getName() {
		String name = fileHandler.getFileName();
		if (name.contains(".")) {
			name = name.substring(0, name.lastIndexOf("."));
		}
		return name;
	}

	public String getFullPathName() {
		return fileHandler.getFullPathName();
	}

	public GridElementListener getEntityListener(GridElement e) {
		if (e instanceof Relation) {
			if (relationListener == null) {
				relationListener = new OldRelationListener(this);
			}
			return relationListener;
		}
		else {
			if (gridElementListener == null) {
				gridElementListener = new GridElementListener(this);
			}
			return gridElementListener;
		}
	}

	public boolean askSaveIfDirty() {
		if (isChanged) {
			int ch = JOptionPane.showOptionDialog(Main.getInstance().getGUI().getMainFrame(), "Save changes?", Program.NAME + " - " + getName(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
			if (ch == JOptionPane.YES_OPTION) {
				doSave();
				return true;
			}
			else if (ch == JOptionPane.NO_OPTION) {
				return true;
			}
			return false;
		}
		return true;
	}

	public void setHelpText(String helptext) {
		this.helptext = helptext;
		BaseGUI gui = Main.getInstance().getGUI();
		if (gui != null && gui.getPropertyPane() != null) {
			gui.getPropertyPane().switchToNonElement(this.helptext);
		}
	}

	public String getHelpText() {
		if (helptext == null) {
			return Constants.getDefaultHelptext();
		}
		else {
			return helptext;
		}
	}

	public boolean isChanged() {
		return isChanged;
	}

	public int getGridSize() {
		return gridSize;
	}

	public float getZoomFactor() {
		return (float) getGridSize() / (float) Constants.DEFAULTGRIDSIZE;
	}

	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	public int realignToGrid(double val) {
		return realignToGrid(true, val, false);
	}

	public int realignToGrid(boolean logRealign, double val) {
		return realignToGrid(logRealign, val, false);
	}

	public int realignToGrid(boolean logRealign, double val, boolean roundUp) {
		return SharedUtils.realignTo(logRealign, val, roundUp, gridSize);
	}

	public static int realignTo(int val, int toVal) {
		return SharedUtils.realignTo(false, val, false, toVal);
	}

	public static void zoomEntity(int fromFactor, int toFactor, GridElement e) {
		Vector<GridElement> vec = new Vector<GridElement>();
		vec.add(e);
		zoomEntities(fromFactor, toFactor, vec);
	}

	public static void zoomEntities(int fromFactor, int toFactor, List<GridElement> selectedEntities) {

		/**
		 * The entities must be resized to the new factor
		 */

		for (GridElement entity : selectedEntities) {
			int newX = entity.getRectangle().x * toFactor / fromFactor;
			int newY = entity.getRectangle().y * toFactor / fromFactor;
			int newW = entity.getRectangle().width * toFactor / fromFactor;
			int newH = entity.getRectangle().height * toFactor / fromFactor;
			entity.setLocation(realignTo(newX, toFactor), realignTo(newY, toFactor));
			// Normally there should be no realign here but relations and custom elements sometimes must be realigned therefore we don't log it as an error
			entity.setSize(realignTo(newW, toFactor), realignTo(newH, toFactor));

			// Resize the coordinates of the points of the relations
			if (entity instanceof Relation) {
				for (Point point : ((Relation) entity).getLinePoints()) {
					newX = point.getX() * toFactor / fromFactor;
					newY = point.getY() * toFactor / fromFactor;
					point.setX(realignTo(newX, toFactor));
					point.setY(realignTo(newY, toFactor));
				}
			}
		}
	}

	public void setGridAndZoom(int factor) {
		setGridAndZoom(factor, true);
	}

	public void setGridAndZoom(int factor, boolean manualZoom) {

		/**
		 * Store the old gridsize and the new one. Furthermore check if the zoom process must be made
		 */

		int oldGridSize = getGridSize();

		if (factor < 1 || factor > 20)
		{
			return; // Only zoom between 10% and 200% is allowed
		}
		if (factor == oldGridSize)
		{
			return; // Only zoom if gridsize has changed
		}

		setGridSize(factor);

		/**
		 * Zoom entities to the new gridsize
		 */

		zoomEntities(oldGridSize, gridSize, getDrawPanel().getGridElements());

		// AB: Zoom origin
		getDrawPanel().zoomOrigin(oldGridSize, gridSize);

		/**
		 * The zoomed diagram will shrink to the upper left corner and grow to the lower right
		 * corner but we want to have the zoom center in the middle of the actual visible drawpanel
		 * so we have to change the coordinates of the entities again
		 */

		if (manualZoom) {
			// calculate mouse position relative to UMLet scrollpane
			Point mouseLocation = Converter.convert(MouseInfo.getPointerInfo().getLocation());
			Point viewportLocation = Converter.convert(getDrawPanel().getScrollPane().getViewport().getLocationOnScreen());
			float x = mouseLocation.x - viewportLocation.x;
			float y = mouseLocation.y - viewportLocation.y;

			// And add any space on the upper left corner which is not visible but reachable by scrollbar
			x += getDrawPanel().getScrollPane().getViewport().getViewPosition().getX();
			y += getDrawPanel().getScrollPane().getViewport().getViewPosition().getY();

			// The result is the point where we want to center the zoom of the diagram
			float diffx, diffy;
			diffx = x - x * gridSize / oldGridSize;
			diffy = y - y * gridSize / oldGridSize;

			// AB: Move origin in opposite direction
			log.debug("diffX/diffY: " + diffx + "/" + diffy);
			log.debug("Manual Zoom Delta: " + realignToGrid(false, diffx) + "/" + realignToGrid(false, diffy));
			getDrawPanel().moveOrigin(realignToGrid(false, -diffx), realignToGrid(false, -diffy));

			for (GridElement e : getDrawPanel().getGridElements()) {
				e.setLocationDifference(realignToGrid(false, diffx), realignToGrid(false, diffy));
			}

			/**
			 * Now we have to do some additional "clean up" stuff which is related to the zoom
			 */

			getDrawPanel().updatePanelAndScrollbars();

			// Set changed only if diagram is not empty (otherwise no element has been changed)
			if (!drawpanel.getGridElements().isEmpty()) {
				setChanged(true);
			}

			BaseGUI gui = Main.getInstance().getGUI();
			if (gui != null) {
				gui.setValueOfZoomDisplay(factor);
			}

			float zoomFactor = Main.getInstance().getDiagramHandler().getZoomFactor() * 100;
			String zoomtext;
			if (Main.getInstance().getDiagramHandler() instanceof PaletteHandler) {
				zoomtext = "Palette zoomed to " + Integer.toString((int) zoomFactor) + "%";
			}
			else {
				zoomtext = "Diagram zoomed to " + Integer.toString((int) zoomFactor) + "%";
			}
			Notifier.getInstance().showNotification(zoomtext);
		}
	}

	public void setHandlerAndInitListeners(GridElement element) {
		if (Main.getHandlerForElement(element) != null) {
			((Component) element.getComponent()).removeMouseListener(Main.getHandlerForElement(element).getEntityListener(element));
			((Component) element.getComponent()).removeMouseMotionListener(Main.getHandlerForElement(element).getEntityListener(element));
		}
		Main.setHandlerForElement(element, this);
		((Component) element.getComponent()).addMouseListener(Main.getHandlerForElement(element).getEntityListener(element));
		((Component) element.getComponent()).addMouseMotionListener(Main.getHandlerForElement(element).getEntityListener(element));
		if (element instanceof NewGridElement) {
			((DrawHandlerSwing) ((NewGridElement) element).getDrawer()).setHandler(this);
			((DrawHandlerSwing) ((NewGridElement) element).getMetaDrawer()).setHandler(this);
		}
		if (element instanceof SequenceDiagram) {
			((SequenceDiagram) element).zoomValues();
		}
		element.updateModelFromText(); // must be updated here because the new handler could have a different zoom level
	}
}
