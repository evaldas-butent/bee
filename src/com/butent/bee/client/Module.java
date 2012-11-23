package com.butent.bee.client;

/**
 * sets requirements for class initialization priority, most of the classes depend on other ones to
 * be initialised before them.
 * 
 * 
 */

public interface Module {
  int PRIORITY_INIT = 1;
  int PRIORITY_START = 2;
  int PRIORITY_END = 3;

  int DO_NOT_CALL = -1;

  void onExit();

  String getName();

  int getPriority(int p);

  void init();

  void start();
}
