package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.List;

public enum TradeAccountsPrecedence implements HasLocalizedCaption {

  DOCUMENT_LINE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.trdDocumentLine();
    }
  },

  ITEM {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemOrService();
    }
  },

  ITEM_GROUP {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemGroup();
    }
  },

  ITEM_TYPE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemType();
    }
  },

  ITEM_CATEGORY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemCategory();
    }
  },

  DOCUMENT {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.trdDocumentLong();
    }
  },

  COMPANY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.company();
    }
  },

  OPERATION {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.trdOperation();
    }
  },

  WAREHOUSE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.warehouse();
    }
  };

  public static String asString(List<TradeAccountsPrecedence> list) {
    return EnumUtils.joinNames(list);
  }

  public static List<TradeAccountsPrecedence> parse(String input) {
    List<TradeAccountsPrecedence> result = new ArrayList<>();

    List<TradeAccountsPrecedence> list =
        EnumUtils.parseNameList(TradeAccountsPrecedence.class, input);

    for (TradeAccountsPrecedence tap : list) {
      if (!result.contains(tap)) {
        result.add(tap);
      }
    }

    if (result.size() < values().length) {
      for (TradeAccountsPrecedence tap : values()) {
        if (!result.contains(tap)) {
          result.add(tap);
        }
      }
    }

    return result;
  }
}
