package com.baselet.control;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.baselet.control.enumerations.AlignHorizontal;
import com.baselet.control.enumerations.Direction;
import com.baselet.diagram.draw.geom.Dimension;
import com.baselet.diagram.draw.geom.DimensionDouble;
import com.baselet.diagram.draw.geom.Point;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.GridElement;
import com.baselet.element.sticking.StickableMap;
import com.baselet.element.sticking.StickingPolygon;
import com.baselet.elementnew.Component;
import com.baselet.elementnew.ElementId;
import com.baselet.gui.AutocompletionText;

public class TestRelateManager {
	
	private static Logger log = Logger.getLogger(TestRelateManager.class);

	@BeforeClass
	public static void setupBeforeClass() {
		Main.getInstance().initLogger();
	}
	
	public static class MockGridElement implements GridElement {

		@Override
		public String getPanelAttributes() {
			return null;
		}

		@Override
		public void setPanelAttributes(String panelAttributes) {
		}

		@Override
		public List<AutocompletionText> getAutocompletionList() {
			return null;
		}

		@Override
		public void setParent(GridElement parent) {
		}

		@Override
		public void addChild(GridElement child) {
		}

		@Override
		public void removeChild(GridElement child) {
		}

		@Override
		public void setRectangle(Rectangle bounds) {
		}

		@Override
		public Integer getGroup() {
			return null;
		}

		@Override
		public void setLocationDifference(int diffx, int diffy) {
		}

		@Override
		public String getAdditionalAttributes() {
			return null;
		}

		@Override
		public void setAdditionalAttributes(String additionalAttributes) {
		}

		@Override
		public void setLocation(int x, int y) {
		}

		@Override
		public void setSize(int width, int height) {
		}

		@Override
		public Set<Direction> getResizeArea(int x, int y) {
			return null;
		}

		@Override
		public StickingPolygon generateStickingBorder(Rectangle rect) {
			return null;
		}

		@Override
		public Rectangle getRectangle() {
			return null;
		}

		@Override
		public void repaint() {
		}

		@Override
		public void changeSize(int diffx, int diffy) {
		}

		@Override
		public Dimension getRealSize() {
			return null;
		}

		@Override
		public boolean isInRange(Rectangle rectangle) {
			return false;
		}

		@Override
		public Component getComponent() {
			return null;
		}

		@Override
		public void setProperty(String key, Object newValue) {
		}

		@Override
		public void updateModelFromText() {
		}

		@Override
		public String getSetting(String key) {
			return null;
		}

		@Override
		public Integer getLayer() {
			return null;
		}

		@Override
		public void handleAutoresize(DimensionDouble necessaryElementDimension, AlignHorizontal alignHorizontal) {
		}

		@Override
		public ElementId getId() {
			return null;
		}

		@Override
		public void drag(Collection<Direction> resizeDirection, int diffX, int diffY, Point mousePosBeforeDrag, boolean isShiftKeyDown, boolean firstDrag, StickableMap stickables, boolean undoable) {
		}

		@Override
		public boolean isSelectableOn(Point point) {
			return false;
		}

		@Override
		public void dragEnd() {
		}

		@Override
		public List<String> getPanelAttributesAsList() {
			return null;
		}

		@Override
		public void setRectangleDifference(int diffx, int diffy, int diffw, int diffh, boolean firstDrag, StickableMap stickables, boolean undoable) {
		}

		@Override
		public void undoDrag() {
		}

		@Override
		public void redoDrag() {
		}

		@Override
		public void mergeUndoDrag() {
		}

		@Override
		public void setRelateSettings(String json) {
		}

		@Override
		public String getRelateSettings() {
			return null;
		}
		
	}
	
	@SuppressWarnings({ "unused" })
	@Test
	public void testConstructorRelateManager() {
		try{
			new RelateManager(new ArrayList<GridElement>());
			new RelateManager(Arrays.asList(new MockGridElement(), new MockGridElement()));
		} catch (Exception e) {
			log.error("testConstructorRelateManager", e);
			fail("Threw exception on constructor");
		}
	}

	@Test
	public void testAddPair() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		MockGridElement mge1 = new MockGridElement();
		MockGridElement mge2 = new MockGridElement();

		assertFalse(rm.hasParent(mge2));
		assertFalse(rm.hasChild(mge1));
		
		rm.addPair(mge1, mge2);
		
		assertTrue(rm.hasParent(mge2));
		assertTrue(rm.hasChild(mge1));
	}

	@Test
	public void testGetElementById() {
		// The generator is determinicstic always starting at the same number
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		MockGridElement mge1 = new MockGridElement();
		long id1 = rm.getIdByElement(mge1);
		assertTrue(rm.getElementById(id1).isPresent());

		rm = new RelateManager(new ArrayList<GridElement>());
		assertFalse(rm.getElementById(id1).isPresent());
		
		rm = new RelateManager(Arrays.asList(mge1));
		assertEquals(id1, rm.getIdByElement(mge1), 0);
	}

	@Test
	public void testGetIdByElement() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		MockGridElement mge1 = new MockGridElement();
		MockGridElement mge2 = new MockGridElement();
		
		Long id1 = rm.getIdByElement(mge1);
		assertEquals(id1, rm.getIdByElement(mge1), 0);
		Long id2 = rm.getIdByElement(mge2);
		assertEquals(id2, rm.getIdByElement(mge2), 0);
		assertEquals(id1, rm.getIdByElement(mge1), 0);
	}

	@Test
	public void testRemoveChild() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		
		MockGridElement parent = new MockGridElement();
		MockGridElement child = new MockGridElement();
		
		assertFalse(rm.hasParent(child));
		assertFalse(rm.hasChild(parent));
		
		rm.addPair(parent, child);
		assertTrue(rm.hasParent(child));
		assertTrue(rm.hasChild(parent));
		
		rm.removeChild(child, parent);
		assertFalse(rm.hasParent(child));
		assertFalse(rm.hasChild(parent));
	}

	@Test
	public void testRemoveAllChilds() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		
		MockGridElement parent = new MockGridElement();
		
		MockGridElement child1 = new MockGridElement();
		MockGridElement child2 = new MockGridElement();
		MockGridElement child3 = new MockGridElement();
		
		assertFalse(rm.hasParent(child1));
		assertFalse(rm.hasParent(child2));
		assertFalse(rm.hasParent(child3));
		assertFalse(rm.hasChild(parent));
		
		rm.addPair(parent, child1);
		rm.addPair(parent, child2);
		rm.addPair(parent, child3);

		assertTrue(rm.hasParent(child1));
		assertTrue(rm.hasParent(child2));
		assertTrue(rm.hasParent(child3));
		assertTrue(rm.hasChild(parent));
		
		rm.removeAllChilds(parent);
		assertFalse(rm.hasParent(child1));
		assertFalse(rm.hasParent(child2));
		assertFalse(rm.hasParent(child3));
		assertFalse(rm.hasChild(parent));
	}

	@Test
	public void testGetChildren() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		
		MockGridElement parent = new MockGridElement();
		
		MockGridElement child = new MockGridElement();

		assertFalse(rm.getChildren(parent).contains(child));

		rm.addPair(parent, child);
		
		assertTrue(rm.getChildren(parent).contains(child));
	}

	@Test
	public void testGetChildrenIds() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		
		MockGridElement parent = new MockGridElement();
		
		MockGridElement child = new MockGridElement();

		Long childId = rm.getIdByElement(child);
		Long parentId = rm.getIdByElement(parent);
		
		rm.addPair(parent, child);
		
		assertTrue(rm.getChildrenIds(parentId).contains(childId));
	}

	@Test
	public void testGetParentGridElement() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		
		MockGridElement parent = new MockGridElement();
		
		MockGridElement child = new MockGridElement();
		
		assertFalse(rm.getParent(child).isPresent());
		
		rm.addPair(parent, child);
		
		assertTrue(rm.getParent(child).isPresent());
		assertTrue(rm.getParent(child).orElse(new MockGridElement()).equals(parent));
	}

	@Test
	public void testGetParentLong() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		
		MockGridElement parent = new MockGridElement();
		
		MockGridElement child = new MockGridElement();

		Long childId = rm.getIdByElement(child);
		Long parentId = rm.getIdByElement(parent);
		
		assertFalse(rm.getParent(childId).isPresent());
		
		rm.addPair(parent, child);
		
		assertTrue(rm.getParent(childId).isPresent());
		assertTrue(rm.getParent(childId).orElse(-1l).equals(parentId));
	}

	@Test
	public void testGetJSON() {
		RelateManager rm = new RelateManager(new ArrayList<GridElement>());
		
		MockGridElement parent = new MockGridElement();
		
		MockGridElement child = new MockGridElement();
		
		Long parentId = rm.getIdByElement(parent);
		Long childId = rm.getIdByElement(child);
		
		assertEquals("{\"id\":" + childId + ",\"children\":[],\"parent\":null}", rm.getJSON(child));
		assertEquals("{\"id\":" + parentId + ",\"children\":[],\"parent\":null}", rm.getJSON(parent));
		
		rm.addPair(parent, child);

		assertEquals("{\"id\":" + childId + ",\"children\":[],\"parent\":" + parentId + "}", rm.getJSON(child));
		assertEquals("{\"id\":" + parentId + ",\"children\":[" + childId + "],\"parent\":null}", rm.getJSON(parent));
	}

}
