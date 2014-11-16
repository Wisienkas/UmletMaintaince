package com.baselet.diagram.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.baselet.diagram.DiagramHandler;

import difflib.DiffUtils;
import difflib.Patch;

public class FileChangeListener extends Thread {

	File file;
	String originalFileContent;
	DiagramHandler handler;
	DiagramFileHandler fileHandler;
	long timeStamp;

	public FileChangeListener(DiagramHandler handler, DiagramFileHandler fileHandler, File file) {
		this.file = file;
		this.handler = handler;
		this.fileHandler = fileHandler;
		timeStamp = file.lastModified();

		// Save the content of the opened file at the time it is opened
		try {
			originalFileContent = new Scanner(file).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {

			java.nio.file.Path myDir = Paths.get(file.getParent());

			System.out.println(myDir.toString());

			// WatchService watcher = myDir.getFileSystem().newWatchService();
			// myDir.register(watcher, ENTRY_MODIFY);
			//
			// WatchKey watckKey = watcher.take();

			while (true) {
				Thread.sleep(1000);

				if (handler.isChanged()) {
					fileHandler.doSave();
				}
				if (file.lastModified() == timeStamp) {
					continue;
				}

				timeStamp = file.lastModified();

				List<String> originalLines = StringToLines(originalFileContent);
				List<String> othersLines = StringToLines(new Scanner(file).useDelimiter("\\Z").next());
				List<String> myLines = StringToLines(fileHandler.createStringToBeSaved());
				List<String> result = new ArrayList<String>();

				Patch patch = DiffUtils.diff(originalLines, othersLines);
				result = (List<String>) DiffUtils.patch(myLines, patch);

				StringBuilder sb = new StringBuilder();
				for (String line : result) {
					sb.append(line + "\n");
				}

				originalFileContent = sb.toString();

				synchronized (handler.getDrawPanel().getGridElements()) {
					handler.getDrawPanel().getGridElements().clear();
					handler.getDrawPanel().removeAll();

					SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
					InputStream input = new ByteArrayInputStream(sb.toString().getBytes());
					InputHandler xmlhandler = new InputHandler(handler);
					parser.parse(input, xmlhandler);
					input.close();

					handler.getDrawPanel().updatePanelAndScrollbars();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: " + e.toString());
			run();
		}
	}

	private List<String> StringToLines(String text) {
		ArrayList<String> lines = new ArrayList<String>();

		for (String s : text.split("\n")) {
			lines.add(s);
		}
		return lines;
	}
}