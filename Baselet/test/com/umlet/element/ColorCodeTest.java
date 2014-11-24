package com.umlet.element;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

import com.baselet.control.Main;
import com.baselet.diagram.DiagramHandler;


public class ColorCodeTest {

	@Test
	public void testColors(){
		
		Class testClass = new Class();
		Color postTestColor;
		//true
		String trueStringRed = "red+number:intred";
		String trueStringStaticOrange = "orange_+word:String_orange";
		String trueStringItalicBlue = "/blue+NOTHING:doubleblue/";
		String trueStringGrayYellow = "grayyellow-charArray:Char[]yellowgray";
		// false
		String falseStringRed = "red+number:intre";
		String falseStringGrayYellow = "grayyellow-charArray:Char[]grayyellow";
		
		Main.setHandlerForElement(testClass, new DiagramHandler(null));
		BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = bufferedImage.createGraphics();
	
		testClass.setPanelAttributes("placeholder");
		testClass.paintEntity(g2);
		// Doing test top-down starting with trueStringRed
		// Test case 1 - changing color from default black to red
		// assigning test string and repainting
		testClass.setPanelAttributes(trueStringRed);
		testClass.paintEntity(g2);
		// saving new color
		postTestColor = g2.getColor();
		
		Assert.assertTrue("Test case 1: Success, formatting has changed to red", postTestColor == Color.RED);
		
		// Test case 2 - Changing color to orange and while also assigning static
		// color > static ordering
		testClass.setPanelAttributes(trueStringStaticOrange);
		testClass.paintEntity(g2);
		postTestColor = g2.getColor();
		
		Assert.assertTrue("Test case 2: Success, formatting has changed to orange", postTestColor == Color.ORANGE);
		
		// Test case 3 - Same as case 2 with different format ordering
		// italic > color ordering
		testClass.setPanelAttributes(trueStringItalicBlue);
		testClass.paintEntity(g2);
		postTestColor = g2.getColor();
		
		Assert.assertTrue("Test case 3: Success, formatting has changed to blue", postTestColor == Color.BLUE);
		
		// Test case 4 - Assigning multiple colors, should result in a single color.
		testClass.setPanelAttributes(trueStringGrayYellow);
		testClass.paintEntity(g2);
		postTestColor = g2.getColor();
		
		Assert.assertTrue("Test case 4: Success, formatting has changed to yellow", postTestColor == Color.YELLOW);
		
		// Test case 5 - Missing single letter from color assignment
		testClass.setPanelAttributes(falseStringRed);
		testClass.paintEntity(g2);
		postTestColor = g2.getColor();
		
		Assert.assertFalse("Test case 5 - Success, formatting has not changed to red", postTestColor == Color.RED);
		
		// Test case 6 - Differing ordering of formatting
		testClass.setPanelAttributes(falseStringGrayYellow);
		testClass.paintEntity(g2);
		postTestColor = g2.getColor();
		
		Assert.assertFalse("Test case 6 - Success, formatting has not changed to gray or yellow", postTestColor == Color.YELLOW || postTestColor == Color.GRAY);
	}
}
