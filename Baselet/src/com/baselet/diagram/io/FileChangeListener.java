package com.baselet.diagram.io;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
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

	private WatchService watcher;
	private Scanner openFileScanner;
	private InputStream resultInputStream;

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
			watcher = myDir.getFileSystem().newWatchService();
			myDir.register(watcher, ENTRY_MODIFY);

			log.info("Now watching file: " + openFile.getAbsolutePath());

			WatchKey watchKey = watcher.take();
			while (watchKey != null && !Thread.currentThread().isInterrupted()) {
				List<WatchEvent<?>> pollEvents = watchKey.pollEvents();
				for (int i = 0; i < pollEvents.size(); i++) {

					if (openFile.lastModified() == lastModified) {
						continue;
					}

					// Sleep for a short period to ensure the file operation is finished when we start reading
					Thread.sleep(2000);

					openFileScanner = new Scanner(openFile);
					String othersChanges = openFileScanner.useDelimiter("\\Z").next();
					openFileScanner.close();
					String myChanges = diagramFileHandler.createStringToBeSaved();
					lastModified = openFile.lastModified();

					// If we were the ones doing the change or the user does not want to merge in changes then just continue
					if (othersChanges.trim().equals(myChanges.trim()) || !showYesNOOptionPane()) {
						continue;
					}

					diff_match_patch diffGenerator = new diff_match_patch();
					LinkedList<diff_match_patch.Patch> patches = diffGenerator.patch_make(originalFileContent, othersChanges);
					String mergedResult = (String) diffGenerator.patch_apply(patches, myChanges)[0];

					originalFileContent = othersChanges;

					//Now update the diagram onscreen with the new merged result
					synchronized (diagramHandler.getDrawPanel().getGridElements()) {
						diagramHandler.getDrawPanel().getGridElements().clear();
						diagramHandler.getDrawPanel().removeAll();

						SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
						resultInputStream = new ByteArrayInputStream(mergedResult.getBytes());
						InputHandler xmlhandler = new InputHandler(diagramHandler);
						parser.parse(resultInputStream, xmlhandler);
						resultInputStream.close();

						diagramHandler.getDrawPanel().updatePanelAndScrollbars();
					}
				}
				watchKey.reset();
				watchKey = watcher.take();
			}
		} catch (InterruptedException e) {
			// The thread is interrupted meaning the diagram has been closed. We will therefore just stop the FileChangedListener
			log.info("Stopping FileChangeListener for file " + openFile.getAbsolutePath());
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			// If any other exception apart from an interrupted exception occurs, try restarting the FileChangeListener
			e.printStackTrace();
			run();
		} finally {
			//Making sure all resources are closed
			try {
				if (watcher != null) {
					watcher.close();
				}
				if (openFileScanner != null) {
					openFileScanner.close();
				}
				if(resultInputStream != null){
					resultInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		this.interrupt();
	}
}