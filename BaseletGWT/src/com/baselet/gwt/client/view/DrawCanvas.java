package com.baselet.gwt.client.view;

import java.util.List;

import com.baselet.control.SharedConstants;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.GridElement;
import com.baselet.element.Selector;
import com.baselet.elementnew.ElementId;
import com.baselet.gwt.client.element.ComponentGwt;
import com.baselet.gwt.client.element.ElementFactory;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.ui.FocusWidget;

public class DrawCanvas {
	public interface HelptextResources extends ClientBundle {
		HelptextResources INSTANCE = GWT.create(HelptextResources.class);

		@Source("Helptext.txt")
		TextResource helpText();
	}

	private final Canvas canvas = Canvas.createIfSupported();

	public FocusWidget getWidget() {
		return canvas;
	}

	public Context2d getContext2d() {
		return canvas.getContext2d();
	}

	public void clearAndSetSize(int width, int height) {
		// setCoordinateSpace always clears the canvas. To avoid that see https://groups.google.com/d/msg/google-web-toolkit/dpc84mHeKkA/3EKxrlyFCEAJ
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
	}

	public int getWidth() {
		return canvas.getCoordinateSpaceWidth();
	}

	public int getHeight() {
		return canvas.getCoordinateSpaceHeight();
	}

	public CanvasElement getCanvasElement() {
		return canvas.getCanvasElement();
	}

	public String toDataUrl(String type) {
		return canvas.toDataUrl(type);
	}

	void draw(boolean drawEmptyInfo, List<GridElement> gridElements, Selector selector) {
		if (SharedConstants.dev_mode) {
			CanvasUtils.drawGridOn(getContext2d());
		}

		if (drawEmptyInfo && gridElements.isEmpty()) {
			drawEmptyInfoText();
		}
		else {
			// if (tryOptimizedDrawing()) return;
			for (GridElement ge : gridElements) {
				((ComponentGwt) ge.getComponent()).drawOn(canvas.getContext2d(), selector.isSelected(ge));
			}
		}
	}

	private void drawEmptyInfoText() {
		double elWidth = 440;
		double elHeight = 150;
		double elXPos = getWidth() / 2 - elWidth / 2;
		double elYPos = getHeight() / 2 - elHeight;
		GridElement emptyElement = ElementFactory.create(ElementId.Text, new Rectangle(elXPos, elYPos, elWidth, elHeight), HelptextResources.INSTANCE.helpText().getText(), "", null);
		((ComponentGwt) emptyElement.getComponent()).drawOn(canvas.getContext2d(), false);

	}

	// TODO would not work because canvas gets always resized and therefore cleaned -> so everything must be redrawn
	// private boolean tryOptimizedDrawing() {
	// List<GridElement> geToRedraw = new ArrayList<GridElement>();
	// for (GridElement ge : gridElements) {
	// if(((GwtComponent) ge.getComponent()).isRedrawNecessary()) {
	// for (GridElement geRedraw : geToRedraw) {
	// if (geRedraw.getRectangle().intersects(ge.getRectangle())) {
	// return false;
	// }
	// }
	// geToRedraw.add(ge);
	// }
	// }
	//
	// for (GridElement ge : gridElements) {
	// elementCanvas.getContext2d().clearRect(0, 0, ge.getRectangle().getWidth(), ge.getRectangle().getHeight());
	// ((GwtComponent) ge.getComponent()).drawOn(elementCanvas.getContext2d());
	// }
	// return true;
	// }
}
