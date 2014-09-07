package com.baselet.element.sticking.polygon;

import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.sticking.StickingPolygon;

public interface StickingPolygonGenerator {
	public StickingPolygon generateStickingBorder(Rectangle rect);
}
