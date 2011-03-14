package com.butent.bee.client.language;

import com.google.gwt.core.client.JavaScriptObject;

public final class Option extends JavaScriptObject {

  public static Option newInstance(String text, ContentType contentType) {
    return newInstance(text, contentType.getValue());
  }

  private static native Option newInstance(String text, String type) /*-{
    var option = new Object();
    option.text = text;
    option.type = type;
    return option;
  }-*/;

  protected Option() {
  }

  public native String getText() /*-{
    return this.text;
  }-*/;

  public ContentType getType() {
    String type = getTypeString();
    if (type.equalsIgnoreCase(ContentType.TEXT.getValue())) {
      return ContentType.TEXT;
    } else if (type.equalsIgnoreCase(ContentType.HTML.getValue())) {
      return ContentType.HTML;
    }
    return null;
  }

  private native String getTypeString() /*-{
    return this.type;
  }-*/;
}
