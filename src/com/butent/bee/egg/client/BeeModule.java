package com.butent.bee.egg.client;

public interface BeeModule {
  static int PRIORITY_INIT = 1;
  static int PRIORITY_START = 2;
  static int PRIORITY_END = 3;

  static int DO_NOT_CALL = -1;

  String getName();

  int getPriority(int p);

  void init();

  void start();

  void end();
}
