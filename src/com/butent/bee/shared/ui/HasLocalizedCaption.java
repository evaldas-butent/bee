package com.butent.bee.shared.ui;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;

@FunctionalInterface
public interface HasLocalizedCaption extends HasCaption {

  @Override
  default String getCaption() {
    return getCaption(Localized.getConstants());
  }

  String getCaption(LocalizableConstants constants);
}
