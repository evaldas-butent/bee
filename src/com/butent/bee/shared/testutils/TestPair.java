package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.Pair;

public class TestPair {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public final void testGetA() {
		Pair p = new Pair("a", 1.2);
		assertEquals("a", p.getA());

		p = new Pair(null, 5);
		assertEquals(null, p.getA());

		p = new Pair('\0', 7);
		assertEquals('\0', p.getA());

		Pair<String, Double> g = new Pair<String, Double>("a", 1.2);
		assertEquals("a", g.getA());

		g = new Pair<String, Double>(null, (double) 5);
		assertEquals(null, g.getA());

		g = new Pair<String, Double>(String.valueOf('\0'), (double) 7);
		assertEquals("\0", g.getA());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public final void testGetB() {
		Pair p = new Pair(1.2, "a");
		assertEquals("a", p.getB());

		p = new Pair(5, null);
		assertEquals(null, p.getB());

		p = new Pair(7, '\0');
		assertEquals('\0', p.getB());

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public final void testToString() {
		Pair p = new Pair(1.2, "a");
		assertEquals("1.2, a", p.toString());

		p = new Pair(5, null);
		assertEquals("5", p.toString());

		p = new Pair(7, '\0');
		assertEquals("7, \0", p.toString());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public final void testTransform() {
		Pair p = new Pair("a", 1.2);
		assertEquals("a, 1.2", p.transform());

		p = new Pair(null, 5);
		assertEquals("5", p.transform());
	
		p = new Pair('\0', 7);
		assertEquals("\0, 7", p.transform());
	}

}
