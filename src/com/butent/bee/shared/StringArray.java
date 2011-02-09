package com.butent.bee.shared;

public class StringArray extends ArraySequence<String> {
  
  public StringArray(String[] values) {
    super(values);
  }

  public StringArray(int size) {
    super(new String[size]);
  }
}
