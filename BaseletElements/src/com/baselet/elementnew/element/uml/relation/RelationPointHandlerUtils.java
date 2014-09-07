package com.baselet.elementnew.element.uml.relation;

import com.baselet.control.SharedConstants;
import com.baselet.control.SharedUtils;
import com.baselet.diagram.draw.geom.Point;
import com.baselet.diagram.draw.geom.PointDouble;
import com.baselet.diagram.draw.geom.Rectangle;

public class RelationPointHandlerUtils {

	static Rectangle calculateRelationRectangleBasedOnPoints(PointDouble upperLeftCorner, int gridSize, RelationPointList relationPoints) {
		// Calculate new Relation position and size
		Rectangle newSize = relationPoints.createRectangleContainingAllPointsAndTextSpace();
		if (newSize == null) {
			throw new RuntimeException("This relation has no points: " + relationPoints);
		}
		// scale with zoom factor
		newSize.setBounds(
				newSize.getX() * gridSize / SharedConstants.DEFAULT_GRID_SIZE,
				newSize.getY() * gridSize / SharedConstants.DEFAULT_GRID_SIZE,
				newSize.getWidth() * gridSize / SharedConstants.DEFAULT_GRID_SIZE,
				newSize.getHeight() * gridSize / SharedConstants.DEFAULT_GRID_SIZE);
		// Realign new size to grid (should not be necessary as long as SELECTCIRCLERADIUS == DefaultGridSize) and add 1x gridSize to the right end (otherwise the selection-circles would change by 1px because Swing draws only to width-1 instead of width)
		newSize.setLocation(SharedUtils.realignTo(false, newSize.getX(), false, gridSize), SharedUtils.realignTo(false, newSize.getY(), false, gridSize));
		newSize.setSize(SharedUtils.realignTo(false, newSize.getWidth(), true, gridSize) + gridSize, SharedUtils.realignTo(false, newSize.getHeight(), true, gridSize) + gridSize);
		// and move to correct place of Relation
		newSize.move(upperLeftCorner.getX().intValue(), upperLeftCorner.getY().intValue());

		return newSize;
	}

	static Rectangle toRectangle(PointDouble p, double size) {
		return new Rectangle(p.x - size, p.y - size, size * 2, size * 2);
	}

	static PointDoubleIndexed getRelationPointContaining(Point point, RelationPointList points) {
		for (RelationPoint relationPoint : points.getPointHolders()) {
			if (relationPoint.getSizeAbsolute().contains(point)) {
				return relationPoint.getPoint();
			}
		}
		return null;
	}
}
