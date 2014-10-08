package com.butent.bee.client.modules.trade.acts;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.output.Exporter;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportParameters;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.StringList;

import java.util.Arrays;
import java.util.List;

public class TradeActItemsByCompanyReport extends ReportInterceptor {

  private static BeeLogger logger = LogUtils.getLogger(TradeActItemsByCompanyReport.class);

  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_END_DATE = "EndDate";

  private static final String NAME_CURRENCY = COL_TA_CURRENCY;

  private static final List<String> FILTER_NAMES = Arrays.asList(COL_TA_COMPANY, COL_TA_OBJECT,
      COL_TA_OPERATION, COL_TA_STATUS, COL_TA_SERIES, COL_TA_MANAGER, COL_WAREHOUSE,
      COL_CATEGORY, COL_TA_ITEM);

  private static final List<String> GROUP_NAMES =
      Arrays.asList("Group0", "Group1", "Group2", "Group3");
  private static final List<String> GROUP_VALUES = Arrays.asList(COL_ITEM_TYPE, COL_ITEM_GROUP,
      COL_TA_ITEM, COL_TA_COMPANY, COL_TA_OBJECT, COL_TA_MANAGER, COL_WAREHOUSE);

  private static final String STYLE_PREFIX = TradeActKeeper.STYLE_PREFIX + "report-ibc-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_HEADER = STYLE_PREFIX + "header";

  private final XSheet sheet = new XSheet();

  public TradeActItemsByCompanyReport() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeActItemsByCompanyReport();
  }

  @Override
  public void onLoad(FormView form) {
    ReportParameters parameters = readParameters();
    if (parameters == null) {
      return;
    }

    loadDateTime(parameters, NAME_START_DATE, form);
    loadDateTime(parameters, NAME_END_DATE, form);

    loadId(parameters, NAME_CURRENCY, form);

    loadMulti(parameters, FILTER_NAMES, form);

    loadGroupBy(parameters, GROUP_NAMES, form);

    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    storeDateTimeValues(NAME_START_DATE, NAME_END_DATE);
    storeEditorValues(NAME_CURRENCY);

    storeEditorValues(FILTER_NAMES);

    storeGroupBy(GROUP_NAMES);
  }

  @Override
  protected void clearFilter() {
    clearEditor(NAME_START_DATE);
    clearEditor(NAME_END_DATE);

    for (String name : FILTER_NAMES) {
      clearEditor(name);
    }
  }

  @Override
  protected void doReport() {
    DateTime start = getDateTime(NAME_START_DATE);
    DateTime end = getDateTime(NAME_END_DATE);

    if (!checkRange(start, end)) {
      return;
    }

    ParameterList params = TradeActKeeper.createArgs(SVC_ITEMS_BY_COMPANY_REPORT);
    final List<String> headers = StringList.of(getReportCaption());

    if (start != null) {
      params.addDataItem(Service.VAR_FROM, start.getTime());
    }
    if (end != null) {
      params.addDataItem(Service.VAR_TO, end.getTime());
    }
    if (start != null || end != null) {
      headers.add(Format.renderPeriod(start, end));
    }

    for (String name : FILTER_NAMES) {
      String ids = getEditorValue(name);

      if (!BeeUtils.isEmpty(ids)) {
        params.addDataItem(name, ids);

        boolean plural = DataUtils.parseIdSet(ids).size() > 1;
        String label = TradeActUtils.getLabel(name, plural);
        if (BeeUtils.isEmpty(label)) {
          logger.warning(name, "has no label");
        }

        headers.add(BeeUtils.joinWords(label, getFilterLabel(name)));
      }
    }

    List<String> groupBy = getGroupBy(GROUP_NAMES, GROUP_VALUES);
    if (!groupBy.isEmpty()) {
      params.addDataItem(Service.VAR_GROUP_BY, NameUtils.join(groupBy));
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasMessages()) {
          response.notify(getFormView());
        }

        if (response.hasResponse(SimpleRowSet.class)) {
          renderData(SimpleRowSet.restore(response.getResponseAsString()));

          sheet.addHeaders(headers);
          sheet.autoSizeAll();

        } else {
          getFormView().notifyWarning(Localized.getConstants().nothingFound());
        }
      }
    });
  }

  @Override
  protected void export() {
    if (!sheet.isEmpty()) {
      Exporter.maybeExport(sheet, getReportCaption());
    }
  }

  @Override
  protected String getBookmarkLabel() {
    List<String> labels = StringList.of(getReportCaption(),
        Format.renderPeriod(getDateTime(NAME_START_DATE), getDateTime(NAME_END_DATE)),
        getFilterLabel(NAME_CURRENCY));

    for (String name : FILTER_NAMES) {
      labels.add(getFilterLabel(name));
    }

    for (String groupName : GROUP_NAMES) {
      labels.add(getGroupByLabel(groupName));
    }

    return BeeUtils.joinWords(labels);
  }

  @Override
  protected Report getReport() {
    return Report.TRADE_ACT_ITEMS_BY_COMPANY;
  }

  @Override
  protected ReportParameters getReportParameters() {
    ReportParameters parameters = new ReportParameters();

    addDateTimeValues(parameters, NAME_START_DATE, NAME_END_DATE);
    addEditorValues(parameters, NAME_CURRENCY);

    addEditorValues(parameters, FILTER_NAMES);
    addGroupBy(parameters, GROUP_NAMES);

    return parameters;
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    DateTime start = parameters.getDateTime(NAME_START_DATE);
    DateTime end = parameters.getDateTime(NAME_END_DATE);

    return checkRange(start, end);
  }

  private void renderData(SimpleRowSet data) {
    HasIndexedWidgets container = getDataContainer();
    if (container == null) {
      return;
    }

    sheet.clear();

    if (!container.isEmpty()) {
      container.clear();
    }

    HtmlTable table = new HtmlTable(STYLE_TABLE);
    int r = 0;
    int c = 0;

    for (int j = 0; j < data.getNumberOfColumns(); j++) {
      String colName = data.getColumnName(j);
      table.setText(r, c++, colName, STYLE_HEADER);
    }

    r++;

    for (int i = 0; i < data.getNumberOfRows(); i++) {
      c = 0;

      for (int j = 0; j < data.getNumberOfColumns(); j++) {
        table.setText(r, c++, data.getValue(i, j));
      }

      r++;
    }

    container.add(table);
  }
}
