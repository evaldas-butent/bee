package com.butent.bee.shared;

import com.butent.bee.shared.data.StringRow;

/**
 * Enables creating and cloning string row array data structures.
 */

public class StringRowArray extends ArraySequence<StringRow> {

  /**
   * Creates new string array with new values.
   * 
   * @param values array of string values
   */
  public StringRowArray(StringRow[] values) {
    super(values);
  }

  @Override
  public Sequence<StringRow> copy() {
    int len = getLength();
    if (len <= 0) {
      return new StringRowArray(new StringRow[0]);
    }

    StringRow[] arr = new StringRow[len];
    for (int i = 0; i < len; i++) {
      arr[i] = get(i).copy();
    }
    return new StringRowArray(arr);
  }
}
