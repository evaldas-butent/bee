package com.butent.bee.client.media;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface MediaStreamError {

  @JsProperty
  String getName();

  @JsProperty
  String getMessage();

  @JsProperty
  String getConstraintName();
}
