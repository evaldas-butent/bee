package com.butent.bee.server.i18n;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Localizations {

  private static BeeLogger logger = LogUtils.getLogger(Localizations.class);

  private static final EnumMap<SupportedLocale, Map<String, String>> defaultGlossaries =
      new EnumMap<>(SupportedLocale.class);
  private static final EnumMap<SupportedLocale, Map<String, String>> customGlossaries =
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
    Map<String, String> result = new HashMap<>();

    if (supportedLocale == null) {
      logger.severe("Localizations getGlossary: supportedLocale is null");

    } else {
      Map<String, String> glossary = defaultGlossaries.get(supportedLocale);
      if (BeeUtils.isEmpty(glossary)) {
        logger.severe("Localizations getGlossary:", supportedLocale, "default glossary not found");
      } else {
        result.putAll(glossary);
      }

      glossary = customGlossaries.get(supportedLocale);
      if (!BeeUtils.isEmpty(glossary)) {
        result.putAll(glossary);
      }
    }

    if (supportedLocale != SupportedLocale.DICTIONARY_DEFAULT) {
      Map<String, String> glossary = getGlossary(SupportedLocale.DICTIONARY_DEFAULT);

      if (!BeeUtils.isEmpty(glossary)) {
        if (result.isEmpty()) {
          result.putAll(glossary);

        } else {
          glossary.forEach(result::putIfAbsent);
        }
      }
    }

    return result;
  }

  public static Map<String, String> getGlossary(String language) {
    return getGlossary(SupportedLocale.parse(language));
  }

  public static List<Property> getInfo() {
    List<Property> result = new ArrayList<>();

    result.add(new Property("Default Glossaries", defaultGlossaries.size()));
    defaultGlossaries.forEach((supportedLocale, glossary) ->
        result.add(new Property(supportedLocale.name(), BeeUtils.size(glossary))));

    result.add(new Property("Custom Glossaries", customGlossaries.size()));
    customGlossaries.forEach((supportedLocale, glossary) ->
        result.add(new Property(supportedLocale.name(), BeeUtils.size(glossary))));

    result.add(new Property("Dictionaries", dictionaries.keySet().toString()));

    return result;
  }

  public static void init() {
    loadDefaultGlossaries();
  }

  static Map<SupportedLocale, Map<String, String>> getDefaultGlossaries() {
    return defaultGlossaries;
  }

  static synchronized void setCustomGlossary(SupportedLocale supportedLocale,
      Map<String, String> glossary) {

    if (supportedLocale == null) {
      logger.warning("Localizations setCustomGlossary: supportedLocale is null");

    } else if (BeeUtils.isEmpty(glossary)) {
      if (customGlossaries.containsKey(supportedLocale)) {
        customGlossaries.remove(supportedLocale);
        logger.info("removed custom glossary", supportedLocale);
      }

    } else {
      customGlossaries.put(supportedLocale, glossary);
      putDictionaryIfAbsent(supportedLocale);

      logger.info("Localizations:", glossary.size(), "entries put into custom glossary",
          supportedLocale);
    }
  }

  private static Dictionary createDictionary(SupportedLocale supportedLocale) {
    if (supportedLocale == SupportedLocale.DICTIONARY_DEFAULT) {
      return key -> BeeUtils.nvl(getValue(supportedLocale, key), key);

    } else {
      return key -> {
        String value = getValue(supportedLocale, key);
        if (value == null) {
          value = getValue(SupportedLocale.DICTIONARY_DEFAULT, key);
        }

        return BeeUtils.nvl(value, key);
      };
    }
  }

  private static String getCustomValue(SupportedLocale supportedLocale, String key) {
    if (customGlossaries.containsKey(supportedLocale)) {
      return customGlossaries.get(supportedLocale).get(key);
    } else {
      return null;
    }
  }

  private static String getDefaultValue(SupportedLocale supportedLocale, String key) {
    if (defaultGlossaries.containsKey(supportedLocale)) {
      return defaultGlossaries.get(supportedLocale).get(key);
    } else {
      return null;
    }
  }

  private static String getValue(SupportedLocale supportedLocale, String key) {
    String value = getCustomValue(supportedLocale, key);
    if (value == null) {
      value = getDefaultValue(supportedLocale, key);
    }

    return value;
  }

  private static void loadDefaultGlossaries() {
    if (!defaultGlossaries.isEmpty()) {
      defaultGlossaries.clear();
    }

    for (SupportedLocale supportedLocale : SupportedLocale.values()) {
      Map<String, String> glossary = I18nUtils.readProperties(supportedLocale);
      if (BeeUtils.isEmpty(glossary)) {
        logger.severe(supportedLocale, "glossary properties not found");
      } else {
        defaultGlossaries.put(supportedLocale, glossary);
      }

      putDictionaryIfAbsent(supportedLocale);
    }
  }

  private static void putDictionaryIfAbsent(SupportedLocale supportedLocale) {
    if (!dictionaries.containsKey(supportedLocale)) {
      dictionaries.put(supportedLocale, createDictionary(supportedLocale));
    }
  }

  private Localizations() {
  }
}
