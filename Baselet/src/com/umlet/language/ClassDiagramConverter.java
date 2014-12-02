package com.umlet.language;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaSource;

import com.baselet.control.Constants;
import com.baselet.control.Main;
import com.baselet.control.Path;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.FontHandler;
import com.baselet.diagram.command.AddElement;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.GridElement;
import com.baselet.elementnew.ElementId;
import com.umlet.elementnew.ElementFactory;
import com.umlet.language.converters.IClassConverter;
import com.umlet.language.sorting.AlphabetLayout;
import com.umlet.language.sorting.HeightLayout;
import com.umlet.language.sorting.PackageLayout;
import com.umlet.language.sorting.RelationLayout;

/**
 * Creates a class element from a filename pointing to a .class or .java file according to UML standards,
 * adds the class to the current diagram and resizes this class element to minimum size where all text is visible.
 *
 * @author Lisi Bluemelhuber
 *
 */
public class ClassDiagramConverter {

	private final int GRIDSIZE;

	public ClassDiagramConverter() {
		GRIDSIZE = Main.getInstance().getDiagramHandler().getGridSize();
	}

	public void createClassDiagram(String filename) {
		List<String> fileNames = new ArrayList<String>();
		fileNames.add(filename);
		createClassDiagrams(fileNames);
	}

	public void createClassDiagrams(List<String> filesToOpen) {
		List<SortableElement> elements = new ArrayList<SortableElement>();
		for (String filename : filesToOpen) {
			SortableElement element = createElement(filename);
			if (element != null) {
				elements.add(element);
			}
		}

		switch (Constants.generateClassSortings) {
			case PACKAGE:
				new PackageLayout().layout(elements);
				break;
			case ALPHABET:
				new AlphabetLayout().layout(elements);
				break;
			case RELATIONS:
				new RelationLayout().layout(elements);
				break;
			default:
				new HeightLayout().layout(elements); // by height
		}

		addElementsToDiagram(elements);
	}

	private SortableElement createElement(String filename) {
		@SuppressWarnings("unchecked")
		IClassConverter<JavaSource<?>> converter = (IClassConverter<JavaSource<?>>) ClassConverterProvider.getInstance().getCompatibleConverter(Path.getExtension(filename));
		if (converter == null) {
			return null;
		}

		String propertiesText = converter.getElementProperties(filename);
		if (propertiesText == null) {
			return null;
		}

		propertiesText.split("\n");
		List<String> propList = Arrays.asList(propertiesText.split("\n"));
		Rectangle initialSize = adjustSize(propList);
		GridElement clazz = ElementFactory.create(ElementId.UMLClass, initialSize, propertiesText, null, Main.getInstance().getDiagramHandler());
		return new SortableElement(clazz, new File(filename).getName());
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

	/**
	 * Adjusts a Class GridElement to the minimum size where all text is visible.
	 *
	 * @param clazz
	 * @return
	 */
	private Rectangle adjustSize(List<String> strings) {
		// GridElement clazz not yet fully initialized, cannot call clazz.getHandler();
		FontHandler fontHandler = Main.getInstance().getDiagramHandler().getFontHandler();

		int width = 0;
		int height = strings.size();
		double heightTweaker = 0.1;
		for (String string : strings) {
			if (string.isEmpty()) {
				heightTweaker += 1;
			}
			else if (string.equals("--")) {
				heightTweaker += 0.5;
			}
			if (fontHandler.getTextWidth(string) > width) {
				width = (int) (fontHandler.getTextWidth(string) + fontHandler.getDistanceBetweenTexts()) + 10;
			}
		}
		height = (int) (fontHandler.getFontSize() + fontHandler.getDistanceBetweenTexts()) * (height - (int) heightTweaker);

		return new Rectangle(0, 0, align(width), align(height)); // width&height must be multiples of grid size
	}

	private int align(int n) {
		return n - n % GRIDSIZE + GRIDSIZE;
	}
}
