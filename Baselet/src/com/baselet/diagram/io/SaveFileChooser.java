package com.baselet.diagram.io;

import java.io.File;

import javax.swing.JFileChooser;

import com.baselet.control.Constants;
import com.baselet.control.Main;

public class SaveFileChooser {

	private static JFileChooser instance;

	private static JFileChooser getInstance() {
		if (instance == null) {
			instance = new JFileChooser(Constants.openFileHome);
			instance.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			instance.setMultiSelectionEnabled(false);
		}
		return instance;
	}

	public static String getFileToSave() {
		String file = null;

		int returnVal = getInstance().showSaveDialog(Main.getInstance().getGUI().getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = getInstance().getSelectedFile();

			file = selectedFile.getAbsolutePath();
		}

		return file;
	}

}
