package com.baselet.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.draw.DrawHandler;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.GridElement;
import com.baselet.elementnew.ElementId;
import com.baselet.elementnew.NewGridElement;
import com.baselet.elementnew.PropertiesParserState;
import com.baselet.elementnew.settings.Settings;
import com.baselet.gui.MenuFactory;
import com.baselet.gui.MenuFactorySwing;
import com.umlet.element.custom.Component;

public class TestRelate {

	private final static String TEST_FILE_NAME = "testsubjects/testRelate.uxf";
	private static Logger log = Logger.getLogger(TestRelate.class);

	private static DiagramHandler handler;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestLogger.initLogger();
		handler = new DiagramHandler(new File(TestRelate.TEST_FILE_NAME));
	}

	/**
	 * Taken from main method. Apparently it works
	 */


	@Test
	public void testNormalAddPair() {
		GridElement parent1 = getGridElement("Parent 1");
		Assert.assertNotNull(parent1);

		GridElement child1 = getGridElement("child to parent 1, with no childs");
		Assert.assertNotNull(child1);

		JMenuItem rightClickChild = MenuFactorySwing.getInstance().createRelateAround(child1, handler);
		
		doRecursive(rightClickChild);
		
		// Else the action is not invoked
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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
		
		// Everything should no longer be true
		handler.getController().undo();

		Assert.assertFalse("Parent should no longer have any childs", 
				handler
					.getRelationManager()
					.hasChild(parent1));
		
		Assert.assertFalse("Child have no parent", 
				handler
					.getRelationManager()
					.getParent(child1)
					.isPresent());
	}

	private GridElement getGridElement(String name) {
		GridElement parent1 = handler.getDrawPanel()
				.getGridElements()
				.stream()
				.filter(g ->
					g.getPanelAttributes()
						.contains(name)
						)
				.findFirst()
				.get();
		return parent1;
	}

	/**
	 * Used to invoke the Create parent command
	 * @param menuElement
	 */
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
