package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.utils.Wildcards;
import com.butent.bee.shared.utils.Wildcards.Pattern;

/**
 * Tests {@link com.butent.bee.shared.utils.Wildcards.Pattern}.
 */
public class TestWildcardsPattern {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test
	public final void testPattern()
	{
		try
		{
			Pattern a = Wildcards.getPattern(null, '\0', '\0', false);
			fail ("Exceptions not work");
		}
		catch (BeeRuntimeException e)
		{
			assertTrue(true);			
		}
		catch (Exception e)
		{
			fail ("Need BeeRuntime Exception");			
		}
	}
	
	@Test
	public final void testGetExpr() {
		Pattern a = Wildcards.getPattern("File*.txt=5", '*', '\0', false);
		        
		assertEquals ("File*.txt=5", a.getExpr());
		
		a = Wildcards.getPattern("     File*.txt=5   \t \t \t \r ", '*', '\0', false);
		assertEquals ("File*.txt=5", a.getExpr());
	} 

	@Test
	public final void testGetTokens() {
		Pattern a = Wildcards.getPattern("File*.txt=5", '*', '\0', false);
        
		String b[] = {"File", "*", ".txt=5"};
		assertArrayEquals (b, a.getTokens());
		
		a = Wildcards.getPattern("File*.txt=5", '*', '.', false);
		String c[] = {"File", ".", "*", "txt=5"};
		assertArrayEquals (c, a.getTokens());
		
		assertEquals("File*.txt=5", a.toString());
		
		a = Wildcards.getPattern("File*.txt=5", '*', '.', true);
		assertEquals("File*.txt=5 (sensitive)", a.toString());
		
		a = Wildcards.getPattern("File*.txt5=", ';', '>', true);
		assertEquals("File*.txt5 (sensitive) (exact)", a.toString());
		
		a = Wildcards.getPattern("File*.txt5", ';', '>', true);
		assertEquals("File*.txt5 (sensitive)", a.toString());
		
		a = Wildcards.getPattern("abcd>efgh>opkg<asdda>>>", '>', '<', false);
		assertEquals("abcd>efgh>opkg<asdda>>>", a.toString());
		
		Object[] mas = {"abcd", ">", "efgh", ">", "opkg","<", "asdda", ">"};
		assertArrayEquals(mas, a.getTokens());
	}
}
