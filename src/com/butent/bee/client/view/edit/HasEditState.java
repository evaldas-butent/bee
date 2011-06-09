package com.butent.bee.client.view.edit;

/**
 * Requires implementing classes to have methods for managing edit state.
 */

public interface HasEditState {

  boolean isEditing();

  void setEditing(boolean editing);
}
