package com.butent.bee.client.webrtc;

import com.butent.bee.client.dom.DOMError;
import com.butent.bee.client.js.JsConsumer;
import com.butent.bee.client.media.MediaStream;
import com.butent.bee.client.media.MediaStreamEvent;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface RTCPeerConnection {

  void addIceCandidate(RTCIceCandidate candidate);

  void addStream(MediaStream stream);

  void close();

  void createAnswer(JsConsumer<RTCSessionDescription> successCallback,
      JsConsumer<DOMError> failureCallback);

  RTCDataChannel createDataChannel(String label, RTCDataChannelInit dataChannelDict);

  void createOffer(JsConsumer<RTCSessionDescription> successCallback,
      JsConsumer<DOMError> failureCallback);

  void setLocalDescription(RTCSessionDescription description);

  @JsProperty
  void setOnaddstream(JsConsumer<MediaStreamEvent> onaddstream);

  @JsProperty
  void setOndatachannel(JsConsumer<DataChannelEvent> ondatachannel);

  @JsProperty
  void setOnicecandidate(JsConsumer<RTCPeerConnectionIceEvent> onicecandidate);

  @JsProperty
  void setOnnegotiationneeded(JsConsumer<NegotiationNeededEvent> negotiationneeded);

  @JsProperty
  void setOntrack(JsConsumer<RTCTrackEvent> ontrack);

  void setRemoteDescription(RTCSessionDescription description);
}
