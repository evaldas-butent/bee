package com.butent.bee.client.webrtc;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface RTCIceCandidate {

  @JsProperty
  String getCandidate();

  @JsProperty
  String getSdpMid();

  @JsProperty
  Object getSdpMLineIndex();
}
