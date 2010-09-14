package com.butent.bee.egg.shared;

public interface BeeSerializable {
  void deserialize(String s);

  String serialize();
}
