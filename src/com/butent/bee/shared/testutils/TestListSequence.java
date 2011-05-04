package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.ListSequence;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

/**
 * Tests {@link com.butent.bee.shared.ListSequence}.
 */
public class TestListSequence {

	private ListSequence<String> LS;
	private ListSequence<Object> LS1;

	@Before
	public void setUp() throws Exception {
		ArrayList<String> ar = new ArrayList<String>();
		ar.add("this");
		ar.add("the");
		ar.add("junit");
		ar.add("test");
		
		LS = new ListSequence<String>(ar);
		LS1 = new ListSequence<Object>(5);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testClear() {
		LS.clear();
		LS1.clear();
		
		assertEquals (0, LS.getLength());
		assertEquals (0, LS1.getLength());
	}

	@Test
	public final void testGet() {
		assertEquals ("junit", LS.get(2));
		assertEquals (null, LS1.get(2));
		
		assertEquals ("this", LS.get(0));
		assertEquals (null, LS1.get(0));
		
		assertEquals ("test", LS.get(3));
		assertEquals (null , LS1.get(4));
		
		try
		{
			LS.get(-1);
			fail ("Exceptions not work: " + LS.get(-1)); 
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need  BeeRuntimeException: " + e.getMessage());
		}
		
		try
		{
			LS.get(5);
			fail ("Exceptions not work: " + LS.get(-1));
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need  BeeRuntimeException: " + e.getMessage());
		}
		
		try
		{
			LS1.get(-1);
			fail ("Exceptions not work: " + LS.get(-1));
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need  BeeRuntimeException: " + e.getMessage());
		}
		
		try
		{
			LS1.get(5);
			fail ("Exceptions not work: " + LS.get(-1));
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need  BeeRuntimeException: " + e.getMessage());
		}
	}

	@Test
	public final void testGetArray() {
		String [] ex1 = {"this", "the", "junit", "test"};
		Object [] ex2 = {null, null, null, null, null};
		String [] pr2 = {};
		Object [] pr3 = {};
		Object [] re1 = LS.getArray(pr2).getA();
		Object [] re2 = LS1.getArray(pr3).getA();
		
		String [] pr1 = {"one", "two", "three", "four"};
	
		assertArrayEquals(ex1, re1);
		assertArrayEquals(ex2, re2);
		assertArrayEquals (ex2, LS1.getArray(pr1).getA());
	}

	@Test
	public final void testGetList() {
		ArrayList<String> ex1 = new ArrayList<String>();
		ArrayList<Object> ex2 = new ArrayList<Object>();
		
		ex1.add("this");
		ex1.add("the");
		ex1.add("junit");
		ex1.add("test");
		
		ex2.add(null);
		ex2.add(null);
		ex2.add(null);
		ex2.add(null);
		ex2.add(null);
		
		assertArrayEquals(ex1.toArray(), LS.getList().toArray());
		assertArrayEquals(ex2.toArray(), LS1.getList().toArray());
	}

	@Test
	public final void testInsert() {
		try
		{
			LS.insert(5, ".");
			fail("Exceptions not woks " + LS.getLength() );
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail("Need BeeRuntimeException :" + e.getMessage());
		}
		
		try
		{
			LS.insert(-1, ".");
			fail("Exceptions not woks");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail("Need BeeRuntimeException :" + e.getMessage());
		}
		
		
		
		try
		{
			LS1.insert(-1, 0);
			fail("Exceptions not woks");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail("Need BeeRuntimeException :" + e.getMessage());
		}
		
		try
		{
			LS1.insert(6, "5");
			fail("Exceptions not woks ");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail("Need BeeRuntimeException :" + e.getMessage());
		}
				
		String [] exp1a = {"this", "is", "the", "junit", "test"};
		String [] exp1b = {".", "this", "is", "the", "junit", "test"};
		String [] exp1c = {".", "this", "is", "the", "junit", "test", "!"};
		String [] pr = {};
		Object [] pr1 = {};
		Object [] exp2a = {null, 10, null, null, null, null};
		Object [] exp2b = {"the", null, 10, null, null, null, null};
		Object [] exp2c = {"the", null, 10, null, null, null, null, 1L};
		
		LS.insert(1, "is");
		assertArrayEquals (exp1a, LS.getArray(pr).getA());
		
		LS.insert(0, ".");
		assertArrayEquals(exp1b, LS.getArray(pr).getA());
		
		LS.insert(6, "!");
		assertArrayEquals(exp1c, LS.getArray(pr).getA());
		
		LS1.insert(1, 10);
		assertArrayEquals (exp2a, LS1.getArray(pr1).getA());
		
		LS1.insert(0, "the");
		assertArrayEquals(exp2b, LS1.getArray(pr1).getA());
		
		LS1.insert(7, 1L);
		assertArrayEquals(exp2c, LS1.getArray(pr1).getA());
				
	}

	@Test
	public final void testLength() {
		assertEquals (4, LS.getLength());
		assertEquals (5, LS1.getLength());
	}

	@SuppressWarnings("unused")
	@Test
	public final void testRemove() {
		
 		String [] exp1a = {"this", "junit", "test"};
 		String [] exp1b = {"junit", "test"};
 		String [] exp1c = {"junit"};
 		
 		String [] exp2a = {null , null, null , null , null};
 		String [] exp2b = {"junit", "test"};
 		String [] exp2c = {"junit"};
 		String [] pr = {};
 		
 		LS.remove(1);
 		assertArrayEquals (exp1a, LS.getArray(pr).getA());
 		
 		LS.remove(0);
 		assertArrayEquals (exp1b, LS.getArray(pr).getA()); 		
 		
 		LS.remove(1);
 		assertArrayEquals (exp1c, LS.getArray(pr).getA());
		
		try
		{
			LS.remove(-1);
			fail ("Exceptions not works");
		}
		catch(BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need bee runtime exception" + e.getMessage());
		}
		
		try
		{
			LS.remove(10);
			fail ("Exceptions not works");
		}
		catch(BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need bee runtime exception" + e.getMessage());
		}
		
		try
		{
			LS1.remove(-1);
			fail ("Exceptions not works");
		}
		catch(BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need bee runtime exception" + e.getMessage());
		}
		
		try
		{
			LS1.remove(10);
			fail ("Exceptions not works");
		}
		catch(BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need bee runtime exception" + e.getMessage());
		}
		
	}

	@Test
	public final void testSet() {
		
		String [] exp1a = {"this", "the", "junit", "test"};
		String [] exp1b = {"there", "the", "junit", "test"};
		String [] exp1c = {"there", "the", "junit", "mod"};
		String [] pr = {};
		Object [] pr1 = {};
		
		Object [] exp2a = {null, null, 5.3, null, null};
		Object [] exp2b = {"a", null, 5.3 , null, null};
		Object [] exp2c = {"a", null, 5.3 , null, ""};
		
		
		LS.set(1, "the");
		assertArrayEquals(exp1a, LS.getArray(pr).getA());
		
		LS.set(0, "there");
		assertArrayEquals(exp1b, LS.getArray(pr).getA());
		
		LS.set(3, "mod");
		assertArrayEquals (exp1c, LS.getArray(pr).getA());
		
		
		LS1.set(2, 5.3);
		assertArrayEquals(exp2a, LS1.getArray(pr1).getA());
		
		LS1.set(0, "a");
		assertArrayEquals(exp2b, LS1.getArray(pr1).getA());
		
		LS1.set(4, "");
		assertArrayEquals (exp2c, LS1.getArray(pr1).getA());
		
		try
		{
			LS.set(-1, ".");
			fail("Exceptiions not works");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need BeeRuntimeException :" + e.getMessage());
		}
		
		try
		{
			LS.set(10, ".");
			fail("Exceptiions not works");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need BeeRuntimeException :" + e.getMessage());
		}
		
		try
		{
			LS1.set(-1, ".");
			fail("Exceptiions not works");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need BeeRuntimeException :" + e.getMessage());
		}
		
		try
		{
			LS1.set(10, ".");
			fail("Exceptiions not works");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need BeeRuntimeException :" + e.getMessage());
		}
		
	}

	@Test
	public final void testSetValuesListOfT() {
		ArrayList<String> exp1 = new ArrayList<String>();
		ArrayList<Object> exp2 = new ArrayList<Object>();
		String [] pr = {};
		Object [] pr1 = {};
		exp1.add("one");
		exp1.add("two");
		exp1.add("tree");
		
		exp2.add(1);
		exp2.add(3.14);
		exp2.add ("");
		
		LS.setValues(exp1);
		LS1.setValues(exp2);
		
		assertArrayEquals(exp1.toArray(), LS.getArray(pr).getA());
		assertArrayEquals(exp2.toArray(), LS1.getArray(pr1).getA());
		
	}

	@Test
	public final void testSetValuesTArray() {
	 String [] exp1 = {"abc", "cde", "qwe"};
	 Object [] exp2 = {"", null, 1, 2, 3.14, 2L};
	 String [] pr = {};
	 Object [] pr1 = {}; 
	 
	 LS.setValues(exp1);
	 LS1.setValues(exp2);
	 
	 assertArrayEquals (exp1, LS.getArray(pr).getA());
	 assertArrayEquals (exp2, LS1.getArray(pr1).getA());
	 
	 exp1[0] = "123";
	 assertArrayEquals (exp1, LS.getArray(pr).getA());
	}

	@SuppressWarnings("unused")
	@Test
	public final void testListSequence()
	{
		try
		{
			ListSequence <Object> a = new ListSequence<Object>(null);
			fail ("Exceptions not work");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need BeeRuntimeException" + e.getMessage());
		}
		
		try
		{
			ListSequence <Object> a = new ListSequence<Object>(-1);
			fail ("Exceptions not work");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Need BeeRuntimeException" + e.getMessage());
		}
	}
}
