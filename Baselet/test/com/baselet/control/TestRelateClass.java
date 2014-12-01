package com.baselet.control;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.baselet.control.RelateManager.Relate;

public class TestRelateClass {
	
	@Before
	public void setup() {
		
	}
	
	@Test
	public void testConstructorRelateLong() {
		try{
			new Relate(15l);
			new Relate(0l);
			new Relate(1l);
			new Relate(-1l);
			new Relate(Long.MAX_VALUE);
			new Relate(Long.MIN_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown by constructor");
		}
	}
	
	@Test
	public void testConstructorRelateOther() {
		try{
			assertEquals("Should be id 0.", 
					0l, 
					new Relate(new Relate(0l)).id,
					0);
		} catch (Exception e){
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testAddChild() {
		Relate relate = new Relate(0l);
		assertEquals("Should be 0 in size",
				0, 
				relate.children.size(),
				0);
		relate.addChild(1l);
		assertEquals("Should be 1 in size",
				1, 
				relate.children.size(),
				0);
		assertTrue(relate.children.contains(1l));
		relate.addChild(0l);
		assertEquals("Should be 2 in size",
				2, 
				relate.getChildren().size(),
				0);
		assertTrue(relate.children.contains(0l));
		assertTrue(relate.children.contains(1l));
	}

	@Test
	public void testRemoveChild() {
		Relate relate = new Relate(0l);
		assertEquals("Should be 0 in size",
				0, 
				relate.children.size(),
				0);
		relate.addChild(6l);
		assertEquals("Should be 1 in size",
				1, 
				relate.children.size(),
				0);
		relate.removeChild(5l);
		assertEquals("Should be 1 in size",
				1, 
				relate.children.size(),
				0);
		relate.removeChild(6l);
		assertEquals("Should be 0 in size",
				0, 
				relate.children.size(),
				0);
	}
}
