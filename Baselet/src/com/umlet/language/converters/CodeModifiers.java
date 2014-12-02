package com.umlet.language.converters;

public class CodeModifiers {
	public boolean slash = false;
	public boolean underscore = false;
	public boolean star = false;

	public String parse(String string)
	{
		boolean changed = false;

		if (string.startsWith("/") && string.endsWith("/")) {
			slash = true;
			changed = true;
		}
		else if (string.startsWith("_") && string.endsWith("_")) {
			underscore = true;
			changed = true;
		}
		else if (string.startsWith("*") && string.endsWith("*")) {
			star = true;
			changed = true;
		}

		if (changed) {
			string = parse(string.substring(1, string.length() - 1));
		}

		return string;
	}

	@Override
	public String toString()
	{
		boolean first = true;

		StringBuilder sb = new StringBuilder();
		if (slash) {
			if (!first)
			{
				sb.append("\t");
				first = false;
			}
			sb.append("/");
		}

		if (underscore) {
			if (!first)
			{
				sb.append("\t");
				first = false;
			}
			sb.append("_");
		}

		if (star) {
			if (!first)
			{
				sb.append("\t");
				first = false;
			}
			sb.append("*");
		}

		return sb.toString();
	}
}
