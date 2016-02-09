package com.butent.bee.client.media;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface MediaStreamEvent {

  @JsProperty
  MediaStream getStream();
}
