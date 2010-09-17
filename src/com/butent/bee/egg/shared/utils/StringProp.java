package com.butent.bee.egg.shared.utils;

public class StringProp extends BeeProp<String> {
  public static String[] HEADERS = new String[]{"Property", "Value"};
  public static int HEADER_COUNT = HEADERS.length;

  public StringProp() {
    super();
  }

  public StringProp(String name) {
    super(name);
  }

  public StringProp(String name, String value) {
    super(name, value);
  }

}
