package com.butent.bee.egg.shared.communication;

import com.butent.bee.egg.shared.Transformable;

public enum ContentType implements Transformable {
  TEXT, XML, TABLE, IMAGE, RESOURCE, BINARY, ZIP, MULTIPART, UNKNOWN;

  public String transform() {
    return name();
  }
}
