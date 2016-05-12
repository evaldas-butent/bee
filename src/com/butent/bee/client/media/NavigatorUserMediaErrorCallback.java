package com.butent.bee.client.media;

import jsinterop.annotations.JsFunction;

@JsFunction
@FunctionalInterface
public interface NavigatorUserMediaErrorCallback {
  void onError(MediaStreamError error);
}
