package com.butent.bee.client.language;

import com.google.gwt.core.client.JavaScriptObject;

public class TranslationResult extends JavaScriptObject {

  protected TranslationResult() {
  }

  public final native Error getError() /*-{
    return this.error;
  }-*/;

  public final native String getTranslatedText() /*-{
    return this.translation;
  }-*/;
}
