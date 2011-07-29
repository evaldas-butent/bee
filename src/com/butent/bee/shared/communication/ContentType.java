package com.butent.bee.shared.communication;

import com.butent.bee.shared.Transformable;

/**
 * Contains a list of possible communication content types (text, xml, table, image, binary etc).
 */

public enum ContentType implements Transformable {
  TEXT, XML, HTML, TABLE, IMAGE, RESOURCE, BINARY, ZIP, MULTIPART, UNKNOWN;

  public String transform() {
    return name();
  }
}
