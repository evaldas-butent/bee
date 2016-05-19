package com.butent.bee.client.webrtc;

import jsinterop.annotations.JsProperty;

public interface DataChannelEvent {

  @JsProperty
  RTCDataChannel getChannel();
}
