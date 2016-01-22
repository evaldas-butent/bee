package com.butent.bee.client.webrtc;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface RTCSessionDescription {

  @JsProperty
  String getType();

  @JsProperty
  String getSdp();
}
