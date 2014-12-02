package com.umlet.language.converters;

import java.util.function.Function;

public class SourceObject<T> {
	private final T sourceObject;
	private final Function<T, String> sourceName;
	private final Function<T, String> sourcePackage;

	public SourceObject(T sourceObject, Function<T, String> sourceName, Function<T, String> sourcePackage)
	{
		this.sourceObject = sourceObject;
		this.sourceName = sourceName;
		this.sourcePackage = sourcePackage;
	}

	public String getName()
	{
		return sourceName.apply(sourceObject);
	}

	public String getPackage()
	{
		return sourcePackage.apply(sourceObject);
	}

	public T getSource()
	{
		return sourceObject;
	}

	@Override
	public String toString()
	{
		return sourceObject.toString();
	}
}
