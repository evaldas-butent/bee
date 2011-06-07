package com.butent.bee.shared;

import com.butent.bee.shared.data.StringRow;

public class StringRowArray extends ArraySequence<StringRow> {

  /**
   * Creates new string array with new values.
   * 
   * @param values arraoy of string values
   */
  public StringRowArray(StringRow[] values) {
    super(values);
  }

  @Override
  public StringRowArray clone() {
    int len = getLength();
    if (len <= 0) {
      return new StringRowArray(new StringRow[0]);
    }
    
    StringRow[] arr = new StringRow[len];
    for (int i = 0; i < len; i++) {
      arr[i] = get(i).clone();
    }
    return new StringRowArray(arr);
  }
}
