package com.butent.bee.shared.data;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class CustomProperties extends HashMap<String, Object> {
  public static CustomProperties create() {
    return new CustomProperties();
  }
  
  @Override
  public CustomProperties clone() {
    CustomProperties properties = create();
    for (Map.Entry<String, Object> entry : entrySet()) {
      properties.put(entry.getKey(), entry.getValue());
    }
    return properties;
  }
}
