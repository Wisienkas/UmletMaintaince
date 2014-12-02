package com.umlet.language;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

public class ClassCodeConverterTestWhiteBox extends ClassCodeConverterTestBase {

	@Test
	public void failWrongSectionSplit() throws FileNotFoundException
	{
		File file = baseTest("ClassFail1\n--\n-asd", "ClassFail1.java", false);
		
		Assert.assertFalse(file.exists());
		
		file.delete();
	}

	@Test
	public void failWrongSectionPlacement() throws FileNotFoundException
	{
		File file = baseTest("/ClassFail2/\n--\n-morning : String\n+morning()\n--\n", "ClassFail2.java", false);
		
		Assert.assertFalse(file.exists());
		
		file.delete();
	}
	
	@Test
	public void testInterfaces() throws FileNotFoundException
	{
		String attributes = "<<Interface>>\n<<Simple>>\ncom.umlet.test::Class1\n";
		String filename = "com/umlet/test/Class1.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}
	
	@Test
	public void testClassWithInterface() throws FileNotFoundException
	{
		String attributes = "<<Simple>>\ncom.umlet.test::Class1\n";
		String filename = "com/umlet/test/Class1.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}
	
	@Test
	public void testTopSection() throws FileNotFoundException
	{
		String attributes = "com.umlet.test.Class1\n";
		String filename = "com/umlet/test/Class1.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}
	
	/**
	 * Test content based on a diagram created from code
	 * @throws FileNotFoundException
	 */
	@Test
	public void generatedTest() throws FileNotFoundException
	{
		String attributes = "yatzyConsole::YatzyConsole\n--\n--\nmain(String[] args): void\nrollDices(): void\nprintDices(): void\n--\n";
		String filename = "yatzyConsole/YatzyConsole.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}
}