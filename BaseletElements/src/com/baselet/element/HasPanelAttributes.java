package com.baselet.element;

import java.util.List;

import com.baselet.gui.AutocompletionText;

public interface HasPanelAttributes {

	String getPanelAttributes();

	void setPanelAttributes(String panelAttributes);

	List<AutocompletionText> getAutocompletionList();

}
