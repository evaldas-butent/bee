package com.butent.bee.shared.ui;

import com.butent.bee.shared.i18n.Dictionary;

public enum WindowType implements HasLocalizedCaption {
  NEW_TAB {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.windowNewTab();
    }
  },

  ON_TOP {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.windowOnTop();
    }
  },

  DETACHED {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.windowDetached();
    }
  },

  MODAL {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.windowModal();
    }
  }
}
