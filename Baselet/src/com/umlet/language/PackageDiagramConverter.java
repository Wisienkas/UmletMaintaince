package com.umlet.language;

import java.util.ArrayList;
import java.util.List;

import com.baselet.control.Main;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.FontHandler;
import com.baselet.diagram.command.AddElement;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.GridElement;
import com.baselet.elementnew.ElementId;
import com.umlet.elementnew.ElementFactory;
import com.umlet.language.java.JavaClass;
import com.umlet.language.java.bcel.BcelJavaClass;
import com.umlet.language.java.jp.JpJavaClass;

public class PackageDiagramConverter {
	private final int GRIDSIZE;
	private final List<String> listOfPackages;

	public PackageDiagramConverter() {
		GRIDSIZE = Main.getInstance().getDiagramHandler().getGridSize();
		listOfPackages = new ArrayList<String>();
	}

	public void createPackageDiagram(String filename) {
		List<String> fileNames = new ArrayList<String>();
		fileNames.add(filename);
		createPackageDiagrams(fileNames);
	}

	public void createPackageDiagrams(List<String> filesToOpen) {
		List<SortableElement> elements = new ArrayList<SortableElement>();
		for (String filename : filesToOpen) {
			SortableElement element = findPackages(filename);
			if (element != null) {
				listOfPackages.add(element.getParsedClass().getPackage());
				elements.add(element);
			}
		}
		// Potential future use for a specific package diagram layout
		// new PackageLayout().layout(elements);
		// new PackageDiagramLayout().layout(elements);
		adjustLocation(elements);
		addElementsToDiagram(elements);
		listOfPackages.clear();
	}

	private SortableElement findPackages(String filename) {
		JavaClass parsedClass = parseFile(filename);
		if (parsedClass == null) {
			return null;
		}
		String parsedClassPackage = parsedClass.getPackage();
		if (listOfPackages.contains(parsedClassPackage)) {
			return null;
		}
		Rectangle initialSize = adjustSize(parsedClassPackage);
		GridElement packageElement = ElementFactory.create(ElementId.UMLPackage, initialSize, parsedClassPackage + "\nbg=orange", null, Main.getInstance().getDiagramHandler());

		return new SortableElement(packageElement, parsedClass);
	}

	private void addElementsToDiagram(List<SortableElement> elements) {
		DiagramHandler handler = Main.getInstance().getDiagramHandler();

		for (SortableElement e : elements) {
			new AddElement(e.getElement(),
					handler.realignToGrid(e.getElement().getRectangle().x),
					handler.realignToGrid(e.getElement().getRectangle().y), false).execute(handler);
		}
		handler.setChanged(true);
	}

	private void adjustLocation(List<SortableElement> packages) {
		int yLocation = 0;

		for (SortableElement element : packages) {
			Rectangle location = element.getElement().getRectangle();
			element.getElement().setLocation(location.x, location.y + yLocation);
			yLocation += 70;
		}

	}

	private Rectangle adjustSize(String packageName) {
		FontHandler fontHandler = Main.getInstance().getDiagramHandler().getFontHandler();

		int width = (int) (fontHandler.getTextWidth(packageName) + fontHandler.getDistanceBetweenTexts() + 50);
		int height = 40;

		return new Rectangle(0, 0, align(width), align(height)); // width&height must be multiples of grid size
	}

	private int align(int n) {
		return n - n % GRIDSIZE + GRIDSIZE;
	}

	private JavaClass parseFile(String filename) {
		try {
			if (getExtension(filename).equals("java")) {
				return parseJavaFile(filename);
			}
			else if (getExtension(filename).equals("class")) {
				return parseClassFile(filename);
			}
		} catch (Exception ignored) {}
		return null;
	}

	private JavaClass parseJavaFile(String filename) {
		try {
			return new JpJavaClass(filename);
		} catch (ClassParserException e) {
			return null;
		}
	}

	private JavaClass parseClassFile(String filename) {
		return new BcelJavaClass(filename);
	}

	private String getExtension(String filename) {
		int dotPosition = filename.lastIndexOf(".");
		return filename.substring(dotPosition + 1, filename.length());
	}
}
