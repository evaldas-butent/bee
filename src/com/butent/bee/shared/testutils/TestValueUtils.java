package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.utils.ValueUtils;

/**
 * Tests {@link com.butent.bee.shared.utils.ValueUtils}.
 */
public class TestValueUtils {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetDouble() {
			
		assertEquals(0.0, ValueUtils.getDouble(null), 0);
		assertEquals(1.0, ValueUtils.getDouble(1), 0);
		assertEquals(1.0, ValueUtils.getDouble(1L), 0);
		assertEquals(1.0, ValueUtils.getDouble((short)1), 0);
		assertEquals(1.0, ValueUtils.getDouble(true), 0);
		assertEquals(0.0, ValueUtils.getDouble(false), 0);
		assertEquals(1.0, ValueUtils.getDouble("1"), 0);
		assertEquals(53.0, ValueUtils.getDouble('5'), 0);
		assertEquals(99.0, ValueUtils.getDouble('c'), 0);
		assertEquals(122.0, ValueUtils.getDouble('z'), 0);
		
		TransObject obj = new TransObject();
		TransObjectLong objLong = new TransObjectLong();
		TransObjectInt objInt = new TransObjectInt();
		obj.setValue(5.0);
		objLong.setValue(2L);
		objInt.setValue(4);
		
		assertEquals(5.0, ValueUtils.getDouble(obj), 0);
		assertEquals(2.0, ValueUtils.getDouble(objLong), 0);
		assertEquals(4.0, ValueUtils.getDouble(objInt), 0);
		assertEquals(0.0, ValueUtils.getDouble(new Error()),0);
	}

	@Test
	public final void testGetInt() {
		assertEquals(0, ValueUtils.getInt(null));
		assertEquals(1, ValueUtils.getInt(1));
		assertEquals(1, ValueUtils.getInt(1L));
		assertEquals(1, ValueUtils.getInt((short)1));
		assertEquals(1, ValueUtils.getInt(true));
		assertEquals(0, ValueUtils.getInt(false));
		assertEquals(1, ValueUtils.getInt("1"));
		
		TransObject obj = new TransObject();
		TransObjectLong objLong = new TransObjectLong();
		TransObjectInt objInt = new TransObjectInt();
		obj.setValue(5.0);
		objLong.setValue(2L);
		objInt.setValue(4);
		
		assertEquals(0, ValueUtils.getInt(obj));
		assertEquals(0, ValueUtils.getInt(objLong));
		assertEquals(4, ValueUtils.getInt(objInt));
		assertEquals(0, ValueUtils.getInt(new Error()));
		
		assertEquals(53, ValueUtils.getInt(new Character('5')));
		assertEquals(53, ValueUtils.getInt('5'));
		assertEquals(99, ValueUtils.getInt('c'));
		assertEquals(122, ValueUtils.getInt('z'));
	}

	@Test
	public final void testGetLong() {
		assertEquals(0L, ValueUtils.getLong(null));
		assertEquals(1L, ValueUtils.getLong(1));
		assertEquals(1L, ValueUtils.getLong(1L));
		assertEquals(1L, ValueUtils.getLong((short)1));
		assertEquals(1L, ValueUtils.getLong(true));
		assertEquals(0L, ValueUtils.getLong(false));
		assertEquals(1L, ValueUtils.getLong("1"));
		
		
		TransObject obj = new TransObject();
		TransObjectLong objLong = new TransObjectLong();
		TransObjectInt objInt = new TransObjectInt();
		obj.setValue(5.0);
		objLong.setValue(2L);
		objInt.setValue(4);
		
		assertEquals(0L, ValueUtils.getLong(obj));
		assertEquals(2L, ValueUtils.getLong(objLong));
		assertEquals(4L, ValueUtils.getLong(objInt));
		assertEquals(0L, ValueUtils.getLong(new Error()));
		
		assertEquals(53L, ValueUtils.getLong('5'));
		assertEquals(99L, ValueUtils.getLong('c'));
		assertEquals(122L, ValueUtils.getLong('z'));
	}

	@Test
	public final void testSetDouble() {	
		
		assertEquals(null, ValueUtils.setDouble(null, 5.0));
		assertEquals(5.0, ValueUtils.setDouble(2, 5.0));
		assertEquals("15.0", ValueUtils.setDouble("12", 15.0));
		assertEquals(false, ValueUtils.setDouble(false, 1.0));
		assertEquals(false, ValueUtils.setDouble(true, 1.0));
		
		
		TransObject obj = new TransObject();
		TransObjectLong objLong = new TransObjectLong();
		TransObjectInt objInt = new TransObjectInt();
		obj.setValue(5.0);
		objLong.setValue(2L);
		objInt.setValue(4);
		
		Error err = new Error();
		
		assertEquals(obj, ValueUtils.setDouble(obj, 3.0));
		assertEquals(objLong, ValueUtils.setDouble(objLong, 4.0));
		assertEquals(objInt, ValueUtils.setDouble(objInt, 5.0));
		assertEquals(err, ValueUtils.setDouble(err, 0.0));
		
		assertEquals("8.0", ValueUtils.setDouble("dasdas", 8.0));
		assertEquals('<', ValueUtils.setDouble('5', 60));
		assertEquals('=', ValueUtils.setDouble('c', 61));
		assertEquals('>', ValueUtils.setDouble('z', 62));
		
	}

	@Test
	public final void testSetInt() {
		
		assertEquals(null, ValueUtils.setInt(null, 5));
		assertEquals(5, ValueUtils.setInt(2, 5));
		assertEquals("15", ValueUtils.setInt("12", 15));
		assertEquals(true, ValueUtils.setInt(false, 1));
		assertEquals(true, ValueUtils.setInt(true, 1));
		
		
		TransObject obj = new TransObject();
		TransObjectLong objLong = new TransObjectLong();
		TransObjectInt objInt = new TransObjectInt();
		obj.setValue(5.0);
		objLong.setValue(2L);
		objInt.setValue(4);
		
		Error err = new Error();
		
		assertEquals(obj, ValueUtils.setInt(obj, 3));
		assertEquals(objLong, ValueUtils.setInt(objLong, 4));
		assertEquals(objInt, ValueUtils.setInt(objInt, 5));
		assertEquals(err, ValueUtils.setInt(err, 0));
		
		assertEquals("8", ValueUtils.setInt("dasdas", 8));
		assertEquals('<', ValueUtils.setInt('5', 60));
		assertEquals('=', ValueUtils.setInt('c', 61));
		assertEquals('>', ValueUtils.setInt('z', 62));
	}

	@Test
	public final void testSetLong() {
		assertEquals(null, ValueUtils.setLong(null, 5L));
		assertEquals(5L, ValueUtils.setLong(2, 5L));
		assertEquals("15", ValueUtils.setLong("12", 15L));
		assertEquals(true, ValueUtils.setLong(false, 1L));
		assertEquals(false, ValueUtils.setLong(true, 2L));
		
		
		TransObject obj = new TransObject();
		TransObjectLong objLong = new TransObjectLong();
		TransObjectInt objInt = new TransObjectInt();
		obj.setValue(5.0);
		objLong.setValue(2L);
		objInt.setValue(4);
		
		Error err = new Error();
		
		assertEquals(obj, ValueUtils.setLong(obj, 3));
		assertEquals(objLong, ValueUtils.setLong(objLong, 4));
		assertEquals(objInt, ValueUtils.setLong(objInt, 5));
		assertEquals(err, ValueUtils.setLong(err, 0));
		
		assertEquals("8", ValueUtils.setLong("dasdas", 8));
		assertEquals('<', ValueUtils.setLong('5', 60));
		assertEquals('=', ValueUtils.setLong('c', 61));
		assertEquals('>', ValueUtils.setLong('z', 62));
	}

}
