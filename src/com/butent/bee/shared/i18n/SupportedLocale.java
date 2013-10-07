package com.butent.bee.shared.i18n;

import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

public enum SupportedLocale implements HasCaption {
  LT {
    @Override
    public String getCaption() {
      return "Lietuviškai";
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
    public String getLanguage() {
      return "en";
    }
  };
  
  public static final SupportedLocale DEFAULT = LT;
  
  public static SupportedLocale getByLanguage(String language) {
    for (SupportedLocale locale : values()) {
      if (BeeUtils.same(locale.getLanguage(), language)) {
        return locale;
      }
    }
    return null;
  }

  public abstract String getLanguage();
}
