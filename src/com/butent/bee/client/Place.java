package com.butent.bee.client;

import com.butent.bee.shared.Assert;

public abstract class Place {
  
  private final String id;
  
  public Place(String id) {
    super();
    this.id = Assert.notEmpty(id);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Place) ? id.equals(((Place) obj).getId()) : false;
  }

  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  abstract boolean activate();
}
