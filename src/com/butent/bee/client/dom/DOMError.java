package com.butent.bee.client.dom;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface DOMError {

  @JsProperty
  String getName();
}
