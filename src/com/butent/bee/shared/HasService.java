package com.butent.bee.shared;

/**
 * Requires any implementing classes to have get and set methods for services.
 */

public interface HasService {
  
  String ATTR_SERVICE = "service";

  String getService();

  void setService(String svc);
}
