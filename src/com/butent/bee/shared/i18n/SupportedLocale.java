package com.butent.bee.shared.i18n;

import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;

public enum SupportedLocale implements HasCaption {
  LT {
    @Override
    public String getCaption() {
      return "Lietuviškai";
    }

    @Override
    public String getIconName() {
      return "lithuanian";
    }

    @Override
    public String getLanguage() {
      return "lt";
    }
  },

  EN {
    @Override
    public String getCaption() {
      return "English";
    }

    @Override
    public String getIconName() {
      return "english";
    }

    @Override
    public String getLanguage() {
      return "en";
    }
  },

  RU {
    @Override
    public String getCaption() {
      return "Русский";
    }

    @Override
    public String getIconName() {
      return "russian";
    }

    @Override
    public String getLanguage() {
      return "ru";
    }
  },

  FI {
    @Override
    public String getCaption() {
      return "Suomi";
    }

    @Override
    public String getIconName() {
      return "finnish";
    }

    @Override
    public String getLanguage() {
      return "fi";
    }

  },

  LV {
    @Override
    public String getCaption() {
      return "Latviešu";
    }

    @Override
    public String getIconName() {
      return "latvian";
    }

    @Override
    public String getLanguage() {
      return "lv";
    }
  },

  DE {
    @Override
    public String getCaption() {
      return "Deutsch";
    }

    @Override
    public String getIconName() {
      return "german";
    }

    @Override
    public String getLanguage() {
      return "de";
    }
  };

  public static final SupportedLocale DICTIONARY_DEFAULT = EN;
  public static final SupportedLocale USER_DEFAULT = LT;

  public static SupportedLocale getByLanguage(String language) {
    for (SupportedLocale locale : values()) {
      if (BeeUtils.same(locale.getLanguage(), language)) {
        return locale;
      }
    }
    return null;
  }

  public static String normalizeLanguage(String language) {
    return parse(language).getLanguage();
  }

  public static SupportedLocale parse(String language) {
    return BeeUtils.nvl(getByLanguage(language), USER_DEFAULT);
  }

  public static List<SupportedLocale> parseList(String languages) {
    List<SupportedLocale> result = new ArrayList<>();

    for (String language : NameUtils.toList(languages)) {
      SupportedLocale supportedLocale = getByLanguage(language);
      if (supportedLocale != null && !result.contains(supportedLocale)) {
        result.add(supportedLocale);
      }
    }

    return result;
  }

  public String getDictionaryCustomColumnName() {
    return "Custom" + getLanguage().toUpperCase();
  }

  public String getDictionaryDefaultColumnName() {
    return "Default" + getLanguage().toUpperCase();
  }

  public String getDictionaryFileName() {
    return "dictionary_" + getLanguage().toLowerCase();
  }

  public abstract String getIconName();

  public abstract String getLanguage();
}
