package com.umlet.language;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

public class ClassCodeConverterTestWhiteBox extends ClassCodeConverterTestBase {

	@Test
	public void fail() throws FileNotFoundException
	{
		File file = baseTest("ClassFail1\n--\n-asd", "ClassFail1.java", false);
		
		Assert.assertFalse(file.exists());
		
		file.delete();
	}

	@Test
	public void fail2() throws FileNotFoundException
	{
		File file = baseTest("/ClassFail2/\n--\n-morning : String\n--+morning()", "ClassFail2.java", false);
		
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
	public void testInterfaces2() throws FileNotFoundException
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
	
	@Test
	public void testClass1() throws FileNotFoundException
	{
		String attributes = "Classtest\n";
		String filename = "Classtest.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}

	@Test
	public void testClass2() throws FileNotFoundException
	{
		String attributes = "<<Interface>>\n" + "com.umlet.test.unittest.Class2";
		String filename = "com/umlet/test/unittest/Class2.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}

	@Test
	public void testClass3() throws FileNotFoundException
	{
		String attributes = "<<Interface>>\n" + "com.umlet.test.unittest.Class3" + "\n--\n--\n+morning()\n";
		String filename = "com/umlet/test/unittest/Class3.java";
		File file = baseTest(attributes, filename);

		file.delete();
	}

	@Test
	public void testClass4() throws FileNotFoundException
	{
		String attributes = "/" + "com.umlet.test.unittest::Class4" + "/\n--\n-morning : String\n--\n+morning()\n";
		String filename = "com/umlet/test/unittest/Class4.java";
		File file = baseTest(attributes, filename);

		file.delete();
	}

	@Test
	public void testClass5() throws FileNotFoundException
	{
		String attributes = "//hej med dig\n/" + "com.umlet.test.unittest::Class5" + "/\n--\n-moring : String\n--\n+method()\n";
		String filename = "com/umlet/test/unittest/Class5.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}

	@Test
	public void generatedTest() throws FileNotFoundException
	{
		String attributes = "yatzyConsole::YatzyConsole\n--\n--\nmain(String[] args): void\nrollDices(): void\nprintDices(): void\n--\n";
		String filename = "yatzyConsole/YatzyConsole.java";
		File file = baseTest(attributes, filename);
		file.delete();
	}
}