package com.butent.bee.client.language;

/**
 * Contains a list of possible translation content types, for example html or text.
 */

public enum ContentType {
  HTML("html"), TEXT("text");

  private String value;

  private ContentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
