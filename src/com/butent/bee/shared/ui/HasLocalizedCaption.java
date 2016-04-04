package com.butent.bee.shared.ui;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;

@FunctionalInterface
public interface HasLocalizedCaption extends HasCaption {

  @Override
  default String getCaption() {
    return getCaption(Localized.dictionary());
  }

  String getCaption(Dictionary dictionary);
}
