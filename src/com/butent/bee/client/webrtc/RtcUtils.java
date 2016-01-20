package com.butent.bee.client.webrtc;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DOMError;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.media.MediaStreamConstraints;
import com.butent.bee.client.media.MediaUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Video;
import com.butent.bee.shared.css.CssProperties;
import com.butent.bee.shared.css.values.Visibility;
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
    controls.add(callButton);

    final Button hangupButton = new Button("hang up");
    controls.add(hangupButton);

    panel.add(controls);

    disable(callButton);
    disable(hangupButton);

    final RtcHolder holder = new RtcHolder();

    startButton.addClickHandler(event -> {
      disable(startButton);

      MediaStreamConstraints constraints = new MediaStreamConstraints(true, true);
      RtcAdapter.getUserMedia(constraints, stream -> {
        logger.debug("received local stream");
        holder.setLocalStream(stream);
        RtcAdapter.attachMediaStream(localVideo.getMediaElement(), stream);
        enable(callButton);
      }, error -> logger.severe("gum error", MediaUtils.format(error)));
    });

    callButton.addClickHandler(ce -> {
      disable(callButton);
      enable(hangupButton);
      logger.debug("starting call");

      RTCConfiguration pcConfig = null;

      holder.setLocalPeerConnection(RtcAdapter.createRTCPeerConnection(pcConfig));
      logger.debug("created local peer connection");

      holder.getLocalPeerConnection().setOnicecandidate(iceEvent -> {
        if (iceEvent.getCandidate() != null) {
          logger.debug("local ice candidate", iceEvent.getCandidate().getCandidate());

          RTCIceCandidate candidate = RtcAdapter.createRTCIceCandidate(iceEvent.getCandidate());
          holder.getRemotePeerConnection().addIceCandidate(candidate);
        }
      });

      holder.setRemotePeerConnection(RtcAdapter.createRTCPeerConnection(pcConfig));
      logger.debug("created remote peer connection");

      holder.getRemotePeerConnection().setOnicecandidate(iceEvent -> {
        if (iceEvent.getCandidate() != null) {
          logger.debug("remote ice candidate", iceEvent.getCandidate().getCandidate());

          RTCIceCandidate candidate = RtcAdapter.createRTCIceCandidate(iceEvent.getCandidate());
          holder.getLocalPeerConnection().addIceCandidate(candidate);
        }
      });

      holder.getRemotePeerConnection().setOnaddstream(streamEvent -> {
        RtcAdapter.attachMediaStream(remoteVideo.getMediaElement(), streamEvent.getStream());
        logger.debug("received remote stream");
      });

      holder.getLocalPeerConnection().addStream(holder.getLocalStream());
      logger.debug("added localStream to localPeerConnection");

      holder.getLocalPeerConnection().createOffer(localDescription -> {
        holder.getLocalPeerConnection().setLocalDescription(localDescription);
        logger.debug("offer from localPeerConnection", localDescription.getSdp());

        holder.getRemotePeerConnection().setRemoteDescription(localDescription);
        holder.getRemotePeerConnection().createAnswer(remoteDescription -> {
          holder.getRemotePeerConnection().setLocalDescription(remoteDescription);
          logger.debug("answer from remotePeerConnection", remoteDescription.getSdp());
          holder.getLocalPeerConnection().setRemoteDescription(remoteDescription);
        }, RtcUtils::handleError);
      }, RtcUtils::handleError);
    });

    hangupButton.addClickHandler(event -> {
      logger.debug("ending call");

      if (holder.getLocalPeerConnection() != null) {
        holder.getLocalPeerConnection().close();
        holder.setLocalPeerConnection(null);
      }
      if (holder.getRemotePeerConnection() != null) {
        holder.getRemotePeerConnection().close();
        holder.setRemotePeerConnection(null);
      }

      disable(hangupButton);
      enable(callButton);
    });

    return panel;
  }

  private static void disable(Widget widget) {
    StyleUtils.setProperty(widget, CssProperties.VISIBILITY, Visibility.HIDDEN);
  }

  private static void enable(Widget widget) {
    StyleUtils.setProperty(widget, CssProperties.VISIBILITY, Visibility.VISIBLE);
  }

  private static void handleError(DOMError error) {
    logger.severe((error == null) ? "error" : error.getName());
  }

  private RtcUtils() {
  }
}
