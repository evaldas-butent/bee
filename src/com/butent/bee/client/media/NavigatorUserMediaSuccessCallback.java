package com.butent.bee.client.media;

import jsinterop.annotations.JsFunction;

@JsFunction
@FunctionalInterface
public interface NavigatorUserMediaSuccessCallback {
  void onSuccess(MediaStream stream);
}
