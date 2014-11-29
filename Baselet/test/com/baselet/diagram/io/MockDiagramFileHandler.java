package com.baselet.diagram.io;

import java.io.File;
import java.util.Scanner;

import com.baselet.diagram.Controller;
import com.baselet.diagram.command.Command;
import com.baselet.diagram.command.MergeChangesFromDisk;

public class MockDiagramFileHandler extends DiagramFileHandler {

	//This variable simulates the in memory diagram
	public String diagram;
	
	private Scanner fileScanner;
	private MockController controller;
	
	protected MockDiagramFileHandler(File file) {
		super(null, file);
		controller = new MockController();
		
		try {
			fileScanner = new Scanner(file);
			diagram = fileScanner.useDelimiter("\\Z").next();
		} catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(fileScanner != null) {
				fileScanner.close();
			}
		}
	}
	
	@Override
	protected String createStringToBeSaved() {
		return diagram;
	}
	
	@Override
	public Controller getDiagramController() {
		return this.controller;
	}
	
	//This will simmulate the controller behavior. 
	//Instead of tying to parse the new merged content we just set it as the simulated diagram. 
	private class MockController extends Controller {		
		public MockController() {
			super(null);
		}
		
		@Override
		public void executeCommand(Command newCommand) {
			MergeChangesFromDisk command = (MergeChangesFromDisk) newCommand;
			diagram = command.getMergedContent();
		}
	}
}