package com.butent.bee.client.language;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains core live content translation functions like {@code detectAndTranslate}.
 */

public final class Translation {

  private static final BeeLogger logger = LogUtils.getLogger(Translation.class);

  @SuppressWarnings("serial")
  private static Map<String, String> translationCache = new LinkedHashMap<String, String>() {
    @Override
    protected boolean removeEldestEntry(Entry<String, String> eldest) {
      return size() > 1000;
    }
  };

  public static void detect(final String text, final DetectionCallback callback) {
    Assert.notEmpty(text);
    Assert.notNull(callback);

    if (LanguageUtils.isTranslationLoaded()) {
      detectLanguage(text, callback);
    } else {
      LanguageUtils.loadTranslation(new Runnable() {
        @Override
        public void run() {
          detectLanguage(text, callback);
        }
      });
    }
  }

  public static void detectAndTranslate(String text, String to, TranslationCallback callback) {
    Language dst = Language.getByCode(to);
    if (dst == null) {
      logger.warning("Language", to, "not recognized");
      return;
    }
    translate(text, null, dst, callback);
  }

  public static FontRenderingStatus isFontRenderingSupported(Language lang) {
    int status = isFontRenderingSupported(lang.getLangCode());
    switch (status) {
      case 0:
        return FontRenderingStatus.UNSUPPORTED;
      case 1:
        return FontRenderingStatus.SUPPORTED;
    }
    return FontRenderingStatus.UNKNOWN;
  }

  public static boolean isTranslatable(Language lang) {
    return isTranslatable(lang.getLangCode());
  }

  public static void saveTranslation(String key, String value) {
    Assert.notEmpty(key);
    Assert.notEmpty(value);
    translationCache.put(key, value);
  }

  public static void translate(Option option, Language src, Language dst,
      TranslationCallback callback) {
    Assert.notNull(option);
    Assert.notNull(dst);
    Assert.notNull(callback);
    String codeFrom = (src == null) ? BeeConst.STRING_EMPTY : src.getLangCode();
    String codeTo = dst.getLangCode();
    translateWithOption(option, codeFrom, codeTo,
        translationKey(option.getText(), codeFrom, codeTo), callback);
  }

  public static void translate(final String text, Language src, Language dst,
      final TranslationCallback callback) {
    Assert.notEmpty(text);
    Assert.notNull(dst);
    Assert.notNull(callback);

    final String codeFrom = (src == null) ? BeeConst.STRING_EMPTY : src.getLangCode();
    final String codeTo = dst.getLangCode();
    if (BeeUtils.same(codeFrom, codeTo)) {
      callback.onCallback(text);
      return;
    }

    final String key = translationKey(text, codeFrom, codeTo);
    String value = translationCache.get(key);
    if (!BeeUtils.isEmpty(value)) {
      callback.onCallback(text);
      return;
    }

    if (LanguageUtils.isTranslationLoaded()) {
      translate(text, codeFrom, codeTo, key, callback);
    } else {
      LanguageUtils.loadTranslation(new Runnable() {
        @Override
        public void run() {
          translate(text, codeFrom, codeTo, key, callback);
        }
      });
    }
  }

//CHECKSTYLE:OFF
  public static native void translate(String text, String src, String dst,
      String key, TranslationCallback callback) /*-{
    $wnd.google.language.translate(text, src, dst, function(result) {
      callback.@com.butent.bee.client.language.TranslationCallback::onCallbackWrapper(Ljava/lang/String;Lcom/butent/bee/client/language/TranslationResult;)(key,result);
    });
  }-*/;
//CHECKSTYLE:ON
  
  public static void translate(String text, String from, String to, TranslationCallback callback) {
    Language src = Language.getByCode(from);
    if (src == null) {
      logger.warning("Language", from, "not recognized");
      return;
    }
    Language dst = Language.getByCode(to);
    if (dst == null) {
      logger.warning("Language", to, "not recognized");
      return;
    }
    translate(text, src, dst, callback);
  }

//CHECKSTYLE:OFF
  private static native void detectLanguage(String text, DetectionCallback callback) /*-{
    $wnd.google.language.detect(text, function(result) {
      callback.@com.butent.bee.client.language.DetectionCallback::onCallbackWrapper(Lcom/butent/bee/client/language/DetectionResult;)(result);
    });
  }-*/;

  private static native int isFontRenderingSupported(String langCode) /*-{
    return $wnd.google.language.isFontRenderingSupported(langCode);
  }-*/;

  private static native boolean isTranslatable(String langCode) /*-{
    return $wnd.google.language.isTranslatable(langCode);
  }-*/;

  private static native void translateWithOption(Option option, String src, String dst,
      String key, TranslationCallback callback) /*-{
    $wnd.google.language.translate(option, src, dst, function(result) {
      callback.@com.butent.bee.client.language.TranslationCallback::onCallbackWrapper(Ljava/lang/String;Lcom/butent/bee/client/language/TranslationResult;)(key,result);
    });
  }-*/;
//CHECKSTYLE:ON  

  private static String translationKey(String text, String from, String to) {
    return BeeUtils.join(BeeConst.DEFAULT_VALUE_SEPARATOR, text, from, to);
  }

  private Translation() {
  }
}
