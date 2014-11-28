package com.baselet.diagram.io;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.baselet.diagram.DiagramHandler;

public class FileChangeListener extends Thread {
	private static final Logger log = Logger.getLogger(FileChangeListener.class.getName());

	File openFile;
	String originalFileContent;
	DiagramHandler diagramHandler;
	DiagramFileHandler diagramFileHandler;
	long lastModified;

	// The are used for the confirmation dialog
	boolean dontShow;
	int selectedOption;

	public FileChangeListener(DiagramHandler handler, DiagramFileHandler fileHandler, File file) {
		this.openFile = file;
		this.diagramHandler = handler;
		this.diagramFileHandler = fileHandler;
		lastModified = file.lastModified();

		// Save the content of the opened file at the time it is opened
		try {
			originalFileContent = new Scanner(file).useDelimiter("\\Z").next();
			this.start();
		} catch (FileNotFoundException e) {
			log.warning("Could not start FileChangeListener");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			java.nio.file.Path myDir = Paths.get(openFile.getParent());
			WatchService watcher = myDir.getFileSystem().newWatchService();
			myDir.register(watcher, ENTRY_MODIFY);

			log.info("Now watching file: " + openFile.getAbsolutePath());

			WatchKey watchKey = watcher.take();
			while (watchKey != null) {
				for (WatchEvent<?> event : watchKey.pollEvents()) {
					if (openFile.lastModified() == lastModified) {
						continue;
					}

					// Sleep for a short period to ensure the file operation is finished when we start reading
					Thread.sleep(2000);

					String othersChanges = new Scanner(openFile).useDelimiter("\\Z").next();
					String myChanges = diagramFileHandler.createStringToBeSaved();
					lastModified = openFile.lastModified();

					//If we were the ones doing the change or the user does not want to merge in changes then just continue
					if (othersChanges.trim().equals(myChanges.trim()) || !showYesNOOptionPane()) {
						continue;
					}

					diff_match_patch diffGenerator = new diff_match_patch();
					LinkedList<diff_match_patch.Patch> patches = diffGenerator.patch_make(originalFileContent, othersChanges);
					String mergedResult = (String) diffGenerator.patch_apply(patches, myChanges)[0];

					originalFileContent = othersChanges;

					synchronized (diagramHandler.getDrawPanel().getGridElements()) {
						diagramHandler.getDrawPanel().getGridElements().clear();
						diagramHandler.getDrawPanel().removeAll();

						SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
						InputStream input = new ByteArrayInputStream(mergedResult.getBytes());
						InputHandler xmlhandler = new InputHandler(diagramHandler);
						parser.parse(input, xmlhandler);
						input.close();

						diagramHandler.getDrawPanel().updatePanelAndScrollbars();
					}
				}
				watchKey.reset();
				watchKey = watcher.take();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: " + e.toString());
			run();
		}
	}

	// Returns true if user selected yes, otherwise no
	private boolean showYesNOOptionPane() {
		if (!dontShow) {
			JCheckBox checkbox = new JCheckBox("Do always for this diagram");
			String message = "Somebody else changed the file: " + openFile.getAbsolutePath() + "\nWould you like to merge the changes into your own changes?";
			Object[] params = { message, checkbox };
			selectedOption = JOptionPane.showConfirmDialog(null, params, "File changed", JOptionPane.YES_NO_OPTION);
			dontShow = checkbox.isSelected();
		}
		return selectedOption == JOptionPane.YES_OPTION;
	}

	public void stopListening() {
		// TODO Auto-generated method stub
		
	}
}