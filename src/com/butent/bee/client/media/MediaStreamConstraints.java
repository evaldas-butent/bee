package com.butent.bee.client.media;

import jsinterop.annotations.JsProperty;

public class MediaStreamConstraints {

  @JsProperty
  boolean audio;

  @JsProperty
  boolean video;

  public MediaStreamConstraints() {
  }

  public MediaStreamConstraints(boolean audio, boolean video) {
    this.audio = audio;
    this.video = video;
  }

  public boolean isAudio() {
    return audio;
  }

  public boolean isVideo() {
    return video;
  }

  public void setAudio(boolean audio) {
    this.audio = audio;
  }

  public void setVideo(boolean video) {
    this.video = video;
  }
}
