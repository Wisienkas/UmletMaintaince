package com.umlet.language.converters;


public interface IClassConverter<T> {
	public static final String SPLITTER = "--\n";

	boolean canConvert(String extension);

	String getElementProperties(String file);

	void createTopSection(Object parsedClass, StringBuilder attributes);

	void createFieldSection(Object parsedClass, StringBuilder attributes);

	void createMethodSection(Object parsedClass, StringBuilder attributes);

	SourceObject<T> parseElementProperties(String attributes);

	void parseTopSection(SourceObject<T> code, String attributes);

	void parseFieldSection(SourceObject<T> code, String attributes);

	void parseMethodSection(SourceObject<T> code, String attributes);

}