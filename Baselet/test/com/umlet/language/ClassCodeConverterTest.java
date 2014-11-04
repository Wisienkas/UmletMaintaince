package com.umlet.language;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.baselet.element.GridElement;

public class ClassCodeConverterTest {

	String[] classes = new String[] {
			"classtest",
			"com.umlet.test.unittest.class2",
			"com.umlet.test.unittest.class3",
			"com.umlet.test.unittest::class4",
			"com.umlet.test.unittest::class5"
	};

	String[] filenames = new String[] {
			"classtest.java",
			"com/umlet/test/unittest/class2.java",
			"com/umlet/test/unittest/class3.java",
			"com/umlet/test/unittest/class4.java",
			"com/umlet/test/unittest/class5.java"
	};

	@Test
	public void testCreateSourceCode()
	{
		ClassCodeConverter converter = new ClassCodeConverter();

		File dir = new File(System.getProperty("java.io.tmpdir"), "umlettest/a");
		dir.delete();

		List<GridElement> elements = getList();

		converter.createCodeDiagrams(dir.getPath(), elements);

		for (String file : filenames)
		{
			File fileHandle = new File(dir, file);

			Assert.assertTrue(fileHandle.exists());
		}

		dir.delete();
	}

	private List<GridElement> getList()
	{
		List<GridElement> elements = new LinkedList<GridElement>();

		com.umlet.element.Class class1 = new com.umlet.element.Class();
		class1.setPanelAttributes(classes[0]);
		elements.add(class1);

		com.umlet.element.Class class2 = new com.umlet.element.Class();
		class2.setPanelAttributes("<<Interface>>\n" + classes[1]);
		elements.add(class2);

		com.umlet.element.Class class3 = new com.umlet.element.Class();
		class3.setPanelAttributes("<<Interface>>\n" + classes[2] + "\n+method()\n-moring : String");
		elements.add(class3);

		com.umlet.element.Class class4 = new com.umlet.element.Class();
		class4.setPanelAttributes("/" + classes[3] + "/\n+method()\n-moring : String");
		elements.add(class4);

		com.umlet.element.Class class5 = new com.umlet.element.Class();
		class5.setPanelAttributes("//hej med dig\n/" + classes[4] + "/\n+method()\n-moring : String");
		elements.add(class5);

		return elements;
	}
}