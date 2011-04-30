package com.butent.bee.server.io;

import java.io.File;

/**
 * Checks whether a given path name is a file with information to read.
 */

class InputFileFilter extends AbstractFileFilter {
  @Override
  public boolean accept(File pathname) {
    return FileUtils.isInputFile(pathname);
  }
}
