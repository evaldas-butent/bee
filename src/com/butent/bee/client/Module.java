package com.butent.bee.client;

public interface Module {
  int PRIORITY_INIT = 1;
  int PRIORITY_START = 2;
  int PRIORITY_END = 3;

  int DO_NOT_CALL = -1;

  void end();

  String getName();

  int getPriority(int p);

  void init();

  void start();
}
