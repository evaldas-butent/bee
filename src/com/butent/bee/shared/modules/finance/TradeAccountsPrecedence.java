package com.butent.bee.shared.modules.finance;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

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

    @Override
    public String getTableName() {
      return TBL_TRADE_DOCUMENT_ITEMS;
    }
  },

  ITEM {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemOrService();
    }

    @Override
    public String getTableName() {
      return TBL_ITEMS;
    }
  },

  ITEM_GROUP {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemGroup();
    }

    @Override
    public String getTableName() {
      return TBL_ITEM_CATEGORY_TREE;
    }
  },

  ITEM_TYPE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemType();
    }

    @Override
    public String getTableName() {
      return TBL_ITEM_CATEGORY_TREE;
    }
  },

  ITEM_CATEGORY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.itemCategory();
    }

    @Override
    public String getTableName() {
      return TBL_ITEM_CATEGORY_TREE;
    }
  },

  DOCUMENT {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.trdDocumentLong();
    }

    @Override
    public String getTableName() {
      return TBL_TRADE_DOCUMENTS;
    }
  },

  COMPANY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.company();
    }

    @Override
    public String getTableName() {
      return TBL_COMPANIES;
    }
  },

  OPERATION {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.trdOperation();
    }

    @Override
    public String getTableName() {
      return TBL_TRADE_OPERATIONS;
    }
  },

  WAREHOUSE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.warehouse();
    }

    @Override
    public String getTableName() {
      return TBL_WAREHOUSES;
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

  public abstract String getTableName();
}
