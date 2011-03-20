package com.butent.bee.server.i18n;

import com.google.common.collect.Maps;

import com.butent.bee.server.io.ExtensionFilter;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.io.NameUtils;
import com.butent.bee.server.io.NameUtils.Component;
import com.butent.bee.server.io.WildcardFilter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.resources.LocalizableConstants;
import com.butent.bee.shared.resources.LocalizableMessages;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Wildcards;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class Localized {
  private enum LocalizableType {
    CONSTANTS, MESSAGES
  }

  public static Locale defaultLocale = Locale.getDefault();
  private static Locale rootLocale = Locale.ROOT;

  private static Map<Locale, File> availableConstants = null;
  private static Map<Locale, File> availableMessages = null;

  private static Map<Locale, LocalizableConstants> localizedConstants = Maps.newHashMap();
  private static Map<Locale, LocalizableMessages> localizedMessages = Maps.newHashMap();

  private static Logger logger = Logger.getLogger(Localized.class.getName());

  public static Map<Locale, File> getAvailableConstants() {
    return availableConstants;
  }

  public static Map<Locale, File> getAvailableMessages() {
    return availableMessages;
  }

  public static Collection<Locale> getCachedConstantLocales() {
    return localizedConstants.keySet();
  }

  public static Collection<Locale> getCachedMessageLocales() {
    return localizedMessages.keySet();
  }

  public static LocalizableConstants getConstants() {
    return getConstants(defaultLocale);
  }

  public static LocalizableConstants getConstants(Locale locale) {
    Assert.notNull(locale);
    return ensureConstants(locale);
  }

  public static LocalizableMessages getMessages() {
    return getMessages(defaultLocale);
  }

  public static LocalizableMessages getMessages(Locale locale) {
    Assert.notNull(locale);
    return ensureMessages(locale);
  }

  public static Locale normalize(Locale locale, Map<Locale, File> available) {
    if (locale == null || available == null) {
      return null;
    }
    if (available.containsKey(locale)) {
      return locale;
    }
    Locale z;

    if (!BeeUtils.isEmpty(locale.getVariant())) {
      z = new Locale(locale.getLanguage(), locale.getCountry());
      if (available.containsKey(z)) {
        return z;
      }
    }
    if (!BeeUtils.isEmpty(locale.getCountry())) {
      z = new Locale(locale.getLanguage());
      if (available.containsKey(z)) {
        return z;
      }
    }

    if (available.containsKey(rootLocale)) {
      return rootLocale;
    }
    return null;
  }

  public static String transform(Locale locale) {
    if (locale == null) {
      return BeeConst.NULL;
    }
    if (rootLocale.equals(locale)) {
      return "ROOT";
    }
    return locale.toString();
  }

  private static LocalizableConstants ensureConstants(Locale locale) {
    if (availableConstants == null) {
      Assert.isTrue(init(LocalizableType.CONSTANTS));
    }

    Locale z = normalize(locale, availableConstants);
    Assert.notNull(z, BeeUtils.concat(1, LocalizableType.CONSTANTS, transform(locale),
        "not available"));

    LocalizableConstants constants = localizedConstants.get(z);
    if (constants == null) {
      constants = I18nUtils.createConstants(LocalizableConstants.class,
          FileUtils.readProperties(availableConstants.get(z)));
      Assert.notNull(constants);
      localizedConstants.put(z, constants);
      LogUtils.info(logger, LocalizableType.CONSTANTS, transform(z), "loaded");
    }
    return constants;
  }

  private static LocalizableMessages ensureMessages(Locale locale) {
    if (availableMessages == null) {
      Assert.isTrue(init(LocalizableType.MESSAGES));
    }

    Locale z = normalize(locale, availableMessages);
    Assert.notNull(z, BeeUtils.concat(1, LocalizableType.MESSAGES, transform(locale),
        "not available"));

    LocalizableMessages messages = localizedMessages.get(z);
    if (messages == null) {
      messages = I18nUtils.createMessages(LocalizableMessages.class,
          FileUtils.readProperties(availableMessages.get(z)));
      Assert.notNull(messages);
      localizedMessages.put(z, messages);
      LogUtils.info(logger, LocalizableType.MESSAGES, transform(z), "loaded");
    }
    return messages;
  }

  private static boolean init(LocalizableType type) {
    Class<?> itf;

    switch (type) {
      case CONSTANTS:
        availableConstants = Maps.newHashMap();
        itf = LocalizableConstants.class;
        break;
      case MESSAGES:
        availableMessages = Maps.newHashMap();
        itf = LocalizableMessages.class;
        break;
      default:
        Assert.untouchable();
        itf = null;
    }

    String baseName = itf.getSimpleName();
    int baseLen = baseName.length();
    char sep = I18nUtils.LOCALE_SEPARATOR;

    File dir = FileUtils.toFile(itf).getParentFile();
    List<File> files = FileUtils.findFiles(dir,
        new WildcardFilter(baseName + Wildcards.getFsAny(), Component.BASE_NAME),
        new ExtensionFilter(FileUtils.EXT_PROPERTIES));

    String name, sfx;
    Locale locale;
    int cnt = 0;

    for (File file : files) {
      name = NameUtils.getBaseName(file.getName());
      if (BeeUtils.same(name, baseName)) {
        makeAvailable(type, rootLocale, file);
        cnt++;
        continue;
      }

      if (name.charAt(baseLen) != sep) {
        LogUtils.severe(logger, type, "unrecognized localization", file.getPath());
        continue;
      }

      sfx = name.substring(baseLen + 1);
      locale = I18nUtils.toLocale(sfx);
      if (locale == null) {
        LogUtils.severe(logger, type, sfx, "locale not available", file.getPath());
        continue;
      }

      makeAvailable(type, locale, file);
      cnt++;
    }

    if (cnt <= 0) {
      LogUtils.severe(logger, type, dir.getPath(), baseName, "not found");
    }
    return cnt > 0;
  }

  private static void makeAvailable(LocalizableType type, Locale locale, File file) {
    switch (type) {
      case CONSTANTS:
        availableConstants.put(locale, file);
        break;
      case MESSAGES:
        availableMessages.put(locale, file);
        break;
      default:
        Assert.untouchable();
    }
    LogUtils.info(logger, type, "found localization", transform(locale), file.getPath());
  }

  private Localized() {
  }
}
