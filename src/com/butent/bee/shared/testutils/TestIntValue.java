package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.IntValue;

public class TestIntValue {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public final void testIntValue() {
		IntValue iv = new IntValue(10);

		assertEquals(10, iv.getInt());

		iv.setValue(-98511);

		assertEquals(-98511, iv.getInt());
	}
}
