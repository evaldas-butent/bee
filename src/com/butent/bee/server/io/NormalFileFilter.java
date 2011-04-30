package com.butent.bee.server.io;

import java.io.File;

/**
 * Checks whether a given path name is a file.
 */

class NormalFileFilter extends AbstractFileFilter {
  @Override
  public boolean accept(File pathname) {
    return pathname != null && pathname.isFile();
  }
}
