package com.butent.bee.client.language;

import com.google.common.collect.Lists;

import com.butent.bee.client.ajaxloader.AjaxLoader;
import com.butent.bee.shared.Assert;

import java.util.List;

/**
 * Loads translation application programming interface.
 */

public final class LanguageUtils {

  public static final String TRANSLATION_API_NAME = "language";
  public static final String TRANSLATION_API_VERSION = "1";

  private static boolean translationLoaded;
  private static List<Runnable> queue = Lists.newArrayList();

  public static boolean isTranslationLoaded() {
    return translationLoaded;
  }

  public static void loadTranslation(Runnable onLoad) {
    loadTranslation(TRANSLATION_API_VERSION, onLoad);
  }

  public static void loadTranslation(String version, Runnable onLoad) {
    Assert.notNull(onLoad);
    if (translationLoaded) {
      onLoad.run();
      return;
    }
    Assert.notEmpty(version);
    queue.add(onLoad);
    loadTranslationApi(version);
  }

  private static void loadTranslationApi(String version) {
    AjaxLoader.loadApi(TRANSLATION_API_NAME, version,
        new Runnable() {
          @Override
          public void run() {
            translationLoaded = true;
            for (Runnable r : queue) {
              r.run();
            }
            queue.clear();
          }
        });
  }

  private LanguageUtils() {
  }
}
