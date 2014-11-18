package com.baselet.control.enumerations;

public enum FormatLabels {
	UNDERLINE("_"),
	BOLD("*"),
	ITALIC("/"),
	RED("red"),
	GREEN("green");

	private String value;

	private FormatLabels(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
