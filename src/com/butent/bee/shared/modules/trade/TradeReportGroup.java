package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.report.ReportParameters;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public enum TradeReportGroup {

  ITEM_TYPE("type"),
  ITEM_GROUP("group"),
  ITEM("item"),
  ARTICLE("article"),
  UNIT("unit"),
  WAREHOUSE("warehouse"),
  SUPPLIER("supplier"),
  YEAR_RECEIVED("year"),
  MONTH_RECEIVED("month");

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
}
