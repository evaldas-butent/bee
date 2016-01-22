package com.butent.bee.client.webrtc;

import com.butent.bee.client.media.MediaStream;

public class RtcHolder {

  private MediaStream localStream;

  private RTCPeerConnection localPeerConnection;
  private RTCPeerConnection remotePeerConnection;

  public RtcHolder() {
  }

  public MediaStream getLocalStream() {
    return localStream;
  }

  public RTCPeerConnection getLocalPeerConnection() {
    return localPeerConnection;
  }

  public RTCPeerConnection getRemotePeerConnection() {
    return remotePeerConnection;
  }

  public void setLocalStream(MediaStream localStream) {
    this.localStream = localStream;
  }

  public void setLocalPeerConnection(RTCPeerConnection localPeerConnection) {
    this.localPeerConnection = localPeerConnection;
  }

  public void setRemotePeerConnection(RTCPeerConnection remotePeerConnection) {
    this.remotePeerConnection = remotePeerConnection;
  }
}
