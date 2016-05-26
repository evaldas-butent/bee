package com.butent.bee.shared.modules.classifiers;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum ItemPrice implements HasLocalizedCaption {
  SALE {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.salePrice();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.salePriceLabel();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE;
    }
  },

  COST {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.cost();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_COST_CURRENCY;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.costLabel();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_COST;
    }
  },

  PRICE_1 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price1();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_1;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price1Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_1;
    }
  },

  PRICE_2 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price2();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_2;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price2Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_2;
    }
  },

  PRICE_3 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price3();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_3;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price3Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_3;
    }
  },

  PRICE_4 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price4();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_4;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price4Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_4;
    }
  },

  PRICE_5 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price5();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_5;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price5Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_5;
    }
  },

  PRICE_6 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price6();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_6;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price6Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_6;
    }
  },

  PRICE_7 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price7();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_7;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price7Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_7;
    }
  },

  PRICE_8 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price8();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_8;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price8Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_8;
    }
  },

  PRICE_9 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price9();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_9;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price9Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_9;
    }
  },

  PRICE_10 {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.price10();
    }

    @Override
    public String getCurrencyColumn() {
      return ClassifierConstants.COL_ITEM_CURRENCY_10;
    }

    @Override
    public String getLabel(Dictionary constants) {
      return constants.price10Label();
    }

    @Override
    public String getPriceColumn() {
      return ClassifierConstants.COL_ITEM_PRICE_10;
    }
  };

  public String getCurrencyAlias() {
    return "Item" + getCurrencyColumn();
  }

  public abstract String getCurrencyColumn();

  public String getCurrencyNameAlias() {
    return getCurrencyColumn() + "Name";
  }

  public String getLabel() {
    return getLabel(Localized.dictionary());
  }

  public abstract String getLabel(Dictionary constants);

  public String getPriceAlias() {
    return "Item" + getPriceColumn();
  }

  public abstract String getPriceColumn();
}
