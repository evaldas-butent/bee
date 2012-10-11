package com.butent.bee.shared;

/**
 * Implements operations with arrays of String.
 */
public class StringArray extends ArraySequence<String> {

  /**
   * Creates new string array with new values.
   * 
   * @param values array of string values
   */
  public StringArray(String[] values) {
    super(values);
  }

  @Override
  public StringArray copy() {
    int len = getLength();
    if (len <= 0) {
      return new StringArray(new String[0]);
    }

    String[] arr = new String[len];
    for (int i = 0; i < len; i++) {
      arr[i] = get(i);
    }
    return new StringArray(arr);
  }
}
