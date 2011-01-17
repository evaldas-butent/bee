package com.butent.bee.shared;

public interface BeeSerializable {
  void deserialize(String s);

  String serialize();
}
