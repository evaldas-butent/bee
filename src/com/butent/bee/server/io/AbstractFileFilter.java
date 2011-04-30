package com.butent.bee.server.io;

import java.io.File;

/**
 * Contains necessary <code>accept</code> methods for file filter classes.
 */

public abstract class AbstractFileFilter implements Filter {
  public boolean accept(File dir, String name) {
    return true;
  }

  public abstract boolean accept(File pathname);
}
