package com.umlet.language;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

public class ClassCodeConverterTestBlackBox extends ClassCodeConverterTestBase {

	@Test
	public void simpleClass() throws FileNotFoundException
	{
		String attributes = "Classtest\n";
		String filename = "Classtest.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}

	@Test
	public void simpleInterfaceTest() throws FileNotFoundException
	{
		String attributes = "<<Interface>>\ncom.umlet.test.unittest.Interface2";
		String filename = "com/umlet/test/unittest/Interface2.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}

	@Test
	public void interfaceContent() throws FileNotFoundException
	{
		String attributes = "<<Interface>>\ncom.umlet.test.unittest.Interface3\n--\n--\n+morning()\n";
		String filename = "com/umlet/test/unittest/Interface3.java";
		File file = baseTest(attributes, filename);

		file.delete();
	}

	@Test
	public void abstractClass() throws FileNotFoundException
	{
		String attributes = "/" + "com.umlet.test.unittest::Class4/\n--\n-morning : String\n--\n+morning()\n";
		String filename = "com/umlet/test/unittest/Class4.java";
		File file = baseTest(attributes, filename);

		file.delete();
	}

	@Test
	public void abstractClassComments() throws FileNotFoundException
	{
		String attributes = "//hej med dig\n/com.umlet.test.unittest::Class5/\n--\n-moring : String\n--\n+method()\n";
		String filename = "com/umlet/test/unittest/Class5.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}
}