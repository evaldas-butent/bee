package com.butent.bee.client.modules.trade.reports;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.report.ReportParameters;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class TradeMovementOfGoodsReport extends TradeStockReport {

  public TradeMovementOfGoodsReport() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeMovementOfGoodsReport();
  }

  @Override
  public void onLoad(FormView form) {
    ReportParameters parameters = readParameters();

    if (parameters != null) {
      loadDateTime(parameters, RP_START_DATE, form);
      loadDateTime(parameters, RP_END_DATE, form);

      commonLoad(parameters, form);

      loadListByValue(parameters, RP_MOVEMENT_COLUMNS, form);
    }

    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    storeDateTimeValues(RP_START_DATE, RP_END_DATE);
    commonStore();
    storeSelectedValue(RP_MOVEMENT_COLUMNS, 1);
  }

  @Override
  protected void clearFilter() {
    commonClearFilter();
  }

  @Override
  protected String getService() {
    return SVC_TRADE_MOVEMENT_OF_GOODS_REPORT;
  }

  @Override
  protected Report getReport() {
    return Report.TRADE_MOVEMENT_OF_GOODS;
  }

  @Override
  protected String stylePrefix() {
    return TradeKeeper.STYLE_PREFIX + "report-movement-of-goods-";
  }

  @Override
  protected ReportParameters getReportParameters() {
    ReportParameters parameters = new ReportParameters();

    addDateTimeValues(parameters, RP_START_DATE, RP_END_DATE);
    addCommonParameters(parameters);
    addSelectedValue(parameters, RP_MOVEMENT_COLUMNS, 1);

    return parameters;
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    DateTime start = parameters.getDateTime(RP_START_DATE);
    DateTime end = parameters.getDateTime(RP_END_DATE);

    return checkRequired(Localized.dictionary().dateFrom(), start)
        && checkRequired(Localized.dictionary().dateTo(), end)
        && checkRange(start, end)
        && super.validateParameters(parameters);
  }

  @Override
  protected String getDateCaption() {
    return Format.renderPeriod(getDateTime(RP_START_DATE), getDateTime(RP_END_DATE));
  }

  @Override
  protected List<String> getLabels(boolean addGrouping) {
    List<String> labels = super.getLabels(addGrouping);

    if (addGrouping && BeeUtils.isPositive(getSelectedIndex(RP_MOVEMENT_COLUMNS))) {
      labels.add(BeeUtils.joinWords(Localized.dictionary().trdReportColumnsMovement(),
          getSelectedItemText(RP_MOVEMENT_COLUMNS)));
    }

    return labels;
  }

  @Override
  protected String getExportFileName() {
    return Localized.dictionary().trdMovement();
  }

  @Override
  protected void render(Map<String, String> data) {
    SimpleRowSet rowSet = SimpleRowSet.restore(data.get(Service.VAR_DATA));

    HasIndexedWidgets container = getDataContainer();
    if (!container.isEmpty()) {
      container.clear();
    }

    HtmlTable table = new HtmlTable();
    int r = 0;

    for (int c = 0; c < rowSet.getNumberOfColumns(); c++) {
      table.setText(r, c, rowSet.getColumnName(c));
    }
    r++;

    for (SimpleRowSet.SimpleRow row : rowSet) {
      for (int c = 0; c < rowSet.getNumberOfColumns(); c++) {
        String text = row.getValue(c);
        if (text != null) {
          table.setText(r, c, BeeUtils.removeTrailingZeros(text));
        }
      }

      r++;
    }

    container.add(table);
  }
}
