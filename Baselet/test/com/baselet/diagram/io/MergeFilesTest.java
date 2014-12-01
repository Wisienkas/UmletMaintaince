package com.baselet.diagram.io;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.baselet.control.Main;

public class MergeFilesTest {
	
	private final static String MERGE_TEST_FILE_NAME = "testsubjects/mergeTest.uxf";

	private MockDiagramFileHandler diagramHandler1;
	private MockDiagramFileHandler diagramHandler2;
	
	private File testFile1;
	private File testFile2;
	
	@Before
	public void setupTest() throws InterruptedException {	
		Main.main(new String[0]);
		//Have to wait a while for the program to init
		Thread.sleep(5000);
		
		testFile1 = new File(MERGE_TEST_FILE_NAME);
		testFile2 = new File(MERGE_TEST_FILE_NAME);
		
		//Create two diagramHandlers that use the same file
		diagramHandler1 = new MockDiagramFileHandler(testFile1);
		diagramHandler2 = new MockDiagramFileHandler(testFile2);
		
		FileChangeListener listener1 = diagramHandler1.getFileChangeListener();
		FileChangeListener listener2 = diagramHandler2.getFileChangeListener();

		//to avoid a popup that requires user interaction we will setup the filehandlers
		//to automatically merge when a filechange is registered		
		listener1.dontShow = listener2.dontShow = true;
		listener1.selectedOption = listener2.selectedOption = JOptionPane.YES_OPTION;
	}

	@Test
	public void mergeTest() throws InterruptedException, IOException {
		//This first test will test the core merge functionality of the merge library
		coreMergeFucntion();
		
		//The first case simulates one user changing the diagram and the other one does not
		simpleCase();
		
		//The second case simulates both users changing the diagram simultaneously
		simultaniousChanges();
	}
	
	private void coreMergeFucntion() {
		String base = 
				"aaa\n" +
				"bbb\n" +
				"ccc\n" +
				"ddd\n";
		
		//In case of conflict changes1 will "win"
		String changes1 =
				"111\n" +  	//Changes whole line
				"bb1\n" +  	//Changes last char
				"ccc\n" +   //No changes
				"d1d\n";	//Changes to same char in both 1 and 2
		
		String changes2 =
				"aaa\n" +	//No changes to this line
				"b2b\n" +  	//Changes to same line as changes1
							//Deleted a line
				"d2d\n" +	//Changes to same char in both 1 and 2
				"eee";		//Added an extra line
		
		String expected = 
				"111\n" +	//This line comes from changes1
				"bb1\n" +	//because both made changes to this line changes1 "wins"
				"" +		//This line is deleted by changes 2
				"d1d\n" +	//both made changes to the same char so changes1 will "win"
				"eee";		//changes 2 added this extra line
		
		Assert.assertEquals(expected, FileChangeListener.merge(base, changes1, changes2));
	}
	
	private void simpleCase() throws IOException, InterruptedException {
		//Simulate change of the first diagram by randomly appending some string
		diagramHandler1.diagram += UUID.randomUUID().toString();
		diagramHandler1.doSave();

		//Wait at for the second instance to register and merge in the change
		Thread.sleep(5000);	
		
		Assert.assertEquals(diagramHandler1.diagram, diagramHandler2.diagram);
	}
	
	private void simultaniousChanges() throws IOException, InterruptedException {
		//Simulate both users are making a change 
		diagramHandler1.diagram += UUID.randomUUID().toString();
		diagramHandler2.diagram += UUID.randomUUID().toString();
		
		//Save both diagrams
		diagramHandler1.doSave();
		Thread.sleep(5000);
		diagramHandler2.doSave();
		Thread.sleep(5000);
		
		Assert.assertEquals(diagramHandler1.diagram, diagramHandler2.diagram);
	}
}
