package com.butent.bee.client.ui;

public interface AcceptsCaptions {

  void setCaptions(String captionKey);

  void setCaptions(Class<? extends Enum<?>> clazz);
}
