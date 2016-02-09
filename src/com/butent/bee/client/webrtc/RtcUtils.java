package com.butent.bee.client.webrtc;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DOMError;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.media.MediaStreamConstraints;
import com.butent.bee.client.media.MediaUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.websocket.Endpoint;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Video;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.CssProperties;
import com.butent.bee.shared.css.values.Visibility;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.websocket.messages.SignalingMessage;

import java.util.HashMap;
import java.util.Map;

import jsinterop.annotations.JsMethod;

public final class RtcUtils {

  private static final BeeLogger logger = LogUtils.getLogger(RtcUtils.class);

  private static final String KEY_CANDIDATE = "candidate";
  private static final String KEY_SDP = "sdp";

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "rtc-";
  private static final String STYLE_PANEL = STYLE_PREFIX + "panel";
  private static final String STYLE_LOCAL = STYLE_PREFIX + "local";
  private static final String STYLE_REMOTE = STYLE_PREFIX + "remote";

  private static final Map<String, RtcHolder> rooms = new HashMap<>();

  public static void call(final String session) {
    final Flow videos = new Flow(STYLE_PANEL);

    final Video localVideo = new Video(STYLE_LOCAL);
    localVideo.setAutoplay(true);
    localVideo.setControls(true);
    localVideo.setMuted(true);

    videos.add(localVideo);

    final Video remoteVideo = new Video(STYLE_REMOTE);
    remoteVideo.setAutoplay(true);
    remoteVideo.setControls(true);

    videos.add(remoteVideo);

    final String room = BeeUtils.randomString(10, BeeConst.DIGITS);

    final RtcHolder holder = new RtcHolder();

    RTCConfiguration pcConfig = null;
    holder.setLocalPeerConnection(RtcAdapter.createRTCPeerConnection(pcConfig));
    logger.debug("created local peer connection");

    holder.getLocalPeerConnection().setOnicecandidate(iceEvent -> {
      if (iceEvent.getCandidate() != null) {
        logger.debug("on ice candidate");
        send(session, room, iceEvent.getCandidate());
      }
    });

    holder.getLocalPeerConnection().setOnaddstream(streamEvent -> {
      logger.debug("on add stream");
      RtcAdapter.attachMediaStream(remoteVideo.getMediaElement(), streamEvent.getStream());
    });

    rooms.put(room, holder);
    BeeKeeper.getScreen().show(videos);

    MediaStreamConstraints constraints = new MediaStreamConstraints(true, true);

    RtcAdapter.getUserMedia(constraints, stream -> {
      logger.debug("received local stream");
      holder.setLocalStream(stream);
      RtcAdapter.attachMediaStream(localVideo.getMediaElement(), stream);

      holder.getLocalPeerConnection().addStream(stream);
      logger.debug("added local stream");

      holder.getLocalPeerConnection().createOffer(description -> {
        logger.debug("create offer");
        holder.getLocalPeerConnection().setLocalDescription(description);
        send(session, room, description);
      }, RtcUtils::handleError);
    }, error -> logger.severe("gum error", MediaUtils.format(error)));
  }

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
          logger.debug("local ice candidate",
              formatCandidate(iceEvent.getCandidate().getCandidate()));

          RTCIceCandidate candidate = RtcAdapter.createRTCIceCandidate(iceEvent.getCandidate());
          holder.getRemotePeerConnection().addIceCandidate(candidate);
        }
      });

      holder.setRemotePeerConnection(RtcAdapter.createRTCPeerConnection(pcConfig));
      logger.debug("created remote peer connection");

      holder.getRemotePeerConnection().setOnicecandidate(iceEvent -> {
        if (iceEvent.getCandidate() != null) {
          logger.debug("remote ice candidate",
              formatCandidate(iceEvent.getCandidate().getCandidate()));

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

      offer(holder.getLocalPeerConnection(), holder.getRemotePeerConnection());
    });

    hangupButton.addClickHandler(event -> {
      logger.debug("ending call");

      holder.closeConnections();

      disable(hangupButton);
      enable(callButton);
    });

    return panel;
  }

  public static IdentifiableWidget createTextDemo() {
    Flow panel = new Flow();

    Horizontal inputs = new Horizontal();
    inputs.setBorderSpacing(20);

    final InputArea sendArea = new InputArea();
    sendArea.setVisibleLines(5);
    sendArea.setEnabled(false);
    DomUtils.setPlaceholder(sendArea, "press start, enter some text, then press send");

    inputs.add(sendArea);

    final InputArea receiveArea = new InputArea();
    receiveArea.setVisibleLines(5);
    receiveArea.setEnabled(false);

    inputs.add(receiveArea);

    panel.add(inputs);

    Horizontal controls = new Horizontal();
    controls.setBorderSpacing(20);

    final Button startButton = new Button("start");
    controls.add(startButton);

    final Button sendButton = new Button("send");
    controls.add(sendButton);

    final Button closeButton = new Button("stop");
    controls.add(closeButton);

    panel.add(controls);

    disable(sendButton);
    disable(closeButton);

    final RtcHolder holder = new RtcHolder();

    startButton.addClickHandler(clickEvent -> {
      RTCConfiguration pcConfig = null;
      RTCDataChannelInit dcConfig = null;

      holder.setLocalPeerConnection(RtcAdapter.createRTCPeerConnection(pcConfig));
      logger.debug("created local peer connection");

      holder.setSendChannel(holder.getLocalPeerConnection().createDataChannel("textDataChannel",
          dcConfig));
      logger.debug("created send data channel");

      holder.getLocalPeerConnection().setOnicecandidate(iceEvent -> {
        if (iceEvent.getCandidate() != null) {
          logger.debug("local ice candidate",
              formatCandidate(iceEvent.getCandidate().getCandidate()));
          holder.getRemotePeerConnection().addIceCandidate(iceEvent.getCandidate());
        }
      });

      holder.getSendChannel().setOnopen(openEvent -> {
        logger.debug("send channel open");

        sendArea.setEnabled(true);
        sendArea.setFocus(true);

        enable(sendButton);
        enable(closeButton);
      });

      holder.getSendChannel().setOnclose(closeEvent -> {
        logger.debug("send channel close");

        sendArea.setEnabled(false);

        disable(sendButton);
        disable(closeButton);
      });

      holder.setRemotePeerConnection(RtcAdapter.createRTCPeerConnection(pcConfig));
      logger.debug("created remote peer connection");

      holder.getRemotePeerConnection().setOnicecandidate(iceEvent -> {
        if (iceEvent.getCandidate() != null) {
          logger.debug("remote ice candidate",
              formatCandidate(iceEvent.getCandidate().getCandidate()));
          holder.getLocalPeerConnection().addIceCandidate(iceEvent.getCandidate());
        }
      });

      holder.getRemotePeerConnection().setOndatachannel(channelEvent -> {
        logger.debug("on receive channel");
        holder.setReceiveChannel(channelEvent.getChannel());

        holder.getReceiveChannel().setOnmessage(messageEvent -> {
          String data = messageEvent.getData();
          logger.debug("received", BeeUtils.length(data));
          receiveArea.setValue(data);
        });

        holder.getReceiveChannel().setOnopen(openEvent -> logger.debug("receive channel open"));
        holder.getReceiveChannel().setOnclose(closeEvent -> logger.debug("receive channel close"));
      });

      offer(holder.getLocalPeerConnection(), holder.getRemotePeerConnection());

      disable(startButton);
      enable(sendButton);
    });

    sendButton.addClickHandler(clickEvent -> {
      String data = BeeUtils.trimRight(sendArea.getValue());
      holder.getSendChannel().send(data);

      logger.debug("sent", data.length());
    });

    closeButton.addClickHandler(clickEvent -> {
      logger.debug("closing");

      holder.closeDataChannels();
      holder.closeConnections();

      sendArea.clearValue();
      receiveArea.clearValue();

      enable(startButton);
      disable(sendButton);
      disable(closeButton);
    });

    return panel;
  }

  public static void onMessage(SignalingMessage message) {
    String room = message.getLabel();
    logger.debug("on message", message.getKey());

    if (!rooms.containsKey(room)) {
      answer(message.getFrom(), room);
    }

    RtcHolder holder = rooms.get(room);

    switch (message.getKey()) {
      case KEY_CANDIDATE:
        RTCIceCandidate c = RtcAdapter.createRTCIceCandidate(parseIceCandidate(message.getValue()));
        holder.getLocalPeerConnection().addIceCandidate(c);
        break;

      case KEY_SDP:
        RTCSessionDescription sdp =
            RtcAdapter.createRTCSessionDescription(parseSessionDescription(message.getValue()));
        holder.getLocalPeerConnection().setRemoteDescription(sdp);
        break;
    }
  }

  private static void answer(final String session, final String room) {
    final Flow videos = new Flow(STYLE_PANEL);

    final Video localVideo = new Video(STYLE_LOCAL);
    localVideo.setAutoplay(true);
    localVideo.setControls(true);
    localVideo.setMuted(true);

    videos.add(localVideo);

    final Video remoteVideo = new Video(STYLE_REMOTE);
    remoteVideo.setAutoplay(true);
    remoteVideo.setControls(true);

    videos.add(remoteVideo);

    final RtcHolder holder = new RtcHolder();

    RTCConfiguration pcConfig = null;
    holder.setLocalPeerConnection(RtcAdapter.createRTCPeerConnection(pcConfig));
    logger.debug("created local peer connection");

    holder.getLocalPeerConnection().setOnicecandidate(iceEvent -> {
      if (iceEvent.getCandidate() != null) {
        logger.debug("on ice candidate");
        send(session, room, iceEvent.getCandidate());
      }
    });

    holder.getLocalPeerConnection().setOnaddstream(streamEvent -> {
      logger.debug("on add stream");
      RtcAdapter.attachMediaStream(remoteVideo.getMediaElement(), streamEvent.getStream());
    });

    rooms.put(room, holder);
    BeeKeeper.getScreen().show(videos);

    MediaStreamConstraints constraints = new MediaStreamConstraints(true, true);

    RtcAdapter.getUserMedia(constraints, stream -> {
      logger.debug("received local stream");
      holder.setLocalStream(stream);
      RtcAdapter.attachMediaStream(localVideo.getMediaElement(), stream);

      holder.getLocalPeerConnection().addStream(stream);
      logger.debug("added local stream");

      holder.getLocalPeerConnection().createAnswer(description -> {
        logger.debug("create answer");
        holder.getLocalPeerConnection().setLocalDescription(description);
        send(session, room, description);
      }, RtcUtils::handleError);
    }, error -> logger.severe("gum error", MediaUtils.format(error)));
  }

  private static void disable(Widget widget) {
    StyleUtils.setProperty(widget, CssProperties.VISIBILITY, Visibility.HIDDEN);
  }

  private static void enable(Widget widget) {
    StyleUtils.setProperty(widget, CssProperties.VISIBILITY, Visibility.VISIBLE);
  }

  private static String formatCandidate(String candidate) {
    return BeeUtils.clip(candidate, 128);
  }

  private static String formatSdp(String sdp) {
    return BeeUtils.clip(sdp, 128);
  }

  private static void handleError(DOMError error) {
    logger.severe((error == null) ? "error" : stringify(error));
  }

  private static void offer(final RTCPeerConnection localPc, final RTCPeerConnection remotePc) {
    localPc.createOffer(localDescription -> {
      logger.debug("offer from localPeerConnection", formatSdp(localDescription.getSdp()));
      localPc.setLocalDescription(localDescription);
      remotePc.setRemoteDescription(localDescription);

      remotePc.createAnswer(remoteDescription -> {
        logger.debug("answer from remotePeerConnection", formatSdp(remoteDescription.getSdp()));

        remotePc.setLocalDescription(remoteDescription);
        localPc.setRemoteDescription(remoteDescription);
      }, RtcUtils::handleError);
    }, RtcUtils::handleError);
  }

  @JsMethod(namespace = "JSON", name = "parse")
  private static native RTCIceCandidate parseIceCandidate(String input);

  @JsMethod(namespace = "JSON", name = "parse")
  private static native RTCSessionDescription parseSessionDescription(String input);

  private static void send(String to, String label, RTCIceCandidate candidate) {
    Endpoint.send(SignalingMessage.signal(Endpoint.getSessionId(), to, label,
        KEY_CANDIDATE, stringify(candidate)));
  }

  private static void send(String to, String label, RTCSessionDescription sdp) {
    Endpoint.send(SignalingMessage.signal(Endpoint.getSessionId(), to, label,
        KEY_SDP, stringify(sdp)));
  }

  @JsMethod(namespace = "JSON")
  private static native String stringify(Object obj);

  private RtcUtils() {
  }
}
