package com.baselet.elementnew.facet.common;

import com.baselet.diagram.draw.DrawHandler;
import com.baselet.diagram.draw.helper.ColorOwn;
import com.baselet.elementnew.PropertiesParserState;
import com.baselet.elementnew.facet.KeyValueFacet;

public class AutosizeFacet extends KeyValueFacet {

	public static AutosizeFacet INSTANCE = new AutosizeFacet();

	private AutosizeFacet() {}

	public static final String KEY = "autosize";

	@Override
	public KeyValue getKeyValue() {
		return new KeyValue(KEY, false, "true", "Auto resize " + ColorOwn.EXAMPLE_TEXT);
	}

	@Override
	public void handleValue(String value, DrawHandler drawer, PropertiesParserState state) {
		if (value.equalsIgnoreCase("true")) {
			state.setAutoresize(true);
		}
		else {
			state.setAutoresize(false);
		}
	}

	@Override
	public Priority getPriority() {
		return Priority.HIGHEST;
	}
}
