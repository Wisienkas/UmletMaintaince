package com.umlet.element;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

import com.baselet.control.Main;
import com.baselet.diagram.DiagramHandler;

public class SequenceDiagramTest {

	@Test
	public void resizeSequenceDiagramFromArrowMessage() {

		SequenceDiagram sq = new SequenceDiagram();

		String testString1 = "title:test \n_alpha:A~id1_|_beta:B~id2_\nid1->>id2:id1,id2";
		String testString2 = "title:test \n_alpha:A~id1_|_beta:B~id2_\nid1->>id2:id1,id2: test";
		String testString3 = "title:test \n_alpha:A~id1_|_beta:B~id2_\nid1->>id2:id1,id2: testtesttesttesttesttettesttesttesttesttesttet";

		Main.setHandlerForElement(sq, new DiagramHandler(null));
		BufferedImage buffer = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = buffer.createGraphics();

		// set sequence diagram to the first test component:
		sq.setPanelAttributes(testString1);
		sq.paintEntity(g2);

		int startSize = sq.rectWidth;
		int changedSize;
		// alter sequence diagram, by adding arrow message
		// this message should be so short that the layout should not change!
		sq.setPanelAttributes(testString2);
		sq.paintEntity(g2);
		changedSize = sq.rectWidth;

		Assert.assertTrue("Layout has changed!", startSize == changedSize);

		// this message should be so long that the layout is altered:
		sq.setPanelAttributes(testString3);
		sq.paintEntity(g2);
		changedSize = sq.rectWidth;

		Assert.assertTrue("Layout has not changed!", startSize < changedSize);

		// removing the message should set the size back to the initial size defined by the rectangles:
		sq.setPanelAttributes(testString1);
		sq.paintEntity(g2);
		changedSize = sq.rectWidth;
		Assert.assertTrue("Layout has not changed!", startSize == changedSize);

	}
}
