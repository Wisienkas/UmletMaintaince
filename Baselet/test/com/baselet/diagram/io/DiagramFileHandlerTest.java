package com.baselet.diagram.io;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JFileChooser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.baselet.control.Constants;
import com.baselet.control.Main;
import com.baselet.diagram.DiagramHandler;

public class DiagramFileHandlerTest {
	public static final String lastlocation = "C:\\";

	private DiagramHandler handler;
	private DiagramFileHandler fileHandler;

	@Before
	public void setup() {
		handler = new DiagramHandler(null);
		fileHandler = new DiagramFileHandler(handler, null);
	}

	@SuppressWarnings("null")
	@Test
	public void RememberLastSaveLocationFileChooserIsNull() throws Exception {
		String assertPath = "C:\\";
		Field fileChooser = fileHandler.getClass().getDeclaredField("saveFileChooser");

		fileChooser.setAccessible(true);
		// fileChooser.set(fileHandler, null);

		Constants.last_saved_path = assertPath;
		Main.getInstance().setCurrentDiagramHandler(handler);
		Object obj = null;
		Method[] meths = fileHandler.getClass().getDeclaredMethods();
		for (Method method : meths) {
			if (method.getName().equals("reloadSaveFileChooser")) {
				method.setAccessible(true);
				obj = method.invoke(fileHandler, new Boolean(false));
			}
		}
		Assert.assertNotNull("The FileChooser is null!", obj);
		if (obj != null) {
			JFileChooser jfc = (JFileChooser) obj;
			File file = jfc.getCurrentDirectory();
			org.junit.Assert.assertTrue("The path is not the last saved path!", file.getPath().equals(assertPath));
		}
	}
}
