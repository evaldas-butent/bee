package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.utils.Codec;

/**
 * Tests {@link com.butent.bee.shared.utils.Codec}
 */
public class TestCodec {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testAdler32ByteArray() {
		byte btExpected1[] = { 0, 97, 0, 98, 0, 99 };
		byte btExpected2[] = { 0, 113, 0, 119, 0, 101 };
		byte val[] = null;
		byte val1[] = {};
		byte btExpected4[] = { 97, 98, 99 };

		assertEquals(Integer.toHexString(38600999), Codec.adler32(btExpected4));
		assertEquals("3740127", Codec.adler32(btExpected1));
		assertEquals("405014e", Codec.adler32(btExpected2));

		try {
			assertEquals("1", Codec.adler32(val1));
			fail("BeeRuntimeException does not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}

		try {
			assertEquals("", Codec.adler32(val));
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!! "
					+ e.getMessage());
		}
	}

	@Test
	public final void testAdler32String() {
		assertEquals("20350398", Codec.adler32("Wikipedia"));
		assertEquals("3740127", Codec.adler32("abc"));
		assertEquals("405014e", Codec.adler32("qwe"));
	}

	@Test
	public final void testBeeDeserialize() {
		String a = "15hello12ab13abc";
		String b = "-15hello12ab13abc";
		String c = " 5     ";
		String d = "0";
		String exsp[] = { "hello", "ab", "abc" };
		String exsp3[] = { null };
		String exsp4[] = {};
		String g[] = Codec.beeDeserialize(a);

		for (int i = 0; i < g.length; i++) {
			System.out.println(g[i]);
		}

		assertArrayEquals(exsp, Codec.beeDeserialize(a));
		assertArrayEquals(exsp4, Codec.beeDeserialize(b));
		assertArrayEquals(exsp3, Codec.beeDeserialize(d));
		assertArrayEquals(exsp4, Codec.beeDeserialize(c));
	}

	@Test
	public final void testBeeSerialize() {
		String exsp1 = "15hello12ab13abc";
		String in1[] = { "hello", "ab", "abc" };
		int in2[] = { 1, 2, 10, 100 };

		assertEquals(exsp1, Codec.beeSerialize((Object) in1));
		assertEquals("0", Codec.beeSerialize((Object) null));
		assertEquals("111112121013100", Codec.beeSerialize((Object) in2));

		List<Object> testc3 = new ArrayList<Object>();
		List<Object> testc4 = new ArrayList<Object>();
		testc3.add("This");
		testc3.add("is");
		testc3.add("a");
		testc3.add("test");

		testc4.add(testc3);
		testc4.add("end line");

		assertEquals("21914This12is11a14test18end line",
				Codec.beeSerialize(testc4));
		Map<String, String> testc1 = new TreeMap<String, String>();
		Map<String, String> testc2 = new TreeMap<String, String>();
		Map<String, Object> maptest = new TreeMap<String, Object>();

		testc1.put("Name1", "Petras");
		testc1.put("Name2", "Jonas");
		testc1.put("Name3", "Juozas");
		testc1.put("Name4", "Petras2");

		testc2.put("Phone", "NOKLA");
		testc2.put("Fax", "Philips");

		assertEquals(
				"15Name116Petras15Name215Jonas15Name316Juozas15Name417Petras2",
				Codec.beeSerialize(testc1));

		maptest.put("No", "1");
		maptest.put("Map", testc2);

		assertEquals("13Map22813Fax17Philips15Phone15NOKLA12No111",
				Codec.beeSerialize(maptest));

		TransObjectInt intObj = new TransObjectInt();
		assertEquals("135.0", Codec.beeSerialize(intObj));

		TransObjectLong longObj = new TransObjectLong();
		assertEquals("Hello world", Codec.beeSerialize(longObj));

		Thread thr = new Thread("Fastest thread");
		assertEquals("229Thread[Fastest thread,5,main]",
				Codec.beeSerialize(thr));
	}

	@Test
	public final void testBeeSerializeAll() {
		String exsp1 = "21615hello12ab13abc";
		String exsp2 = "15hello12ab13abc";
		String in1[] = { "hello", "ab", "abc" };
		int in2[] = { 1, 2, 10, 100 };

		assertEquals(exsp1, Codec.beeSerializeAll((Object) in1));
		assertEquals(exsp2, Codec.beeSerializeAll("hello", "ab", "abc"));
		assertEquals("0", Codec.beeSerializeAll((Object) null));
		assertEquals("215111112121013100", Codec.beeSerializeAll((Object) in2));
		assertEquals("111112121013100", Codec.beeSerializeAll(1, 2, 10, 100));

		List<Object> testc3 = new ArrayList<Object>();
		List<Object> testc4 = new ArrayList<Object>();
		testc3.add("This");
		testc3.add("is");
		testc3.add("a");
		testc3.add("test");

		testc4.add(testc3);
		testc4.add("end line");

		assertEquals("23221914This12is11a14test18end line",
				Codec.beeSerializeAll(testc4));

		Map<String, String> testc1 = new TreeMap<String, String>();
		Map<String, String> testc2 = new TreeMap<String, String>();
		Map<String, Object> maptest = new TreeMap<String, Object>();

		testc1.put("Name1", "Petras");
		testc1.put("Name2", "Jonas");
		testc1.put("Name3", "Juozas");
		testc1.put("Name4", "Petras2");

		testc2.put("Phone", "NOKLA");
		testc2.put("Fax", "Philips");

		assertEquals(
				"26015Name116Petras15Name215Jonas15Name316Juozas15Name417Petras2",
				Codec.beeSerializeAll(testc1));

		maptest.put("No", "1");
		maptest.put("Map", testc2);

		assertEquals("24313Map22813Fax17Philips15Phone15NOKLA12No111",
				Codec.beeSerializeAll(maptest));

		TransObjectInt intObj = new TransObjectInt();
		assertEquals("135.0", Codec.beeSerializeAll(intObj));

		TransObjectLong longObj = new TransObjectLong();
		assertEquals("211Hello world", Codec.beeSerializeAll(longObj));

		Thread thr = new Thread("Fastest thread");
		assertEquals("229Thread[Fastest thread,5,main]",
				Codec.beeSerializeAll(thr));
	}

	@Test
	public final void testCrc16ByteArray() {
		byte btempty[] = null;
		byte btExpected1[] = { 0, 97, 0, 98, 0, 99 };
		byte btExpected2[] = { 0, 113, 0, 119, 0, 101 };

		assertEquals("3f5c", Codec.crc16(btExpected1));
		assertEquals("3a0c", Codec.crc16(btExpected2));

		try {
			assertEquals("0", Codec.crc16(btempty));
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testCrc16String() {

		assertEquals("3f5c", Codec.crc16("abc"));
		assertEquals("3a0c", Codec.crc16("qwe"));

		try {
			assertEquals("0", Codec.crc16(""));
			assertEquals("0", Codec.crc16((String) null));
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testCrc32ByteArray() {

		byte btempty[] = null;
		byte btExpected1[] = { 0, 97, 0, 98, 0, 99 };
		byte btExpected2[] = { 0, 113, 0, 119, 0, 101 };

		assertEquals("8a78d0f2", Codec.crc32(btExpected1));
		assertEquals("191683de", Codec.crc32(btExpected2));

		try {
			assertEquals("0", Codec.crc32(btempty));
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testCrc32String() {
		assertEquals("8a78d0f2", Codec.crc32("abc"));
		assertEquals("191683de", Codec.crc32("qwe"));

		try {
			assertEquals("0", Codec.crc32(""));
			assertEquals("0", Codec.crc32((String) null));
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testCrc32DirectByteArray() {
		byte btempty[] = null;
		byte btExpected1[] = { 0, 97, 0, 98, 0, 99 };
		byte btExpected2[] = { 0, 113, 0, 119, 0, 101 };

		assertEquals("8a78d0f2", Codec.crc32Direct(btExpected1));
		assertEquals("191683de", Codec.crc32Direct(btExpected2));

		try {
			assertEquals("0", Codec.crc32Direct(btempty));
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testCrc32DirectString() {
		assertEquals("8a78d0f2", Codec.crc32Direct("abc"));
		assertEquals("191683de", Codec.crc32Direct("qwe"));

		try {
			assertEquals("0", Codec.crc32Direct(""));
			assertEquals("0", Codec.crc32Direct((String) null));
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testDecodeBase64() {
		String g = "";
		for (int i = 0; i < 240; i++) {
			g += "A string";
		}
		String fc = Codec.encodeBase64(g);
		assertEquals(g, Codec.decodeBase64(fc));

		assertEquals("A string", Codec.decodeBase64("AEEAIABzAHQAcgBpAG4AZw=="));
	}

	@Test
	public final void testDeserializeLength() {

		Pair<Integer, Integer> a = new Pair<Integer, Integer>(53, 3);
		Pair<Integer, Integer> b = new Pair<Integer, Integer>(8, 2);

		assertEquals(a.getA(), Codec.deserializeLength("253", 0).getA());
		assertEquals(a.getB(), Codec.deserializeLength("253", 0).getB());

		assertEquals(b.getA(), Codec.deserializeLength("18A String", 0).getA());
		assertEquals(b.getB(), Codec.deserializeLength("18A String", 0).getB());

		assertEquals((Object) 0, Codec.deserializeLength("08A String", 0)
				.getA());
		assertEquals((Object) 1, Codec.deserializeLength("08A String", 0)
				.getB());

	}

	@Test
	public final void testDeserializeValues() {
		String in1[] = { "hello", "ab", "abc" };
		String in4[] = { "hello", "", "abc" };
		String in3[] = { ";", "ab", "abc" };
		String in32[] = { null, null, null, "ab", "abc" };

		assertArrayEquals(in1, Codec.deserializeValues("3;5;hello2;ab3;abc"));
		assertArrayEquals(in4, Codec.deserializeValues("3;5;hello0;3;abc"));
		assertArrayEquals(in3, Codec.deserializeValues("3;1;;2;ab3;abc"));
		assertArrayEquals(in32, Codec.deserializeValues("5;;;;2;ab3;abc"));

		try {
			assertEquals(in3[0], Codec.deserializeValues(""));
			assertEquals(in3[0], Codec.deserializeValues(null));
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testEncodeBase64() {
		String longBase64 = "";
		String longString = "";

		for (int i = 0; i < 2000; i++) {
			longBase64 += "AGEAYgBj";
			longString += "abc";
		}

		assertEquals(longBase64, Codec.encodeBase64(longString));
		assertEquals("AGE=", Codec.encodeBase64("a"));

		try {
			assertEquals("", Codec.encodeBase64(""));
			assertEquals("", Codec.encodeBase64(null));
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void encodeBase64():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testEscapeHtml() {
		assertEquals("", Codec.escapeHtml(""));
		assertEquals("", Codec.escapeHtml(null));
		assertEquals("This is an &amp;", Codec.escapeHtml("This is an &"));
		assertEquals("A  A", Codec.escapeHtml("A  A"));
	}

	@Test
	public final void testEscapeUnicode() {

		assertEquals("", Codec.escapeUnicode(""));
		assertEquals("", Codec.escapeUnicode(null));

		assertEquals("null char &#x0;", Codec.escapeUnicode("null char \u0000"));
		assertEquals("Backspace &#x8; character",
				Codec.escapeUnicode("Backspace \u0008 character"));
		assertEquals("+ ", Codec.escapeUnicode("+ "));
		assertEquals("normal string", Codec.escapeUnicode("normal string"));
	}

	@Test
	public final void testFromBase64() {
		byte btExpected1[] = { 105, -73, 28 };
		byte btExpected2[] = { 97 };
		byte btExpected3[] = { 83, 116, 114, 105, 110, 103 };

		assertArrayEquals(btExpected1, Codec.fromBase64("abcc"));
		assertArrayEquals(btExpected2, Codec.fromBase64("YQ=="));
		assertArrayEquals(btExpected3, Codec.fromBase64("U3RyaW5n"));

		try {
			assertEquals("0", Codec.fromBase64(""));
			assertEquals("0", Codec.fromBase64((String) null));
			assertEquals("0", Codec.fromBase64("abc"));
			fail("BeeRuntimeException does not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testFromBytes() {
		byte btempty[] = null;
		byte btExpected0[] = {};
		byte btExpected2[] = { 0, 97 };
		byte btExpected21[] = { 0, 97, 0 };
		byte btExpected3[] = { 0, 83, 0, 116, 0, 114, 0, 105, 0, 110, 0, 103 };

		assertEquals("a", Codec.fromBytes(btExpected2));
		assertEquals("String", Codec.fromBytes(btExpected3));

		try {
			assertEquals("0", Codec.fromBytes(btempty));
			assertEquals("0", Codec.fromBytes(btExpected0));
			assertEquals("0", Codec.fromBytes(btExpected21));
			fail("BeeRuntimeException does not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testAdler32ByteArray():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testIsValidUnicodeChar() {
		assertEquals(true, Codec.isValidUnicodeChar('a'));
		assertEquals(false, Codec.isValidUnicodeChar('\u0000'));
		assertEquals(true, Codec.isValidUnicodeChar('\u0021'));
		assertEquals(false, Codec.isValidUnicodeChar('\u007f'));
		assertEquals(false, Codec.isValidUnicodeChar('\u028d'));
		assertEquals(true, Codec.isValidUnicodeChar('\u0244'));
		assertEquals(true, Codec.isValidUnicodeChar('\u0415'));
	}

	@Test
	public final void testMd5() {
		assertEquals("eebae6b863620dc2e7f2bc7754bda625", Codec.md5("A string"));
		assertEquals("8c327a8598a4596fb9ae0046a12e7db2", Codec.md5("Check"));

		try {
			assertEquals("0", Codec.md5(""));
			assertEquals("0", Codec.md5(null));
			fail("BeeRuntimeException does not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void md5():" + e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testSerialize() {
		TransObject obj = new TransObject();

		assertEquals(";", Codec.serialize(null));
		assertEquals("0;", Codec.serialize(""));
		assertEquals("1;5", Codec.serialize(5));
		assertEquals("6;string", Codec.serialize("string"));
		assertEquals("1;c", Codec.serialize('c'));
		assertEquals("3;2.0", Codec.serialize(2.0));
		assertEquals("2;-1", Codec.serialize(-1));
		assertEquals("3;5.0", Codec.serialize(obj));
	}

	@Test
	public final void testSerializeLength() {
		assertEquals("0", Codec.serializeLength(0));
		assertEquals("15", Codec.serializeLength(5));
		assertEquals("215", Codec.serializeLength(15));

		try {
			assertEquals("0", Codec.serializeLength(-1));
			fail("BeeRuntimeException does not work");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void serialiseLength():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}

	}

	@Test
	public final void testSerializeValues() {
		assertEquals("4;1;16;string1;67;letters",
		Codec.serializeValues(1, "string", 6, "letters"));
		assertEquals("2;3;5.01;1", Codec.serializeValues(5.0, 1));
		assertEquals("1;0;", Codec.serializeValues(""));
		assertEquals("1;;", Codec.serializeValues((Object) null));

		try {
			assertEquals("", Codec.serializeValues());
			fail("Assert not working");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println(" bad length SerializeValues " + e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException  !!!"
					+ e.getMessage());
		}
	}

	@Test
	public final void testSerializeWithLength() {

		StringBuilder sb = new StringBuilder();
		Codec.serializeWithLength(sb, null);
		assertEquals("0", sb.toString());

		TransObjectLong obj = new TransObjectLong();
		StringBuilder sb2 = new StringBuilder();
		Codec.serializeWithLength(sb2, obj);
		assertEquals("211Hello world", sb2.toString());

		Codec.serializeWithLength(sb2, "A string");
		assertEquals("211Hello world18A string", sb2.toString());

		Codec.serializeWithLength(sb2, "    ");
		assertEquals("211Hello world18A string14    ", sb2.toString());

		StringBuilder sb3 = new StringBuilder();
		Codec.serializeWithLength(sb3, "");
		assertEquals("0", sb3.toString());

		try {
			Codec.serializeWithLength(null, 5);
			Codec.serializeWithLength(null, null);
			fail("Assert not working");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println(" bad length SerializeValues " + e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException  !!!");
		}
	}

	@Test
	public final void testToBytesString() {
		byte btExpected1[] = { 0, 32, 0, 33, 0, 35 };
		byte btExpected2[] = { 0, 113, 0, 119, 0, 101, 0, 32, 0, 33, 0, 35 };
		byte btExpected3[] = { 0, 113, 0, 119, 0, 101, 0, 32, 0, 33, 0, 35, 0,
				101 };

		assertArrayEquals(btExpected1, Codec.toBytes(" !#"));
		assertArrayEquals(btExpected2, Codec.toBytes("qwe !#"));
		assertArrayEquals(btExpected3, Codec.toBytes("qwe !#e"));
	}

	@Test
	public final void testToBytesStringInt() {
		byte btExpected1[] = { 0, 32, 0, 33, 0, 35 };
		byte btExpected2[] = { 0, 101, 0, 32, 0, 33, 0, 35, 0, 101 };

		assertArrayEquals(btExpected1, Codec.toBytes(" !#", 0));
		assertArrayEquals(btExpected1, Codec.toBytes("qwe !#", 3)); 
		assertArrayEquals(btExpected2, Codec.toBytes("qwe !#e", 2));
	}

	@Test
	public final void testToBytesStringIntInt() {
		try {
			assertEquals("", Codec.toBytes(null, 0, 1));
			fail("Assert not working");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println(e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException  !!!");
		}

		try {
			assertEquals("", Codec.toBytes("", 0, 1));
			fail("Assert not working");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println(" positive " + e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException  !!!");
		}

		try {
			assertEquals("", Codec.toBytes("sdsd", -5, 1));
			fail("Assert not working");
		} catch (BeeRuntimeException e) {
			assertTrue(true);

			System.out.println(" positive " + e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException  !!!");
		}

		try {
			assertEquals("", Codec.toBytes("vsdds", 10, 20));
			fail("Assert not working");
		} catch (BeeRuntimeException e) {
			assertTrue(true);

			System.out.println(" positive " + e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException  !!!");
		}

		try {
			assertEquals("", Codec.toBytes("vsdds", 2, 100));
			fail("Assert not working");
		} catch (BeeRuntimeException e) {
			assertTrue(true);

			System.out.println(" positive " + e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException  !!!");
		}
		byte btExpected1[] = { 0, 32, 0, 33, 0, 35 };
		byte btExpected2[] = { 0, 33, 0, 35 };

		assertArrayEquals(btExpected1, Codec.toBytes(" !#", 0, 3));
		assertArrayEquals(btExpected2, Codec.toBytes(" !#", 1, 3));
		assertArrayEquals(btExpected1, Codec.toBytes("qwe !#erty", 3, 6));
		assertArrayEquals(btExpected1, Codec.toBytes("qwe !#", 3, 6));

	}

	@Test
	public final void testToHexByteArray() {

		byte bt[] = { 0, 32, 0, 33, 0, 35 };
		byte bt2[] = null;
		byte bt3[] = {};

		assertEquals("002000210023", Codec.toHex(bt));
		assertEquals(null, Codec.toHex(bt3));
		assertEquals(null, Codec.toHex(bt2));
	}

	@Test
	public final void testToHexChar() {

		assertEquals("0063", Codec.toHex('c'));
		assertEquals("0020", Codec.toHex(' '));
		assertEquals("0023", Codec.toHex('\u0023'));
	}

	@Test
	public final void testToHexCharArray() {

		char[] mas = { 'a', 'b', 'c' };
		char[] mas2 = {};
		char[] mas3 = { ' ' };
		char[] mas4 = null;

		assertEquals("006100620063", Codec.toHex(mas));
		assertEquals(null, Codec.toHex(mas2));
		assertEquals("0020", Codec.toHex(mas3));
		assertEquals(null, Codec.toHex(mas4));
	}
}
