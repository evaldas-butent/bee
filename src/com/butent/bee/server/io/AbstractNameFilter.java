package com.butent.bee.server.io;

import java.io.File;

public abstract class AbstractNameFilter implements Filter {

  @Override
  public boolean accept(File pathname) {
    return true;
  }
}
