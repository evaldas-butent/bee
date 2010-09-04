package com.butent.bee.egg.shared;

public interface BeeSerializable {
  String serialize();
  void deserialize(String s);
}
