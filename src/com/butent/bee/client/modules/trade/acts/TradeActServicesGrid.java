package com.butent.bee.client.modules.trade.acts;


import com.butent.bee.client.data.*;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.data.*;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.modules.trade.acts.TradeActTimeUnit;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.TBL_APPOINTMENT_OWNERS;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.COL_SERVICE_OBJECT;
import static com.butent.bee.shared.modules.service.ServiceConstants.VIEW_SERVICE_DATES;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.*;
import java.util.function.Consumer;

public class TradeActServicesGrid extends AbstractGridInterceptor implements
    SelectionHandler<BeeRowSet> {

  private static final String STYLE_COMMAND_APPLY_TARIFF = TradeActKeeper.STYLE_PREFIX
      + "command-apply-tariff";

  private static final String STYLE_COMMAND_APPLY_RENT = TradeActKeeper.STYLE_PREFIX
          + "command-apply-rent";

  private static GridView getItemsGridView(GridView servicesGridView) {
    IsRow formRow = ViewHelper.getFormRow(servicesGridView.asWidget());

    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, formRow);
    return TradeActKind.RENT_PROJECT.equals(kind)
            ? ViewHelper.getSiblingGrid(servicesGridView.asWidget(), "RPTradeActItems")
            : ViewHelper.getSiblingGrid(servicesGridView.asWidget(), GRID_TRADE_ACT_ITEMS);
  }

  private static double getItemTotal(GridView gridView) {
    double total = BeeConst.DOUBLE_ZERO;

    if (gridView == null) {
      return total;
    }

    GridView items = getItemsGridView(gridView);

    if (items != null && !items.isEmpty()) {
      Totalizer totalizer = new Totalizer(items.getDataColumns());

      int qtyIndex = items.getDataIndex(COL_TRADE_ITEM_QUANTITY);
      totalizer.setQuantityFunction(new QuantityReader(qtyIndex));

      Collection<RowInfo> selectedItems = items.getSelectedRows(GridView.SelectedRows.ALL);

      if (!selectedItems.isEmpty()) {
        for (RowInfo selectedItem : selectedItems) {
          IsRow row = items.getGrid().getRowById(selectedItem.getId());
          Double amount = totalizer.getTotal(row);
          if (BeeUtils.isDouble(amount)) {
            total += amount;
          }
        }
      } else {
        for (IsRow row : items.getRowData()) {
          Double amount = totalizer.getTotal(row);
          if (BeeUtils.isDouble(amount)) {
            total += amount;
          }
        }
      }
    }

    return total;
  }

  private TradeActServicePicker picker;
  private Button commandApplyTariff;
  private Button commandApplyRental;
  private final CheckBox showAllServices = new CheckBox("Visos paslaugos");
  private final CheckBox hideFinishedServices = new CheckBox("Slėpti baigtas paslaugas");
  private final CheckBox hideCompletedServices = new CheckBox("Slėpti įvykdytas paslaugas");

  TradeActServicesGrid() {
  }

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (Objects.equals(source, COL_TRADE_SUPPLIER)) {
      ((DataSelector) editor).addSelectorHandler(event -> {
        if (event.isOpened()) {
          event.getSelector().setAdditionalFilter(Filter.idIn(VIEW_ITEM_SUPPLIERS,
              COL_TRADE_SUPPLIER, Filter.equals(COL_ITEM, getLongValue(COL_ITEM))));
        }
      });
    } else if (Objects.equals(source, CalendarConstants.COL_APPOINTMENT)) {
      ((DataSelector) editor).addSelectorHandler(event -> {
        if (event.isNewRow()) {
          event.consume();
          createAppointment();
        }
      });

      ((DataSelector) editor).addKeyDownHandler(keyDownEvent -> {
        if (keyDownEvent.getNativeKeyCode() == KeyCodes.KEY_X) {
          createAppointment();
        }
      });
    }
    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    GridView gridView = presenter.getGridView();

    if (gridView != null) {
      presenter.getHeader().addCommandItem(showAllServices);
      presenter.getHeader().addCommandItem(hideCompletedServices);
      presenter.getHeader().addCommandItem(hideFinishedServices);
      final Set<Long> ids = new HashSet<>();
      final Set<Long> ids1 = new HashSet<>();

      ClickHandler svcFilter = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {

          if (gridView == null || gridView.getRowData() == null) {
            return;
          }

          if (Objects.equals(clickEvent.getSource(), showAllServices) && showAllServices.isChecked()
                  || !hideCompletedServices.isChecked() && !hideFinishedServices.isChecked()) {
            hideCompletedServices.setChecked(false);
            hideFinishedServices.setChecked(false);
            ids.clear();
            ids1.clear();
            getGridPresenter().tryFilter(null, null, false);
            return;
          }


          for (IsRow row : gridView.getRowData()) {
            TradeActTimeUnit tu = Data.getEnum(gridView.getViewName(), row, COL_TIME_UNIT, TradeActTimeUnit.class);
            Double propServiceRange = row.getPropertyDouble(PRP_SERVICE_RANGE);
            Double serviceInvoiceRange = Data.getDouble(gridView.getViewName(), row, ALS_SERVICE_INVOICE_RANGE);
            JustDate dateFrom = Data.getDate(gridView.getViewName(), row, COL_TA_SERVICE_FROM);
            JustDate dateTo = getDateTo(row);

            if (hideCompletedServices.isChecked()) {
              showAllServices.setChecked(false);
              if (tu == null && dateFrom != null && serviceInvoiceRange == null) {
                ids.add(row.getId());
              } else if ( tu!= null && dateTo != null && serviceInvoiceRange == null
              || tu!= null && dateTo != null && serviceInvoiceRange != null && !Objects.equals(serviceInvoiceRange, propServiceRange)) {
                ids.add(row.getId());
              }
            } else {
              ids.clear();
            }

            if (hideFinishedServices.isChecked()) {
              showAllServices.setChecked(false);
              if (tu == null && dateFrom != null && serviceInvoiceRange != null) {
                ids1.add(row.getId());
              } else if (tu!= null && dateTo != null && serviceInvoiceRange != null && Objects.equals(serviceInvoiceRange, propServiceRange)) {
                ids1.add(row.getId());
              }
            } else {
              ids1.clear();
            }
          }

          Set<Long> idsAll = new HashSet<>();
          idsAll.addAll(ids);
          idsAll.addAll(ids1);

         if (!idsAll.isEmpty()) {
           getGridPresenter().tryFilter(Filter.isNot(Filter.idIn(idsAll)), null, true);
         }
        }
      };


      showAllServices.addClickHandler(svcFilter);
      hideCompletedServices.addClickHandler(svcFilter);
      hideFinishedServices.addClickHandler(svcFilter);
    }

    if (gridView != null && !gridView.isReadOnly()) {
      commandApplyTariff = new Button(Localized.dictionary().taTariff());
      commandApplyTariff.addStyleName(STYLE_COMMAND_APPLY_TARIFF);
      commandApplyTariff.addClickHandler(event -> applyTariff());

      commandApplyRental = new Button(Localized.dictionary().taApplyRentalPrice());
      commandApplyRental.addStyleName(STYLE_COMMAND_APPLY_RENT);
      commandApplyRental.addClickHandler(event -> applyRentalPrice());

      presenter.getHeader().addCommandItem(commandApplyTariff);
      presenter.getHeader().addCommandItem(commandApplyRental);
    }
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    if (copy) {
      return true;
    }
    final IsRow parentRow = ViewHelper.getFormRow(presenter.getMainView());

    if (parentRow != null) {
      if (DataUtils.isId(Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CONTINUOUS))) {
        getGridView().notifySevere(Localized.dictionary().actionCanNotBeExecuted());
        return false;
      }

      FormView parentForm = ViewHelper.getForm(presenter.getMainView());
      FormInterceptor parentInterceptor = parentForm.getFormInterceptor();
      TradeActForm parentTaForm = null;

      if (parentInterceptor instanceof TradeActForm) {
        parentTaForm = (TradeActForm) parentInterceptor;
      }

      if (!parentForm.validate(parentForm, true) || (parentTaForm != null
          && !parentTaForm.validateBeforeSave(parentForm, parentRow, false))) {
        return false;
      }

      ensurePicker().show(parentRow, presenter.getMainView().getElement());
    }

    return false;
  }

  @Override
  public void beforeRender(GridView gridView, RenderingEvent event) {
    super.beforeRender(gridView, event);

    IsRow parentRow = ViewHelper.getFormRow(gridView);

    if (commandApplyTariff != null) {
      commandApplyTariff.setVisible(parentRow != null
          && !DataUtils.isId(Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CONTINUOUS)));
    }

    if (commandApplyRental != null) {
      commandApplyRental.setVisible(parentRow != null
          && !DataUtils.isId(Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CONTINUOUS)));
    }
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    IsRow row = event.getRowValue();
    String value = Data.getString(getViewName(), row, COL_TA_RUN);
    Long serviceObject = Data.getLong(getViewName(), row, COL_SERVICE_OBJECT);
    String oldRun = Data.getString(getViewName(), row, COL_TA_RUN);

    if (Objects.equals(event.getColumnId(), COL_TA_RUN)) {
      event.consume();

      if (BeeUtils.isPositive(serviceObject)) {
        updateServiceObjectRun(serviceObject, value, oldRun);
      } else {
        getGridView().notifySevere(Localized.dictionary().fieldRequired(Localized.dictionary().svcDevice()));
      }

    } else if (Objects.equals(event.getColumnId(), COL_SERVICE_OBJECT) && !BeeUtils.isEmpty(oldRun)) {
      if(event.isDelete()) {
        event.consume();
      }
    } else {
      super.onEditStart(event);
    }
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow, boolean copy) {
    FormView parentForm = null;
    IsRow parentRow = null;

    if (gridView != null) {
      parentForm = ViewHelper.getForm(gridView.asWidget());
    }

    if (parentForm != null) {
      parentRow = parentForm.getActiveRow();
    }

    if (parentRow != null
        && BeeUtils.same(parentForm.getFormName(), FORM_TRADE_ACT)) {

      int idxDate = gridView.getDataIndex(COL_TA_SERVICE_FROM);
      int idxParentDate = parentForm.getDataIndex(COL_TA_DATE);

      if (!BeeConst.isUndef(idxDate) && !BeeConst.isUndef(idxParentDate)) {
        newRow.setValue(idxDate, parentRow.getDateTime(idxParentDate).getDate());
      }
    }
    return true;
  }

  @Override
  public void onSelection(SelectionEvent<BeeRowSet> event) {
    addItems(event.getSelectedItem());
  }

  @Override
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {
    GridView gridView = getGridView();

    if (BeeUtils.inList(column.getId(), COL_TA_SERVICE_TARIFF, COL_TRADE_ITEM_QUANTITY,
        COL_TA_SERVICE_TO, COL_TA_ITEM_VALUE, COL_TRADE_ITEM_PRICE)) {

      final LinkedHashMap<String, String> values = new LinkedHashMap<>();

      IsRow row = gridView.getGrid().getRowById(result.getId());
      boolean hasTotal = BeeUtils.isPositive(getTAItemValue(row)) || COL_TA_ITEM_VALUE.equals(column.getId());
      Double calculatedItemTotal = getItemTotal(gridView);
      Double total = hasTotal && getTimeUnit(row) == null ? getTAItemValue(row) : calculatedItemTotal;

      Double tariff = BeeUtils.same(column.getId(), COL_TA_SERVICE_TARIFF)
              ? BeeUtils.toDoubleOrNull(newValue)
              : getTariff(row);

      Double quantity = BeeUtils.same(column.getId(), COL_TRADE_ITEM_QUANTITY)
              ? BeeUtils.toDoubleOrNull(newValue)
              : getQuantity(row);

      JustDate dateTo = BeeUtils.same(column.getId(), COL_TA_SERVICE_TO)
              ? TimeUtils.toDateOrNull(newValue)
              : getDateTo(row);

      Double price = COL_ITEM_PRICE.equals(column.getId())
              ? BeeUtils.toDoubleOrNull(newValue)
              : getPrice(row);

      TradeActTimeUnit timeUnit  = getTimeUnit(row);

      boolean isRentalPrice = isRentalPrice(row);


      switch (column.getId()) {
        case COL_TA_SERVICE_TARIFF:
        case COL_TA_SERVICE_TO:
          if (calculatePrice(row, dateTo, calculatedItemTotal, tariff, quantity)) {
            values.clear();
            values.put(COL_IS_ITEM_APPLY_TARIFF, BeeConst.STRING_TRUE);
            values.put(COL_IS_ITEM_RENTAL_PRICE, null);
            values.put(COL_TA_ITEM_VALUE, BeeUtils.toStringOrNull(calculatedItemTotal));
            updateRow(row, values);
          }
          break;
        case COL_TRADE_ITEM_PRICE:
          if (BeeUtils.isPositive(calculatedItemTotal) && BeeUtils.isPositive(price)) {
            values.clear();

            if (timeUnit != null) {
              Double t = getTariff(price,  getQuantity(row), calculatedItemTotal);
              values.put(COL_TA_SERVICE_TARIFF, BeeUtils.toStringOrNull(t));
            }
            values.put(COL_IS_ITEM_RENTAL_PRICE, null);
            values.put(COL_IS_ITEM_APPLY_TARIFF, null);
            values.put(COL_TA_ITEM_VALUE, BeeUtils.toStringOrNull(calculatedItemTotal));

            updateRow(row, values);
          }
          break;
        case COL_TA_ITEM_VALUE:
          if (BeeUtils.isPositive(total) && !isRentalPrice && BeeUtils.isPositive(price)) {
            values.clear();
            values.put(COL_TA_ITEM_VALUE, BeeUtils.toStringOrNull(total));

            if (timeUnit != null) {
              Double t = getTariff(price, getQuantity(row), total);
              values.put(COL_TA_SERVICE_TARIFF, BeeUtils.toStringOrNull(t));
            }

            updateRow(row, values);
          }
          break;
        case COL_TRADE_ITEM_QUANTITY:
          if (!calculatePrice(row, dateTo, calculatedItemTotal, tariff, quantity)
                  && isRentalPrice && BeeUtils.isPositive(calculatedItemTotal)) {

            price = BeeUtils.unbox(getPrice(row)) * BeeUtils.toDouble(oldValue)
                    / BeeUtils.toDouble(newValue);

            if (BeeUtils.isPositive(price)) {
              updateRowCell(row, COL_TRADE_ITEM_PRICE, BeeUtils.toStringOrNull(price));
              applyItemValue(gridView, row, true);
            }
          }
          break;
      }
    } else if (BeeUtils.same(CalendarConstants.COL_APPOINTMENT, column.getId())) {

      Long tradeActService = null;
      String appointment = null;

      if (BeeUtils.isEmpty(oldValue) && newValue != null) {
        tradeActService = getActiveRowId();
        appointment = newValue;
      } else if (BeeUtils.isEmpty(newValue) && oldValue != null) {
        tradeActService = null;
        appointment = oldValue;
      }

      Queries.update(CalendarConstants.VIEW_APPOINTMENTS, BeeUtils.toLong(appointment), "TradeActService",
        new LongValue(tradeActService));
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActServicesGrid();
  }

  void applyItemValue(GridView gridView, IsRow row, boolean updateTariff) {
    Double total = getItemTotal(gridView);
    Map<String, String> values = new LinkedHashMap<>();
    boolean isRentalPrice =  isRentalPrice(row);

    if (BeeUtils.isPositive(total) && !isRentalPrice) {
      values.put(COL_TA_ITEM_VALUE, BeeUtils.toStringOrNull(total));

      if (updateTariff && getTimeUnit(row) != null) {
        Double t = getTariff(getPrice(row),  getQuantity(row), total);
        values.put(COL_TA_SERVICE_TARIFF, BeeUtils.toStringOrNull(t));
      }

      updateRow(row, (LinkedHashMap<String, String>) values);
    }
  }

  void applyTariff() {
    GridView gridView = getGridView();
    if (gridView == null || gridView.isEmpty()) {
      return;
    }

    if (gridView.getActiveRow() == null) {
      gridView.notifyWarning(Data.getViewCaption(VIEW_TRADE_ACT_SERVICES), Localized.dictionary().selectAtLeastOneRow());
      return;
    }

    IsRow row = getActiveRow();

    if (!gridView.isRowEditable(row, gridView)) {
      gridView.notifyWarning(Localized.dictionary().rowIsReadOnly());
      return;
    }

    double total = getItemTotal(gridView);

    if (!BeeUtils.isPositive(total)) {
      gridView.notifyWarning(Data.getViewCaption(VIEW_TRADE_ACT_ITEMS), Localized.dictionary().selectAtLeastOneRow());
      return;
    }

    Double tariff = getTariff(row);
    LinkedHashMap<String, String> values = new LinkedHashMap<>();

    if (BeeUtils.isPositive(tariff)) {
      Double price =
          calculatePrice(getPrice(row), getDateTo(row), total, tariff, getQuantity(row));
      values.put(COL_TRADE_ITEM_PRICE, BeeUtils.toStringOrNull(price));
      values.put(COL_IS_ITEM_RENTAL_PRICE, null);
      values.put(COL_IS_ITEM_APPLY_TARIFF, BeeConst.STRING_TRUE);
      values.put(COL_TA_ITEM_VALUE, BeeUtils.toStringOrNull(total));

      updateRow(row, values);
      gridView.notifyInfo(Localized.dictionary().taRecalculatedPrices(tariff));
    } else if (getTimeUnit(row) != null) {
      Double newTariff = getTariff(getPrice(row), getQuantity(row), total);
      values.put(COL_TA_SERVICE_TARIFF, BeeUtils.toStringOrNull(newTariff));
      values.put(COL_TA_ITEM_VALUE, BeeUtils.toStringOrNull(total));
      values.put(COL_IS_ITEM_RENTAL_PRICE, null);
      updateRow(row, values);
      gridView.notifyInfo("Perskaičiuotas naujas tarifas pagal vertę ", BeeUtils.toStringOrNull(total));
    } else if (!BeeUtils.isPositive(tariff) || getTimeUnit(row) == null) {
      gridView.notifyWarning(Localized.dictionary().valueRequired(), Localized.dictionary().taTimeUnit(),
              Localized.dictionary().filterOr(), Localized.dictionary().taTariff());
    }
  }

  private static Double getTariff(Double price, Double quantity, Double total) {
    if (!BeeUtils.isDouble(price) || !BeeUtils.isDouble(quantity) || !BeeUtils.isDouble(total)) {
      return null;
    }
    return price * 100 * quantity / total;
  }

  private void addItems(final BeeRowSet rowSet) {
    if (!DataUtils.isEmpty(rowSet) && VIEW_ITEMS.equals(rowSet.getViewName())) {
      getGridView().ensureRelId(result -> {
        IsRow parentRow = ViewHelper.getFormRow(getGridView());

        if (DataUtils.idEquals(parentRow, result)) {
          ItemPrice itemPrice = TradeActKeeper.getItemPrice(VIEW_TRADE_ACTS, parentRow);

          String ip = rowSet.getTableProperty(PRP_ITEM_PRICE);
          if (BeeUtils.isDigit(ip)) {
            itemPrice = EnumUtils.getEnumByIndex(ItemPrice.class, ip);
          }

          DateTime date = Data.getDateTime(VIEW_TRADE_ACTS, parentRow, COL_TA_DATE);
          Long currency = Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CURRENCY);

          addItems(parentRow, date, currency, itemPrice, rowSet);
        }
      });
    }
  }

  private void addItems(IsRow parentRow, DateTime date, Long currency, ItemPrice defPrice,
      BeeRowSet items) {

    List<String> colNames =
        Lists.newArrayList(COL_TRADE_ACT, COL_TA_ITEM,
            COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_TRADE_VAT,
            COL_TRADE_VAT_PERC, COL_TA_SERVICE_TO, COL_TA_SERVICE_FROM, COL_TA_SERVICE_FROM,
            COL_TA_SERVICE_TARIFF, COL_TRADE_DISCOUNT, COL_TA_SERVICE_DAYS, COL_TA_SERVICE_MIN, COL_TA_ITEM_VALUE);
    BeeRowSet rowSet = new BeeRowSet(getViewName(), Data.getColumns(getViewName(), colNames));

    int actIndex = rowSet.getColumnIndex(COL_TRADE_ACT);
    int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    int vatIndex = rowSet.getColumnIndex(COL_TRADE_VAT);
    int vatPercIndex = rowSet.getColumnIndex(COL_TRADE_VAT_PERC);
    int dateFrom = rowSet.getColumnIndex(COL_TA_SERVICE_FROM);
    int dateTo = rowSet.getColumnIndex(COL_TA_SERVICE_TO);
    int tariff = rowSet.getColumnIndex(COL_TA_SERVICE_TARIFF);
    int discount = rowSet.getColumnIndex(COL_TRADE_DISCOUNT);
    int serviceDays = rowSet.getColumnIndex(COL_TA_SERVICE_DAYS);
    int serviceMinTerm = rowSet.getColumnIndex(COL_TA_SERVICE_MIN);
    int itemValue = rowSet.getColumnIndex(COL_TA_ITEM_VALUE);

    for (BeeRow item : items) {
      Double qty = BeeUtils.toDoubleOrNull(item.getProperty(PRP_QUANTITY));

      if (BeeUtils.isDouble(qty)) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());

        row.setValue(actIndex, parentRow.getId());
        row.setValue(itemIndex, item.getId());

        row.setValue(qtyIndex, qty);

        row.setValue(vatIndex, item.getValue(Data.getColumnIndex(VIEW_ITEMS,
            ClassifierConstants.COL_ITEM_VAT_PERCENT)));
        row.setValue(vatPercIndex, item.getBoolean(Data.getColumnIndex(VIEW_ITEMS, COL_ITEM_VAT)));
        row.setValue(dateFrom, picker.getDatesFrom().get(item.getId()));
        row.setValue(dateTo, picker.getDatesTo().get(item.getId()));
        row.setValue(tariff, picker.getTariffs().get(item.getId()));

        Double total = getItemTotal(getGridView());

        row.setValue(discount, picker.getDiscounts().get(item.getId()));
        row.setValue(serviceDays, item.getString(Data.getColumnIndex(VIEW_ITEMS,
            COL_TA_SERVICE_DAYS)));
        row.setValue(serviceMinTerm, item.getString(Data.getColumnIndex(VIEW_ITEMS,
            COL_TA_SERVICE_MIN)));

        ItemPrice itemPrice = defPrice;

        String ip = item.getProperty(PRP_ITEM_PRICE);
        if (BeeUtils.isDigit(ip)) {
          itemPrice = EnumUtils.getEnumByIndex(ItemPrice.class, ip);
        }

        if (itemPrice != null) {
          Double price = item.getDouble(items.getColumnIndex(itemPrice.getPriceColumn()));

          if (BeeUtils.isDouble(price)) {
            if (DataUtils.isId(currency)) {
              Long ic = item.getLong(items.getColumnIndex(itemPrice.getCurrencyColumn()));
              if (DataUtils.isId(ic) && !currency.equals(ic)) {
                price = Money.exchange(ic, currency, price, date);
              }
            }

            row.setValue(priceIndex, Data.round(getViewName(), COL_TRADE_ITEM_PRICE, price));
          }
        }

        if (BeeUtils.isPositive(total)) {
          row.setValue(itemValue, total);

          if (!BeeUtils.isPositive(row.getDouble(tariff))
                  && item.getInteger(items.getColumnIndex(COL_TIME_UNIT)) != null) {
            Double t = getTariff(row.getDouble(priceIndex), row.getDouble(qtyIndex), total);
            row.setValue(tariff, t);
          }
        }

        rowSet.addRow(row);
      }
    }

    if (!rowSet.isEmpty()) {
      Queries.insertRows(rowSet);
    }
  }

  private void applyRentalPrice() {
    GridView gridView = getGridView();
    if (gridView == null || gridView.isEmpty()) {
      return;
    }

    if (gridView.getActiveRow() == null) {
      gridView.notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }

    if (!gridView.isRowEditable(gridView.getActiveRow(), gridView)){
      return;
    }

    GridView itemsGridView = getItemsGridView(gridView);
    Collection<RowInfo> selItems = itemsGridView.getSelectedRows(GridView.SelectedRows.ALL);
    double rentalAmount = 0D;

    int qtyIndex = itemsGridView.getDataIndex(COL_TRADE_ITEM_QUANTITY);
    QuantityReader quantityReader = new QuantityReader(qtyIndex);
    RowToDouble amountReader = row -> {
      double qty = BeeUtils.unbox(quantityReader.apply(row));
      double rp = BeeUtils.unbox(row.getDouble(itemsGridView.getDataIndex(COL_ITEM_RENTAL_PRICE)));
      Double irp = row.getDouble(itemsGridView.getDataIndex("ItemRentalPrice"));

      return BeeUtils.isDouble(irp) ? qty * irp : qty * rp;
    };

    if (BeeUtils.isEmpty(selItems)) {
      for (IsRow row : itemsGridView.getRowData()) {
        Double amount = amountReader.apply(row);
        if (BeeUtils.isDouble(amount)) {
          rentalAmount += amount;
        }
      }
    } else {
      for (RowInfo selectedItem : selItems) {
        IsRow row = itemsGridView.getGrid().getRowById(selectedItem.getId());
        Double amount = amountReader.apply(row);
        if (BeeUtils.isDouble(amount)) {
          rentalAmount += amount;
        }
      }
    }

    String viewName = gridView.getViewName();
    IsRow row = gridView.getActiveRow();
    Double quantity = getQuantity(row);

    if (!BeeUtils.isPositive(quantity)) {
      gridView.notifyWarning(Localized.dictionary().valueRequired(), Data.getColumnLabel(viewName, COL_TRADE_ITEM_QUANTITY));
      return;
    }

    Double price = rentalAmount / quantity;
    Double total = getItemTotal(gridView);
    Double tariff = getTariff(price, quantity, total);
    LinkedHashMap<String, String> values = new LinkedHashMap<>();

    values.put(COL_TRADE_ITEM_PRICE, BeeUtils.toStringOrNull(price));
    values.put(COL_TA_SERVICE_TARIFF, BeeUtils.toStringOrNull(tariff));
    values.put(COL_IS_ITEM_RENTAL_PRICE, BeeConst.STRING_TRUE);
    values.put(COL_IS_ITEM_APPLY_TARIFF, null);

    if (getTimeUnit(row) != null) {
      values.put(COL_TA_ITEM_VALUE, BeeUtils.toStringOrNull(total));
    }

    updateRow(row, values);
  }

  private Double calculatePrice(Double defPrice, JustDate dateTo, Double itemTotal, Double
      tariff, Double quantity) {

    Integer scale = Data.getColumnScale(getViewName(), COL_TRADE_ITEM_PRICE);
    return TradeActUtils.calculateServicePrice(defPrice, dateTo, itemTotal, tariff, quantity,
        scale);
  }

  private boolean calculatePrice(IsRow row, JustDate dateTo, Double total, Double tariff, Double quantity) {
    if (BeeUtils.isPositive(total) && BeeUtils.isPositive(tariff)) {
      Double price = calculatePrice(getPrice(row), dateTo, total,
              tariff, quantity);

      if (BeeUtils.isPositive(price)) {
        updateRowCell(row, COL_TRADE_ITEM_PRICE, BeeUtils.toStringOrNull(price));
      }
      return true;
    } else {
      return false;
    }
  }

  private TradeActItemPicker ensurePicker() {
    if (picker == null) {
      picker = new TradeActServicePicker();
      picker.addSelectionHandler(this);
    }

    return picker;
  }

  private JustDate getDateTo(IsRow row) {
    return Data.getDate(getViewName(), row, COL_TA_SERVICE_TO);
  }

  private Double getPrice(IsRow row) {
    return Data.getDouble(getViewName(), row, COL_TRADE_ITEM_PRICE);
  }

  private Double getQuantity(IsRow row) {
    return Data.getDouble(getViewName(), row, COL_TRADE_ITEM_QUANTITY);
  }

  private Double getTAItemValue(IsRow row) {
    return Data.getDouble(getViewName(), row, COL_TA_ITEM_VALUE);
  }

  private Double getTariff(IsRow row) {
    return Data.getDouble(getViewName(), row, COL_TA_SERVICE_TARIFF);
  }

  private TradeActTimeUnit getTimeUnit(IsRow row) {
    return Data.getEnum(getViewName(), row, COL_TRADE_TIME_UNIT, TradeActTimeUnit.class);
  }

  private boolean isApllyTariff(IsRow row) {
    return BeeUtils.unbox(Data.getBoolean(getViewName(), row, COL_IS_ITEM_APPLY_TARIFF));
  }

  private boolean isRentalPrice(IsRow row) {
    return BeeUtils.unbox(Data.getBoolean(getViewName(), row, COL_IS_ITEM_RENTAL_PRICE));
  }

  private void setApplyTariff(IsRow row, Boolean applyTariff) {
    Data.setValue(getViewName(), row, COL_IS_ITEM_APPLY_TARIFF, applyTariff);
  }

  private void setDateTo(IsRow row, JustDate dateTo) {
    Data.setValue(getViewName(), row, COL_TA_SERVICE_TO, dateTo);
  }

  private void setPrice(IsRow row, Double price) {
    Data.setValue(getViewName(), row, COL_TRADE_ITEM_PRICE, price);
  }

  private void setQuantity(IsRow row, Double quantity) {
    Data.setValue(getViewName(), row, COL_TRADE_ITEM_QUANTITY, quantity);
  }

  private void setRentalPrice(IsRow row, Boolean rentalPrice) {
    Data.setValue(getViewName(), row, COL_IS_ITEM_RENTAL_PRICE, rentalPrice);
  }

  private void setTariff(IsRow row, Double tariff) {
    Data.setValue(getViewName(), row, COL_TA_SERVICE_TARIFF, tariff);
  }

  private void setTimeUnit(IsRow row, TradeActTimeUnit timeUnit) {
    Data.setValue(getViewName(), row, COL_TRADE_TIME_UNIT, timeUnit == null ? null: timeUnit.ordinal());
  }

  private boolean updateRowCell(IsRow oldRow, String column, String newValue) {
    String oldValue = Data.getString(getViewName(), oldRow, column);
    String normalizedValue = newValue;

    if (ValueType.isNumeric(Data.getColumn(getViewName(), column).getType())) {
      Integer scale = Data.getColumnScale(getViewName(), column);
      normalizedValue = BeeUtils.toString(BeeUtils.toDoubleOrNull(newValue), scale);
    }

    if (Objects.equals(oldValue, normalizedValue)) {
      return false;
    }

    Queries.updateCellAndFire(getViewName(), oldRow.getId(), oldRow.getVersion(), column, oldValue, normalizedValue);

    return true;
  }

  private void updateRow(IsRow row, LinkedHashMap<String, String> newValues) {
    List<String> oldValues = new ArrayList<>();

    newValues.keySet().forEach(col -> {
      oldValues.add(Data.getString(getViewName(), row, col));
    });

    Queries.update(getViewName(), row.getId(), row.getVersion(),
            Data.getColumns(getViewName(), Lists.newArrayList(newValues.keySet())),
            oldValues,
            Lists.newArrayList(newValues.values()), null, result-> {
                RowUpdateEvent.fire(BeeKeeper.getBus(), getViewName(), result);
            });
  }

  private void updateServiceObjectRun(Long serviceObject, String newValue, String oldRun) {
    Queries.getRowSet(VIEW_SERVICE_DATES, null,
      Filter.equals("TradeActService", getActiveRowId()),null, (BeeRowSet rows) -> {

      DataInfo info = Data.getDataInfo(VIEW_SERVICE_DATES);
      BeeRow dateRow;

      if (rows.isEmpty()) {
        dateRow = RowFactory.createEmptyRow(info, true);
        Data.setValue(VIEW_SERVICE_DATES, dateRow, COL_SERVICE_OBJECT, serviceObject);
        Data.setValue(VIEW_SERVICE_DATES, dateRow, "TradeActService", getActiveRowId());
        Data.setValue(VIEW_SERVICE_DATES, dateRow, COL_TA_RUN, newValue);

        RowFactory.createRow(info, dateRow, Opener.MODAL, (BeeRow result) -> {
          Queries.updateCellAndFire(getViewName(),
            getActiveRowId(), getActiveRow().getVersion(), COL_TA_RUN, oldRun, Data.getString(VIEW_SERVICE_DATES,
              result, COL_TA_RUN));
        });

      } else {
        dateRow = rows.getRow(0);
        Data.setValue(VIEW_SERVICE_DATES, dateRow, COL_TA_RUN, newValue);

        RowEditor.open(VIEW_SERVICE_DATES, dateRow, Opener.MODAL, result ->
          Queries.updateCellAndFire(getViewName(), getActiveRowId(), getActiveRow().getVersion(), COL_TA_RUN, oldRun,
            Data.getString(VIEW_SERVICE_DATES, result, COL_TA_RUN)));
      }
    });
  }

  private void createAppointment() {
    Consumer<BeeRow> initializer = eventRow -> {
      IsRow serviceRow = getActiveRow();

      Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, "TradeActService", getActiveRowId());

      Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, COL_TRADE_SUPPLIER,
        Data.getLong(VIEW_TRADE_ACT_SERVICES, serviceRow, COL_TRADE_SUPPLIER));

      Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, ALS_SUPPLIER_NAME,
        Data.getString(VIEW_TRADE_ACT_SERVICES, serviceRow, ALS_SUPPLIER_NAME));

      Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, COL_COST_AMOUNT,
        Data.getDouble(VIEW_TRADE_ACT_SERVICES, serviceRow, COL_COST_AMOUNT));

      String seriesName = Data.getString(VIEW_TRADE_ACT_SERVICES, serviceRow, "TradeSeriesName");
      String itemName = Data.getString(VIEW_TRADE_ACT_SERVICES, serviceRow, "ItemName");
      Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, CalendarConstants.COL_SUMMARY,
        BeeUtils.joinWords(seriesName, itemName));

      FormView tradeActForm = ViewHelper.getForm(getGridView());
      if (tradeActForm != null) {
        Long company = tradeActForm.getLongValue(COL_COMPANY);
        String companyName = tradeActForm.getStringValue(ALS_COMPANY_NAME);
        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, COL_COMPANY, company);
        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, ALS_COMPANY_NAME, companyName);

        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, COL_COMPANY_PERSON,
          tradeActForm.getLongValue(COL_CONTACT));
        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, TaskConstants.ALS_PERSON_FIRST_NAME,
          tradeActForm.getStringValue(ALS_CONTACT_FIRST_NAME));
        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, TaskConstants.ALS_PERSON_LAST_NAME,
          tradeActForm.getStringValue(ALS_CONTACT_LAST_NAME));

        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, COL_TRADE_ACT, tradeActForm.getActiveRowId());
        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, "TradeNumber",
          tradeActForm.getStringValue(COL_TA_NUMBER));
        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, "TradeCompanyName", companyName);
        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, COL_SERIES_NAME, seriesName);

        String tradeActName = tradeActForm.getStringValue(COL_TRADE_ACT_NAME);
        String objectName = tradeActForm.getStringValue(COL_COMPANY_OBJECT_NAME);
        String objectAddress = tradeActForm.getStringValue(COL_COMPANY_OBJECT_ADDRESS);

        Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, eventRow, COL_ITEM_DESCRIPTION,
          BeeUtils.joinWords(objectName, objectAddress) + "\n" + BeeUtils.joinWords("Kontaktas:",
            tradeActForm.getStringValue(ALS_CONTACT_FIRST_NAME), tradeActForm.getStringValue(ALS_CONTACT_LAST_NAME),
            tradeActForm.getStringValue("ContactMobile")) + "\n" + tradeActName);

        Long manager = tradeActForm.getLongValue(COL_TRADE_MANAGER);
        if (manager != null) {
          eventRow.setProperty(TBL_APPOINTMENT_OWNERS, manager);
        }
      }
    };

    CalendarKeeper.createAppointment(initializer, null, appointment -> {
      Queries.update(VIEW_TRADE_ACT_SERVICES, getActiveRowId(), CalendarConstants.COL_APPOINTMENT,
        new LongValue(appointment.getId()), result -> Data.refreshLocal(getViewName()));
    });
  }
}
