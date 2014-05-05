package com.butent.bee.client.modules.service;

import com.google.common.primitives.Longs;

import java.util.Objects;

class ServiceCustomerWrapper {
  
  private final long id;
  private final String name;

  ServiceCustomerWrapper(long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ServiceCustomerWrapper) {
      return Objects.equals(id, ((ServiceCustomerWrapper) obj).id);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Longs.hashCode(id);
  }

  long getId() {
    return id;
  }

  String getName() {
    return name;
  }
}
