package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.ArraySequence;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

/**
 * Tests {@link com.butent.bee.shared.ArraySequence}.
 */
public class TestArraySequence {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test
	public final void testArraySequence() {
		Integer in[] = { 9, 8, 7 };
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);

	}

	@SuppressWarnings("unused")
	@Test
	public final void testClear() {
		try {
			ArraySequence<Integer> as1 = new ArraySequence<Integer>(null);
			fail("Exceptios not works, the object must to be create with null parrameter");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException " + e.getMessage());
		}
		Integer in[] = { 9, 8, 7, 10, 25, 40, 55 };
		Integer in1[] = {};
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);

		ArraySequence<Integer> as2 = new ArraySequence<Integer>(in1);

		as2.clear();

		assertArrayEquals(in1, as2.getArray(null).getA());

		as.clear();
		assertArrayEquals(in, as.getArray(null).getA());
		assertEquals((Object) 0, as.getArray(null).getB());
	}

	@Test
	public final void testGet() {
		Integer in[] = { 9, 8, 7, 10, 25, 40, 55 };
		Integer in1[] = {};
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		ArraySequence<Integer> as2 = new ArraySequence<Integer>(in1);

		assertEquals((Object) 9, as.get(0));
		assertEquals((Object) 10, as.get(3));
		assertEquals((Object) 55, as.get(6));

		try {
			as.get(-1);
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail(" Need Bee Runtime exception : " + e.getMessage());
		}

		try {
			as.get(7);
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail(" Need Bee Runtime exception : " + e.getMessage());
		}

		try {
			as2.get(0);
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail(" Need Bee Runtime exception : " + e.getMessage());
		}

	}

	@Test
	public final void testGetArray() {
		Integer in[] = { 9, 8, 7 };
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);

		Integer ina[] = { 9, 8, 7 };
		Integer inb[] = { 1, 2, 3 };
		assertArrayEquals(ina, as.getArray(null).getA());

		assertArrayEquals(ina, as.getArray(inb).getA());

		in[1] = 10;
		assertEquals(in[1], (as.getArray(null)).getA()[1]);
	}

	@Test
	public final void testGetList() {
		Integer in[] = { 9, 8, 7 };
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);

		List<Integer> l = as.getList();

		Object ina[] = l.toArray();

		assertArrayEquals(in, ina);
	}

	@Test
	public final void testInsert() {

		Integer in[] = { 9, 8, 7 };
		Integer in1[] = {};
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		ArraySequence<Integer> as2 = new ArraySequence<Integer>(in1);

		try {
			as.insert(-1, 5);
			fail("Exceptions not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			as.insert(4, 5);
			fail("Exceptions not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		as.insert(1, 10);

		Integer aa[] = { 9, 10, 8, 7 };

		assertArrayEquals(aa, as.getArray(null).getA());

		as.insert(0, 6);

		Integer aa1[] = { 6, 9, 10, 8, 7 };

		assertArrayEquals(aa1, as.getArray(null).getA());

		Integer aa2[] = { 6, 9, 10, 8, 7, 5 };
		as.insert(5, 5);

		assertArrayEquals(aa2, as.getArray(null).getA());

		Integer aa3[] = { 6, 9, 10, 8, 7, 100, 5 };
		as.insert(5, 100);

		assertArrayEquals(aa3, as.getArray(null).getA());

		as2.insert(0, 1);
		Integer ab[] = { 1 };

		assertArrayEquals(ab, as2.getArray(null).getA());

	}

	@Test
	public final void testLength() {
		try {
			@SuppressWarnings("unused")
			ArraySequence<Integer> as1 = new ArraySequence<Integer>(null);
			fail("Exceptions not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRutimeException " + e.getMessage());
		}
		Integer in[] = { 9, 8, 7 };
		Integer in1[] = {};
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);

		ArraySequence<Integer> as2 = new ArraySequence<Integer>(in1);

		assertEquals(3, as.getLength());
		assertEquals(0, as2.getLength());

	}

	@Test
	public final void testRemove() {
		Integer in[] = { 9, 8, 7, 10, 25, 40, 55 };
		Integer in1[] = {};
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		ArraySequence<Integer> as2 = new ArraySequence<Integer>(in1);

		try {
			as.remove(-1);
			fail("Exceptions not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			as.remove(7);
			fail("Exceptions not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			as2.remove(0);
			fail("Exceptins not works !");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		as.remove(1);

		Integer aa[] = { 9, 7, 10, 25, 40, 55 };

		assertEquals((Object) 6, as.getArray(null).getB());
		
		try {
			assertArrayEquals(aa, as.getArray(null).getA());
		} catch (AssertionError e) {
			Integer asa[] = as.getArray(null).getA();
			for (int i = 0; i < as.getArray(null).getB(); i++) {
				assertEquals(aa[i], asa[i]);
			}
		}

		as.remove(0);

		Integer aa1[] = { 7, 10, 25, 40, 55 };

		try {
			assertArrayEquals(aa1, as.getArray(null).getA());
		} catch (AssertionError e) {
			Integer aar[] = as.getArray(null).getA();

			for (int i = 0; i < as.getArray(null).getB(); i++) {
				assertEquals(aa1[i], aar[i]);
			}
		}

		Integer aa2[] = { 7, 10, 25, 55 };
		as.remove(3);

		try {
			assertArrayEquals(aa2, as.getArray(null).getA());
		} catch (AssertionError e) {
			Integer arr2[] = as.getArray(null).getA();

			for (int i = 0; i < as.getArray(null).getB(); i++) {
				assertEquals(aa2[i], arr2[i]);
			}
		}

		Integer aa3[] = { 7, 10, 25 };
		as.remove(3);

		try {
			assertArrayEquals(aa3, as.getArray(null).getA());
		} catch (AssertionError e) {
			Integer arr3[] = as.getArray(null).getA();

			for (int i = 0; i < as.getArray(null).getB(); i++) {
				assertEquals(aa3[i], arr3[i]);
			}
		}

	}

	@Test
	public final void testSet() {
		Integer in[] = { 9, 8, 7, 10, 25, 40, 55 };
		Integer in1[] = {};
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		ArraySequence<Integer> as2 = new ArraySequence<Integer>(in1);

		Integer aa[] = { 9, -8, 7, 10, 25, 40, 55 };
		as.set(1, -8);
		assertArrayEquals(aa, as.getArray(null).getA());

		Integer aa1[] = { -99, -8, 7, 10, 25, 40, 55 };
		as.set(0, -99);
		assertArrayEquals(aa1, as.getArray(null).getA());

		Integer aa2[] = { -99, -8, 7, 10, 25, 40, -200 };
		as.set(6, -200);
		assertArrayEquals(aa2, as.getArray(null).getA());

		try
		{
			as.set(-1, 100);
			fail("Exceptions not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException :" + e.getMessage());
		}

		try 
		{
			as.set(7, 100);
			fail("Exceptions not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException :" + e.getMessage());
		}

		try
		{
			as2.set(0, 100);
			fail("Exceptions not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException :" + e.getMessage());
		}
	}

	@Test
	public final void testSetValuesListOfT() {
		Integer in[] = {};
		Integer in1[] = { 9, 8, 7, 10, 25, 40, 55 };
		Integer in2[] = { 1, 2, 3, 4, 55 };
		List<Integer> l = new ArrayList<Integer>();
		l.add(9);
		l.add(8);
		l.add(7);
		l.add(10);
		l.add(25);
		l.add(40);
		l.add(55);

		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		as.setValues(l);
		assertArrayEquals(in1, as.getArray(null).getA());

		as = new ArraySequence<Integer>(in2);
		as.setValues(l);
		assertArrayEquals(in1, as.getArray(null).getA());

		List<Integer> l1 = new ArrayList<Integer>();
		as = new ArraySequence<Integer>(in2);
		as.setValues(l);

		assertArrayEquals(in1, as.getArray(null).getA());
		l1 = new ArrayList<Integer>();
		l1.add(3);
		l1.add(4);
		as.setValues(l1);
		Integer in3[] = { 3, 4 };
		assertEquals(in3[0], as.getArray(null).getA()[0]);
		assertEquals(in3[1], as.getArray(null).getA()[1]);

		try {
			as.setValues((ArrayList<Integer>) null);
			fail("Exceptions not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException :" + e.getMessage());
		}

		try
		{
			as = new ArraySequence<Integer>(null);
			as.setValues(l);
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

	}

	@Test
	public final void testAdd() {
		Integer in[] = { 9, 8, 7, 10, 25, 40, 55 };
		Integer in1[] = {};
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		ArraySequence<Integer> as2 = new ArraySequence<Integer>(in1);

		Integer aa[] = { 9, 8, 7, 10, 25, 40, 55, 10 };
		as.add(10);
		assertArrayEquals(aa, as.getArray(null).getA());

		as2.add(5);
		Integer aa1[] = { 5 };
		assertArrayEquals(aa1, as2.getArray(null).getA());

	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public final void testAbstractSequenceHasNext() {
		Integer in[] = { 9, 8 };
		Integer in1[] = { 9 };
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		ArraySequence<Integer> as1 = new ArraySequence<Integer>(in1);

		Iterator iter = as.iterator();
		Iterator iter1 = as1.iterator();
		assertEquals(true, iter.hasNext());
		assertEquals(true, iter1.hasNext());
		iter1.next();
		assertEquals(false, iter1.hasNext());

		try {
			iter1.next();
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public final void testAbstractSequenceRemove() {
		Integer in[] = { 9, 8 };
		Integer in1[] = { 8 };
		ArraySequence<Integer> as = new ArraySequence<Integer>(in);
		ArraySequence<Integer> as1 = new ArraySequence<Integer>(in1);

		Iterator iter = as.iterator();
		iter.next();
		iter.remove();
		assertEquals(as.get(0), as1.get(0));
	}
}
