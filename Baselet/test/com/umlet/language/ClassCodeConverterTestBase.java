package com.umlet.language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.SyntaxError;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.baselet.element.GridElement;
import com.umlet.language.converters.IClassConverter;

public class ClassCodeConverterTestBase {

	public static final File tmpDir = new File(System.getProperty("java.io.tmpdir"), "umlettest/a");

	@BeforeClass
	public static void setupClass()
	{
		if (tmpDir.exists()) {
			try {
				removeRecursive(tmpDir.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// this is required to setup log4j
		com.baselet.control.Main.getInstance().initLogger();
	}
	
	protected File baseTest(String attributes, String file) throws FileNotFoundException
	{
		return baseTest(attributes, file, true);
	}
	
	protected File baseTest(String attributes, String file, boolean shouldExists) throws FileNotFoundException
	{
		if (!file.contains(".java"))
		{
			throw new AssertionError("Needs a file extension");
		}
		
		Assert.assertFalse(occursMoreThanOnce(file, '.'));
		ClassCodeConverter converter = new ClassCodeConverter();

		List<GridElement> elements = new LinkedList<GridElement>();

		com.umlet.element.Class clazz = new com.umlet.element.Class();
		clazz.setPanelAttributes(attributes);
		elements.add(clazz);

		converter.createCodeDiagrams(tmpDir.getPath(), elements, true);
		
		File fileHandle = new File(tmpDir, file);

		if (!shouldExists)
		{
			return fileHandle;
		}
		
		Assert.assertTrue(fileHandle.exists());

		System.out.println(fileHandle);

		JavaType<?> source = Roaster.parse(fileHandle);

		if (source.getSyntaxErrors().size() != 0)
		{
			for (SyntaxError err : source.getSyntaxErrors())
			{
				System.out.println(err);
			}
		}

		Assert.assertTrue(source.getSyntaxErrors().size() == 0);

		String filePath = fileHandle.getAbsolutePath();
		
		IClassConverter<?> converter2 = ClassConverterProvider.getInstance().getCompatibleConverter(com.baselet.control.Path.getExtension(filePath));
		
		String propertiesText = converter2.getElementProperties(filePath);

		System.out.println("Expected:\n" + propertiesText + "\n|||\n");
		
		// this would require a parameter with the expected output 
		//Assert.assertTrue(attributes.equals(propertiesText));
		
		return fileHandle;
	}

	protected static void removeRecursive(Path path) throws IOException
	{
		Files.walkFileTree(path, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					throws IOException
			{
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				if (exc == null)
				{
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
				else
				{
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}

	protected static boolean occursMoreThanOnce(String string, char character)
	{
		boolean hasFound = false;
		for (int i = 0; i < string.length(); i++)
		{
			if (string.charAt(i) == character)
			{
				if (hasFound) {
					return true;
				}
				hasFound = true;
			}
		}
		return false;
	}
}