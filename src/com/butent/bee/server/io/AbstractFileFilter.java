package com.butent.bee.server.io;

import java.io.File;

public abstract class AbstractFileFilter implements Filter {

  @Override
  public boolean accept(File dir, String name) {
    return true;
  }
}
