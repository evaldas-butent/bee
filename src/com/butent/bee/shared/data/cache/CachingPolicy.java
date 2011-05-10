package com.butent.bee.shared.data.cache;

/**
 * Contains a list of possible read and write settings.
 */

public enum CachingPolicy {
  NONE(false, false), READ(true, false), WRITE(false, true), FULL(true, true);

  private boolean read;
  private boolean write;

  private CachingPolicy(boolean read, boolean write) {
    this.read = read;
    this.write = write;
  }

  public boolean doRead() {
    return read;
  }

  public boolean doWrite() {
    return write;
  }
}
