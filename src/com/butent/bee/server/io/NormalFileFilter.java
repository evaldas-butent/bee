package com.butent.bee.server.io;

import java.io.File;

class NormalFileFilter extends AbstractFileFilter {
  @Override
  public boolean accept(File pathname) {
    return pathname != null && pathname.isFile();
  }
}
