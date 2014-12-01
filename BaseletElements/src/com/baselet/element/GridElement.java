package com.baselet.element;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.baselet.control.enumerations.AlignHorizontal;
import com.baselet.control.enumerations.Direction;
import com.baselet.diagram.draw.geom.Dimension;
import com.baselet.diagram.draw.geom.DimensionDouble;
import com.baselet.diagram.draw.geom.Point;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.sticking.StickableMap;
import com.baselet.element.sticking.StickingPolygon;
import com.baselet.elementnew.Component;
import com.baselet.elementnew.ElementId;

public interface GridElement extends HasPanelAttributes, Relateable{

	void setRectangle(Rectangle bounds);

	Integer getGroup();

	void setLocationDifference(int diffx, int diffy);

	String getAdditionalAttributes();

	void setAdditionalAttributes(String additionalAttributes);

	void setLocation(int x, int y);

	void setSize(int width, int height);

	Set<Direction> getResizeArea(int x, int y);

	StickingPolygon generateStickingBorder(Rectangle rect);

	/**
	 * position of the element on the drawpanel.
	 * x and y: distance from the upper left corner of the drawpanel.
	 * width and height: size of the element.
	 *
	 */
	Rectangle getRectangle();

	void repaint();

	void changeSize(int diffx, int diffy);

	/**
	 * @return size of the element as if the zoomlevel would be 100% (eg: if zoom is 80% and width is 80 it would be returned as 100)
	 */
	Dimension getRealSize();

	boolean isInRange(Rectangle rectangle);

	Component getComponent();

	void setProperty(String key, Object newValue);

	void updateModelFromText();

	String getSetting(String key);

	Integer getLayer();

	void handleAutoresize(DimensionDouble necessaryElementDimension, AlignHorizontal alignHorizontal);

	ElementId getId();

	void drag(Collection<Direction> resizeDirection, int diffX, int diffY, Point mousePosBeforeDrag, boolean isShiftKeyDown, boolean firstDrag, StickableMap stickables, boolean undoable);

	boolean isSelectableOn(Point point);

	void dragEnd();

	List<String> getPanelAttributesAsList();

	void setRectangleDifference(int diffx, int diffy, int diffw, int diffh, boolean firstDrag, StickableMap stickables, boolean undoable);

	void undoDrag();

	void redoDrag();

	void mergeUndoDrag();

	void setRelateSettings(String json);

	String getRelateSettings();

}
