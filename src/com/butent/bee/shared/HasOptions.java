package com.butent.bee.shared;

/**
 * Requires implementing classes to have methods for getting and setting options.
 */

public interface HasOptions {

  String ATTR_OPTIONS = "options";

  String getOptions();

  void setOptions(String options);
}
