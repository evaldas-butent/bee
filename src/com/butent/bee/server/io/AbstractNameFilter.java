package com.butent.bee.server.io;

import java.io.File;

/**
 * Is an abstract class, implementing <code>Filter</code> interface requirements.
 */

public abstract class AbstractNameFilter implements Filter {
  public boolean accept(File pathname) {
    return true;
  }

  public abstract boolean accept(File dir, String name);
}
