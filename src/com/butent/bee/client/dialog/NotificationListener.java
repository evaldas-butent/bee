package com.butent.bee.client.dialog;

public interface NotificationListener {
  
  void notifyInfo(String... messages);
  
  void notifySevere(String... messages);

  void notifyWarning(String... messages);
}
