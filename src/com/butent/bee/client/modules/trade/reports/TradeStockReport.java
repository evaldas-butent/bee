package com.butent.bee.client.modules.trade.reports;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportParameters;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
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

  private static final String NAME_DATE = COL_TRADE_DATE;

  private static final String NAME_QUANTITY = COL_STOCK_QUANTITY;
  private static final String NAME_AMOUNT = COL_TRADE_AMOUNT;

  private static final String NAME_PRICE = COL_TRADE_ITEM_PRICE;
  private static final String NAME_CURRENCY = COL_TRADE_CURRENCY;

  private static final List<String> SELECTOR_NAMES = Arrays.asList(
      COL_WAREHOUSE, COL_TRADE_SUPPLIER, COL_ITEM_MANUFACTURER, COL_TRADE_DOCUMENT,
      COL_ITEM_TYPE, COL_ITEM_GROUP, COL_CATEGORY, COL_ITEM);

  private static final String NAME_RECEIVED_FROM = "ReceivedFrom";
  private static final String NAME_RECEIVED_TO = "ReceivedTo";

  private static final String NAME_ITEM_FILTER = "ItemFilter";

  private static final List<String> GROUP_NAMES =
      Arrays.asList("Group0", "Group1", "Group2", "Group3");

  private static final String NAME_SUMMARY = "Summary";
  private static final String NAME_COLUMNS = "Columns";

  private static final String STYLE_PREFIX = TradeKeeper.STYLE_PREFIX + "report-stock-";

  public TradeStockReport() {
  }

  @Override
  public void onLoad(FormView form) {
    ReportParameters parameters = readParameters();

    if (parameters != null) {
      loadDateTime(parameters, NAME_DATE, form);

      loadBoolean(parameters, NAME_QUANTITY, form);
      loadBoolean(parameters, NAME_AMOUNT, form);

      loadList(parameters, NAME_PRICE, form);
      loadId(parameters, NAME_CURRENCY, form);

      loadMulti(parameters, SELECTOR_NAMES, form);

      loadDateTime(parameters, NAME_RECEIVED_FROM, form);
      loadDateTime(parameters, NAME_RECEIVED_TO, form);

      loadText(parameters, NAME_ITEM_FILTER, form);

      loadGroupBy(parameters, GROUP_NAMES, form);
      loadBoolean(parameters, NAME_SUMMARY, form);

      loadList(parameters, NAME_COLUMNS, form);
    }

    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    storeDateTimeValues(NAME_DATE, NAME_RECEIVED_FROM, NAME_RECEIVED_TO);
    storeBooleanValues(NAME_QUANTITY, NAME_AMOUNT, NAME_SUMMARY);

    storeSelectedIndex(NAME_PRICE, 0);
    storeSelectedIndex(NAME_COLUMNS, 1);

    storeEditorValues(NAME_CURRENCY, NAME_ITEM_FILTER);
    storeEditorValues(SELECTOR_NAMES);

    storeGroupBy(GROUP_NAMES);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeStockReport();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (COL_TRADE_DOCUMENT.equals(name) && widget instanceof DataSelector) {
      ((DataSelector) widget).setAdditionalFilter(getDocumentSelectorFilter());
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  protected void clearFilter() {
    clearEditors(NAME_DATE, NAME_RECEIVED_FROM, NAME_RECEIVED_TO, NAME_ITEM_FILTER);
    clearEditors(SELECTOR_NAMES);
  }

  @Override
  protected void doReport() {
  }

  @Override
  protected String getBookmarkLabel() {
    List<String> labels = StringList.uniqueCaseSensitive();

    labels.addAll(getCaptions(getDateTime(NAME_DATE),
        getBoolean(NAME_QUANTITY), getBoolean(NAME_AMOUNT),
        getItemPrice(), getSelectorLabel(NAME_CURRENCY)));

    SELECTOR_NAMES.forEach(name -> labels.add(getSelectorLabel(name)));

    DateTime rFrom = getDateTime(NAME_RECEIVED_FROM);
    DateTime rTo = getDateTime(NAME_RECEIVED_TO);

    if (rFrom != null || rTo != null) {
      labels.add(BeeUtils.joinWords(Localized.dictionary().received(),
          Format.renderPeriod(rFrom, rTo)));
    }

    labels.add(getEditorValue(NAME_ITEM_FILTER));

    GROUP_NAMES.stream()
        .filter(name -> BeeUtils.isPositive(getSelectedIndex(name)))
        .forEach(name -> labels.add(getSelectedItemText(name)));

    if (getBoolean(NAME_SUMMARY)) {
      labels.add(Localized.dictionary().summary());
    }

    if (BeeUtils.isPositive(getSelectedIndex(NAME_COLUMNS))) {
      labels.add(BeeUtils.joinWords(Localized.dictionary().columns(),
          getSelectedItemText(NAME_COLUMNS)));
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

    addDateTimeValues(parameters, NAME_DATE, NAME_RECEIVED_FROM, NAME_RECEIVED_TO);
    addBooleanValues(parameters, NAME_QUANTITY, NAME_AMOUNT, NAME_SUMMARY);

    addSelectedIndex(parameters, NAME_PRICE, 0);
    addSelectedIndex(parameters, NAME_COLUMNS, 1);

    addEditorValues(parameters, NAME_CURRENCY, NAME_ITEM_FILTER);
    addEditorValues(parameters, SELECTOR_NAMES);

    addGroupBy(parameters, GROUP_NAMES);

    return parameters;
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    return checkRange(getDateTime(NAME_RECEIVED_FROM), getDateTime(NAME_RECEIVED_TO));
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
    return EnumUtils.getEnumByIndex(ItemPrice.class, getSelectedIndex(NAME_PRICE));
  }
}
