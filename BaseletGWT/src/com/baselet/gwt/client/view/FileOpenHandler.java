package com.baselet.gwt.client.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vectomatic.file.ErrorCode;
import org.vectomatic.file.File;
import org.vectomatic.file.FileError;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import com.baselet.gwt.client.DiagramXmlParser;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class FileOpenHandler {

	private Logger log = Logger.getLogger(FileOpenHandler.class);

	protected FileReader reader;
	protected List<File> readQueue = new ArrayList<File>();

	public FileOpenHandler(final DrawPanel diagramHandler) {
		reader = new FileReader();
		reader.addLoadEndHandler(new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				if (reader.getError() == null) {
					if (readQueue.size() > 0) {
						try {
							String result = reader.getStringResult();
							diagramHandler.setDiagram(DiagramXmlParser.xmlToDiagram(result));
						} catch (RuntimeException e) {
							log.error("Error at loading diagram from file", e);
						} finally {
							readQueue.remove(0);
							readNext();
						}
					}
				}
			}
		});
	}

	public void processFiles(FileList files) {
		GWT.log("length=" + files.getLength());
		for (File file : files) {
			readQueue.add(file);
		}
		readNext();
	}

	private void readNext() {
		if (readQueue.size() > 0) {
			File file = readQueue.get(0);
			try {
				reader.readAsText(file);
			} catch (Throwable t) {
				// Necessary for FF (see bug https://bugzilla.mozilla.org/show_bug.cgi?id=701154)
				// Standard-complying browsers will to go in this branch
				handleError(file);
				readQueue.remove(0);
				readNext();
			}
		}
	}

	private void handleError(File file) {
		FileError error = reader.getError();
		String errorDesc = "";
		if (error != null) {
			ErrorCode errorCode = error.getCode();
			if (errorCode != null) {
				errorDesc = ": " + errorCode.name();
			}
		}
		Window.alert("File loading error for file: " + file.getName() + "\n" + errorDesc);
	}

}
