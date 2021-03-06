package com.baselet.diagram.io;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import com.baselet.control.Main;
import com.baselet.diagram.command.MergeChangesFromDisk;

public class FileChangeListener extends Thread {
	private static final Logger log = Logger.getLogger(FileChangeListener.class.getName());

	private File watchedFile;
	private String baseContent;
	private DiagramFileHandler diagramFileHandler;
	private long lastModified;

	// These are used for the confirmation dialog
	public boolean dontShow;
	public int selectedOption;

	private WatchService watcher;
	private Scanner openFileScanner;

	public FileChangeListener(DiagramFileHandler fileHandler, File file) {
		this.watchedFile = file;
		this.diagramFileHandler = fileHandler;
		lastModified = file.lastModified();

		// Save the content of the opened file at the time it is opened
		try {
			baseContent = new Scanner(file).useDelimiter("\\Z").next();
			this.start();
		} catch (FileNotFoundException e) {
			log.warning("Could not start FileChangeListener");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			java.nio.file.Path myDir = Paths.get(watchedFile.getParent());
			watcher = myDir.getFileSystem().newWatchService();
			myDir.register(watcher, ENTRY_MODIFY);

			log.info("Now watching file: " + watchedFile.getAbsolutePath());

			WatchKey watchKey = watcher.take();
			while (watchKey != null && !Thread.currentThread().isInterrupted()) {
				List<WatchEvent<?>> pollEvents = watchKey.pollEvents();
				for (int i = 0; i < pollEvents.size(); i++) {

					if (watchedFile.lastModified() == lastModified) {
						continue;
					}

					// Sleep for a short period to ensure the file operation is finished when we start reading
					Thread.sleep(2000);

					openFileScanner = new Scanner(watchedFile);
					String onDiskChanges = openFileScanner.useDelimiter("\\Z").next();
					openFileScanner.close();
					String myChanges = diagramFileHandler.createStringToBeSaved();
					String oldBase = baseContent;
					
					lastModified = watchedFile.lastModified();
					baseContent = onDiskChanges;

					// If we were the ones doing the change or the user does not want to merge in changes then just continue
					if (onDiskChanges.trim().equals(myChanges.trim()) || !showYesNOOptionPane()) {
						continue;
					}

					log.info("Merging in changes");					
					//Perform a 3-way merge against the old base
					String mergedResult = FileChangeListener.merge(oldBase, onDiskChanges, myChanges);
					
					//Create and execute the diagram update action // if action is undone we will return to myChanges //
					diagramFileHandler.getDiagramController().executeCommand(new MergeChangesFromDisk(myChanges, mergedResult));
				}
				watchKey.reset();
				watchKey = watcher.take();
			}
		} catch (InterruptedException e) {
			// The thread is interrupted meaning the diagram has been closed. We will therefore just stop the FileChangedListener
			log.info("Stopping FileChangeListener for file " + watchedFile.getAbsolutePath());
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return true if user selected yes, otherwise no
	 */
	private boolean showYesNOOptionPane() {
		if (!dontShow) {
			JCheckBox checkbox = new JCheckBox("Do always for this diagram");
			String message = "Somebody else changed the file: " + watchedFile.getAbsolutePath() + "\nWould you like to merge the changes into your own changes?";
			Object[] params = { message, checkbox };
			selectedOption = JOptionPane.showConfirmDialog(Main.getInstance().getGUI().getMainFrame(), params, "File changed", JOptionPane.YES_NO_OPTION);
			dontShow = checkbox.isSelected();
		}
		return selectedOption == JOptionPane.YES_OPTION;
	}
	
	/**
	 * @return The the result of a 3-way merge operation of changes1 and changes2
	 * against a common base.  
	 */
	public static String merge(String base, String changes1, String changes2) {
		//Create a new diff_match_patch
		diff_match_patch diffGenerator = new diff_match_patch();
		
		//Calculate the patches needed to go from the base to changes1
		LinkedList<diff_match_patch.Patch> patches = diffGenerator.patch_make(base, changes1);
		
		//Apply those patches to changes2
		Object[] mergeResult = diffGenerator.patch_apply(patches, changes2); 
		
		return (String) mergeResult[0];
	}

	public void stopListening() {
		this.interrupt();
	}
}