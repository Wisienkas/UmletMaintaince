package com.baselet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({com.baselet.control.AllTests.class, 
	com.baselet.diagram.io.DiagramFileHandlerTest.class,
	com.baselet.diagram.io.MergeFilesTest.class,
	com.plotlet.parser.DataSetTest.class,
	com.umlet.element.ColorCodeTest.class,
	com.umlet.element.SequenceDiagramTest.class,
	com.umlet.language.AllTests.class })
public class AllTests {

}
