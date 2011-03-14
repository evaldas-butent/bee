package com.butent.bee.client.language;

import com.google.gwt.core.client.JavaScriptObject;

public class DetectionResult extends JavaScriptObject {
  protected DetectionResult() {
  }

  public final native double getConfidence() /*-{
    return this.confidence;
  }-*/;

  public final native Error getError() /*-{
    return this.error;
  }-*/;

  public final native String getLanguage() /*-{
    return this.language;
  }-*/;

  public final native boolean isReliable() /*-{
    return this.isReliable;
  }-*/;
}
