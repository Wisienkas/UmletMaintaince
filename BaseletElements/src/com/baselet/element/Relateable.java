package com.baselet.element;

public interface Relateable {
	
	void setParent(GridElement parent);
	void addChild(GridElement child);
	void removeChild(GridElement child);
}
