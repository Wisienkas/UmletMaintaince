package com.baselet.control.enumerations;

public enum FormatLabels {
	UNDERLINE("_"),
	BOLD("*"),
	ITALIC("/"),
	RED("red"),
	GREEN("green"),
	BLUE("blue"),
	YELLOW("yellow"),
	MAGENTA("magenta"),
	WHITE("white"),
	BLACK("black"),
	ORANGE("orange"),
	CYAN("cyan"),
	DARK_GRAY("darkgray"),
	GRAY("gray"),
	LIGHT_GRAY("lightgray"),
	PINK("pink");

	private String value;

	private FormatLabels(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
