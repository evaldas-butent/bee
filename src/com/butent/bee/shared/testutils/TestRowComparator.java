package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.utils.RowComparator;

/**
 * Tests {@link com.butent.bee.shared.utils.RowComparator}.
 */
public class TestRowComparator {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public final void testCompare() {
		
		RowComparator obj1 = new RowComparator();
		String[] strMas = {"this", "is", "a", "string"};
		String[] strMas2 = {"this", "is", "a", "betkas"};
		
		assertEquals(0, obj1.compare((Object[])strMas, (Object[])strMas2));
		
		RowComparator obj2 = new RowComparator(true);
		Integer[] intMas = {15,6,8,7,1,2};
		Integer[] intMas2 = {8,9,5,1,2,3,14};
		
		assertEquals(-7, obj2.compare((Object[])intMas, (Object[])intMas2));
		
		RowComparator obj3 = new RowComparator(5, true);
		Integer[] intMas3 = {15,6,8,7,1,2};
		Integer[] intMas4 = {8,9,5,1,2,3,14};
		
		assertEquals(-1, obj3.compare((Object[])intMas3, (Object[])intMas4));
		
	
		RowComparator obj4 = new RowComparator(2);
		Integer[] intMas41 = {15,6,8,7,1,2};
		Integer[] intMas42 = {8,9,5,1,2,3,14};
		
		assertEquals(3, obj4.compare((Object[])intMas41, (Object[])intMas42));
		
		RowComparator obj6 = new RowComparator(-2);
		Integer[] intMas61 = {15,6,8,7,1,2};
		Integer[] intMas62 = {8,9,5,1,2,3,14};
		
		assertEquals(-3, obj6.compare((Object[])intMas61, (Object[])intMas62));
		
		
		RowComparator obj5 = new RowComparator(5,true,BeeType.INT);
		Integer[] intMas51 = {15,6,8,7,1,2,5,5,5,5,5,5,5,5,5,5,5};
		Integer[] intMas52 = {8,9,5,1,2,2,14};
		
		assertEquals(0, obj5.compare((Object[])intMas51, (Object[])intMas52));
		
		RowComparator obj7 = new RowComparator(0,true,BeeType.STRING);
		String[] strMas71 = {"this", "is", "a", "string"};
		String[] strMas72 = {"this", "is", "a"};
		assertEquals(0, obj7.compare((Object[])strMas71, (Object[])strMas72));
		
		RowComparator obj8 = new RowComparator(1,BeeType.STRING);
		String[] strMas81 = {"this", "is", "a", "string"};
		String[] strMas82 = {"sda", "dsads"};
		assertEquals(true, obj8.compare((Object[])strMas81, (Object[])strMas82)>0);
		int[] a = {0,1,3};
		
		RowComparator obj9 = new RowComparator(a,BeeType.STRING);
		String[] strMas91 = {"this", "is", "a", "string"};
		String[] strMas92 = {"this", "is", "a", "Not string"};
		assertEquals(true, obj9.compare((Object[])strMas91, (Object[])strMas92)>0);
		
		int[] b = {0,1,3};
		RowComparator obj10 = new RowComparator(b);
		String[] strMas101 = {"this", "is", "a", "string"};
		String[] strMas102 = {"this", "is", "a", "Not string"};
		assertEquals(true, obj10.compare((Object[])strMas101, (Object[])strMas102)>0);
		
		int[] c = {0,1,3};
		RowComparator obj11 = new RowComparator(c, false);
		String[] strMas111 = {"this", "is", "a", "string"};
		String[] strMas112 = {"this", "is", "beetype", "Not string"};
		assertEquals(true, obj11.compare((Object[])strMas111, (Object[])strMas112)<0);
		
		
		int[] d = {0,1,3};
		BeeType[] d1 = {BeeType.BLOB, BeeType.ENUM};
		RowComparator obj12 = new RowComparator(d, true,d1);
		String[] strMas121 = {"this", "is", "a", "string"};
		String[] strMas122 = {"this", "is", "beetype", "Not string"};
		assertEquals(37, obj12.compare((Object[])strMas121, (Object[])strMas122));
		
		
		int[] e = {0,1,3};
		BeeType[] e1 = {BeeType.BLOB, BeeType.ENUM};
		boolean[] boolmas = {true, false, true};
		RowComparator obj13 = new RowComparator(e, boolmas,e1);
		Integer[] intMas131 = {15,6,8,7,1,2};
		Integer[] intMas132 = {8,9,5,1,2,3,14};
		assertEquals(true, obj13.compare((Object[])intMas131, (Object[])intMas132)<0);
		
		
		int[] f = {0,1,3};
		boolean[] boolmas2 = {true, false, true};
		RowComparator obj14 = new RowComparator(f, boolmas2,BeeType.INT);
		Integer[] intMas141 = {15,6,8,7,1,2};
		Integer[] intMas142 = {8,9,5,1,2,3,14};
		assertEquals(1, obj14.compare((Object[])intMas141, (Object[])intMas142));
		
		int[] g = {0,1,3};
	
		RowComparator obj15 = new RowComparator(g, e1);
		Integer[] intMas151 = {15,6,8,7,1,2};
		Integer[] intMas152 = {8,9,5,1,2,3,14};
		assertEquals(-7, obj15.compare((Object[])intMas151, (Object[])intMas152));
	
		
		
		int[] h = {0,-1,-3};
		BeeType[] h1 = {BeeType.BLOB, BeeType.INT};
		boolean[] boolmash = {true, false, true};
		RowComparator obj16 = new RowComparator(h, boolmash,h1);
		Integer[] intMas161 = {15,6,8,7,1,2};
		Integer[] intMas162 = {8,9,5,1,2,3,14};
		assertEquals(true, obj16.compare((Object[])intMas161, (Object[])intMas162)<0);	
	}
}
