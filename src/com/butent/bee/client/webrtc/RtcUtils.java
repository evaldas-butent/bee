package com.butent.bee.client.webrtc;

import com.butent.bee.client.dom.DOMError;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.media.MediaStream;
import com.butent.bee.client.media.MediaStreamConstraints;
import com.butent.bee.client.media.MediaUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Video;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

public final class RtcUtils {

  private static final BeeLogger logger = LogUtils.getLogger(RtcUtils.class);

  public static IdentifiableWidget createBasicDemo() {
    Flow panel = new Flow();

    Horizontal videos = new Horizontal();
    videos.setBorderSpacing(10);

    final Video localVideo = new Video();
    localVideo.setAutoplay(true);
    localVideo.setControls(true);
    localVideo.setMuted(true);

    videos.add(localVideo);

    final Video remoteVideo = new Video();
    remoteVideo.setAutoplay(true);
    remoteVideo.setControls(true);
    remoteVideo.setMuted(true);

    videos.add(remoteVideo);

    panel.add(videos);

    Horizontal controls = new Horizontal();
    controls.setBorderSpacing(20);

    final Button startButton = new Button("start");
    controls.add(startButton);

    final Button callButton = new Button("call");
    callButton.setEnabled(false);
    controls.add(callButton);

    final Button hangupButton = new Button("hang up");
    hangupButton.setEnabled(false);
    controls.add(hangupButton);

    panel.add(controls);

    final Holder<MediaStream> localStream = Holder.absent();
    final Holder<RTCPeerConnection> localPeerConnection = Holder.absent();
    final Holder<RTCPeerConnection> remotePeerConnection = Holder.absent();

    startButton.addClickHandler(event -> {
      startButton.setEnabled(false);

      MediaStreamConstraints constraints = new MediaStreamConstraints(true, true);
      RtcAdapter.getUserMedia(constraints, stream -> {
        logger.debug("received local stream");
        localStream.set(stream);
        RtcAdapter.attachMediaStream(localVideo.getMediaElement(), stream);
        callButton.setEnabled(true);
      }, error -> logger.severe("gum error", MediaUtils.format(error)));
    });

    callButton.addClickHandler(ce -> {
      callButton.setEnabled(false);
      hangupButton.setEnabled(true);
      logger.debug("starting call");

      RTCConfiguration pcConfig = null;

      localPeerConnection.set(RtcAdapter.createRTCPeerConnection(pcConfig));
      logger.debug("created local peer connection");

      localPeerConnection.get().setOnicecandidate(iceEvent -> {
        if (iceEvent.getCandidate() != null) {
          logger.debug("local ice candidate", iceEvent.getCandidate().getCandidate());

          RTCIceCandidate candidate = RtcAdapter.createRTCIceCandidate(iceEvent.getCandidate());
          remotePeerConnection.get().addIceCandidate(candidate);
        }
      });

      remotePeerConnection.set(RtcAdapter.createRTCPeerConnection(pcConfig));
      logger.debug("created remote peer connection");

      remotePeerConnection.get().setOnicecandidate(iceEvent -> {
        if (iceEvent.getCandidate() != null) {
          logger.debug("remote ice candidate", iceEvent.getCandidate().getCandidate());

          RTCIceCandidate candidate = RtcAdapter.createRTCIceCandidate(iceEvent.getCandidate());
          localPeerConnection.get().addIceCandidate(candidate);
        }
      });

      remotePeerConnection.get().setOnaddstream(streamEvent -> {
        RtcAdapter.attachMediaStream(remoteVideo.getMediaElement(), streamEvent.getStream());
        logger.debug("received remote stream");
      });

      localPeerConnection.get().addStream(localStream.get());
      logger.debug("added localStream to localPeerConnection");

      localPeerConnection.get().createOffer(localDescription -> {
        localPeerConnection.get().setLocalDescription(localDescription);
        logger.debug("offer from localPeerConnection", localDescription.getSdp());

        remotePeerConnection.get().setRemoteDescription(localDescription);
        remotePeerConnection.get().createAnswer(remoteDescription -> {
          remotePeerConnection.get().setLocalDescription(remoteDescription);
          logger.debug("answer from remotePeerConnection", remoteDescription.getSdp());
          localPeerConnection.get().setRemoteDescription(remoteDescription);
        }, RtcUtils::handleError);
      }, RtcUtils::handleError);
    });

    hangupButton.addClickHandler(event -> {
      logger.debug("ending call");

      if (localPeerConnection.isNotNull()) {
        localPeerConnection.get().close();
        localPeerConnection.clear();
      }
      if (remotePeerConnection.isNotNull()) {
        remotePeerConnection.get().close();
        remotePeerConnection.clear();
      }

      hangupButton.setEnabled(false);
      callButton.setEnabled(true);
    });

    return panel;
  }

  private static void handleError(DOMError error) {
    logger.severe((error == null) ? "error" : error.getName());
  }

  private RtcUtils() {
  }
}
