package com.butent.bee.client.event;

/**
 * Requires implementing classes to have a method for after save event.
 */

public interface HasAfterSaveHandler {
  void onAfterSave(String opt);

}
