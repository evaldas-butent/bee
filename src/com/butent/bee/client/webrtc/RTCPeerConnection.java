package com.butent.bee.client.webrtc;

import com.butent.bee.client.media.MediaStream;
import com.butent.bee.client.media.MediaStreamEvent;

import jsinterop.annotations.JsMethod;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class RTCPeerConnection {

  @JsProperty
  private RTCPeerConnectionIceEvent onicecandidate;

  @JsProperty
  private MediaStreamEvent onaddstream;

  @JsMethod
  public native void addStream(MediaStream stream);

  public void setOnicecandidate(RTCPeerConnectionIceEvent onicecandidate) {
    this.onicecandidate = onicecandidate;
  }

  public void setOnaddstream(MediaStreamEvent onaddstream) {
    this.onaddstream = onaddstream;
  }
}
