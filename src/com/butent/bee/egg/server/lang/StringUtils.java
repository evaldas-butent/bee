package com.butent.bee.egg.server.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.butent.bee.egg.shared.lang.CharSequenceUtils;

public class StringUtils extends com.butent.bee.egg.shared.lang.StringUtils {
  /**
   * <p>
   * Removes the accents from a string.
   * </p>
   * <p>
   * NOTE: This is a JDK 1.6 method, it will fail on JDK 1.5.
   * </p>
   * 
   * <pre>
   * StringUtils.stripAccents(null)                = null
   * StringUtils.stripAccents("")                  = ""
   * StringUtils.stripAccents("control")           = "control"
   * StringUtils.stripAccents("&ecute;clair")      = "eclair"
   * </pre>
   * 
   * @param input
   *          String to be stripped
   * @return String without accents on the text
   * 
   * @since 3.0
   */
  public static String stripAccents(String input) {
    if (input == null) {
      return null;
    }
    if (SystemUtils.isJavaVersionAtLeast(1.6f)) {

      // String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);

      // START of 1.5 reflection - in 1.6 use the line commented out above
      try {
        // get java.text.Normalizer.Form class
        Class<?> normalizerFormClass = ClassUtils.getClass(
            "java.text.Normalizer$Form", false);

        // get Normlizer class
        Class<?> normalizerClass = ClassUtils.getClass("java.text.Normalizer",
            false);

        // get static method on Normalizer
        java.lang.reflect.Method method = normalizerClass.getMethod(
            "normalize", CharSequence.class, normalizerFormClass);

        // get Normalizer.NFD field
        java.lang.reflect.Field nfd = normalizerFormClass.getField("NFD");

        // invoke method
        String decomposed = (String) method.invoke(null, input, nfd.get(null));
        // END of 1.5 reflection

        java.util.regex.Pattern accentPattern = java.util.regex.Pattern
            .compile("\\p{InCombiningDiacriticalMarks}+");
        return accentPattern.matcher(decomposed).replaceAll("");
      } catch (ClassNotFoundException cnfe) {
        throw new RuntimeException(
            "ClassNotFoundException occurred during 1.6 backcompat code", cnfe);
      } catch (NoSuchMethodException nsme) {
        throw new RuntimeException(
            "NoSuchMethodException occurred during 1.6 backcompat code", nsme);
      } catch (NoSuchFieldException nsfe) {
        throw new RuntimeException(
            "NoSuchFieldException occurred during 1.6 backcompat code", nsfe);
      } catch (IllegalAccessException iae) {
        throw new RuntimeException(
            "IllegalAccessException occurred during 1.6 backcompat code", iae);
      } catch (IllegalArgumentException iae) {
        throw new RuntimeException(
            "IllegalArgumentException occurred during 1.6 backcompat code", iae);
      } catch (java.lang.reflect.InvocationTargetException ite) {
        throw new RuntimeException(
            "InvocationTargetException occurred during 1.6 backcompat code",
            ite);
      } catch (SecurityException se) {
        throw new RuntimeException(
            "SecurityException occurred during 1.6 backcompat code", se);
      }
    } else {
      throw new UnsupportedOperationException(
          "The stripAccents(String) method is not supported until Java 1.6");
    }
  }

  /**
   * <p>
   * Checks if a CharSequence is whitespace, empty ("") or null.
   * </p>
   * 
   * <pre>
   * StringUtils.isBlank(null)      = true
   * StringUtils.isBlank("")        = true
   * StringUtils.isBlank(" ")       = true
   * StringUtils.isBlank("bob")     = false
   * StringUtils.isBlank("  bob  ") = false
   * </pre>
   * 
   * @param cs
   *          the CharSequence to check, may be null
   * @return <code>true</code> if the CharSequence is null, empty or whitespace
   * @since 2.0
   * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
   */
  public static boolean isBlank(CharSequence cs) {
    int strLen;
    if (cs == null || (strLen = cs.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if ((Character.isWhitespace(cs.charAt(i)) == false)) {
        return false;
      }
    }
    return true;
  }

  /**
   * <p>
   * Checks if a CharSequence is not empty (""), not null and not whitespace
   * only.
   * </p>
   * 
   * <pre>
   * StringUtils.isNotBlank(null)      = false
   * StringUtils.isNotBlank("")        = false
   * StringUtils.isNotBlank(" ")       = false
   * StringUtils.isNotBlank("bob")     = true
   * StringUtils.isNotBlank("  bob  ") = true
   * </pre>
   * 
   * @param cs
   *          the CharSequence to check, may be null
   * @return <code>true</code> if the CharSequence is not empty and not null and
   *         not whitespace
   * @since 2.0
   * @since 3.0 Changed signature from isNotBlank(String) to
   *        isNotBlank(CharSequence)
   */
  public static boolean isNotBlank(CharSequence cs) {
    return !StringUtils.isBlank(cs);
  }

  // Stripping
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Strips whitespace from the start and end of a String.
   * </p>
   * 
   * <p>
   * This is similar to {@link #trim(String)} but removes whitespace. Whitespace
   * is defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.strip(null)     = null
   * StringUtils.strip("")       = ""
   * StringUtils.strip("   ")    = ""
   * StringUtils.strip("abc")    = "abc"
   * StringUtils.strip("  abc")  = "abc"
   * StringUtils.strip("abc  ")  = "abc"
   * StringUtils.strip(" abc ")  = "abc"
   * StringUtils.strip(" ab c ") = "ab c"
   * </pre>
   * 
   * @param str
   *          the String to remove whitespace from, may be null
   * @return the stripped String, <code>null</code> if null String input
   */
  public static String strip(String str) {
    return strip(str, null);
  }

  /**
   * <p>
   * Strips whitespace from the start and end of a String returning
   * <code>null</code> if the String is empty ("") after the strip.
   * </p>
   * 
   * <p>
   * This is similar to {@link #trimToNull(String)} but removes whitespace.
   * Whitespace is defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <pre>
   * StringUtils.stripToNull(null)     = null
   * StringUtils.stripToNull("")       = null
   * StringUtils.stripToNull("   ")    = null
   * StringUtils.stripToNull("abc")    = "abc"
   * StringUtils.stripToNull("  abc")  = "abc"
   * StringUtils.stripToNull("abc  ")  = "abc"
   * StringUtils.stripToNull(" abc ")  = "abc"
   * StringUtils.stripToNull(" ab c ") = "ab c"
   * </pre>
   * 
   * @param str
   *          the String to be stripped, may be null
   * @return the stripped String, <code>null</code> if whitespace, empty or null
   *         String input
   * @since 2.0
   */
  public static String stripToNull(String str) {
    if (str == null) {
      return null;
    }
    str = strip(str, null);
    return str.length() == 0 ? null : str;
  }

  /**
   * <p>
   * Strips whitespace from the start and end of a String returning an empty
   * String if <code>null</code> input.
   * </p>
   * 
   * <p>
   * This is similar to {@link #trimToEmpty(String)} but removes whitespace.
   * Whitespace is defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <pre>
   * StringUtils.stripToEmpty(null)     = ""
   * StringUtils.stripToEmpty("")       = ""
   * StringUtils.stripToEmpty("   ")    = ""
   * StringUtils.stripToEmpty("abc")    = "abc"
   * StringUtils.stripToEmpty("  abc")  = "abc"
   * StringUtils.stripToEmpty("abc  ")  = "abc"
   * StringUtils.stripToEmpty(" abc ")  = "abc"
   * StringUtils.stripToEmpty(" ab c ") = "ab c"
   * </pre>
   * 
   * @param str
   *          the String to be stripped, may be null
   * @return the trimmed String, or an empty String if <code>null</code> input
   * @since 2.0
   */
  public static String stripToEmpty(String str) {
    return str == null ? EMPTY : strip(str, null);
  }

  /**
   * <p>
   * Strips any of a set of characters from the start and end of a String. This
   * is similar to {@link String#trim()} but allows the characters to be
   * stripped to be controlled.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. An empty string
   * ("") input returns the empty string.
   * </p>
   * 
   * <p>
   * If the stripChars String is <code>null</code>, whitespace is stripped as
   * defined by {@link Character#isWhitespace(char)}. Alternatively use
   * {@link #strip(String)}.
   * </p>
   * 
   * <pre>
   * StringUtils.strip(null, *)          = null
   * StringUtils.strip("", *)            = ""
   * StringUtils.strip("abc", null)      = "abc"
   * StringUtils.strip("  abc", null)    = "abc"
   * StringUtils.strip("abc  ", null)    = "abc"
   * StringUtils.strip(" abc ", null)    = "abc"
   * StringUtils.strip("  abcyx", "xyz") = "  abc"
   * </pre>
   * 
   * @param str
   *          the String to remove characters from, may be null
   * @param stripChars
   *          the characters to remove, null treated as whitespace
   * @return the stripped String, <code>null</code> if null String input
   */
  public static String strip(String str, String stripChars) {
    if (isEmpty(str)) {
      return str;
    }
    str = stripStart(str, stripChars);
    return stripEnd(str, stripChars);
  }

  /**
   * <p>
   * Strips any of a set of characters from the start of a String.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. An empty string
   * ("") input returns the empty string.
   * </p>
   * 
   * <p>
   * If the stripChars String is <code>null</code>, whitespace is stripped as
   * defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <pre>
   * StringUtils.stripStart(null, *)          = null
   * StringUtils.stripStart("", *)            = ""
   * StringUtils.stripStart("abc", "")        = "abc"
   * StringUtils.stripStart("abc", null)      = "abc"
   * StringUtils.stripStart("  abc", null)    = "abc"
   * StringUtils.stripStart("abc  ", null)    = "abc  "
   * StringUtils.stripStart(" abc ", null)    = "abc "
   * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
   * </pre>
   * 
   * @param str
   *          the String to remove characters from, may be null
   * @param stripChars
   *          the characters to remove, null treated as whitespace
   * @return the stripped String, <code>null</code> if null String input
   */
  public static String stripStart(String str, String stripChars) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }
    int start = 0;
    if (stripChars == null) {
      while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
        start++;
      }
    } else if (stripChars.length() == 0) {
      return str;
    } else {
      while ((start != strLen)
          && (stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND)) {
        start++;
      }
    }
    return str.substring(start);
  }

  /**
   * <p>
   * Strips any of a set of characters from the end of a String.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. An empty string
   * ("") input returns the empty string.
   * </p>
   * 
   * <p>
   * If the stripChars String is <code>null</code>, whitespace is stripped as
   * defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <pre>
   * StringUtils.stripEnd(null, *)          = null
   * StringUtils.stripEnd("", *)            = ""
   * StringUtils.stripEnd("abc", "")        = "abc"
   * StringUtils.stripEnd("abc", null)      = "abc"
   * StringUtils.stripEnd("  abc", null)    = "  abc"
   * StringUtils.stripEnd("abc  ", null)    = "abc"
   * StringUtils.stripEnd(" abc ", null)    = " abc"
   * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
   * </pre>
   * 
   * @param str
   *          the String to remove characters from, may be null
   * @param stripChars
   *          the characters to remove, null treated as whitespace
   * @return the stripped String, <code>null</code> if null String input
   */
  public static String stripEnd(String str, String stripChars) {
    int end;
    if (str == null || (end = str.length()) == 0) {
      return str;
    }

    if (stripChars == null) {
      while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
        end--;
      }
    } else if (stripChars.length() == 0) {
      return str;
    } else {
      while ((end != 0)
          && (stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND)) {
        end--;
      }
    }
    return str.substring(0, end);
  }

  /**
   * Check whether the given String contains any whitespace characters.
   * 
   * @param str
   *          the String to check (may be <code>null</code>)
   * @return <code>true</code> if the String is not empty and contains at least
   *         1 whitespace character
   * @see java.lang.Character#isWhitespace
   * @since 3.0
   */
  // From org.springframework.util.StringUtils, under Apache License 2.0
  public static boolean containsWhitespace(String str) {
    if (isEmpty(str)) {
      return false;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * <p>
   * Splits the provided text into an array, separators specified, preserving
   * all tokens, including empty tokens created by adjacent separators. This is
   * an alternative to using StringTokenizer.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as separators for empty tokens. For more control
   * over the split use the StrTokenizer class.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separatorChars splits on whitespace.
   * </p>
   * 
   * <pre>
   * StringUtils.splitPreserveAllTokens(null, *)           = null
   * StringUtils.splitPreserveAllTokens("", *)             = []
   * StringUtils.splitPreserveAllTokens("abc def", null)   = ["abc", "def"]
   * StringUtils.splitPreserveAllTokens("abc def", " ")    = ["abc", "def"]
   * StringUtils.splitPreserveAllTokens("abc  def", " ")   = ["abc", "", def"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":")   = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef:", ":")  = ["ab", "cd", "ef", ""]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef::", ":") = ["ab", "cd", "ef", "", ""]
   * StringUtils.splitPreserveAllTokens("ab::cd:ef", ":")  = ["ab", "", cd", "ef"]
   * StringUtils.splitPreserveAllTokens(":cd:ef", ":")     = ["", cd", "ef"]
   * StringUtils.splitPreserveAllTokens("::cd:ef", ":")    = ["", "", cd", "ef"]
   * StringUtils.splitPreserveAllTokens(":cd:ef:", ":")    = ["", cd", "ef", ""]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be <code>null</code>
   * @param separatorChars
   *          the characters used as the delimiters, <code>null</code> splits on
   *          whitespace
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.1
   */
  public static String[] splitPreserveAllTokens(String str,
      String separatorChars) {
    return splitWorker(str, separatorChars, -1, true);
  }

  /**
   * <p>
   * Splits the provided text into an array with a maximum length, separators
   * specified, preserving all tokens, including empty tokens created by
   * adjacent separators.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as separators for empty tokens. Adjacent separators
   * are treated as one separator.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separatorChars splits on whitespace.
   * </p>
   * 
   * <p>
   * If more than <code>max</code> delimited substrings are found, the last
   * returned string includes all characters after the first
   * <code>max - 1</code> returned strings (including separator characters).
   * </p>
   * 
   * <pre>
   * StringUtils.splitPreserveAllTokens(null, *, *)            = null
   * StringUtils.splitPreserveAllTokens("", *, *)              = []
   * StringUtils.splitPreserveAllTokens("ab de fg", null, 0)   = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 0) = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 0)    = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 2)    = ["ab", "cd:ef"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 2) = ["ab", "  de fg"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 3) = ["ab", "", " de fg"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 4) = ["ab", "", "", "de fg"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be <code>null</code>
   * @param separatorChars
   *          the characters used as the delimiters, <code>null</code> splits on
   *          whitespace
   * @param max
   *          the maximum number of elements to include in the array. A zero or
   *          negative value implies no limit
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.1
   */
  public static String[] splitPreserveAllTokens(String str,
      String separatorChars, int max) {
    return splitWorker(str, separatorChars, max, true);
  }

  /**
   * Performs the logic for the <code>split</code> and
   * <code>splitPreserveAllTokens</code> methods that return a maximum array
   * length.
   * 
   * @param str
   *          the String to parse, may be <code>null</code>
   * @param separatorChars
   *          the separate character
   * @param max
   *          the maximum number of elements to include in the array. A zero or
   *          negative value implies no limit.
   * @param preserveAllTokens
   *          if <code>true</code>, adjacent separators are treated as empty
   *          token separators; if <code>false</code>, adjacent separators are
   *          treated as one separator.
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  private static String[] splitWorker(String str, String separatorChars,
      int max, boolean preserveAllTokens) {
    // Performance tuned for 2.0 (JDK1.4)
    // Direct code is quicker than StringTokenizer.
    // Also, StringTokenizer uses isSpace() not isWhitespace()

    if (str == null) {
      return null;
    }
    int len = str.length();
    if (len == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    List<String> list = new ArrayList<String>();
    int sizePlus1 = 1;
    int i = 0, start = 0;
    boolean match = false;
    boolean lastMatch = false;
    if (separatorChars == null) {
      // Null separator means use whitespace
      while (i < len) {
        if (Character.isWhitespace(str.charAt(i))) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    } else if (separatorChars.length() == 1) {
      // Optimise 1 character case
      char sep = separatorChars.charAt(0);
      while (i < len) {
        if (str.charAt(i) == sep) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    } else {
      // standard case
      while (i < len) {
        if (separatorChars.indexOf(str.charAt(i)) >= 0) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    }
    if (match || (preserveAllTokens && lastMatch)) {
      list.add(str.substring(start, i));
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * <p>
   * Splits a String by Character type as returned by
   * <code>java.lang.Character.getType(char)</code>. Groups of contiguous
   * characters of the same type are returned as complete tokens.
   * 
   * <pre>
   * StringUtils.splitByCharacterType(null)         = null
   * StringUtils.splitByCharacterType("")           = []
   * StringUtils.splitByCharacterType("ab de fg")   = ["ab", " ", "de", " ", "fg"]
   * StringUtils.splitByCharacterType("ab   de fg") = ["ab", "   ", "de", " ", "fg"]
   * StringUtils.splitByCharacterType("ab:cd:ef")   = ["ab", ":", "cd", ":", "ef"]
   * StringUtils.splitByCharacterType("number5")    = ["number", "5"]
   * StringUtils.splitByCharacterType("fooBar")     = ["foo", "B", "ar"]
   * StringUtils.splitByCharacterType("foo200Bar")  = ["foo", "200", "B", "ar"]
   * StringUtils.splitByCharacterType("ASFRules")   = ["ASFR", "ules"]
   * </pre>
   * 
   * @param str
   *          the String to split, may be <code>null</code>
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.4
   */
  public static String[] splitByCharacterType(String str) {
    return splitByCharacterType(str, false);
  }

  /**
   * <p>
   * Splits a String by Character type as returned by
   * <code>java.lang.Character.getType(char)</code>. Groups of contiguous
   * characters of the same type are returned as complete tokens, with the
   * following exception: the character of type
   * <code>Character.UPPERCASE_LETTER</code>, if any, immediately preceding a
   * token of type <code>Character.LOWERCASE_LETTER</code> will belong to the
   * following token rather than to the preceding, if any,
   * <code>Character.UPPERCASE_LETTER</code> token.
   * 
   * <pre>
   * StringUtils.splitByCharacterTypeCamelCase(null)         = null
   * StringUtils.splitByCharacterTypeCamelCase("")           = []
   * StringUtils.splitByCharacterTypeCamelCase("ab de fg")   = ["ab", " ", "de", " ", "fg"]
   * StringUtils.splitByCharacterTypeCamelCase("ab   de fg") = ["ab", "   ", "de", " ", "fg"]
   * StringUtils.splitByCharacterTypeCamelCase("ab:cd:ef")   = ["ab", ":", "cd", ":", "ef"]
   * StringUtils.splitByCharacterTypeCamelCase("number5")    = ["number", "5"]
   * StringUtils.splitByCharacterTypeCamelCase("fooBar")     = ["foo", "Bar"]
   * StringUtils.splitByCharacterTypeCamelCase("foo200Bar")  = ["foo", "200", "Bar"]
   * StringUtils.splitByCharacterTypeCamelCase("ASFRules")   = ["ASF", "Rules"]
   * </pre>
   * 
   * @param str
   *          the String to split, may be <code>null</code>
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.4
   */
  public static String[] splitByCharacterTypeCamelCase(String str) {
    return splitByCharacterType(str, true);
  }

  /**
   * <p>
   * Splits a String by Character type as returned by
   * <code>java.lang.Character.getType(char)</code>. Groups of contiguous
   * characters of the same type are returned as complete tokens, with the
   * following exception: if <code>camelCase</code> is <code>true</code>, the
   * character of type <code>Character.UPPERCASE_LETTER</code>, if any,
   * immediately preceding a token of type
   * <code>Character.LOWERCASE_LETTER</code> will belong to the following token
   * rather than to the preceding, if any,
   * <code>Character.UPPERCASE_LETTER</code> token.
   * 
   * @param str
   *          the String to split, may be <code>null</code>
   * @param camelCase
   *          whether to use so-called "camel-case" for letter types
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.4
   */
  private static String[] splitByCharacterType(String str, boolean camelCase) {
    if (str == null) {
      return null;
    }
    if (str.length() == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    char[] c = str.toCharArray();
    List<String> list = new ArrayList<String>();
    int tokenStart = 0;
    int currentType = Character.getType(c[tokenStart]);
    for (int pos = tokenStart + 1; pos < c.length; pos++) {
      int type = Character.getType(c[pos]);
      if (type == currentType) {
        continue;
      }
      if (camelCase && type == Character.LOWERCASE_LETTER
          && currentType == Character.UPPERCASE_LETTER) {
        int newTokenStart = pos - 1;
        if (newTokenStart != tokenStart) {
          list.add(new String(c, tokenStart, newTokenStart - tokenStart));
          tokenStart = newTokenStart;
        }
      } else {
        list.add(new String(c, tokenStart, pos - tokenStart));
        tokenStart = pos;
      }
      currentType = type;
    }
    list.add(new String(c, tokenStart, c.length - tokenStart));
    return list.toArray(new String[list.size()]);
  }

  // Delete
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Deletes all whitespaces from a String as defined by
   * {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <pre>
   * StringUtils.deleteWhitespace(null)         = null
   * StringUtils.deleteWhitespace("")           = ""
   * StringUtils.deleteWhitespace("abc")        = "abc"
   * StringUtils.deleteWhitespace("   ab  c  ") = "abc"
   * </pre>
   * 
   * @param str
   *          the String to delete whitespace from, may be null
   * @return the String without whitespaces, <code>null</code> if null String
   *         input
   */
  public static String deleteWhitespace(String str) {
    if (isEmpty(str)) {
      return str;
    }
    int sz = str.length();
    char[] chs = new char[sz];
    int count = 0;
    for (int i = 0; i < sz; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        chs[count++] = str.charAt(i);
      }
    }
    if (count == sz) {
      return str;
    }
    return new String(chs, 0, count);
  }

  /**
   * <p>
   * Converts a String to upper case as per {@link String#toUpperCase(Locale)}.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.upperCase(null, Locale.ENGLISH)  = null
   * StringUtils.upperCase("", Locale.ENGLISH)    = ""
   * StringUtils.upperCase("aBc", Locale.ENGLISH) = "ABC"
   * </pre>
   * 
   * @param str
   *          the String to upper case, may be null
   * @param locale
   *          the locale that defines the case transformation rules, must not be
   *          null
   * @return the upper cased String, <code>null</code> if null String input
   * @since 2.5
   */
  public static String upperCase(String str, Locale locale) {
    if (str == null) {
      return null;
    }
    return str.toUpperCase(locale);
  }

  /**
   * <p>
   * Converts a String to lower case as per {@link String#toLowerCase(Locale)}.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.lowerCase(null, Locale.ENGLISH)  = null
   * StringUtils.lowerCase("", Locale.ENGLISH)    = ""
   * StringUtils.lowerCase("aBc", Locale.ENGLISH) = "abc"
   * </pre>
   * 
   * @param str
   *          the String to lower case, may be null
   * @param locale
   *          the locale that defines the case transformation rules, must not be
   *          null
   * @return the lower cased String, <code>null</code> if null String input
   * @since 2.5
   */
  public static String lowerCase(String str, Locale locale) {
    if (str == null) {
      return null;
    }
    return str.toLowerCase(locale);
  }

  /**
   * <p>
   * Capitalizes a String changing the first letter to title case as per
   * {@link Character#toTitleCase(char)}. No other letters are changed.
   * </p>
   * 
   * <p>
   * For a word based algorithm, see
   * {@link com.butent.bee.egg.server.lang.text.lang3.text.WordUtils#capitalize(String)}
   * . A <code>null</code> input String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.capitalize(null)  = null
   * StringUtils.capitalize("")    = ""
   * StringUtils.capitalize("cat") = "Cat"
   * StringUtils.capitalize("cAt") = "CAt"
   * </pre>
   * 
   * @param cs
   *          the String to capitalize, may be null
   * @return the capitalized String, <code>null</code> if null String input
   * @see com.butent.bee.egg.server.lang.text.lang3.text.WordUtils#capitalize(String)
   * @see #uncapitalize(CharSequence)
   * @since 2.0
   * @since 3.0 Changed signature from capitalize(String) to
   *        capitalize(CharSequence)
   */
  public static String capitalize(CharSequence cs) {
    if (cs == null) {
      return null;
    }
    int strLen;
    if ((strLen = cs.length()) == 0) {
      return cs.toString();
    }
    return new StringBuilder(strLen)
        .append(Character.toTitleCase(cs.charAt(0)))
        .append(CharSequenceUtils.subSequence(cs, 1)).toString();
  }

  /**
   * <p>
   * Swaps the case of a String changing upper and title case to lower case, and
   * lower case to upper case.
   * </p>
   * 
   * <ul>
   * <li>Upper case character converts to Lower case</li>
   * <li>Title case character converts to Lower case</li>
   * <li>Lower case character converts to Upper case</li>
   * </ul>
   * 
   * <p>
   * For a word based algorithm, see
   * {@link com.butent.bee.egg.server.lang.text.lang3.text.WordUtils#swapCase(String)}
   * . A <code>null</code> input String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.swapCase(null)                 = null
   * StringUtils.swapCase("")                   = ""
   * StringUtils.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
   * </pre>
   * 
   * <p>
   * NOTE: This method changed in Lang version 2.0. It no longer performs a word
   * based algorithm. If you only use ASCII, you will notice no change. That
   * functionality is available in org.apache.commons.lang3.text.WordUtils.
   * </p>
   * 
   * @param str
   *          the String to swap case, may be null
   * @return the changed String, <code>null</code> if null String input
   */
  public static String swapCase(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }
    StringBuilder buffer = new StringBuilder(strLen);

    char ch = 0;
    for (int i = 0; i < strLen; i++) {
      ch = str.charAt(i);
      if (Character.isUpperCase(ch)) {
        ch = Character.toLowerCase(ch);
      } else if (Character.isTitleCase(ch)) {
        ch = Character.toLowerCase(ch);
      } else if (Character.isLowerCase(ch)) {
        ch = Character.toUpperCase(ch);
      }
      buffer.append(ch);
    }
    return buffer.toString();
  }

  /**
   * <p>
   * Checks if the CharSequence contains only whitespace.
   * </p>
   * 
   * <p>
   * <code>null</code> will return <code>false</code>. An empty CharSequence
   * (length()=0) will return <code>true</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.isWhitespace(null)   = false
   * StringUtils.isWhitespace("")     = true
   * StringUtils.isWhitespace("  ")   = true
   * StringUtils.isWhitespace("abc")  = false
   * StringUtils.isWhitespace("ab2c") = false
   * StringUtils.isWhitespace("ab-c") = false
   * </pre>
   * 
   * @param cs
   *          the CharSequence to check, may be null
   * @return <code>true</code> if only contains whitespace, and is non-null
   * @since 2.0
   * @since 3.0 Changed signature from isWhitespace(String) to
   *        isWhitespace(CharSequence)
   */
  public static boolean isWhitespace(CharSequence cs) {
    if (cs == null) {
      return false;
    }
    int sz = cs.length();
    for (int i = 0; i < sz; i++) {
      if ((Character.isWhitespace(cs.charAt(i)) == false)) {
        return false;
      }
    }
    return true;
  }

  // Reversing
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Reverses a String as per {@link StringBuilder#reverse()}.
   * </p>
   * 
   * <p>
   * A <code>null</code> String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.reverse(null)  = null
   * StringUtils.reverse("")    = ""
   * StringUtils.reverse("bat") = "tab"
   * </pre>
   * 
   * @param str
   *          the String to reverse, may be null
   * @return the reversed String, <code>null</code> if null String input
   */
  public static String reverse(String str) {
    if (str == null) {
      return null;
    }
    return new StringBuilder(str).reverse().toString();
  }

  // StripAll
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Strips whitespace from the start and end of every String in an array.
   * Whitespace is defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <p>
   * A new array is returned each time, except for length zero. A
   * <code>null</code> array will return <code>null</code>. An empty array will
   * return itself. A <code>null</code> array entry will be ignored.
   * </p>
   * 
   * <pre>
   * StringUtils.stripAll(null)             = null
   * StringUtils.stripAll([])               = []
   * StringUtils.stripAll(["abc", "  abc"]) = ["abc", "abc"]
   * StringUtils.stripAll(["abc  ", null])  = ["abc", null]
   * </pre>
   * 
   * @param strs
   *          the array to remove whitespace from, may be null
   * @return the stripped Strings, <code>null</code> if null array input
   */
  public static String[] stripAll(String[] strs) {
    return stripAll(strs, null);
  }

  /**
   * <p>
   * Strips any of a set of characters from the start and end of every String in
   * an array.
   * </p>
   * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
   * 
   * <p>
   * A new array is returned each time, except for length zero. A
   * <code>null</code> array will return <code>null</code>. An empty array will
   * return itself. A <code>null</code> array entry will be ignored. A
   * <code>null</code> stripChars will strip whitespace as defined by
   * {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <pre>
   * StringUtils.stripAll(null, *)                = null
   * StringUtils.stripAll([], *)                  = []
   * StringUtils.stripAll(["abc", "  abc"], null) = ["abc", "abc"]
   * StringUtils.stripAll(["abc  ", null], null)  = ["abc", null]
   * StringUtils.stripAll(["abc  ", null], "yz")  = ["abc  ", null]
   * StringUtils.stripAll(["yabcz", null], "yz")  = ["abc", null]
   * </pre>
   * 
   * @param strs
   *          the array to remove characters from, may be null
   * @param stripChars
   *          the characters to remove, null treated as whitespace
   * @return the stripped Strings, <code>null</code> if null array input
   */
  public static String[] stripAll(String[] strs, String stripChars) {
    int strsLen;
    if (strs == null || (strsLen = strs.length) == 0) {
      return strs;
    }
    String[] newArr = new String[strsLen];
    for (int i = 0; i < strsLen; i++) {
      newArr[i] = strip(strs[i], stripChars);
    }
    return newArr;
  }

  /**
   * <p>
   * Splits the provided text into an array, separators specified. This is an
   * alternative to using StringTokenizer.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as one separator. For more control over the split
   * use the StrTokenizer class.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separatorChars splits on whitespace.
   * </p>
   * 
   * <pre>
   * StringUtils.split(null, *)         = null
   * StringUtils.split("", *)           = []
   * StringUtils.split("abc def", null) = ["abc", "def"]
   * StringUtils.split("abc def", " ")  = ["abc", "def"]
   * StringUtils.split("abc  def", " ") = ["abc", "def"]
   * StringUtils.split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be null
   * @param separatorChars
   *          the characters used as the delimiters, <code>null</code> splits on
   *          whitespace
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  public static String[] split(String str, String separatorChars) {
    return splitWorker(str, separatorChars, -1, false);
  }

  /**
   * <p>
   * Splits the provided text into an array with a maximum length, separators
   * specified.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as one separator.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separatorChars splits on whitespace.
   * </p>
   * 
   * <p>
   * If more than <code>max</code> delimited substrings are found, the last
   * returned string includes all characters after the first
   * <code>max - 1</code> returned strings (including separator characters).
   * </p>
   * 
   * <pre>
   * StringUtils.split(null, *, *)            = null
   * StringUtils.split("", *, *)              = []
   * StringUtils.split("ab de fg", null, 0)   = ["ab", "cd", "ef"]
   * StringUtils.split("ab   de fg", null, 0) = ["ab", "cd", "ef"]
   * StringUtils.split("ab:cd:ef", ":", 0)    = ["ab", "cd", "ef"]
   * StringUtils.split("ab:cd:ef", ":", 2)    = ["ab", "cd:ef"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be null
   * @param separatorChars
   *          the characters used as the delimiters, <code>null</code> splits on
   *          whitespace
   * @param max
   *          the maximum number of elements to include in the array. A zero or
   *          negative value implies no limit
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  public static String[] split(String str, String separatorChars, int max) {
    return splitWorker(str, separatorChars, max, false);
  }

  // Splitting
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Splits the provided text into an array, using whitespace as the separator.
   * Whitespace is defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as one separator. For more control over the split
   * use the StrTokenizer class.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.split(null)       = null
   * StringUtils.split("")         = []
   * StringUtils.split("abc def")  = ["abc", "def"]
   * StringUtils.split("abc  def") = ["abc", "def"]
   * StringUtils.split(" abc ")    = ["abc"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be null
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  public static String[] split(String str) {
    return split(str, null, -1);
  }

  /**
   * Performs the logic for the
   * <code>splitByWholeSeparatorPreserveAllTokens</code> methods.
   * 
   * @param str
   *          the String to parse, may be <code>null</code>
   * @param separator
   *          String containing the String to be used as a delimiter,
   *          <code>null</code> splits on whitespace
   * @param max
   *          the maximum number of elements to include in the returned array. A
   *          zero or negative value implies no limit.
   * @param preserveAllTokens
   *          if <code>true</code>, adjacent separators are treated as empty
   *          token separators; if <code>false</code>, adjacent separators are
   *          treated as one separator.
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.4
   */
  private static String[] splitByWholeSeparatorWorker(String str,
      String separator, int max, boolean preserveAllTokens) {
    if (str == null) {
      return null;
    }

    int len = str.length();

    if (len == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    if ((separator == null) || (EMPTY.equals(separator))) {
      // Split on whitespace.
      return splitWorker(str, null, max, preserveAllTokens);
    }

    int separatorLength = separator.length();

    ArrayList<String> substrings = new ArrayList<String>();
    int numberOfSubstrings = 0;
    int beg = 0;
    int end = 0;
    while (end < len) {
      end = str.indexOf(separator, beg);

      if (end > -1) {
        if (end > beg) {
          numberOfSubstrings += 1;

          if (numberOfSubstrings == max) {
            end = len;
            substrings.add(str.substring(beg));
          } else {
            // The following is OK, because String.substring( beg, end )
            // excludes
            // the character at the position 'end'.
            substrings.add(str.substring(beg, end));

            // Set the starting point for the next search.
            // The following is equivalent to beg = end + (separatorLength - 1)
            // + 1,
            // which is the right calculation:
            beg = end + separatorLength;
          }
        } else {
          // We found a consecutive occurrence of the separator, so skip it.
          if (preserveAllTokens) {
            numberOfSubstrings += 1;
            if (numberOfSubstrings == max) {
              end = len;
              substrings.add(str.substring(beg));
            } else {
              substrings.add(EMPTY);
            }
          }
          beg = end + separatorLength;
        }
      } else {
        // String.substring( beg ) goes from 'beg' to the end of the String.
        substrings.add(str.substring(beg));
        end = len;
      }
    }

    return substrings.toArray(new String[substrings.size()]);
  }

  // -----------------------------------------------------------------------
  /**
   * <p>
   * Splits the provided text into an array, using whitespace as the separator,
   * preserving all tokens, including empty tokens created by adjacent
   * separators. This is an alternative to using StringTokenizer. Whitespace is
   * defined by {@link Character#isWhitespace(char)}.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as separators for empty tokens. For more control
   * over the split use the StrTokenizer class.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>.
   * </p>
   * 
   * <pre>
   * StringUtils.splitPreserveAllTokens(null)       = null
   * StringUtils.splitPreserveAllTokens("")         = []
   * StringUtils.splitPreserveAllTokens("abc def")  = ["abc", "def"]
   * StringUtils.splitPreserveAllTokens("abc  def") = ["abc", "", "def"]
   * StringUtils.splitPreserveAllTokens(" abc ")    = ["", "abc", ""]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be <code>null</code>
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.1
   */
  public static String[] splitPreserveAllTokens(String str) {
    return splitWorker(str, null, -1, true);
  }

  /**
   * <p>
   * Splits the provided text into an array, separator string specified.
   * </p>
   * 
   * <p>
   * The separator(s) will not be included in the returned String array.
   * Adjacent separators are treated as one separator.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separator splits on whitespace.
   * </p>
   * 
   * <pre>
   * StringUtils.splitByWholeSeparator(null, *)               = null
   * StringUtils.splitByWholeSeparator("", *)                 = []
   * StringUtils.splitByWholeSeparator("ab de fg", null)      = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab   de fg", null)    = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
   * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be null
   * @param separator
   *          String containing the String to be used as a delimiter,
   *          <code>null</code> splits on whitespace
   * @return an array of parsed Strings, <code>null</code> if null String was
   *         input
   */
  public static String[] splitByWholeSeparator(String str, String separator) {
    return splitByWholeSeparatorWorker(str, separator, -1, false);
  }

  /**
   * <p>
   * Splits the provided text into an array, separator string specified. Returns
   * a maximum of <code>max</code> substrings.
   * </p>
   * 
   * <p>
   * The separator(s) will not be included in the returned String array.
   * Adjacent separators are treated as one separator.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separator splits on whitespace.
   * </p>
   * 
   * <pre>
   * StringUtils.splitByWholeSeparator(null, *, *)               = null
   * StringUtils.splitByWholeSeparator("", *, *)                 = []
   * StringUtils.splitByWholeSeparator("ab de fg", null, 0)      = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab   de fg", null, 0)    = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab:cd:ef", ":", 2)       = ["ab", "cd:ef"]
   * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 5) = ["ab", "cd", "ef"]
   * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2) = ["ab", "cd-!-ef"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be null
   * @param separator
   *          String containing the String to be used as a delimiter,
   *          <code>null</code> splits on whitespace
   * @param max
   *          the maximum number of elements to include in the returned array. A
   *          zero or negative value implies no limit.
   * @return an array of parsed Strings, <code>null</code> if null String was
   *         input
   */
  public static String[] splitByWholeSeparator(String str, String separator,
      int max) {
    return splitByWholeSeparatorWorker(str, separator, max, false);
  }

  /**
   * <p>
   * Splits the provided text into an array, separator string specified.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as separators for empty tokens. For more control
   * over the split use the StrTokenizer class.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separator splits on whitespace.
   * </p>
   * 
   * <pre>
   * StringUtils.splitByWholeSeparatorPreserveAllTokens(null, *)               = null
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("", *)                 = []
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null)      = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null)    = ["ab", "", "", "de", "fg"]
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be null
   * @param separator
   *          String containing the String to be used as a delimiter,
   *          <code>null</code> splits on whitespace
   * @return an array of parsed Strings, <code>null</code> if null String was
   *         input
   * @since 2.4
   */
  public static String[] splitByWholeSeparatorPreserveAllTokens(String str,
      String separator) {
    return splitByWholeSeparatorWorker(str, separator, -1, true);
  }

  /**
   * <p>
   * Splits the provided text into an array, separator string specified. Returns
   * a maximum of <code>max</code> substrings.
   * </p>
   * 
   * <p>
   * The separator is not included in the returned String array. Adjacent
   * separators are treated as separators for empty tokens. For more control
   * over the split use the StrTokenizer class.
   * </p>
   * 
   * <p>
   * A <code>null</code> input String returns <code>null</code>. A
   * <code>null</code> separator splits on whitespace.
   * </p>
   * 
   * <pre>
   * StringUtils.splitByWholeSeparatorPreserveAllTokens(null, *, *)               = null
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("", *, *)                 = []
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null, 0)      = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null, 0)    = ["ab", "", "", "de", "fg"]
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":", 2)       = ["ab", "cd:ef"]
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 5) = ["ab", "cd", "ef"]
   * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 2) = ["ab", "cd-!-ef"]
   * </pre>
   * 
   * @param str
   *          the String to parse, may be null
   * @param separator
   *          String containing the String to be used as a delimiter,
   *          <code>null</code> splits on whitespace
   * @param max
   *          the maximum number of elements to include in the returned array. A
   *          zero or negative value implies no limit.
   * @return an array of parsed Strings, <code>null</code> if null String was
   *         input
   * @since 2.4
   */
  public static String[] splitByWholeSeparatorPreserveAllTokens(String str,
      String separator, int max) {
    return splitByWholeSeparatorWorker(str, separator, max, true);
  }

}
