/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.butent.bee.egg.shared.lang;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Operations on arrays, primitive arrays (like <code>int[]</code>) and
 * primitive wrapper arrays (like <code>Integer[]</code>).
 * </p>
 * 
 * <p>
 * This class tries to handle <code>null</code> input gracefully. An exception
 * will not be thrown for a <code>null</code> array input. However, an Object
 * array that contains a <code>null</code> element may throw an exception. Each
 * method documents its behaviour.
 * </p>
 * 
 * <p>
 * #ThreadSafe#
 * </p>
 * 
 * @author Apache Software Foundation
 * @author Moritz Petersen
 * @author <a href="mailto:fredrik@westermarck.com">Fredrik Westermarck</a>
 * @author Nikolay Metchev
 * @author Matthew Hawthorne
 * @author Tim O'Brien
 * @author Pete Gieser
 * @author Gary Gregory
 * @author <a href="mailto:equinus100@hotmail.com">Ashwin S</a>
 * @author Maarten Coene
 * @author <a href="mailto:levon@lk.otherinbox.com">Levon Karayan</a>
 * @since 2.0
 * @version $Id: ArrayUtils.java 918868 2010-03-04 06:22:16Z bayard $
 */
public class ArrayUtils {

  /**
   * An empty immutable <code>Object</code> array.
   */
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  /**
   * An empty immutable <code>Class</code> array.
   */
  public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
  /**
   * An empty immutable <code>String</code> array.
   */
  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  /**
   * An empty immutable <code>long</code> array.
   */
  public static final long[] EMPTY_LONG_ARRAY = new long[0];
  /**
   * An empty immutable <code>Long</code> array.
   */
  public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
  /**
   * An empty immutable <code>int</code> array.
   */
  public static final int[] EMPTY_INT_ARRAY = new int[0];
  /**
   * An empty immutable <code>Integer</code> array.
   */
  public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
  /**
   * An empty immutable <code>short</code> array.
   */
  public static final short[] EMPTY_SHORT_ARRAY = new short[0];
  /**
   * An empty immutable <code>Short</code> array.
   */
  public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
  /**
   * An empty immutable <code>byte</code> array.
   */
  public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  /**
   * An empty immutable <code>Byte</code> array.
   */
  public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
  /**
   * An empty immutable <code>double</code> array.
   */
  public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
  /**
   * An empty immutable <code>Double</code> array.
   */
  public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
  /**
   * An empty immutable <code>float</code> array.
   */
  public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
  /**
   * An empty immutable <code>Float</code> array.
   */
  public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
  /**
   * An empty immutable <code>boolean</code> array.
   */
  public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
  /**
   * An empty immutable <code>Boolean</code> array.
   */
  public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
  /**
   * An empty immutable <code>char</code> array.
   */
  public static final char[] EMPTY_CHAR_ARRAY = new char[0];
  /**
   * An empty immutable <code>Character</code> array.
   */
  public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

  /**
   * The index value when an element is not found in a list or array:
   * <code>-1</code>. This value is returned by methods in this class and can
   * also be used in comparisons with values returned by various method from
   * {@link java.util.List}.
   */
  public static final int INDEX_NOT_FOUND = -1;

  /**
   * <p>
   * ArrayUtils instances should NOT be constructed in standard programming.
   * Instead, the class should be used as
   * <code>ArrayUtils.clone(new int[] {2})</code>.
   * </p>
   * 
   * <p>
   * This constructor is public to permit tools that require a JavaBean instance
   * to operate.
   * </p>
   */
  public ArrayUtils() {
    super();
  }

  // To map
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Converts the given array into a {@link java.util.Map}. Each element of the
   * array must be either a {@link java.util.Map.Entry} or an Array, containing
   * at least two elements, where the first element is used as key and the
   * second as value.
   * </p>
   * 
   * <p>
   * This method can be used to initialize:
   * </p>
   * 
   * <pre>
   * // Create a Map mapping colors.
   * Map colorMap = MapUtils.toMap(new String[][] {{
   *     {"RED", "#FF0000"},
   *     {"GREEN", "#00FF00"},
   *     {"BLUE", "#0000FF"}});
   * </pre>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          an array whose elements are either a {@link java.util.Map.Entry}
   *          or an Array containing at least two elements, may be
   *          <code>null</code>
   * @return a <code>Map</code> that was created from the array
   * @throws IllegalArgumentException
   *           if one element of this Array is itself an Array containing less
   *           then two elements
   * @throws IllegalArgumentException
   *           if the array contains elements other than
   *           {@link java.util.Map.Entry} and an Array
   */
  public static Map<Object, Object> toMap(Object[] array) {
    if (array == null) {
      return null;
    }
    final Map<Object, Object> map = new HashMap<Object, Object>(
        (int) (array.length * 1.5));
    for (int i = 0; i < array.length; i++) {
      Object object = array[i];
      if (object instanceof Map.Entry<?, ?>) {
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
        map.put(entry.getKey(), entry.getValue());
      } else if (object instanceof Object[]) {
        Object[] entry = (Object[]) object;
        if (entry.length < 2) {
          throw new IllegalArgumentException("Array element " + i + ", '"
              + object + "', has a length less than 2");
        }
        map.put(entry[0], entry[1]);
      } else {
        throw new IllegalArgumentException("Array element " + i + ", '"
            + object + "', is neither of type Map.Entry nor an Array");
      }
    }
    return map;
  }

  // Generic array
  // -----------------------------------------------------------------------
  /**
   * Create a type-safe generic array.
   * 
   * <p>
   * Arrays are covariant i.e. they cannot be created from a generic type:
   * </p>
   * 
   * <pre>
   * public static &lt;T&gt; T[] createAnArray(int size) {
   *   return T[size]; // compiler error here
   * }
   * 
   * public static &lt;T&gt; T[] createAnArray(int size) {
   *   return (T[]) Object[size]; // ClassCastException at runtime
   * }
   * </pre>
   * 
   * <p>
   * Therefore new arrays of generic types can be created with this method, e.g.
   * an arrays of Strings:
   * </p>
   * 
   * <pre>
   * String[] array = ArrayUtils.toArray(&quot;1&quot;, &quot;2&quot;);
   * String[] emptyArray = ArrayUtils.&lt;String&gt; toArray();
   * </pre>
   * 
   * The method is typically used in scenarios, where the caller itself uses
   * generic types that have to be combined into an array.
   * 
   * Note, this method makes only sense to provide arguments of the same type so
   * that the compiler can deduce the type of the array itself. While it is
   * possible to select the type explicitly like in
   * <code>Number[] array = ArrayUtils.<Number>toArray(new
   * Integer(42), new Double(Math.PI))</code>, there is no real advantage to
   * <code>new
   * Number[] {new Integer(42), new Double(Math.PI)}</code> anymore.
   * 
   * @param <T>
   *          the array's element type
   * @param items
   *          the items of the array
   * @return the array
   * @since 3.0
   */
  public static <T> T[] toArray(final T... items) {
    return items;
  }

  // nullToEmpty
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Object[] nullToEmpty(Object[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static String[] nullToEmpty(String[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_STRING_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static long[] nullToEmpty(long[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_LONG_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static int[] nullToEmpty(int[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_INT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static short[] nullToEmpty(short[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_SHORT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static char[] nullToEmpty(char[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_CHAR_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static byte[] nullToEmpty(byte[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_BYTE_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static double[] nullToEmpty(double[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static float[] nullToEmpty(float[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_FLOAT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static boolean[] nullToEmpty(boolean[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_BOOLEAN_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Long[] nullToEmpty(Long[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_LONG_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Integer[] nullToEmpty(Integer[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_INTEGER_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Short[] nullToEmpty(Short[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_SHORT_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Character[] nullToEmpty(Character[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_CHARACTER_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Byte[] nullToEmpty(Byte[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_BYTE_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Double[] nullToEmpty(Double[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_DOUBLE_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Float[] nullToEmpty(Float[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_FLOAT_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Defensive programming technique to change a <code>null</code> reference to
   * an empty one.
   * </p>
   * 
   * <p>
   * This method returns an empty array for a <code>null</code> input array.
   * </p>
   * 
   * <p>
   * As a memory optimizing technique an empty array passed in will be
   * overridden with the empty <code>public static</code> references in this
   * class.
   * </p>
   * 
   * @param array
   *          the array to check for <code>null</code> or empty
   * @return the same array, <code>public static</code> empty array if
   *         <code>null</code> or empty input
   * @since 2.5
   */
  public static Boolean[] nullToEmpty(Boolean[] array) {
    if (array == null || array.length == 0) {
      return EMPTY_BOOLEAN_OBJECT_ARRAY;
    }
    return array;
  }

  /**
   * <p>
   * Produces a new <code>long</code> array containing the elements between the
   * start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static long[] subarray(long[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_LONG_ARRAY;
    }

    long[] subarray = new long[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * <p>
   * Produces a new <code>int</code> array containing the elements between the
   * start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static int[] subarray(int[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_INT_ARRAY;
    }

    int[] subarray = new int[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * <p>
   * Produces a new <code>short</code> array containing the elements between the
   * start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static short[] subarray(short[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_SHORT_ARRAY;
    }

    short[] subarray = new short[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * <p>
   * Produces a new <code>char</code> array containing the elements between the
   * start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static char[] subarray(char[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_CHAR_ARRAY;
    }

    char[] subarray = new char[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * <p>
   * Produces a new <code>byte</code> array containing the elements between the
   * start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static byte[] subarray(byte[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_BYTE_ARRAY;
    }

    byte[] subarray = new byte[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * <p>
   * Produces a new <code>double</code> array containing the elements between
   * the start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static double[] subarray(double[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_DOUBLE_ARRAY;
    }

    double[] subarray = new double[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * <p>
   * Produces a new <code>float</code> array containing the elements between the
   * start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static float[] subarray(float[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_FLOAT_ARRAY;
    }

    float[] subarray = new float[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * <p>
   * Produces a new <code>boolean</code> array containing the elements between
   * the start and end indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * @param array
   *          the array
   * @param startIndexInclusive
   *          the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *          (&gt;array.length) results in an empty array.
   * @param endIndexExclusive
   *          elements up to endIndex-1 are present in the returned subarray.
   *          Undervalue (&lt; startIndex) produces empty array, overvalue
   *          (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end
   *         indices.
   * @since 2.1
   */
  public static boolean[] subarray(boolean[] array, int startIndexInclusive,
      int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_BOOLEAN_ARRAY;
    }

    boolean[] subarray = new boolean[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  // Is same length
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * 
   * <p>
   * Any multi-dimensional aspects of the arrays are ignored.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(Object[] array1, Object[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(long[] array1, long[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(int[] array1, int[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(short[] array1, short[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(char[] array1, char[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(byte[] array1, byte[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(double[] array1, double[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(float[] array1, float[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same length, treating <code>null</code>
   * arrays as length <code>0</code>.
   * </p>
   * 
   * @param array1
   *          the first array, may be <code>null</code>
   * @param array2
   *          the second array, may be <code>null</code>
   * @return <code>true</code> if length of arrays matches, treating
   *         <code>null</code> as an empty array
   */
  public static boolean isSameLength(boolean[] array1, boolean[] array2) {
    if ((array1 == null && array2 != null && array2.length > 0)
        || (array2 == null && array1 != null && array1.length > 0)
        || (array1 != null && array2 != null && array1.length != array2.length)) {
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Checks whether two arrays are the same type taking into account
   * multi-dimensional arrays.
   * </p>
   * 
   * @param array1
   *          the first array, must not be <code>null</code>
   * @param array2
   *          the second array, must not be <code>null</code>
   * @return <code>true</code> if type of arrays matches
   * @throws IllegalArgumentException
   *           if either array is <code>null</code>
   */
  public static boolean isSameType(Object array1, Object array2) {
    if (array1 == null || array2 == null) {
      throw new IllegalArgumentException("The Array must not be null");
    }
    return array1.getClass().getName().equals(array2.getClass().getName());
  }

  // Reverse
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * There is no special handling for multi-dimensional arrays.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(Object[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    Object tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(long[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    long tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(int[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    int tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(short[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    short tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(char[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    char tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(byte[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(double[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    double tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(float[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    float tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  /**
   * <p>
   * Reverses the order of the given array.
   * </p>
   * 
   * <p>
   * This method does nothing for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to reverse, may be <code>null</code>
   */
  public static void reverse(boolean[] array) {
    if (array == null) {
      return;
    }
    int i = 0;
    int j = array.length - 1;
    boolean tmp;
    while (j > i) {
      tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    }
  }

  // IndexOf search
  // ----------------------------------------------------------------------

  // long IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(long[] array, long valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(long[] array, long valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(long[] array, long valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(long[] array, long valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(long[] array, long valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  // int IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(int[] array, int valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(int[] array, int valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(int[] array, int valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(int[] array, int valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(int[] array, int valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  // short IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(short[] array, short valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(short[] array, short valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(short[] array, short valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(short[] array, short valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(short[] array, short valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  // char IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   * @since 2.1
   */
  public static int indexOf(char[] array, char valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   * @since 2.1
   */
  public static int indexOf(char[] array, char valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   * @since 2.1
   */
  public static int lastIndexOf(char[] array, char valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   * @since 2.1
   */
  public static int lastIndexOf(char[] array, char valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   * @since 2.1
   */
  public static boolean contains(char[] array, char valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  // byte IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(byte[] array, byte valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(byte[] array, byte valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(byte[] array, byte valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(byte[] array, byte valueToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(byte[] array, byte valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  // double IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(double[] array, double valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value within a given tolerance in the array.
   * This method will return the index of the first value which falls between
   * the region defined by valueToFind - tolerance and valueToFind + tolerance.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param tolerance
   *          tolerance of the search
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(double[] array, double valueToFind, double tolerance) {
    return indexOf(array, valueToFind, 0, tolerance);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(double[] array, double valueToFind, int startIndex) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index. This method will return the index of the first value which falls
   * between the region defined by valueToFind - tolerance and valueToFind +
   * tolerance.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @param tolerance
   *          tolerance of the search
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(double[] array, double valueToFind, int startIndex,
      double tolerance) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    double min = valueToFind - tolerance;
    double max = valueToFind + tolerance;
    for (int i = startIndex; i < array.length; i++) {
      if (array[i] >= min && array[i] <= max) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(double[] array, double valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value within a given tolerance in the
   * array. This method will return the index of the last value which falls
   * between the region defined by valueToFind - tolerance and valueToFind +
   * tolerance.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param tolerance
   *          tolerance of the search
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int lastIndexOf(double[] array, double valueToFind,
      double tolerance) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE, tolerance);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(double[] array, double valueToFind,
      int startIndex) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index. This method will return the index of the last value which falls
   * between the region defined by valueToFind - tolerance and valueToFind +
   * tolerance.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @param tolerance
   *          search for value within plus/minus this amount
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(double[] array, double valueToFind,
      int startIndex, double tolerance) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    double min = valueToFind - tolerance;
    double max = valueToFind + tolerance;
    for (int i = startIndex; i >= 0; i--) {
      if (array[i] >= min && array[i] <= max) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(double[] array, double valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if a value falling within the given tolerance is in the given array.
   * If the array contains a value within the inclusive range defined by (value
   * - tolerance) to (value + tolerance).
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search
   * @param valueToFind
   *          the value to find
   * @param tolerance
   *          the array contains the tolerance of the search
   * @return true if value falling within tolerance is in array
   */
  public static boolean contains(double[] array, double valueToFind,
      double tolerance) {
    return indexOf(array, valueToFind, 0, tolerance) != INDEX_NOT_FOUND;
  }

  // float IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(float[] array, float valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(float[] array, float valueToFind, int startIndex) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(float[] array, float valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(float[] array, float valueToFind, int startIndex) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(float[] array, float valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  // boolean IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given value in the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(boolean[] array, boolean valueToFind) {
    return indexOf(array, valueToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex is treated as zero. A startIndex larger than the
   * array length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).
   * </p>
   * 
   * @param array
   *          the array to search through for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the index to start searching at
   * @return the index of the value within the array, {@link #INDEX_NOT_FOUND} (
   *         <code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(boolean[] array, boolean valueToFind, int startIndex) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    for (int i = startIndex; i < array.length; i++) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given value within the array.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) if
   * <code>null</code> array input.
   * </p>
   * 
   * @param array
   *          the array to travers backwords looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the object to find
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(boolean[] array, boolean valueToFind) {
    return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given value in the array starting at the given
   * index.
   * </p>
   * 
   * <p>
   * This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a
   * <code>null</code> input array.
   * </p>
   * 
   * <p>
   * A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>
   * ). A startIndex larger than the array length will search from the end of
   * the array.
   * </p>
   * 
   * @param array
   *          the array to traverse for looking for the object, may be
   *          <code>null</code>
   * @param valueToFind
   *          the value to find
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the value within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(boolean[] array, boolean valueToFind,
      int startIndex) {
    if (ArrayUtils.isEmpty(array)) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    for (int i = startIndex; i >= 0; i--) {
      if (valueToFind == array[i]) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Checks if the value is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param valueToFind
   *          the value to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(boolean[] array, boolean valueToFind) {
    return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
  }

  // Primitive/Object array converters
  // ----------------------------------------------------------------------

  // Character array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Characters to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Character</code> array, may be <code>null</code>
   * @return a <code>char</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static char[] toPrimitive(Character[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_CHAR_ARRAY;
    }
    final char[] result = new char[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].charValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Character to primitives handling
   * <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Character</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return a <code>char</code> array, <code>null</code> if null array input
   */
  public static char[] toPrimitive(Character[] array, char valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_CHAR_ARRAY;
    }
    final char[] result = new char[array.length];
    for (int i = 0; i < array.length; i++) {
      Character b = array[i];
      result[i] = (b == null ? valueForNull : b.charValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive chars to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>char</code> array
   * @return a <code>Character</code> array, <code>null</code> if null array
   *         input
   */
  public static Character[] toObject(char[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_CHARACTER_OBJECT_ARRAY;
    }
    final Character[] result = new Character[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = Character.valueOf(array[i]);
    }
    return result;
  }

  // Long array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Longs to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Long</code> array, may be <code>null</code>
   * @return a <code>long</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static long[] toPrimitive(Long[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_LONG_ARRAY;
    }
    final long[] result = new long[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].longValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Long to primitives handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Long</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return a <code>long</code> array, <code>null</code> if null array input
   */
  public static long[] toPrimitive(Long[] array, long valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_LONG_ARRAY;
    }
    final long[] result = new long[array.length];
    for (int i = 0; i < array.length; i++) {
      Long b = array[i];
      result[i] = (b == null ? valueForNull : b.longValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive longs to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>long</code> array
   * @return a <code>Long</code> array, <code>null</code> if null array input
   */
  public static Long[] toObject(long[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_LONG_OBJECT_ARRAY;
    }
    final Long[] result = new Long[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = Long.valueOf(array[i]);
    }
    return result;
  }

  // Int array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Integers to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Integer</code> array, may be <code>null</code>
   * @return an <code>int</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static int[] toPrimitive(Integer[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_INT_ARRAY;
    }
    final int[] result = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].intValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Integer to primitives handling
   * <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Integer</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return an <code>int</code> array, <code>null</code> if null array input
   */
  public static int[] toPrimitive(Integer[] array, int valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_INT_ARRAY;
    }
    final int[] result = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      Integer b = array[i];
      result[i] = (b == null ? valueForNull : b.intValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive ints to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          an <code>int</code> array
   * @return an <code>Integer</code> array, <code>null</code> if null array
   *         input
   */
  public static Integer[] toObject(int[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_INTEGER_OBJECT_ARRAY;
    }
    final Integer[] result = new Integer[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = Integer.valueOf(array[i]);
    }
    return result;
  }

  // Short array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Shorts to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Short</code> array, may be <code>null</code>
   * @return a <code>byte</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static short[] toPrimitive(Short[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_SHORT_ARRAY;
    }
    final short[] result = new short[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].shortValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Short to primitives handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Short</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return a <code>byte</code> array, <code>null</code> if null array input
   */
  public static short[] toPrimitive(Short[] array, short valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_SHORT_ARRAY;
    }
    final short[] result = new short[array.length];
    for (int i = 0; i < array.length; i++) {
      Short b = array[i];
      result[i] = (b == null ? valueForNull : b.shortValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive shorts to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>short</code> array
   * @return a <code>Short</code> array, <code>null</code> if null array input
   */
  public static Short[] toObject(short[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_SHORT_OBJECT_ARRAY;
    }
    final Short[] result = new Short[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = Short.valueOf(array[i]);
    }
    return result;
  }

  // Byte array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Bytes to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Byte</code> array, may be <code>null</code>
   * @return a <code>byte</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static byte[] toPrimitive(Byte[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BYTE_ARRAY;
    }
    final byte[] result = new byte[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].byteValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Bytes to primitives handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Byte</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return a <code>byte</code> array, <code>null</code> if null array input
   */
  public static byte[] toPrimitive(Byte[] array, byte valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BYTE_ARRAY;
    }
    final byte[] result = new byte[array.length];
    for (int i = 0; i < array.length; i++) {
      Byte b = array[i];
      result[i] = (b == null ? valueForNull : b.byteValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive bytes to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>byte</code> array
   * @return a <code>Byte</code> array, <code>null</code> if null array input
   */
  public static Byte[] toObject(byte[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BYTE_OBJECT_ARRAY;
    }
    final Byte[] result = new Byte[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = Byte.valueOf(array[i]);
    }
    return result;
  }

  // Double array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Doubles to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Double</code> array, may be <code>null</code>
   * @return a <code>double</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static double[] toPrimitive(Double[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    }
    final double[] result = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].doubleValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Doubles to primitives handling
   * <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Double</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return a <code>double</code> array, <code>null</code> if null array input
   */
  public static double[] toPrimitive(Double[] array, double valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    }
    final double[] result = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      Double b = array[i];
      result[i] = (b == null ? valueForNull : b.doubleValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive doubles to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>double</code> array
   * @return a <code>Double</code> array, <code>null</code> if null array input
   */
  public static Double[] toObject(double[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_DOUBLE_OBJECT_ARRAY;
    }
    final Double[] result = new Double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = Double.valueOf(array[i]);
    }
    return result;
  }

  // Float array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Floats to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Float</code> array, may be <code>null</code>
   * @return a <code>float</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static float[] toPrimitive(Float[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_FLOAT_ARRAY;
    }
    final float[] result = new float[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].floatValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Floats to primitives handling <code>null</code>
   * .
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Float</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return a <code>float</code> array, <code>null</code> if null array input
   */
  public static float[] toPrimitive(Float[] array, float valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_FLOAT_ARRAY;
    }
    final float[] result = new float[array.length];
    for (int i = 0; i < array.length; i++) {
      Float b = array[i];
      result[i] = (b == null ? valueForNull : b.floatValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive floats to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>float</code> array
   * @return a <code>Float</code> array, <code>null</code> if null array input
   */
  public static Float[] toObject(float[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_FLOAT_OBJECT_ARRAY;
    }
    final Float[] result = new Float[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = Float.valueOf(array[i]);
    }
    return result;
  }

  // Boolean array converters
  // ----------------------------------------------------------------------
  /**
   * <p>
   * Converts an array of object Booleans to primitives.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Boolean</code> array, may be <code>null</code>
   * @return a <code>boolean</code> array, <code>null</code> if null array input
   * @throws NullPointerException
   *           if array content is <code>null</code>
   */
  public static boolean[] toPrimitive(Boolean[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BOOLEAN_ARRAY;
    }
    final boolean[] result = new boolean[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].booleanValue();
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of object Booleans to primitives handling
   * <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>Boolean</code> array, may be <code>null</code>
   * @param valueForNull
   *          the value to insert if <code>null</code> found
   * @return a <code>boolean</code> array, <code>null</code> if null array input
   */
  public static boolean[] toPrimitive(Boolean[] array, boolean valueForNull) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BOOLEAN_ARRAY;
    }
    final boolean[] result = new boolean[array.length];
    for (int i = 0; i < array.length; i++) {
      Boolean b = array[i];
      result[i] = (b == null ? valueForNull : b.booleanValue());
    }
    return result;
  }

  /**
   * <p>
   * Converts an array of primitive booleans to objects.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          a <code>boolean</code> array
   * @return a <code>Boolean</code> array, <code>null</code> if null array input
   */
  public static Boolean[] toObject(boolean[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BOOLEAN_OBJECT_ARRAY;
    }
    final Boolean[] result = new Boolean[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = (array[i] ? Boolean.TRUE : Boolean.FALSE);
    }
    return result;
  }

  // ----------------------------------------------------------------------
  /**
   * <p>
   * Checks if an array of Objects is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static <T> boolean isEmpty(T[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive longs is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(long[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive ints is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(int[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive shorts is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(short[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive chars is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(char[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive bytes is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(byte[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive doubles is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(double[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive floats is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(float[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>
   * Checks if an array of primitive booleans is empty or <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is empty or <code>null</code>
   * @since 2.1
   */
  public static boolean isEmpty(boolean[] array) {
    if (array == null || array.length == 0) {
      return true;
    }
    return false;
  }

  // ----------------------------------------------------------------------
  /**
   * <p>
   * Checks if an array of Objects is not empty or not <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static <T> boolean isNotEmpty(T[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive longs is not empty or not <code>null</code>
   * .
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(long[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive ints is not empty or not <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(int[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive shorts is not empty or not
   * <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(short[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive chars is not empty or not <code>null</code>
   * .
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(char[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive bytes is not empty or not <code>null</code>
   * .
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(byte[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive doubles is not empty or not
   * <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(double[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive floats is not empty or not
   * <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(float[] array) {
    return (array != null && array.length != 0);
  }

  /**
   * <p>
   * Checks if an array of primitive booleans is not empty or not
   * <code>null</code>.
   * </p>
   * 
   * @param array
   *          the array to test
   * @return <code>true</code> if the array is not empty or not
   *         <code>null</code>
   * @since 2.5
   */
  public static boolean isNotEmpty(boolean[] array) {
    return (array != null && array.length != 0);
  }

}
