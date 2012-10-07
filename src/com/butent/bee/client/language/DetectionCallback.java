package com.butent.bee.client.language;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

/**
 * Processes detection result, informs about language detection error if it occurs.
 */

public abstract class DetectionCallback {

  private static final BeeLogger logger = LogUtils.getLogger(DetectionCallback.class);
  
  public final void onCallbackWrapper(DetectionResult result) {
    if (result == null) {
      return;
    }
    Error error = result.getError();
    if (error != null) {
      logger.severe("Language detection error", error.getCode(), error.getMessage());
      return;
    }
    onCallback(result);
  }

  protected abstract void onCallback(DetectionResult result);
}
