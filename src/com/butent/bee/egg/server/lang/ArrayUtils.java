package com.butent.bee.egg.server.lang;

import java.lang.reflect.Array;

import com.butent.bee.egg.server.lang.builder.EqualsBuilder;
import com.butent.bee.egg.server.lang.builder.ToStringBuilder;
import com.butent.bee.egg.server.lang.builder.ToStringStyle;

public class ArrayUtils extends com.butent.bee.egg.shared.lang.ArrayUtils {
  // Basic methods handling multi-dimensional arrays
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Outputs an array as a String, treating <code>null</code> as an empty array.
   * </p>
   * 
   * <p>
   * Multi-dimensional arrays are handled correctly, including multi-dimensional
   * primitive arrays.
   * </p>
   * 
   * <p>
   * The format is that of Java source code, for example <code>{a,b}</code>.
   * </p>
   * 
   * @param array
   *          the array to get a toString for, may be <code>null</code>
   * @return a String representation of the array, '{}' if null array input
   */
  public static String toString(Object array) {
    return toString(array, "{}");
  }

  /**
   * <p>
   * Outputs an array as a String handling <code>null</code>s.
   * </p>
   * 
   * <p>
   * Multi-dimensional arrays are handled correctly, including multi-dimensional
   * primitive arrays.
   * </p>
   * 
   * <p>
   * The format is that of Java source code, for example <code>{a,b}</code>.
   * </p>
   * 
   * @param array
   *          the array to get a toString for, may be <code>null</code>
   * @param stringIfNull
   *          the String to return if the array is <code>null</code>
   * @return a String representation of the array
   */
  public static String toString(Object array, String stringIfNull) {
    if (array == null) {
      return stringIfNull;
    }
    return new ToStringBuilder(array, ToStringStyle.SIMPLE_STYLE).append(array)
        .toString();
  }

  /**
   * <p>
   * Compares two arrays, using equals(), handling multi-dimensional arrays
   * correctly.
   * </p>
   * 
   * <p>
   * Multi-dimensional primitive arrays are also handled correctly by this
   * method.
   * </p>
   * 
   * @param array1
   *          the left hand array to compare, may be <code>null</code>
   * @param array2
   *          the right hand array to compare, may be <code>null</code>
   * @return <code>true</code> if the arrays are equal
   */
  public static boolean isEquals(Object array1, Object array2) {
    return new EqualsBuilder().append(array1, array2).isEquals();
  }

  // Clone
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Shallow clones an array returning a typecast result and handling
   * <code>null</code>.
   * </p>
   * 
   * <p>
   * The objects in the array are not cloned, thus there is no special handling
   * for multi-dimensional arrays.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to shallow clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static <T> T[] clone(T[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static long[] clone(long[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static int[] clone(int[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static short[] clone(short[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static char[] clone(char[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static byte[] clone(byte[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static double[] clone(double[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static float[] clone(float[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * <p>
   * Clones an array returning a typecast result and handling <code>null</code>.
   * </p>
   * 
   * <p>
   * This method returns <code>null</code> for a <code>null</code> input array.
   * </p>
   * 
   * @param array
   *          the array to clone, may be <code>null</code>
   * @return the cloned array, <code>null</code> if <code>null</code> input
   */
  public static boolean[] clone(boolean[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  // Subarrays
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Produces a new array containing the elements between the start and end
   * indices.
   * </p>
   * 
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input
   * produces null output.
   * </p>
   * 
   * <p>
   * The component type of the subarray is always the same as that of the input
   * array. Thus, if the input is an array of type <code>Date</code>, the
   * following usage is envisaged:
   * </p>
   * 
   * <pre>
   * Date[] someDates = (Date[]) ArrayUtils.subarray(allDates, 2, 5);
   * </pre>
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
  public static <T> T[] subarray(T[] array, int startIndexInclusive,
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
    Class<?> type = array.getClass().getComponentType();
    if (newSize <= 0) {
      @SuppressWarnings("unchecked")
      // OK, because array is of type T
      final T[] emptyArray = (T[]) Array.newInstance(type, 0);
      return emptyArray;
    }
    @SuppressWarnings("unchecked")
    // OK, because array is of type T
    T[] subarray = (T[]) Array.newInstance(type, newSize);
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  // Object IndexOf
  // -----------------------------------------------------------------------
  /**
   * <p>
   * Finds the index of the given object in the array.
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
   * @param objectToFind
   *          the object to find, may be <code>null</code>
   * @return the index of the object within the array, {@link #INDEX_NOT_FOUND}
   *         (<code>-1</code>) if not found or <code>null</code> array input
   */
  public static int indexOf(Object[] array, Object objectToFind) {
    return indexOf(array, objectToFind, 0);
  }

  /**
   * <p>
   * Finds the index of the given object in the array starting at the given
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
   * @param objectToFind
   *          the object to find, may be <code>null</code>
   * @param startIndex
   *          the index to start searching at
   * @return the index of the object within the array starting at the index,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    if (objectToFind == null) {
      for (int i = startIndex; i < array.length; i++) {
        if (array[i] == null) {
          return i;
        }
      }
    } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
      for (int i = startIndex; i < array.length; i++) {
        if (objectToFind.equals(array[i])) {
          return i;
        }
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Finds the last index of the given object within the array.
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
   * @param objectToFind
   *          the object to find, may be <code>null</code>
   * @return the last index of the object within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(Object[] array, Object objectToFind) {
    return lastIndexOf(array, objectToFind, Integer.MAX_VALUE);
  }

  /**
   * <p>
   * Finds the last index of the given object in the array starting at the given
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
   * @param objectToFind
   *          the object to find, may be <code>null</code>
   * @param startIndex
   *          the start index to travers backwards from
   * @return the last index of the object within the array,
   *         {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or
   *         <code>null</code> array input
   */
  public static int lastIndexOf(Object[] array, Object objectToFind,
      int startIndex) {
    if (array == null) {
      return INDEX_NOT_FOUND;
    }
    if (startIndex < 0) {
      return INDEX_NOT_FOUND;
    } else if (startIndex >= array.length) {
      startIndex = array.length - 1;
    }
    if (objectToFind == null) {
      for (int i = startIndex; i >= 0; i--) {
        if (array[i] == null) {
          return i;
        }
      }
    } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
      for (int i = startIndex; i >= 0; i--) {
        if (objectToFind.equals(array[i])) {
          return i;
        }
      }
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(null, null)     = null
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * ArrayUtils.addAll([null], [null]) = [null, null]
   * ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array, may be
   *          <code>null</code>
   * @param array2
   *          the second array whose elements are added to the new array, may be
   *          <code>null</code>
   * @return The new array, <code>null</code> if both arrays are
   *         <code>null</code>. The type of the new array is the type of the
   *         first array, unless the first array is null, in which case the type
   *         is the same as the second array.
   * @since 2.1
   * @throws IllegalArgumentException
   *           if the array types are incompatible
   */
  public static <T> T[] addAll(T[] array1, T... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    final Class<?> type1 = array1.getClass().getComponentType();
    @SuppressWarnings("unchecked")
    // OK, because array is of type T
    T[] joinedArray = (T[]) Array.newInstance(type1, array1.length
        + array2.length);
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    try {
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    } catch (ArrayStoreException ase) {
      // Check if problem was due to incompatible types
      /*
       * We do this here, rather than before the copy because:
       * - it would be a wasted check most of the time
       * - safer, in case check turns out to be too strict
       */
      final Class<?> type2 = array2.getClass().getComponentType();
      if (!type1.isAssignableFrom(type2)) {
        throw new IllegalArgumentException("Cannot store " + type2.getName()
            + " in an array of " + type1.getName(), ase);
      }
      throw ase; // No, so rethrow original
    }
    return joinedArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, true)          = [true]
   * ArrayUtils.add([true], false)       = [true, false]
   * ArrayUtils.add([true, false], true) = [true, false, true]
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static boolean[] add(boolean[] array, boolean element) {
    boolean[] newArray = (boolean[]) copyArrayGrow1(array, Boolean.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0)   = [0]
   * ArrayUtils.add([1], 0)    = [1, 0]
   * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static byte[] add(byte[] array, byte element) {
    byte[] newArray = (byte[]) copyArrayGrow1(array, Byte.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, '0')       = ['0']
   * ArrayUtils.add(['1'], '0')      = ['1', '0']
   * ArrayUtils.add(['1', '0'], '1') = ['1', '0', '1']
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static char[] add(char[] array, char element) {
    char[] newArray = (char[]) copyArrayGrow1(array, Character.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0)   = [0]
   * ArrayUtils.add([1], 0)    = [1, 0]
   * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static double[] add(double[] array, double element) {
    double[] newArray = (double[]) copyArrayGrow1(array, Double.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0)   = [0]
   * ArrayUtils.add([1], 0)    = [1, 0]
   * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static float[] add(float[] array, float element) {
    float[] newArray = (float[]) copyArrayGrow1(array, Float.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0)   = [0]
   * ArrayUtils.add([1], 0)    = [1, 0]
   * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static int[] add(int[] array, int element) {
    int[] newArray = (int[]) copyArrayGrow1(array, Integer.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0)   = [0]
   * ArrayUtils.add([1], 0)    = [1, 0]
   * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static long[] add(long[] array, long element) {
    long[] newArray = (long[]) copyArrayGrow1(array, Long.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0)   = [0]
   * ArrayUtils.add([1], 0)    = [1, 0]
   * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
   * </pre>
   * 
   * @param array
   *          the array to copy and add the element to, may be <code>null</code>
   * @param element
   *          the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   * @since 2.1
   */
  public static short[] add(short[] array, short element) {
    short[] newArray = (short[]) copyArrayGrow1(array, Short.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * Returns a copy of the given array of size 1 greater than the argument. The
   * last value of the array is left to the default value.
   * 
   * @param array
   *          The array to copy, must not be <code>null</code>.
   * @param newArrayComponentType
   *          If <code>array</code> is <code>null</code>, create a size 1 array
   *          of this type.
   * @return A new copy of the array of size 1 greater than the input.
   */
  private static Object copyArrayGrow1(Object array,
      Class<?> newArrayComponentType) {
    if (array != null) {
      int arrayLength = Array.getLength(array);
      Object newArray = Array.newInstance(array.getClass().getComponentType(),
          arrayLength + 1);
      System.arraycopy(array, 0, newArray, 0, arrayLength);
      return newArray;
    }
    return Array.newInstance(newArrayComponentType, 1);
  }

  // -----------------------------------------------------------------------
  /**
   * <p>
   * Returns the length of the specified array. This method can deal with
   * <code>Object</code> arrays and with primitive arrays.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, <code>0</code> is returned.
   * </p>
   * 
   * <pre>
   * ArrayUtils.getLength(null)            = 0
   * ArrayUtils.getLength([])              = 0
   * ArrayUtils.getLength([null])          = 1
   * ArrayUtils.getLength([true, false])   = 2
   * ArrayUtils.getLength([1, 2, 3])       = 3
   * ArrayUtils.getLength(["a", "b", "c"]) = 3
   * </pre>
   * 
   * @param array
   *          the array to retrieve the length from, may be null
   * @return The length of the array, or <code>0</code> if the array is
   *         <code>null</code>
   * @throws IllegalArgumentException
   *           if the object arguement is not an array.
   * @since 2.1
   */
  public static int getLength(Object array) {
    if (array == null) {
      return 0;
    }
    return Array.getLength(array);
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new boolean[] array.
   * @since 2.1
   */
  public static boolean[] addAll(boolean[] array1, boolean... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    boolean[] joinedArray = new boolean[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new char[] array.
   * @since 2.1
   */
  public static char[] addAll(char[] array1, char... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    char[] joinedArray = new char[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new byte[] array.
   * @since 2.1
   */
  public static byte[] addAll(byte[] array1, byte... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    byte[] joinedArray = new byte[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new short[] array.
   * @since 2.1
   */
  public static short[] addAll(short[] array1, short... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    short[] joinedArray = new short[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new int[] array.
   * @since 2.1
   */
  public static int[] addAll(int[] array1, int... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    int[] joinedArray = new int[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new long[] array.
   * @since 2.1
   */
  public static long[] addAll(long[] array1, long... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    long[] joinedArray = new long[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new float[] array.
   * @since 2.1
   */
  public static float[] addAll(float[] array1, float... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    float[] joinedArray = new float[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Adds all the elements of the given arrays into a new array.
   * </p>
   * <p>
   * The new array contains all of the element of <code>array1</code> followed
   * by all of the elements <code>array2</code>. When an array is returned, it
   * is always a new array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   * 
   * @param array1
   *          the first array whose elements are added to the new array.
   * @param array2
   *          the second array whose elements are added to the new array.
   * @return The new double[] array.
   * @since 2.1
   */
  public static double[] addAll(double[] array1, double... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    double[] joinedArray = new double[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * <p>
   * Copies the given array and adds the given element at the end of the new
   * array.
   * </p>
   * 
   * <p>
   * The new array contains the same elements of the input array plus the given
   * element in the last position. The component type of the new array is the
   * same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element, unless the
   * element itself is null, in which case the return type is Object[]
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, null)      = [null]
   * ArrayUtils.add(null, "a")       = ["a"]
   * ArrayUtils.add(["a"], null)     = ["a", null]
   * ArrayUtils.add(["a"], "b")      = ["a", "b"]
   * ArrayUtils.add(["a", "b"], "c") = ["a", "b", "c"]
   * </pre>
   * 
   * @param array
   *          the array to "add" the element to, may be <code>null</code>
   * @param element
   *          the object to add, may be <code>null</code>
   * @return A new array containing the existing elements plus the new element
   *         The returned array type will be that of the input array (unless
   *         null), in which case it will have the same type as the element. If
   *         both are null, an IllegalArgumentException is thrown
   * @since 2.1
   * @throws IllegalArgumentException
   *           if both arguments are null
   */
  public static <T> T[] add(T[] array, T element) {
    Class<?> type;
    if (array != null) {
      type = array.getClass();
    } else if (element != null) {
      type = element.getClass();
    } else {
      throw new IllegalArgumentException("Arguments cannot both be null");
    }
    @SuppressWarnings("unchecked")
    // type must be T
    T[] newArray = (T[]) copyArrayGrow1(array, type);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0, null)      = [null]
   * ArrayUtils.add(null, 0, "a")       = ["a"]
   * ArrayUtils.add(["a"], 1, null)     = ["a", null]
   * ArrayUtils.add(["a"], 1, "b")      = ["a", "b"]
   * ArrayUtils.add(["a", "b"], 3, "c") = ["a", "b", "c"]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   * @throws IllegalArgumentException
   *           if both array and element are null
   */
  public static <T> T[] add(T[] array, int index, T element) {
    Class<?> clss = null;
    if (array != null) {
      clss = array.getClass().getComponentType();
    } else if (element != null) {
      clss = element.getClass();
    } else {
      throw new IllegalArgumentException(
          "Array and element cannot both be null");
    }
    @SuppressWarnings("unchecked")
    // the add method creates an array of type clss, which is type T
    final T[] newArray = (T[]) add(array, index, element, clss);
    return newArray;
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0, true)          = [true]
   * ArrayUtils.add([true], 0, false)       = [false, true]
   * ArrayUtils.add([false], 1, true)       = [false, true]
   * ArrayUtils.add([true, false], 1, true) = [true, true, false]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static boolean[] add(boolean[] array, int index, boolean element) {
    return (boolean[]) add(array, index, Boolean.valueOf(element), Boolean.TYPE);
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add(null, 0, 'a')            = ['a']
   * ArrayUtils.add(['a'], 0, 'b')           = ['b', 'a']
   * ArrayUtils.add(['a', 'b'], 0, 'c')      = ['c', 'a', 'b']
   * ArrayUtils.add(['a', 'b'], 1, 'k')      = ['a', 'k', 'b']
   * ArrayUtils.add(['a', 'b', 'c'], 1, 't') = ['a', 't', 'b', 'c']
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static char[] add(char[] array, int index, char element) {
    return (char[]) add(array, index, Character.valueOf(element),
        Character.TYPE);
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add([1], 0, 2)         = [2, 1]
   * ArrayUtils.add([2, 6], 2, 3)      = [2, 6, 3]
   * ArrayUtils.add([2, 6], 0, 1)      = [1, 2, 6]
   * ArrayUtils.add([2, 6, 3], 2, 1)   = [2, 6, 1, 3]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static byte[] add(byte[] array, int index, byte element) {
    return (byte[]) add(array, index, Byte.valueOf(element), Byte.TYPE);
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add([1], 0, 2)         = [2, 1]
   * ArrayUtils.add([2, 6], 2, 10)     = [2, 6, 10]
   * ArrayUtils.add([2, 6], 0, -4)     = [-4, 2, 6]
   * ArrayUtils.add([2, 6, 3], 2, 1)   = [2, 6, 1, 3]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static short[] add(short[] array, int index, short element) {
    return (short[]) add(array, index, Short.valueOf(element), Short.TYPE);
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add([1], 0, 2)         = [2, 1]
   * ArrayUtils.add([2, 6], 2, 10)     = [2, 6, 10]
   * ArrayUtils.add([2, 6], 0, -4)     = [-4, 2, 6]
   * ArrayUtils.add([2, 6, 3], 2, 1)   = [2, 6, 1, 3]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static int[] add(int[] array, int index, int element) {
    return (int[]) add(array, index, Integer.valueOf(element), Integer.TYPE);
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add([1L], 0, 2L)           = [2L, 1L]
   * ArrayUtils.add([2L, 6L], 2, 10L)      = [2L, 6L, 10L]
   * ArrayUtils.add([2L, 6L], 0, -4L)      = [-4L, 2L, 6L]
   * ArrayUtils.add([2L, 6L, 3L], 2, 1L)   = [2L, 6L, 1L, 3L]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static long[] add(long[] array, int index, long element) {
    return (long[]) add(array, index, Long.valueOf(element), Long.TYPE);
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add([1.1f], 0, 2.2f)               = [2.2f, 1.1f]
   * ArrayUtils.add([2.3f, 6.4f], 2, 10.5f)        = [2.3f, 6.4f, 10.5f]
   * ArrayUtils.add([2.6f, 6.7f], 0, -4.8f)        = [-4.8f, 2.6f, 6.7f]
   * ArrayUtils.add([2.9f, 6.0f, 0.3f], 2, 1.0f)   = [2.9f, 6.0f, 1.0f, 0.3f]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static float[] add(float[] array, int index, float element) {
    return (float[]) add(array, index, Float.valueOf(element), Float.TYPE);
  }

  /**
   * <p>
   * Inserts the specified element at the specified position in the array.
   * Shifts the element currently at that position (if any) and any subsequent
   * elements to the right (adds one to their indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * plus the given element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, a new one element array is
   * returned whose component type is the same as the element.
   * </p>
   * 
   * <pre>
   * ArrayUtils.add([1.1], 0, 2.2)              = [2.2, 1.1]
   * ArrayUtils.add([2.3, 6.4], 2, 10.5)        = [2.3, 6.4, 10.5]
   * ArrayUtils.add([2.6, 6.7], 0, -4.8)        = [-4.8, 2.6, 6.7]
   * ArrayUtils.add([2.9, 6.0, 0.3], 2, 1.0)    = [2.9, 6.0, 1.0, 0.3]
   * </pre>
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @return A new array containing the existing elements and the new element
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index > array.length).
   */
  public static double[] add(double[] array, int index, double element) {
    return (double[]) add(array, index, Double.valueOf(element), Double.TYPE);
  }

  /**
   * Underlying implementation of add(array, index, element) methods. The last
   * parameter is the class, which may not equal element.getClass for
   * primitives.
   * 
   * @param array
   *          the array to add the element to, may be <code>null</code>
   * @param index
   *          the position of the new object
   * @param element
   *          the object to add
   * @param clss
   *          the type of the element being added
   * @return A new array containing the existing elements and the new element
   */
  private static Object add(Object array, int index, Object element,
      Class<?> clss) {
    if (array == null) {
      if (index != 0) {
        throw new IndexOutOfBoundsException("Index: " + index + ", Length: 0");
      }
      Object joinedArray = Array.newInstance(clss, 1);
      Array.set(joinedArray, 0, element);
      return joinedArray;
    }
    int length = Array.getLength(array);
    if (index > length || index < 0) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: "
          + length);
    }
    Object result = Array.newInstance(clss, length + 1);
    System.arraycopy(array, 0, result, 0, index);
    Array.set(result, index, element);
    if (index < length) {
      System.arraycopy(array, index, result, index + 1, length - index);
    }
    return result;
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, true)                = null
   * ArrayUtils.removeElement([], true)                  = []
   * ArrayUtils.removeElement([true], false)             = [true]
   * ArrayUtils.removeElement([true, false], false)      = [true]
   * ArrayUtils.removeElement([true, false, true], true) = [false, true]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static boolean[] removeElement(boolean[] array, boolean element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, 1)        = null
   * ArrayUtils.removeElement([], 1)          = []
   * ArrayUtils.removeElement([1], 0)         = [1]
   * ArrayUtils.removeElement([1, 0], 0)      = [1]
   * ArrayUtils.removeElement([1, 0, 1], 1)   = [0, 1]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static byte[] removeElement(byte[] array, byte element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, 'a')            = null
   * ArrayUtils.removeElement([], 'a')              = []
   * ArrayUtils.removeElement(['a'], 'b')           = ['a']
   * ArrayUtils.removeElement(['a', 'b'], 'a')      = ['b']
   * ArrayUtils.removeElement(['a', 'b', 'a'], 'a') = ['b', 'a']
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static char[] removeElement(char[] array, char element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, 1.1)            = null
   * ArrayUtils.removeElement([], 1.1)              = []
   * ArrayUtils.removeElement([1.1], 1.2)           = [1.1]
   * ArrayUtils.removeElement([1.1, 2.3], 1.1)      = [2.3]
   * ArrayUtils.removeElement([1.1, 2.3, 1.1], 1.1) = [2.3, 1.1]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static double[] removeElement(double[] array, double element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, 1.1)            = null
   * ArrayUtils.removeElement([], 1.1)              = []
   * ArrayUtils.removeElement([1.1], 1.2)           = [1.1]
   * ArrayUtils.removeElement([1.1, 2.3], 1.1)      = [2.3]
   * ArrayUtils.removeElement([1.1, 2.3, 1.1], 1.1) = [2.3, 1.1]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static float[] removeElement(float[] array, float element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, 1)      = null
   * ArrayUtils.removeElement([], 1)        = []
   * ArrayUtils.removeElement([1], 2)       = [1]
   * ArrayUtils.removeElement([1, 3], 1)    = [3]
   * ArrayUtils.removeElement([1, 3, 1], 1) = [3, 1]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static int[] removeElement(int[] array, int element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, 1)      = null
   * ArrayUtils.removeElement([], 1)        = []
   * ArrayUtils.removeElement([1], 2)       = [1]
   * ArrayUtils.removeElement([1, 3], 1)    = [3]
   * ArrayUtils.removeElement([1, 3, 1], 1) = [3, 1]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static long[] removeElement(long[] array, long element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, 1)      = null
   * ArrayUtils.removeElement([], 1)        = []
   * ArrayUtils.removeElement([1], 2)       = [1]
   * ArrayUtils.removeElement([1, 3], 1)    = [3]
   * ArrayUtils.removeElement([1, 3, 1], 1) = [3, 1]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static short[] removeElement(short[] array, short element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove(["a"], 0)           = []
   * ArrayUtils.remove(["a", "b"], 0)      = ["b"]
   * ArrayUtils.remove(["a", "b"], 1)      = ["a"]
   * ArrayUtils.remove(["a", "b", "c"], 1) = ["a", "c"]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  @SuppressWarnings("unchecked")
  // remove() always creates an array of the same type as its input
  public static <T> T[] remove(T[] array, int index) {
    return (T[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the first occurrence of the specified element from the specified
   * array. All subsequent elements are shifted to the left (substracts one from
   * their indices). If the array doesn't contains such an element, no elements
   * are removed from the array.
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the first occurrence of the specified element. The component type of
   * the returned array is always the same as that of the input array.
   * </p>
   * 
   * <pre>
   * ArrayUtils.removeElement(null, "a")            = null
   * ArrayUtils.removeElement([], "a")              = []
   * ArrayUtils.removeElement(["a"], "b")           = ["a"]
   * ArrayUtils.removeElement(["a", "b"], "a")      = ["b"]
   * ArrayUtils.removeElement(["a", "b", "a"], "a") = ["b", "a"]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may be <code>null</code>
   * @param element
   *          the element to be removed
   * @return A new array containing the existing elements except the first
   *         occurrence of the specified element.
   * @since 2.1
   */
  public static <T> T[] removeElement(T[] array, Object element) {
    int index = indexOf(array, element);
    if (index == INDEX_NOT_FOUND) {
      return clone(array);
    }
    return remove(array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove([true], 0)              = []
   * ArrayUtils.remove([true, false], 0)       = [false]
   * ArrayUtils.remove([true, false], 1)       = [true]
   * ArrayUtils.remove([true, true, false], 1) = [true, false]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static boolean[] remove(boolean[] array, int index) {
    return (boolean[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove([1], 0)          = []
   * ArrayUtils.remove([1, 0], 0)       = [0]
   * ArrayUtils.remove([1, 0], 1)       = [1]
   * ArrayUtils.remove([1, 0, 1], 1)    = [1, 1]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static byte[] remove(byte[] array, int index) {
    return (byte[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove(['a'], 0)           = []
   * ArrayUtils.remove(['a', 'b'], 0)      = ['b']
   * ArrayUtils.remove(['a', 'b'], 1)      = ['a']
   * ArrayUtils.remove(['a', 'b', 'c'], 1) = ['a', 'c']
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static char[] remove(char[] array, int index) {
    return (char[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove([1.1], 0)           = []
   * ArrayUtils.remove([2.5, 6.0], 0)      = [6.0]
   * ArrayUtils.remove([2.5, 6.0], 1)      = [2.5]
   * ArrayUtils.remove([2.5, 6.0, 3.8], 1) = [2.5, 3.8]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static double[] remove(double[] array, int index) {
    return (double[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove([1.1], 0)           = []
   * ArrayUtils.remove([2.5, 6.0], 0)      = [6.0]
   * ArrayUtils.remove([2.5, 6.0], 1)      = [2.5]
   * ArrayUtils.remove([2.5, 6.0, 3.8], 1) = [2.5, 3.8]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static float[] remove(float[] array, int index) {
    return (float[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove([1], 0)         = []
   * ArrayUtils.remove([2, 6], 0)      = [6]
   * ArrayUtils.remove([2, 6], 1)      = [2]
   * ArrayUtils.remove([2, 6, 3], 1)   = [2, 3]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static int[] remove(int[] array, int index) {
    return (int[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove([1], 0)         = []
   * ArrayUtils.remove([2, 6], 0)      = [6]
   * ArrayUtils.remove([2, 6], 1)      = [2]
   * ArrayUtils.remove([2, 6, 3], 1)   = [2, 3]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static long[] remove(long[] array, int index) {
    return (long[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * <pre>
   * ArrayUtils.remove([1], 0)         = []
   * ArrayUtils.remove([2, 6], 0)      = [6]
   * ArrayUtils.remove([2, 6], 1)      = [2]
   * ArrayUtils.remove([2, 6, 3], 1)   = [2, 3]
   * </pre>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  public static short[] remove(short[] array, int index) {
    return (short[]) remove((Object) array, index);
  }

  /**
   * <p>
   * Removes the element at the specified position from the specified array. All
   * subsequent elements are shifted to the left (substracts one from their
   * indices).
   * </p>
   * 
   * <p>
   * This method returns a new array with the same elements of the input array
   * except the element on the specified position. The component type of the
   * returned array is always the same as that of the input array.
   * </p>
   * 
   * <p>
   * If the input array is <code>null</code>, an IndexOutOfBoundsException will
   * be thrown, because in that case no valid index can be specified.
   * </p>
   * 
   * @param array
   *          the array to remove the element from, may not be <code>null</code>
   * @param index
   *          the position of the element to be removed
   * @return A new array containing the existing elements except the element at
   *         the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index < 0 || index >=
   *           array.length), or if the array is <code>null</code>.
   * @since 2.1
   */
  private static Object remove(Object array, int index) {
    int length = getLength(array);
    if (index < 0 || index >= length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: "
          + length);
    }

    Object result = Array.newInstance(array.getClass().getComponentType(),
        length - 1);
    System.arraycopy(array, 0, result, 0, index);
    if (index < length - 1) {
      System.arraycopy(array, index + 1, result, index, length - index - 1);
    }

    return result;
  }

  /**
   * <p>
   * Checks if the object is in the given array.
   * </p>
   * 
   * <p>
   * The method returns <code>false</code> if a <code>null</code> array is
   * passed in.
   * </p>
   * 
   * @param array
   *          the array to search through
   * @param objectToFind
   *          the object to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(Object[] array, Object objectToFind) {
    return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
  }

}
