package com.umlet.language;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.umlet.language.converters.IClassConverter;
import com.umlet.language.converters.JavaClassConverter;

public class ClassConverterProvider {
	private static ClassConverterProvider instance;

	public static ClassConverterProvider getInstance()
	{
		if (instance == null) {
			instance = new ClassConverterProvider();
		}

		return instance;
	}

	// a converter is available when it has been added to this list
	private final List<IClassConverter<?>> converters = Arrays.asList(new JavaClassConverter());

	private ClassConverterProvider()
	{

	}

	public List<IClassConverter<?>> getConverters()
	{
		return Collections.unmodifiableList(converters);
	}

	public IClassConverter<?> getCompatibleConverter(String ext)
	{
		for (IClassConverter<?> converter : converters)
		{
			if (converter.canConvert(ext)) {
				return converter;
			}
		}

		return null;
	}
}
