package com.butent.bee.client.modules.trade.reports;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.report.ReportParameters;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.StringList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class TradeStockReport extends ReportInterceptor {

  private static BeeLogger logger = LogUtils.getLogger(TradeStockReport.class);

  private static final List<String> SELECTOR_NAMES = Arrays.asList(
      RP_WAREHOUSES, RP_SUPPLIERS, RP_MANUFACTURERS, RP_DOCUMENTS,
      RP_ITEM_TYPES, RP_ITEM_GROUPS, RP_ITEM_CATEGORIES, RP_ITEMS);

  private static final List<String> GROUP_NAMES = reportGroupNames(4);

  private static final String STYLE_PREFIX = TradeKeeper.STYLE_PREFIX + "report-stock-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private static final String STYLE_HEADER = STYLE_PREFIX + "header";
  private static final String STYLE_BODY = STYLE_PREFIX + "body";
  private static final String STYLE_FOOTER = STYLE_PREFIX + "footer";

  private static final String STYLE_QUANTITY = STYLE_PREFIX + "qty";
  private static final String STYLE_AMOUNT = STYLE_PREFIX + "amount";

  public TradeStockReport() {
  }

  @Override
  public void onLoad(FormView form) {
    ReportParameters parameters = readParameters();

    if (parameters != null) {
      loadDateTime(parameters, RP_DATE, form);

      loadBoolean(parameters, RP_SHOW_QUANTITY, form);
      loadBoolean(parameters, RP_SHOW_AMOUNT, form);

      loadListByIndex(parameters, RP_ITEM_PRICE, form);
      loadId(parameters, RP_CURRENCY, form);

      loadMulti(parameters, SELECTOR_NAMES, form);

      loadDateTime(parameters, RP_RECEIVED_FROM, form);
      loadDateTime(parameters, RP_RECEIVED_TO, form);

      loadText(parameters, RP_ITEM_FILTER, form);

      loadGroupByValue(parameters, GROUP_NAMES, form);
      loadBoolean(parameters, RP_SUMMARY, form);

      loadListByValue(parameters, RP_COLUMNS, form);
    }

    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    storeDateTimeValues(RP_DATE, RP_RECEIVED_FROM, RP_RECEIVED_TO);
    storeBooleanValues(RP_SHOW_QUANTITY, RP_SHOW_AMOUNT, RP_SUMMARY);

    storeSelectedIndex(RP_ITEM_PRICE, 0);
    storeSelectedValue(RP_COLUMNS, 1);

    storeEditorValues(RP_CURRENCY, RP_ITEM_FILTER);
    storeEditorValues(SELECTOR_NAMES);

    storeGroupByValue(GROUP_NAMES);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeStockReport();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (RP_DOCUMENTS.equals(name) && widget instanceof DataSelector) {
      ((DataSelector) widget).setAdditionalFilter(getDocumentSelectorFilter());
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  protected void clearFilter() {
    clearEditors(RP_DATE, RP_RECEIVED_FROM, RP_RECEIVED_TO, RP_ITEM_FILTER);
    clearEditors(SELECTOR_NAMES);
  }

  @Override
  protected void doReport() {
    final ReportParameters reportParameters = getReportParameters();

    if (validateParameters(reportParameters)) {
      ParameterList parameters = TradeKeeper.createArgs(SVC_TRADE_STOCK_REPORT);
      parameters.addDataItem(Service.VAR_REPORT_PARAMETERS, reportParameters.serialize());

      BeeKeeper.getRpc().makeRequest(parameters, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasMessages()) {
            response.notify(getFormView());
          }

          if (response.hasResponse(SimpleRowSet.class)) {
            renderData(SimpleRowSet.restore(response.getResponseAsString()),
                reportParameters.getDateTime(RP_DATE));

          } else {
            getFormView().notifyWarning(Localized.dictionary().nothingFound());
          }
        }
      });
    }
  }

  @Override
  protected String getBookmarkLabel() {
    List<String> labels = StringList.uniqueCaseSensitive();

    labels.addAll(getCaptions(getDateTime(RP_DATE),
        getBoolean(RP_SHOW_QUANTITY), getBoolean(RP_SHOW_AMOUNT),
        getItemPrice(), getSelectorLabel(RP_CURRENCY)));

    SELECTOR_NAMES.forEach(name -> labels.add(getSelectorLabel(name)));

    DateTime rFrom = getDateTime(RP_RECEIVED_FROM);
    DateTime rTo = getDateTime(RP_RECEIVED_TO);

    if (rFrom != null || rTo != null) {
      labels.add(BeeUtils.joinWords(Localized.dictionary().received(),
          Format.renderPeriod(rFrom, rTo)));
    }

    labels.add(getEditorValue(RP_ITEM_FILTER));

    GROUP_NAMES.stream()
        .filter(name -> BeeUtils.isPositive(getSelectedIndex(name)))
        .forEach(name -> labels.add(getSelectedItemText(name)));

    if (getBoolean(RP_SUMMARY)) {
      labels.add(Localized.dictionary().summary());
    }

    if (BeeUtils.isPositive(getSelectedIndex(RP_COLUMNS))) {
      labels.add(BeeUtils.joinWords(Localized.dictionary().columns(),
          getSelectedItemText(RP_COLUMNS)));
    }

    return BeeUtils.joinWords(labels);
  }

  @Override
  protected Report getReport() {
    return Report.TRADE_STOCK;
  }

  @Override
  protected ReportParameters getReportParameters() {
    ReportParameters parameters = new ReportParameters();

    addDateTimeValues(parameters, RP_DATE, RP_RECEIVED_FROM, RP_RECEIVED_TO);
    addBooleanValues(parameters, RP_SHOW_QUANTITY, RP_SHOW_AMOUNT, RP_SUMMARY);

    addSelectedIndex(parameters, RP_ITEM_PRICE, 0);
    addSelectedValue(parameters, RP_COLUMNS, 1);

    addEditorValues(parameters, RP_CURRENCY, RP_ITEM_FILTER);
    addEditorValues(parameters, SELECTOR_NAMES);

    addGroupByValue(parameters, GROUP_NAMES);

    return parameters;
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    return checkRange(getDateTime(RP_RECEIVED_FROM), getDateTime(RP_RECEIVED_TO))
        && checkFilter(ClassifierConstants.VIEW_ITEMS, getEditorValue(RP_ITEM_FILTER));
  }

  private static List<String> getCaptions(DateTime date, boolean qty, boolean amount,
      ItemPrice itemPrice, String currencyName) {

    List<String> captions = new ArrayList<>();

    captions.add(Localized.dictionary().trdReportStock());
    if (date != null) {
      captions.add(Format.renderDateLong(date));
    }

    if (qty && !amount) {
      captions.add(Localized.dictionary().quantity());

    } else if (itemPrice != null || !BeeUtils.isEmpty(currencyName)) {
      String priceName = (itemPrice == null) ? null : itemPrice.getCaption();
      captions.add(BeeUtils.joinItems(priceName, currencyName));
    }

    return captions;
  }

  private static Filter getDocumentSelectorFilter() {
    EnumSet<TradeDocumentPhase> phases = EnumSet.noneOf(TradeDocumentPhase.class);
    phases.addAll(Arrays.stream(TradeDocumentPhase.values())
        .filter(TradeDocumentPhase::modifyStock)
        .collect(Collectors.toSet()));

    EnumSet<OperationType> operationTypes = EnumSet.noneOf(OperationType.class);
    operationTypes.addAll(Arrays.stream(OperationType.values())
        .filter(OperationType::producesStock)
        .collect(Collectors.toSet()));

    return Filter.and(Filter.any(COL_TRADE_DOCUMENT_PHASE, phases),
        Filter.any(COL_OPERATION_TYPE, operationTypes));
  }

  private ItemPrice getItemPrice() {
    return EnumUtils.getEnumByIndex(ItemPrice.class, getSelectedIndex(RP_ITEM_PRICE));
  }

  private void renderData(SimpleRowSet data, DateTime date) {
    HasIndexedWidgets container = getDataContainer();
    if (container == null) {
      return;
    }

    if (!container.isEmpty()) {
      container.clear();
    }

    HtmlTable table = new HtmlTable(STYLE_TABLE);
    int r = 0;

    for (int j = 0; j < data.getNumberOfColumns(); j++) {
      String colName = data.getColumnName(j);
      table.setText(r, j, colName);
    }

    table.getRowFormatter().addStyleName(r, STYLE_HEADER);
    r++;

    for (int i = 0; i < data.getNumberOfRows(); i++) {
      for (int j = 0; j < data.getNumberOfColumns(); j++) {
        table.setText(r, j, data.getValue(i, j));
      }

      table.getRowFormatter().addStyleName(r, STYLE_BODY);
      r++;
    }

    container.add(table);
  }
}
