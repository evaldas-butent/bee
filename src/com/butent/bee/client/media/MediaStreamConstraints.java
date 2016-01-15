package com.butent.bee.client.media;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface MediaStreamConstraints {

  @JsProperty
  boolean getAudio();

  @JsProperty
  boolean getVideo();

  @JsProperty
  void setAudio(boolean audio);

  @JsProperty
  void setVideo(boolean video);
}
