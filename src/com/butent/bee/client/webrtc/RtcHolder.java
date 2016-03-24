package com.butent.bee.client.webrtc;

import com.butent.bee.client.media.MediaStream;

public class RtcHolder {

  private MediaStream localStream;

  private RTCPeerConnection localPeerConnection;
  private RTCPeerConnection remotePeerConnection;

  private RTCDataChannel sendChannel;
  private RTCDataChannel receiveChannel;

  public RtcHolder() {
  }

  public void closeConnections() {
    if (getLocalPeerConnection() != null) {
      getLocalPeerConnection().close();
      setLocalPeerConnection(null);
    }

    if (getRemotePeerConnection() != null) {
      getRemotePeerConnection().close();
      setRemotePeerConnection(null);
    }
  }

  public void closeDataChannels() {
    if (getSendChannel() != null) {
      getSendChannel().close();
      setSendChannel(null);
    }

    if (getReceiveChannel() != null) {
      getReceiveChannel().close();
      setReceiveChannel(null);
    }
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

  public RTCDataChannel getSendChannel() {
    return sendChannel;
  }

  public RTCDataChannel getReceiveChannel() {
    return receiveChannel;
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

  public void setSendChannel(RTCDataChannel sendChannel) {
    this.sendChannel = sendChannel;
  }

  public void setReceiveChannel(RTCDataChannel receiveChannel) {
    this.receiveChannel = receiveChannel;
  }
}
