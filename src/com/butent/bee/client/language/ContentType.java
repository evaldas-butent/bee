package com.butent.bee.client.language;

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
