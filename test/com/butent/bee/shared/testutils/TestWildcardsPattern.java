package com.butent.bee.shared.testutils;

import com.butent.bee.shared.utils.Wildcards;
import com.butent.bee.shared.utils.Wildcards.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

  @Test
  public final void testGetExpr() {
    Pattern a = Wildcards.getPattern("File*.txt=5", '*', '\0', false, null);

    assertEquals("File*.txt=5", a.getExpr());

    a = Wildcards.getPattern("     File*.txt=5   \t \t \t \r ", '*', '\0', false, null);
    assertEquals("File*.txt=5", a.getExpr());
  }

  @Test
  public final void testGetTokens() {
    Pattern a = Wildcards.getPattern("File*.txt=5", '*', '\0', false, null);

    String b[] = {"File", "*", ".txt=5"};
    assertArrayEquals(b, a.getTokens());

    a = Wildcards.getPattern("File*.txt=5", '*', '.', false, null);
    String c[] = {"File", ".", "*", "txt=5"};
    assertArrayEquals(c, a.getTokens());

    assertEquals("File*.txt=5", a.toString());

    a = Wildcards.getPattern("File*.txt=5", '*', '.', true, null);
    assertEquals("File*.txt=5 (sensitive)", a.toString());

    a = Wildcards.getPattern("File*.txt5=", ';', '>', true, '=');
    assertEquals("File*.txt5 (sensitive) (exact)", a.toString());

    a = Wildcards.getPattern("File*.txt5", ';', '>', true, null);
    assertEquals("File*.txt5 (sensitive)", a.toString());

    a = Wildcards.getPattern("abcd>efgh>opkg<asdda>>>", '>', '<', false, null);
    assertEquals("abcd>efgh>opkg<asdda>>>", a.toString());

    Object[] mas = {"abcd", ">", "efgh", ">", "opkg", "<", "asdda", ">"};
    assertArrayEquals(mas, a.getTokens());
  }
}
