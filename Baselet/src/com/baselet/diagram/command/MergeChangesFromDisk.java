package com.baselet.diagram.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.io.InputHandler;

public class MergeChangesFromDisk extends Command {

	private String originalContent;
	private String mergedContent;	
	private InputStream inputStream;
	
	public MergeChangesFromDisk(String myContent, String mergedContent) {
		this.originalContent = myContent;
		this.mergedContent = mergedContent;
	}

	@Override
	public void execute(DiagramHandler handler) {
		super.execute(handler);
		updateDiagram(handler, mergedContent);
	}

	@Override
	public void undo(DiagramHandler handler) {
		super.undo(handler);
		updateDiagram(handler, originalContent);
	}

	private void updateDiagram(DiagramHandler diagramHandler, String content) {
		// Now update the diagram on screen with the new merged result
		try {
			synchronized (diagramHandler.getDrawPanel().getGridElements()) {
				diagramHandler.getDrawPanel().getGridElements().clear();
				diagramHandler.getDrawPanel().removeAll();

				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				inputStream = new ByteArrayInputStream(content.getBytes());
				InputHandler xmlhandler = new InputHandler(diagramHandler);
				parser.parse(inputStream, xmlhandler);

				diagramHandler.getDrawPanel().updatePanelAndScrollbars();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getOriginalContent() {
		return originalContent;
	}

	public String getMergedContent() {
		return mergedContent;
	}
}
