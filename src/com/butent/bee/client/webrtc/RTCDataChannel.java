package com.butent.bee.client.webrtc;

import com.butent.bee.client.js.JsConsumer;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface RTCDataChannel {

  @JsProperty
  String getLabel();

  @JsProperty
  String getReadyState();

  void close();

  void send(String data);

  @JsProperty
  void setOnopen(JsConsumer<DataChannelStateChangeEvent> onopen);

  @JsProperty
  void setOnmessage(JsConsumer<DataChannelMessageEvent> onmessage);

  @JsProperty
  void setOnclose(JsConsumer<DataChannelStateChangeEvent> onclose);
}
