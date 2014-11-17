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
		String testString2 = "title:test \n_alpha:A~id1_|_beta:B~id2_\nid1->>id2:id1,id2: testtesttest";
		String testString3 = "title:test \n_alpha:A~id1_|_beta:B~id2_\nid1->>id2:id1,id2: testtesttesttest";
		String testString4 = "title:test \n_alpha:A~id1_|_beta:B~id2_|_delta:d~id3_\nid1->>id3:id1,id3: testtesttesttesttesttest";
		String testString5 = "title:test \n_alpha:A~id1_|_beta:B~id2_|_delta:d~id3_\nid1->>id3:id1,id3: testtesttesttesttesttesttest";

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

		// testing that messages going over a column has more space
		// and that the layout takes that into account:
		sq.setPanelAttributes(testString4);
		sq.paintEntity(g2);
		changedSize = sq.rectWidth;
		Assert.assertTrue("Layout has changed!", startSize == changedSize);

		// testing that messages going over a column has more space
		// and that the layout takes that into account and resizes the component:
		sq.setPanelAttributes(testString5);
		sq.paintEntity(g2);
		changedSize = sq.rectWidth;
		Assert.assertTrue("Layout has not changed!", startSize < changedSize);

		// testing that removing a message changes the layout back to the rectangle defined lay
		sq.setPanelAttributes(testString1);
		sq.paintEntity(g2);
		changedSize = sq.rectWidth;
		Assert.assertTrue("Layout has not changed!", startSize == changedSize);

	}
}
