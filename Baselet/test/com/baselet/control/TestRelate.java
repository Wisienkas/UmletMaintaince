package com.baselet.control;

import static org.junit.Assert.*;

import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.baselet.diagram.DiagramHandler;
import com.baselet.element.GridElement;

public class TestRelate {

	private final static String TEST_FILE_NAME = "testsubjects/testRelate.uxf";
	private static Logger log = Logger.getLogger(TestRelate.class);
	
	private static DiagramHandler handler;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initLogger();
		handler = new DiagramHandler(new File(TestRelate.TEST_FILE_NAME));
		
	}
	
	public static void initLogger() {
		String log4jFilePath = Path.homeProgram() + Constants.LOG4J_PROPERTIES;
		try {
			// If no log4j.properties file exists, we create a simple one
			if (!new File(log4jFilePath).exists()) {
				File tempLog4jFile = File.createTempFile(Constants.LOG4J_PROPERTIES, null);
				tempLog4jFile.deleteOnExit();
				log4jFilePath = tempLog4jFile.getAbsolutePath();
				try(Writer writer = new BufferedWriter(new FileWriter(tempLog4jFile))){
					writer.write(
							"log4j.rootLogger=ERROR, SYSTEM_OUT\n" +
									"log4j.appender.SYSTEM_OUT=org.apache.log4j.ConsoleAppender\n" +
									"log4j.appender.SYSTEM_OUT.layout=org.apache.log4j.PatternLayout\n" +
							"log4j.appender.SYSTEM_OUT.layout.ConversionPattern=%6r | %-5p | %-30c - \"%m\"%n\n");
					writer.flush();
					writer.close();
				}
			}
			Properties props = new Properties();
			props.put("PROJECT_PATH", Path.homeProgram()); // Put homepath as relative variable in properties file
			try(FileInputStream inStream = new FileInputStream(log4jFilePath)) {
				props.load(inStream);
				inStream.close();
			}
			PropertyConfigurator.configure(props);
			log.info("Logger configuration initialized");
		} catch (Exception e) {
			System.err.println("Initialization of " + Constants.LOG4J_PROPERTIES + " failed:");
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalAddPair() {
		GridElement parent1 = handler.getDrawPanel()
				.getGridElements()
				.stream()
				.filter(g -> 
					g.getPanelAttributes()
						.contains("Parent 1") &&
					!g.getPanelAttributes()
						.contains("child"))
				.findFirst()
				.get();
		Assert.assertNotNull(parent1);
		
		GridElement child1 = handler.getDrawPanel()
		.getGridElements()
		.stream()
		.filter(g -> 
			g.getPanelAttributes()
				.contains("child to parent 1, with no childs"))
		.findFirst()
		.get();
		Assert.assertNotNull(child1);
		
//		handler.getEntityListener(child1).mouseClicked(new Mo);
	}

}
