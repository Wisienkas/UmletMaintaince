package com.baselet.gwt.client.version;

import com.google.gwt.i18n.client.Constants;

public interface Build extends Constants {
	String buildtime();

	String version();
}