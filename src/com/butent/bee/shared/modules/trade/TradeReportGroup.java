package com.butent.bee.shared.modules.trade;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.shared.report.ReportParameters;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public enum TradeReportGroup {

  ITEM_TYPE("type") {
    @Override
    public String valueSource() {
      return TBL_ITEMS;
    }

    @Override
    public String valueColumn() {
      return COL_ITEM_TYPE;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return TBL_ITEM_CATEGORY_TREE;
    }

    @Override
    public String labelColumn() {
      return COL_CATEGORY_NAME;
    }
  },

  ITEM_GROUP("group") {
    @Override
    public String valueSource() {
      return TBL_ITEMS;
    }

    @Override
    public String valueColumn() {
      return COL_ITEM_GROUP;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return TBL_ITEM_CATEGORY_TREE;
    }

    @Override
    public String labelColumn() {
      return COL_CATEGORY_NAME;
    }
  },

  ITEM("item") {
    @Override
    public String valueSource() {
      return TBL_TRADE_DOCUMENT_ITEMS;
    }

    @Override
    public String valueColumn() {
      return COL_ITEM;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return TBL_ITEMS;
    }

    @Override
    public String labelColumn() {
      return COL_ITEM_NAME;
    }
  },

  ARTICLE("article") {
    @Override
    public String valueSource() {
      return TBL_TRADE_DOCUMENT_ITEMS;
    }

    @Override
    public String valueColumn() {
      return COL_TRADE_ITEM_ARTICLE;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return null;
    }

    @Override
    public String labelColumn() {
      return null;
    }
  },

  UNIT("unit") {
    @Override
    public String valueSource() {
      return TBL_ITEMS;
    }

    @Override
    public String valueColumn() {
      return COL_UNIT;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return TBL_UNITS;
    }

    @Override
    public String labelColumn() {
      return COL_UNIT_NAME;
    }
  },

  WAREHOUSE("warehouse") {
    @Override
    public String valueSource() {
      return TBL_TRADE_STOCK;
    }

    @Override
    public String valueColumn() {
      return COL_STOCK_WAREHOUSE;
    }

    @Override
    public boolean primaryDocument() {
      return false;
    }

    @Override
    public String labelSource() {
      return TBL_WAREHOUSES;
    }

    @Override
    public String labelColumn() {
      return COL_WAREHOUSE_CODE;
    }
  },

  SUPPLIER("supplier") {
    @Override
    public String valueSource() {
      return TBL_TRADE_DOCUMENTS;
    }

    @Override
    public String valueColumn() {
      return COL_TRADE_SUPPLIER;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return TBL_COMPANIES;
    }

    @Override
    public String labelColumn() {
      return COL_COMPANY_NAME;
    }
  },

  YEAR_RECEIVED("year") {
    @Override
    public String valueSource() {
      return TBL_TRADE_DOCUMENTS;
    }

    @Override
    public String valueColumn() {
      return COL_TRADE_DATE;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return null;
    }

    @Override
    public String labelColumn() {
      return null;
    }
  },

  MONTH_RECEIVED("month") {
    @Override
    public String valueSource() {
      return TBL_TRADE_DOCUMENTS;
    }

    @Override
    public String valueColumn() {
      return COL_TRADE_DATE;
    }

    @Override
    public boolean primaryDocument() {
      return true;
    }

    @Override
    public String labelSource() {
      return null;
    }

    @Override
    public String labelColumn() {
      return null;
    }
  };

  private final String code;

  TradeReportGroup(String code) {
    this.code = code;
  }

  public static TradeReportGroup parse(String input) {
    if (!BeeUtils.isEmpty(input)) {
      for (TradeReportGroup trg : values()) {
        if (BeeUtils.same(trg.code, input)) {
          return trg;
        }
      }
    }
    return null;
  }

  public static List<TradeReportGroup> parseList(ReportParameters reportParameters, int count) {
    List<TradeReportGroup> list = new ArrayList<>();

    if (reportParameters != null) {
      for (int i = 0; i < count; i++) {
        TradeReportGroup trg = parse(reportParameters.getText(TradeConstants.reportGroupName(i)));

        if (trg != null && !list.contains(trg)) {
          list.add(trg);
        }
      }
    }

    return list;
  }

  public static boolean needsItem(TradeReportGroup trg) {
    return trg != null && trg.needsItem();
  }

  public static boolean needsItem(Collection<TradeReportGroup> groups) {
    if (!BeeUtils.isEmpty(groups)) {
      return groups.stream()
          .filter(Objects::nonNull)
          .anyMatch(trg -> trg.needsItem());

    } else {
      return false;
    }
  }

  public static boolean needsPrimaryDocument(TradeReportGroup trg) {
    return trg != null && trg.needsPrimaryDocument();
  }

  public static boolean needsPrimaryDocument(Collection<TradeReportGroup> groups) {
    if (!BeeUtils.isEmpty(groups)) {
      return groups.stream()
          .filter(Objects::nonNull)
          .anyMatch(trg -> trg.needsPrimaryDocument());

    } else {
      return false;
    }
  }

  public static boolean needsPrimaryDocumentItem(TradeReportGroup trg) {
    return trg != null && trg.needsPrimaryDocumentItem();
  }

  public static boolean needsPrimaryDocumentItem(Collection<TradeReportGroup> groups) {
    if (!BeeUtils.isEmpty(groups)) {
      return groups.stream()
          .filter(Objects::nonNull)
          .anyMatch(trg -> trg.needsPrimaryDocumentItem());

    } else {
      return false;
    }
  }

  public abstract String valueSource();

  public abstract String valueColumn();

  public abstract boolean primaryDocument();

  public abstract String labelSource();

  public abstract String labelColumn();

  public boolean needsItem() {
    return TBL_ITEMS.equals(valueSource());
  }

  public boolean needsPrimaryDocument() {
    return primaryDocument() && TBL_TRADE_DOCUMENTS.equals(valueSource());
  }

  public boolean needsPrimaryDocumentItem() {
    return primaryDocument() && TBL_TRADE_DOCUMENT_ITEMS.equals(valueSource());
  }
}
