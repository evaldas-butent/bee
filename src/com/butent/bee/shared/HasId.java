package com.butent.bee.shared;

/**
 * Requires any implementing classes to be able to create, set and get Id.
 */

public interface HasId {
  void createId();

  String getId();

  void setId(String id);
}
