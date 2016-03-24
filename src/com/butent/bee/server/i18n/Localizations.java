package com.butent.bee.server.i18n;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumMap;
import java.util.Map;

public final class Localizations {

  private static BeeLogger logger = LogUtils.getLogger(Localizations.class);

  private static final EnumMap<SupportedLocale, Map<String, String>> glossaries =
      new EnumMap<>(SupportedLocale.class);

  private static final EnumMap<SupportedLocale, Dictionary> dictionaries =
      new EnumMap<>(SupportedLocale.class);

  public static Dictionary getDictionary(SupportedLocale supportedLocale) {
    return dictionaries.get(supportedLocale);
  }

  public static Dictionary getDictionary(String language) {
    return getDictionary(SupportedLocale.parse(language));
  }

  public static Map<String, String> getGlossary(SupportedLocale supportedLocale) {
    return glossaries.get(supportedLocale);
  }

  public static Map<String, String> getGlossary(String language) {
    return getGlossary(SupportedLocale.parse(language));
  }

  public static void init() {
    if (!glossaries.isEmpty()) {
      glossaries.clear();
    }
    if (!dictionaries.isEmpty()) {
      dictionaries.clear();
    }

    Map<String, String> defaultGlossary =
        I18nUtils.readProperties(SupportedLocale.DICTIONARY_DEFAULT);
    if (BeeUtils.isEmpty(defaultGlossary)) {
      logger.severe(SupportedLocale.DICTIONARY_DEFAULT, "glossary not found");
      return;
    }

    glossaries.put(SupportedLocale.DICTIONARY_DEFAULT, defaultGlossary);
    dictionaries.put(SupportedLocale.DICTIONARY_DEFAULT,
        createDictionary(SupportedLocale.DICTIONARY_DEFAULT));

    for (SupportedLocale supportedLocale : SupportedLocale.values()) {
      if (supportedLocale != SupportedLocale.DICTIONARY_DEFAULT) {
        Map<String, String> glossary = I18nUtils.readProperties(supportedLocale);
        if (BeeUtils.isEmpty(glossary)) {
          logger.severe(supportedLocale, "glossary not found");
        }

        glossaries.put(supportedLocale, glossary);
        dictionaries.put(supportedLocale, createDictionary(supportedLocale));
      }
    }
  }

  public static void putAll(SupportedLocale supportedLocale, Map<String, String> glossary) {
    if (supportedLocale == null) {
      logger.warning("Localizations putAll: supportedLocale is null");

    } else if (!BeeUtils.isEmpty(glossary)) {
      if (glossaries.containsKey(supportedLocale)) {
        glossaries.get(supportedLocale).putAll(glossary);
      } else {
        glossaries.put(supportedLocale, glossary);
      }

      if (!dictionaries.containsKey(supportedLocale)) {
        dictionaries.put(supportedLocale, createDictionary(supportedLocale));
      }
    }
  }

  private static Dictionary createDictionary(SupportedLocale supportedLocale) {
    if (supportedLocale == SupportedLocale.DICTIONARY_DEFAULT) {
      return key -> BeeUtils.nvl(glossaries.get(supportedLocale).get(key), key);

    } else {
      return key -> {
        String value = glossaries.get(supportedLocale).get(key);
        if (value == null) {
          value = glossaries.get(SupportedLocale.DICTIONARY_DEFAULT).get(key);
        }

        return BeeUtils.nvl(value, key);
      };
    }
  }

  private Localizations() {
  }
}
