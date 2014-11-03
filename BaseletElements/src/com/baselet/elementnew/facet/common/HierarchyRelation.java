package com.baselet.elementnew.facet.common;

import com.baselet.diagram.draw.DrawHandler;
import com.baselet.elementnew.PropertiesParserState;
import com.baselet.elementnew.facet.KeyValueFacet;

public class HierarchyRelation extends KeyValueFacet {

	public static HierarchyRelation INSTANCE = new HierarchyRelation();

	public static final String KEY = "Relate";

	@Override
	public KeyValue getKeyValue() {
		return new KeyValue(KEY, new ValueInfo(false, "relate"));
	}

	@Override
	public void handleValue(String value, DrawHandler drawer, PropertiesParserState state) {
		// Relate them

	}

	@Override
	public Priority getPriority() {
		return Priority.LOWEST;
	}



}
