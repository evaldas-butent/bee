package com.butent.bee.client.view;

public interface HasKeyboardPaging {

  public enum KeyboardPagingPolicy {
    CURRENT_PAGE(true),
    CHANGE_PAGE(false),
    INCREASE_RANGE(false);

    private final boolean isLimitedToRange;

    private KeyboardPagingPolicy(boolean isLimitedToRange) {
      this.isLimitedToRange = isLimitedToRange;
    }

    public boolean isLimitedToRange() {
      return isLimitedToRange;
    }
  }

  KeyboardPagingPolicy getKeyboardPagingPolicy();

  void setKeyboardPagingPolicy(KeyboardPagingPolicy policy);
}
