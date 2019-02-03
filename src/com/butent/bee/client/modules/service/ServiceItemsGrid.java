package com.butent.bee.client.modules.service;

import com.google.common.collect.ImmutableMap;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.cars.CarsConstants.COL_RESERVE;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.DataChangeCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.modules.classifiers.ItemsPicker;
import com.butent.bee.client.modules.orders.OrderItemsGrid;
import com.butent.bee.client.modules.trade.TradeItemPicker;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.modules.transport.InvoiceCreator;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDiscountMode;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeVatMode;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class ServiceItemsGrid extends OrderItemsGrid {

  private ServiceItemsPicker picker;
  private CustomAction template = new CustomAction(FontAwesome.CUBES, ev -> addItemsFromTemplate());

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (BeeUtils.same(source, COL_REPAIRER) && editor instanceof DataSelector) {
      Long company = Global.getParameterRelation(PRM_COMPANY);

      if (DataUtils.isId(company)) {
        ((DataSelector) editor).setAdditionalFilter(Filter.and(Filter.equals(COL_COMPANY, company),
            Filter.or(Filter.isNull(PayrollConstants.COL_DATE_OF_DISMISSAL),
                Filter.compareWithValue(PayrollConstants.COL_DATE_OF_DISMISSAL, Operator.GT,
                    new DateValue(new JustDate())))));
      }
    }

    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    super.afterCreatePresenter(presenter);

    template.setTitle("Šablonai");
    presenter.getHeader().addCommandItem(template);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    presenter.getGridView().ensureRelId(this::addItems);
    return false;
  }

  @Override
  public ItemsPicker ensurePicker() {
    if (picker == null) {
      picker = new ServiceItemsPicker(Module.SERVICE);
      picker.addSelectionHandler(this);
    }
    return picker;
  }

  @Override
  public Map<String, String> getAdditionalColumns() {
    FormView parentForm = ViewHelper.getForm(getGridView());

    Long repairer = BeeKeeper.getUser().getUserData().getCompanyPerson();
    if (parentForm != null && DataUtils.isId(parentForm.getLongValue(COL_REPAIRER))) {
      repairer = Data.getLong("ServiceMaintenance", parentForm.getActiveRow(),
          "RepairerCompanyPerson");
    }
    return ImmutableMap.of(COL_SERVICE_OBJECT, BeeConst.STRING_EMPTY,
        COL_REPAIRER, BeeUtils.toString(repairer));
  }

  @Override
  public GridInterceptor getInstance() {
    return new ServiceItemsGrid();
  }

  @Override
  public String getParentDateColumnName() {
    return COL_MAINTENANCE_DATE;
  }

  @Override
  public String getParentRelationColumnName() {
    return COL_SERVICE_MAINTENANCE;
  }

  @Override
  public String getParentViewName() {
    return TBL_SERVICE_MAINTENANCE;
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (Objects.equals(event.getColumnId(), COL_RESERVE)) {
      event.consume();
      IsRow row = event.getRowValue();
      String value = Data.getString(getViewName(), row, COL_RESERVE);

      Queries.updateCellAndFire(getViewName(), row.getId(), row.getVersion(), COL_RESERVE,
          value, BeeUtils.toString(!BeeUtils.toBoolean(value)));
    } else {
      super.onEditStart(event);
    }
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    setOrderForm(event.getRowId());
    getInvoice().clear();

    if (DataUtils.isId(getOrderForm())) {
      getInvoice().add(new InvoiceCreator(VIEW_SERVICE_SALES,
          Filter.equals(COL_SERVICE_MAINTENANCE, getOrderForm())));
    }
    template.setVisible(DataUtils.isId(getOrderForm()));
  }

  @Override
  public boolean validParentState(IsRow parentRow) {
    String endingDate = parentRow.getString(Data.getColumnIndex(getParentViewName(),
        COL_ENDING_DATE));
    return BeeUtils.isEmpty(endingDate);
  }

  @Override
  protected double calculateItemPrice(Pair<Double, Double> pair, BeeRow row, int unpackingIdx,
      int qtyIndex) {
    double price = super.calculateItemPrice(pair, row, unpackingIdx, qtyIndex);

    if (picker.isCheckedFilterService()) {
      IsRow parentRow = ViewHelper.getFormRow(getGridView());

      if (parentRow != null) {
        price = ServiceUtils.calculateServicePrice(price, parentRow);
      }
    }
    return price;
  }

  private void addItems(Long docId) {
    FormView parentForm = ViewHelper.getForm(getGridView());

    OperationType operationType = OperationType.SALE;
    Long serviceObject = parentForm.getLongValue(COL_SERVICE_OBJECT);
    Long repairer = BeeUtils.nvl(Data.getLong(COL_SERVICE_MAINTENANCE, parentForm.getActiveRow(),
        "RepairerCompanyPerson"), BeeKeeper.getUser().getUserData().getCompanyPerson());

    getVatPercent(Global.getParameterRelation(PRM_SERVICE_OPERATION), (vatMode, vatPercent) ->
        Global.getParameterRelation(PRM_CURRENCY, (currency, currencyName) -> {
          TradeItemPicker itemsPicker = new TradeItemPicker(TradeDocumentPhase.PENDING,
              operationType, parentForm.getLongValue(COL_WAREHOUSE),
              operationType.getDefaultPrice(), parentForm.getDateTimeValue(COL_MAINTENANCE_DATE),
              currency, currencyName, TradeDiscountMode.FROM_AMOUNT, null, vatMode,
              Collections.singletonMap(COL_COMPANY, parentForm.getStringValue(COL_COMPANY)),
              vatPercent);

          itemsPicker.open((selectedItems, tds) -> {
            List<BeeColumn> columns = DataUtils.getColumns(getDataColumns(),
                Arrays.asList(COL_SERVICE_MAINTENANCE, COL_SERVICE_OBJECT, COL_ITEM,
                    COL_TRADE_ITEM_ARTICLE,
                    COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT,
                    COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC, COL_REPAIRER));

            BeeRowSet rowSet = new BeeRowSet(getViewName(), columns);

            for (BeeRow row : selectedItems) {
              long id = row.getId();
              String article = Data.getString(VIEW_ITEM_SELECTION, row, COL_TRADE_ITEM_ARTICLE);
              double quantity = tds.getQuantity(id);

              Double price = tds.getPrice(id);
              if (BeeUtils.isZero(price)) {
                price = null;
              }
              Pair<Double, Boolean> discountInfo =
                  TradeUtils.normalizeDiscountOrVatInfo(tds.getDiscountInfo(id));
              Pair<Double, Boolean> vatInfo =
                  TradeUtils.normalizeDiscountOrVatInfo(tds.getVatInfo(id));

              rowSet.addRow(DataUtils.NEW_ROW_ID, DataUtils.NEW_ROW_VERSION,
                  Queries.asList(docId, serviceObject, id, article, quantity, price,
                      discountInfo.getA(),
                      (Objects.nonNull(vatMode) && Objects.nonNull(vatInfo.getA()))
                          ? Objects.equals(vatMode, TradeVatMode.PLUS) : null,
                      vatInfo.getA(), vatInfo.getB(), repairer));
            }
            Queries.insertRows(rowSet, new DataChangeCallback(rowSet.getViewName()));
          });
        }));
  }

  private void addItemsFromTemplate() {
    template.running();

    Queries.getRowSet(VIEW_ORDERS_TEMPLATES, null, templateRs -> {
      if (!templateRs.isEmpty()) {
        int nameIdx = templateRs.getColumnIndex(COL_TEMPLATE);
        Map<String, Long> values = new TreeMap<>();
        templateRs.forEach(beeRow -> values.put(beeRow.getString(nameIdx), beeRow.getId()));
        List<String> choices = new ArrayList<>(values.keySet());

        Global.choice("Pasirinkite šabloną", null, choices,
            idx -> Queries.getRowSet(VIEW_ORDER_TMPL_ITEMS, null,
                Filter.equals(COL_TEMPLATE, values.get(choices.get(idx))), itemsRs -> {
                  String view = getViewName();
                  List<String> cols = Arrays.asList(COL_ITEM, COL_ITEM_ARTICLE,
                      COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_SERVICE_OBJECT,
                      COL_SERVICE_MAINTENANCE);

                  FormView parentForm = ViewHelper.getForm(getGridView());
                  Long serviceObject = parentForm.getLongValue(COL_SERVICE_OBJECT);
                  Long docId = parentForm.getActiveRowId();
                  BeeRowSet newRs = new BeeRowSet(view, Data.getColumns(view, cols));

                  itemsRs.forEach(beeRow -> {
                    BeeRow newRow = newRs.addEmptyRow();
                    newRow.setValue(newRs.getColumnIndex(COL_SERVICE_OBJECT), serviceObject);
                    newRow.setValue(newRs.getColumnIndex(COL_SERVICE_MAINTENANCE), docId);

                    cols.stream()
                        .filter(s ->
                            !BeeUtils.inList(s, COL_SERVICE_OBJECT, COL_SERVICE_MAINTENANCE))
                        .forEach(col -> newRow.setValue(newRs.getColumnIndex(col),
                            Data.getString(VIEW_ORDER_TMPL_ITEMS, beeRow, col)));
                  });
                  if (!DataUtils.isEmpty(newRs)) {
                    Queries.insertRows(newRs, result -> getGridPresenter().refresh(false, true));
                  }
                }));
      } else {
        getGridView().notifyWarning(Localized.dictionary().noData());
      }
      template.idle();
    });
  }

  private static void getVatPercent(Long operation, BiConsumer<TradeVatMode, Double> vatConsumer) {
    if (DataUtils.isId(operation)) {
      Queries.getRow(VIEW_TRADE_OPERATIONS, operation, row -> {
        TradeVatMode vatMode = Data.getEnum(VIEW_TRADE_OPERATIONS, row, COL_OPERATION_VAT_MODE,
            TradeVatMode.class);
        Double vatPercent = Data.getDouble(VIEW_TRADE_OPERATIONS, row, COL_OPERATION_VAT_PERCENT);

        if (Objects.nonNull(vatMode) && vatPercent == null) {
          Number p = Global.getParameterNumber(PRM_VAT_PERCENT);

          if (p != null) {
            vatPercent = p.doubleValue();
          }
        }
        vatConsumer.accept(vatMode, vatPercent);
      });
    } else {
      vatConsumer.accept(null, null);
    }
  }
}