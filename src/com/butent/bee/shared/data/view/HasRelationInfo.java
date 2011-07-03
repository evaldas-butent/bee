package com.butent.bee.shared.data.view;

/**
 * Contains necessary methods for data relations classes (getting and setting them)
 */

public interface HasRelationInfo {

  RelationInfo getRelationInfo();

  void setRelationInfo(RelationInfo relationInfo);
}
