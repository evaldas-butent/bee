package com.butent.bee.shared.testutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.butent.bee.shared.utils.*;
import com.google.gwt.dev.util.collect.HashMap;

/**
 * Tests {@link com.butent.bee.shared.utils.BeeUtils#isEmpty(Object)}
 */
@SuppressWarnings("rawtypes")
@RunWith(value=Parameterized.class)
public class TestBeeUtilsisEmpty extends TestCase {

	private Object expected;
	private Object value;
	private static CharSequence testValue1 = "asdasd";
	private static Error testValue2 = new Error();
	private static String testValue3 = new String();

	private static ArrayList testValue4 = new ArrayList();
	private static ArrayList testValue5 = new ArrayList();
	private static Map<String, Integer> testValue6 = new HashMap<String, Integer>();
	private static Map<String, Integer> testValue7 = new HashMap<String, Integer>();
	private static Vector testValue8 = new Vector();
	private static Vector testValue9 = new Vector();
	private static ArrayList testValue10 = new ArrayList();
	private static int[] testValue11 = new int[5];
	private static char  mas[] = {'h', 'e', 'l', 'l', 'o'};  
	private static  String mas3 = "      \n\r";
	private static CharSequence mas1 = java.nio.CharBuffer.wrap(mas);
	private static CharSequence mas2 = "hello2";
	private static CharSequence mas4 = java.nio.CharBuffer.wrap(mas3);
	private static Enumeration en;
	
	@Parameters
	public static Collection <Object[]>getTestParameters()
	{
		return Arrays.asList(new Object[][]
		{
				{true, (Object) 0},
				{false, (Object) 1},
				{true, (Object) 0.0},
				{false, (Object) 0.001},
				{true, (Object) ""},
				{false, (Object) "     j     "},
				{false, (Object) "     1     "},
				{false, (Object) "     1   0  "},
				{true, (Object) "       "},
				{true, (Object) (-0.0)},
				{false, (Object) "@aceis"},
				{true, (Object) "\n \r \t"},
				{false,  (Object)testValue1},
				{false , (Object) testValue2},
				{true,  (Object) testValue3},
				{false , (Object) true},
				{true,  (Object) false},
				{true, (Object) testValue4},
				{false, (Object) testValue5},
				{true, (Object) testValue6},
				{false, (Object) testValue7},
				{true, (Object) testValue8},
				{false, (Object)testValue9},
				{true, (Object)testValue10},
				{false,testValue11},
				{false, mas1},
				{false, mas2},
				{true, null},
				{true, mas4},
				{true, en}	
		});
	}

	public TestBeeUtilsisEmpty(Object expected, Object value) {
		this.expected = expected;
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		testValue5.add('c');
		testValue7.put("String", 5);
		testValue9.add((Object)"String");
		testValue9.add(testValue11);
		en = testValue9.elements();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsEmptyObject() {
		assertEquals(expected, BeeUtils.isEmpty(value));
	}
}
