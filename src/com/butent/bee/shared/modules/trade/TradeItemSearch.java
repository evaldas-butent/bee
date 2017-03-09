package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeItemSearch implements HasLocalizedCaption {

  NAME {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemName();
    }
  },

  NAME_2 {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemName2();
    }
  },

  NAME_3 {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemName3();
    }
  },

  ARTICLE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.article();
    }
  },

  ARTICLE_2 {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.article2();
    }
  },

  BARCODE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemBarcode();
    }

  },

  TYPE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.type();
    }
  },

  GROUP {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.group();
    }
  },

  CATEGORY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.category();
    }
  },

  DESCRIPTION {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.description();
    }
  },

  SUPPLIER {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.supplier();
    }
  },

  MANUFACTURER {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.manufacturer();
    }
  },

  ID {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.captionId();
    }

    @Override
    public String validate(String input, Dictionary dictionary) {
      if (DataUtils.isId(input)) {
        return super.validate(input, dictionary);
      } else {
        return dictionary.invalidIdValue(input);
      }
    }
  };

  public String validate(String input, Dictionary dictionary) {
    return null;
  }
}
