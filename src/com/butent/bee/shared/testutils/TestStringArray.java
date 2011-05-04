package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.butent.bee.shared.StringArray;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

public class TestStringArray {

	private StringArray SA;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public final void testStringArrayStringArray() {
		try
		{
			SA = new StringArray(null);
			fail ("Exceptions not works");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch (Exception e)
		{
			fail ("Java exception, needBeeRuntimeException " + e.getMessage());
		}
		
		String a [] = {"one", "two", "three"};
		
		SA = new StringArray(a);
		
		assertArrayEquals (a, SA.getArray(null).getA());	
	}
}
