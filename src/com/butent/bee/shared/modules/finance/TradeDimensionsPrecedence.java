package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.List;

public enum TradeDimensionsPrecedence implements HasLocalizedCaption {

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

  public static String asString(List<TradeDimensionsPrecedence> list) {
    return EnumUtils.joinNames(list);
  }

  public static List<TradeDimensionsPrecedence> parse(String input) {
    List<TradeDimensionsPrecedence> result = new ArrayList<>();

    List<TradeDimensionsPrecedence> list =
        EnumUtils.parseNameList(TradeDimensionsPrecedence.class, input);

    for (TradeDimensionsPrecedence tdp : list) {
      if (!result.contains(tdp)) {
        result.add(tdp);
      }
    }

    if (result.size() < values().length) {
      for (TradeDimensionsPrecedence tdp : values()) {
        if (!result.contains(tdp)) {
          result.add(tdp);
        }
      }
    }

    return result;
  }
}
