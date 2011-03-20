package com.butent.bee.server.io;

import java.io.File;

class DirectoryFilter extends AbstractFileFilter {
  @Override
  public boolean accept(File pathname) {
    return pathname != null && pathname.isDirectory();
  }
}
