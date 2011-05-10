package com.butent.bee.shared;

/**
 * Requires any implementing classes to have methods for serialization and deserialization.
 */

public interface BeeSerializable {
  void deserialize(String s);

  String serialize();
}
