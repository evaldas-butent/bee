package com.butent.bee.client.webrtc;

import com.butent.bee.client.dom.DOMError;
import com.butent.bee.client.js.JsConsumer;
import com.butent.bee.client.media.MediaStream;
import com.butent.bee.client.media.MediaStreamEvent;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class RTCPeerConnection {

  @JsMethod
  public native void addIceCandidate(RTCIceCandidate candidate);

  @JsMethod
  public native void addStream(MediaStream stream);

  @JsMethod
  public native void close();

  @JsMethod
  public native void createAnswer(JsConsumer<RTCSessionDescription> successCallback,
      JsConsumer<DOMError> failureCallback);

  @JsMethod
  public native void createOffer(JsConsumer<RTCSessionDescription> successCallback,
      JsConsumer<DOMError> failureCallback);

  @JsMethod
  public native void setLocalDescription(RTCSessionDescription description);

  @JsProperty
  public native void setOnicecandidate(JsConsumer<RTCPeerConnectionIceEvent> onicecandidate);

  @JsProperty
  public native void setOnaddstream(JsConsumer<MediaStreamEvent> onaddstream);

  @JsMethod
  public native void setRemoteDescription(RTCSessionDescription description);
}
