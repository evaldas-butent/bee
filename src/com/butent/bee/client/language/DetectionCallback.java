package com.butent.bee.client.language;

import com.butent.bee.client.BeeKeeper;

/**
 * Processes detection result, informs about language detection error if it occurs.
 */

public abstract class DetectionCallback {

  public final void onCallbackWrapper(DetectionResult result) {
    if (result == null) {
      return;
    }
    Error error = result.getError();
    if (error != null) {
      BeeKeeper.getLog().severe("Language detection error", error.getCode(), error.getMessage());
      return;
    }
    onCallback(result);
  }

  protected abstract void onCallback(DetectionResult result);
}
