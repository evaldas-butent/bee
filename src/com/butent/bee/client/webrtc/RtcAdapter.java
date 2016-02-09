package com.butent.bee.client.webrtc;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dom.client.MediaElement;

import com.butent.bee.client.media.MediaStream;
import com.butent.bee.client.media.MediaStreamConstraints;
import com.butent.bee.client.media.NavigatorUserMediaErrorCallback;
import com.butent.bee.client.media.NavigatorUserMediaSuccessCallback;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.List;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;

public final class RtcAdapter {

  private static final BeeLogger logger = LogUtils.getLogger(RtcAdapter.class);

  @JsMethod(namespace = JsPackage.GLOBAL)
  public static native void attachMediaStream(MediaElement element, MediaStream stream);

  @JsMethod(namespace = JsPackage.GLOBAL)
  public static native RTCIceCandidate createRTCIceCandidate(RTCIceCandidate candidate);

  @JsMethod(namespace = JsPackage.GLOBAL, name = "RTCPeerConnection")
  public static native RTCPeerConnection createRTCPeerConnection(RTCConfiguration pcConfig);

  @JsMethod(namespace = JsPackage.GLOBAL)
  public static native RTCSessionDescription createRTCSessionDescription(RTCSessionDescription rsd);

  public static List<Property> getInfo() {
    List<Property> info = new ArrayList<>();

    try {
      if (getWebrtcDetectedBrowser() == null) {
        info.add(new Property(NameUtils.getClassName(RtcAdapter.class), "browser not detected"));

      } else {
        PropertyUtils.addProperties(info,
            "Detected Browser", getWebrtcDetectedBrowser(),
            "Detected Version", getWebrtcDetectedVersion(),
            "Minimum Version", getWebrtcMinimumVersion());
      }

    } catch (JavaScriptException ex) {
      logger.error(ex);
    }

    return info;
  }

  @JsMethod(namespace = JsPackage.GLOBAL)
  public static native void getUserMedia(MediaStreamConstraints constraints,
      NavigatorUserMediaSuccessCallback successCallback,
      NavigatorUserMediaErrorCallback errorCallback);

  @JsProperty(namespace = JsPackage.GLOBAL)
  public static native Object getWebrtcDetectedBrowser();

  @JsProperty(namespace = JsPackage.GLOBAL)
  public static native Object getWebrtcDetectedVersion();

  @JsProperty(namespace = JsPackage.GLOBAL)
  public static native Object getWebrtcMinimumVersion();

  private RtcAdapter() {
  }
}
