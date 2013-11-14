package com.butent.bee.shared.ui;

import com.butent.bee.shared.i18n.LocalizableConstants;

public interface HasLocalizedCaption extends HasCaption {
  String getCaption(LocalizableConstants constants);
}
