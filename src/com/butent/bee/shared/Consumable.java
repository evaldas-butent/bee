package com.butent.bee.shared;

public interface Consumable {

  void consume();

  boolean isConsumed();

  void setConsumed(boolean consumed);
}
