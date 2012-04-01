package com.butent.bee.client.ui;

public interface AcceptsCaptions {
  void addCaptions(String captionKey);

  void addCaptions(Class<? extends Enum<?>> clazz);
}
