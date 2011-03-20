package com.butent.bee.server.io;

import java.io.File;

class InputFileFilter extends AbstractFileFilter {
  @Override
  public boolean accept(File pathname) {
    return FileUtils.isInputFile(pathname);
  }
}
