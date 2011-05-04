package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.utils.ArrayUtils;

/**
 * Tests   {@link com.butent.bee.shared.utils.ArrayUtils}.
 */
public class TestArrayUtils {

	private static int[] intMas1 = {5, 5, 7, -10, 3};
	private static int[] intMas2 = {};
	private static String strMas1[];
	private static double[] doubleMas1 = {1.2, 1.5, 3.14, 2.78};
	private static String[] strMas2 = {};
	private static String[] strMas3 = {"this", "is", "a", null};
	private static CharSequence[] crseMas1={"this", "is", "a", "simple", "text"} ;
	private static boolean[] boolMas1 = {true, true, false, false, true};
	private static char[] charMas1 = {'a', 'b', 'c' , 'd', 'e'};
	private static byte[] byteMas1 = {1 ,2, 3, 4, 5};
	private static short[] shortMas1 = {1 ,2, 3, 4, 5};
	private static long[] longMas1 = {1 ,2, 3, 4, 5};
	private static float[] floatMas1 = {(float)1.5 ,(float)2.4,(float) 3.3, (float)4.2,(float) 5.1};
	private BeeType[] beeMas1 = {BeeType.BLOB, BeeType.BOOLEAN, BeeType.ENUM};
	
	@Before
	public void setUp() throws Exception {
		strMas1 = new String[5];
		strMas1[0] = new String("this"); 
		strMas1[1] = new String("is"); 
		strMas1[2] = new String("a"); 
		strMas1[3] = new String("simple"); 
		strMas1[4] = new String("test"); 
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testContainsIntIntArray() {
		assertEquals(true, ArrayUtils.contains("this", strMas3));
		assertEquals(false, ArrayUtils.contains(-666, strMas3));
		assertEquals(false, ArrayUtils.contains("a", null));
		assertEquals(true, ArrayUtils.contains("a", strMas3));
	}

	@Test
	public void testContainsTTArray() {
		assertEquals(true, ArrayUtils.contains("this", strMas1));
		assertEquals(false, ArrayUtils.contains("nothing", strMas1));
		assertEquals(false, ArrayUtils.contains("infinity", strMas2));
		assertEquals(false, ArrayUtils.contains("", null));
	}

	@Test
	public void testContext() {
		assertEquals (false, ArrayUtils.context("is is", strMas1));
		assertEquals (true, ArrayUtils.context("this", strMas1));
		assertEquals (false, ArrayUtils.context("nothing", strMas1));
		assertEquals (false, ArrayUtils.context("", strMas1));
		assertEquals (false, ArrayUtils.context(null, strMas1));
		assertEquals (false, ArrayUtils.context(null, strMas3));	
	}

	@Test
	public void testCopyOf() {
		Object mas1[] = ArrayUtils.copyOf(strMas1, 3);
		Object mas2[] = ArrayUtils.copyOf(strMas1, 10);    
		int l1 = mas1.length;
		int l2 = mas2.length;
		assertEquals(3, l1 );
		assertEquals(10, l2);
	}

	@Test
	public void testCopyOfRange() {
		Object newstrMas1[] = ArrayUtils.copyOfRange(strMas1, 1, 3);
		assertEquals (2,newstrMas1.length );
			for (int i = 0; i < newstrMas1.length; i++)
			{
				assertEquals (newstrMas1[i].toString(), strMas1[1+i].toString());
			}
	}

	@Test
	public void testCreate() {
		assertEquals(5, ArrayUtils.create(5).length);
		assertEquals(0, ArrayUtils.create(0).length);

	}

	@Test
	public void testGet() {
		try{
			assertEquals (null, ArrayUtils.get(intMas2, 0));
			}catch (ArrayIndexOutOfBoundsException e){
				assertTrue(true);
			}	
			
		assertEquals ("simple", ArrayUtils.get(strMas1, 3));
		assertEquals (false, ArrayUtils.get(boolMas1, 3));
		assertEquals ('c', ArrayUtils.get(charMas1, 2));
		assertEquals ((byte)3, ArrayUtils.get(byteMas1, 2));
		assertEquals ((short)2, ArrayUtils.get(shortMas1, 1));
		assertEquals ((long)1, ArrayUtils.get(longMas1, 0));
		assertEquals (5, ArrayUtils.get(intMas1, 0));	
		assertEquals ((float)5.1, ArrayUtils.get(floatMas1, 4));
		assertEquals ((double)2.78, ArrayUtils.get(doubleMas1, 3));
		assertEquals (null, ArrayUtils.get(null, 5));	
	}

	@Test
	public void testGetQuietly() {
		assertEquals ("simple", ArrayUtils.getQuietly(strMas1, 3));
		assertEquals (BeeType.BLOB, ArrayUtils.getQuietly(beeMas1, 0));
		assertEquals (null, ArrayUtils.getQuietly(null, 5));
	}

	@Test
	public void testIndexOfIntIntArray() {
		assertEquals(-1, ArrayUtils.indexOf(Long.valueOf(5), strMas3));
		assertEquals(0, ArrayUtils.indexOf("this", strMas3));
		assertEquals(-1, ArrayUtils.indexOf(-1, null));
		assertEquals(-1, ArrayUtils.indexOf(-1000, strMas3));
		assertEquals(2, ArrayUtils.indexOf("a", strMas3));
	}

	@Test
	public void testIndexOfTTArray() {
		assertEquals(-1, ArrayUtils.indexOf("This", strMas1));
		assertEquals(0, ArrayUtils.indexOf("this", strMas1));
		assertEquals(2, ArrayUtils.indexOf("a", strMas1));
		assertEquals(-1, ArrayUtils.indexOf(-1, null));
		assertEquals(-1, ArrayUtils.indexOf("text", strMas1));
		assertEquals(-1, ArrayUtils.indexOf("tester", strMas2));
		assertEquals(-1, ArrayUtils.indexOf("This", crseMas1));
		assertEquals(0, ArrayUtils.indexOf("this", crseMas1));
		assertEquals(2, ArrayUtils.indexOf("a", crseMas1));
		assertEquals(-1, ArrayUtils.indexOf(-1, null));
		assertEquals(-1, ArrayUtils.indexOf("test", crseMas1));
	}

	@Test
	public void testInsert() {
		Object mas1 [] = ArrayUtils.insert(strMas1, 2, "not" );
		
		int j = 0;
		for (int i = 0; i < mas1.length; i++)
		{
			if (i == 2)
			{
				assertEquals ("not", mas1[i].toString());
				j--;
			}
			else
			{
				assertEquals(strMas1[j+i].toString(), mas1[i].toString());
			}
		}
		assertEquals (strMas1.length+1, mas1.length);
	}

	@Test
	public void testIsArray() {
		assertEquals(true, ArrayUtils.isArray(intMas1));
		assertEquals(true, ArrayUtils.isArray(intMas2));
		assertEquals(true, ArrayUtils.isArray(strMas1));
		assertEquals(false, ArrayUtils.isArray(null));
		assertEquals(false, ArrayUtils.isArray(1));
	}

	@Test
	public void testIsIndex() {		
		assertEquals(false, ArrayUtils.isIndex(intMas1, -5));
		assertEquals(true, ArrayUtils.isIndex(intMas1, 0));
		assertEquals(true, ArrayUtils.isIndex(intMas1, 4));
		assertEquals(false, ArrayUtils.isIndex(intMas1, 20));
		assertEquals(false, ArrayUtils.isIndex(intMas2, 2));
		assertEquals(false, ArrayUtils.isIndex(null, 20));
	}

	@Test
	public void testIsPrimitiveArray() {
		assertEquals(true, ArrayUtils.isPrimitiveArray(intMas1));
		assertEquals(true, ArrayUtils.isPrimitiveArray(charMas1));
		assertEquals(false, ArrayUtils.isPrimitiveArray(strMas1));
		assertEquals(false, ArrayUtils.isPrimitiveArray(null));
		assertEquals(false, ArrayUtils.isPrimitiveArray(1));
	}

	@Test
	public void testJoinObjectArrayObject() {
		assertEquals("this;is;a;simple;test", ArrayUtils.join(strMas1,";"));
		assertEquals("this     is     a     simple     test", ArrayUtils.join(strMas1,5));
		assertEquals("thisAAisAAaAA", ArrayUtils.join(strMas3,"AA"));
	}

	@Test
	public void testJoinObjectArrayObjectInt() {
		assertEquals("a;simple;test", ArrayUtils.join(strMas1,";",2));
		assertEquals("", ArrayUtils.join(strMas1,5,13));
		assertEquals("thisAAisAAaAA", ArrayUtils.join(strMas3,"AA",-5));
		assertEquals("", ArrayUtils.join(strMas3,"AA",50));
	}

	

	@Test
	public void testLength() {
		assertEquals (5, ArrayUtils.length(strMas1));
		assertEquals (5, ArrayUtils.length(boolMas1));
		assertEquals (5, ArrayUtils.length(charMas1));
		assertEquals (5, ArrayUtils.length(byteMas1));
		assertEquals (5, ArrayUtils.length(shortMas1));
		assertEquals (5, ArrayUtils.length(longMas1));
		assertEquals (5, ArrayUtils.length(intMas1));
		assertEquals (0, ArrayUtils.length(intMas2));
		assertEquals (5, ArrayUtils.length(floatMas1));
		assertEquals (4, ArrayUtils.length(doubleMas1));
		assertEquals (0, ArrayUtils.length(null));
	}

	@Test
	public void testRemove() {
		Object mas1 [] = ArrayUtils.remove(strMas1, 2);
		
		int j = 0;
		for (int i = 0; i < mas1.length; i++)
		{
			if (i == 2)
			{
				j++;
			}
			else
			{
				assertEquals(strMas1[j+i].toString(), mas1[i].toString());
			}
		}
		assertEquals (strMas1.length-1, mas1.length);
		
		
		Object mas4 [] = ArrayUtils.remove(strMas1, 0); 
		
		assertEquals (strMas1.length-1, mas4.length);
		assertEquals (strMas1[1].toString(), mas4[0].toString());
	}

	@Test
	public void testTransform() {
		Object[] testMas1 = {intMas1, strMas1, boolMas1};
		Object[] testMas2 = {intMas1, testMas1, boolMas1};
		Object[] testMas3 = {};
		
		assertEquals("5,5,7,-10,3;5  5  7  -10  3,this  is  a  simple  test,true  true  false  false  true;true,true,false,false,true", ArrayUtils.transform(testMas2, ";", ",", 2));
		assertEquals("5;5;7;-10;3;5;5;7;-10;3;this;is;a;simple;test;true;true;false;false;true;true;true;false;false;true", ArrayUtils.transform(testMas2, ";"));
		assertEquals("", ArrayUtils.transform(testMas3, ";"));
		assertEquals("5, 5, 7, -10, 3, 5, 5, 7, -10, 3, this, is, a, simple, test, true, true, false, false, true, true, true, false, false, true", ArrayUtils.transform(testMas2));
	}


	@Test
	//public static String[] slice(String[] source, int start) {
	public void testSliceStringArrInt()
	{
		String [] arr1 = {"This", "is", "jUnit", "test"};
		String [] earr1 = {"is", "jUnit", "test"};		
		
		String [] aarr1 = ArrayUtils.slice(arr1, 1);	
		String [] arr2 = {null, null, null, null};
		String [] earr2 = {null, null, null};
		
		assertArrayEquals (earr1, aarr1);
		
		assertArrayEquals(arr1, ArrayUtils.slice(arr1, 0));
		
		assertArrayEquals(earr2, ArrayUtils.slice(arr2, 1));
		
		try
		{
			ArrayUtils.slice(null, 0);
			
			assertNull(ArrayUtils.slice(null, 0));
		}
		catch (BeeRuntimeException e)
		{
			assertTrue (true);
		}
		catch (Exception e)
		{
			fail ("Java runtime error. NeedBeeRuntime Exception " + e.getMessage());
		}
		
			String earr3[]  = {"test"};
			assertArrayEquals (earr3, ArrayUtils.slice(arr1, -1));
		
		
		try
		{
			String nullarr[] = {};
			ArrayUtils.slice(arr1, 5);
			assertArrayEquals(nullarr , ArrayUtils.slice(arr1, 5));
		}
		catch(BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch(Exception e)
		{
			fail ("Java lang exception. Need BeeRuntimeException " + e.getMessage());
		}
		
		try
		{
			ArrayUtils.slice(arr1, -10);
			assertArrayEquals(arr1 , ArrayUtils.slice(arr1, -10));
		}
		catch(BeeRuntimeException e)
		{
			assertTrue(true);
		}
		catch(Exception e)
		{
			fail ("Java lang exception. Need BeeRuntimeException " + e.getMessage());
		}


	}
	
	@Test
	public void testSlice ()
	{
		String [] arr1 = {"This", "is", "a", "jUnit", "test"};
		String [] arr2 = {"is", "a" , "jUnit", "test"};
		String [] arr3 = {"This","is", "a", "jUnit"};
		String [] arr4 = {"is"};
		String [] nullArr = {};
		String [] arr5 = {"This"};
		
		assertArrayEquals(arr1, ArrayUtils.slice(arr1, 0, 5));
		assertArrayEquals(nullArr, ArrayUtils.slice(arr1, 0, -5));
		assertArrayEquals(nullArr, ArrayUtils.slice(arr1, -10, -20));
		assertArrayEquals(nullArr, ArrayUtils.slice(arr1, 10, 20));
		assertNull(ArrayUtils.slice(null, 0, 0));
		assertArrayEquals(arr2, ArrayUtils.slice(arr1, 1, 5));
		assertArrayEquals (arr3, ArrayUtils.slice(arr1, -5, -1));
		assertArrayEquals (nullArr, ArrayUtils.slice(arr1, 1, 1));
		assertArrayEquals (arr4, ArrayUtils.slice(arr1, 1, 2));
		assertArrayEquals (arr5, ArrayUtils.slice(arr1, 0, 1));
		
		assertArrayEquals(nullArr, ArrayUtils.slice(nullArr, 0, 10));
		assertArrayEquals(arr1, ArrayUtils.slice(arr1, 0, 100));
		
		assertArrayEquals(nullArr, ArrayUtils.slice(nullArr, -10, 10));
	}
}
