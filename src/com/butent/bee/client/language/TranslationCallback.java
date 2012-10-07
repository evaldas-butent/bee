package com.butent.bee.client.language;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

/**
 * Processes translation result, informs about a translation error if it occurs, saves it if
 * successful.
 */

public abstract class TranslationCallback {

  private static final BeeLogger logger = LogUtils.getLogger(TranslationCallback.class);
  
  public final void onCallbackWrapper(String key, TranslationResult result) {
    Assert.notNull(result);
    Error error = result.getError();
    if (error != null) {
      logger.severe("Translation error", error.getCode(), error.getMessage());
    } else {
      String value = result.getTranslatedText();
      Translation.saveTranslation(key, value);
      onCallback(value);
    }
  }

  protected abstract void onCallback(String translation);
}
