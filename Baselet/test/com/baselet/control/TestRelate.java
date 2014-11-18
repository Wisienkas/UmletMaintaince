package com.baselet.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.baselet.diagram.DiagramHandler;
import com.baselet.element.GridElement;
import com.baselet.gui.MenuFactory;
import com.baselet.gui.MenuFactorySwing;

public class TestRelate {

	private final static String TEST_FILE_NAME = "testsubjects/testRelate.uxf";
	private static Logger log = Logger.getLogger(TestRelate.class);

	private static DiagramHandler handler;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initLogger();
		handler = new DiagramHandler(new File(TestRelate.TEST_FILE_NAME));
	}

	/**
	 * Taken from main method. Apparently it works
	 */
	public static void initLogger() {
		String log4jFilePath = Path.homeProgram() + Constants.LOG4J_PROPERTIES;
		try {
			// If no log4j.properties file exists, we create a simple one
			if (!new File(log4jFilePath).exists()) {
				File tempLog4jFile = File.createTempFile(Constants.LOG4J_PROPERTIES, null);
				tempLog4jFile.deleteOnExit();
				log4jFilePath = tempLog4jFile.getAbsolutePath();
				try (Writer writer = new BufferedWriter(new FileWriter(tempLog4jFile))) {
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
			try (FileInputStream inStream = new FileInputStream(log4jFilePath)) {
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

		JMenuItem rightClickChild = MenuFactorySwing.getInstance().createRelateAround(child1, handler);
		
		doRecursive(rightClickChild);
		
		// Else the action is not invoked
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		log.info("child1 in relationmanager: " + handler.getRelationManager().containsElement(child1));
		log.info("parent1 in relationmanager: " + handler.getRelationManager().containsElement(parent1));

		Assert.assertTrue("Has Child1", handler.getRelationManager().containsElement(child1));
		Assert.assertTrue("Has Parent1", handler.getRelationManager().containsElement(parent1));
		
		Assert.assertTrue("parent1 has child1 as child and parent1 only has 1 child", 
				handler
					.getRelationManager()
					.getChildren(parent1)
					.contains(child1) && 
				handler
					.getRelationManager()
					.getChildren(parent1)
					.size() == 1
				);
		
		Assert.assertTrue("Child1 has no children", 
				!handler.getRelationManager().hasChild(child1));
		log.info("Is parent present: " + handler.getRelationManager().getParent(child1).isPresent());
		
		log.info("parent string: " + handler.getRelationManager().getJSON(parent1));
		
		log.info(handler.getRelationManager().getIdByElement(parent1) == 2);

		Assert.assertTrue("parent1 is the parent based on ID", 
				handler
					.getRelationManager()
					.getIdByElement(parent1)
				== 
				handler
					.getRelationManager()
					.getParent(
						handler
							.getRelationManager()
							.getIdByElement(child1))
							.get()
		);
		
		Assert.assertTrue("parent is parent based on Element",
				handler
					.getRelationManager()
					.getParent(child1)
					.get() == parent1
		);
		
	}

	private void doRecursive(MenuElement menuElement) {
		for(MenuElement me : menuElement.getSubElements()){
			if(me instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) me;
				for(ActionListener al : item.getActionListeners()) {
					try{
						al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
					} catch (Exception e) {
						
					}
				}
				break;
			} else {
				doRecursive(me);
			}

		}
	}

}
