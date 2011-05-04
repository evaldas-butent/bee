package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.common.collect.Sets;
@SuppressWarnings("deprecation")
@RunWith (value=Parameterized.class)
public class TestBeeUtilsFilterType {

	private boolean expected;
	private Object value1;
	private Set<BeeType> value2;
	
	private static Set<BeeType> a  = Sets.newHashSet();
	private static Set<BeeType> a1 = Sets.newHashSet();
	private static Set<BeeType> a2 = Sets.newHashSet();

	@SuppressWarnings("unused")
	private static Set<BeeType> a3 = Sets.newHashSet();
	private static short b1  = 12;
	private static byte b2 = -124;	
	private static Date b3 = new Date();

	private static java.sql.Date b4 = new java.sql.Date(2011, 2, 9);
	private static java.lang.Object data = new Date();
	
	
	private BeeUtils beeUtils;
	
	@Parameters
	public static Collection<Object[]> getTestParameters()
	{
		return Arrays.asList(new Object[][]{
				{true, (Object)  10 , a},
				{true,  b1 , a1} ,
				{false, null, a1},
				{true,  b2, a1},
				{false, b3, a2}, // 4
				{false, b4, a2},
				{false, null, a},
				{false, true, a1},
				{true, 10, a},
				{false, 10.0, a},
				{false, (float)10e-10, a},
				{false , 'a', a},
				{false , BeeType.DATE, a}, 
				{false , data, a}
		});
	}
	
	
	
	public TestBeeUtilsFilterType(boolean expected, Object value1,
			Set<BeeType> value2) {
		super();
		this.expected = expected;
		this.value1 = value1;
		this.value2 = value2;
	}

	@SuppressWarnings("static-access")
	@Before
	public void setUp() throws Exception {
		BeeType b  =  null;
		a.add(b.INT);
		a1.add(b.NUMBER);
		a2.add(b.UNKNOWN);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("static-access")
	@Test
	public void testFilterType() {
		assertEquals(expected, beeUtils.filterType(value1, value2));
	}
}
