package com.butent.bee.client.webrtc;

import com.butent.bee.client.media.MediaStreamTrack;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface RTCTrackEvent {

  @JsProperty
  MediaStreamTrack getTrack();
}
