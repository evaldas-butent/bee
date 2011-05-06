package com.butent.bee.client.language;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;

/**
 * Processes translation result, informs about a translation error if it occurs, saves it if
 * successful.
 */

public abstract class TranslationCallback {
  public final void onCallbackWrapper(String key, TranslationResult result) {
    Assert.notNull(result);
    Error error = result.getError();
    if (error != null) {
      BeeKeeper.getLog().severe("Translation error", error.getCode(), error.getMessage());
    } else {
      String value = result.getTranslatedText();
      Translation.saveTranslation(key, value);
      onCallback(value);
    }
  }

  protected abstract void onCallback(String translation);
}
