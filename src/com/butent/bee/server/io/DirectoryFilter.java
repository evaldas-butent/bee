package com.butent.bee.server.io;

import java.io.File;

/**
 * Check whether a given path is a directory.
 */

class DirectoryFilter extends AbstractFileFilter {
  @Override
  public boolean accept(File pathname) {
    return pathname != null && pathname.isDirectory();
  }
}
