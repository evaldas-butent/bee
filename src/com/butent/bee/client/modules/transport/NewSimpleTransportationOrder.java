package com.butent.bee.client.modules.transport;

import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.ClientDefaults;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.HasRelatedRow;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasCheckedness;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class NewSimpleTransportationOrder extends AbstractFormInterceptor {

  private Long cargoService;

  private DateTime loadingDate;
  private DateTime unloadingDate;

  NewSimpleTransportationOrder() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (COL_ORDER_DATE.equals(name) && widget instanceof InputDateTime) {
      ((InputDateTime) widget).setDateTime(TimeUtils.nowMinutes());

    } else if (AdministrationConstants.COL_CURRENCY.equals(name)
        && widget instanceof UnboundSelector && DataUtils.isId(ClientDefaults.getCurrency())) {

      ((UnboundSelector) widget).setValue(ClientDefaults.getCurrency(), false);

    } else if (COL_TRIP.equals(name) && widget instanceof UnboundSelector) {

      ((UnboundSelector) widget).addSelectorHandler(new SelectorEvent.Handler() {
        @Override
        public void onDataSelector(SelectorEvent event) {
          if (event.isRequest() && event.getRequest() != null
              && event.getRequest().getOffset() <= 0) {

            onRequestTrip(event);
          }
        }
      });
    }
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    Double amount = getNumber(COL_AMOUNT);

    if (BeeUtils.isPositive(amount) && DataUtils.isId(cargoService) && DataUtils.hasId(result)) {
      String currency = null;
      Widget currencyWidget = getFormView().getWidgetByName(AdministrationConstants.COL_CURRENCY);
      if (currencyWidget instanceof Editor) {
        currency = ((Editor) currencyWidget).getValue();
      }

      boolean vatPlus = getBoolean(TradeConstants.COL_TRADE_VAT_PLUS);
      Double vat = getNumber(TradeConstants.COL_TRADE_VAT);
      boolean vatPercent = getBoolean(TradeConstants.COL_TRADE_VAT_PERC);

      List<BeeColumn> columns = new ArrayList<>();
      List<String> values = new ArrayList<>();

      columns.add(Data.getColumn(VIEW_CARGO_INCOMES, COL_CARGO));
      values.add(BeeUtils.toString(result.getId()));

      columns.add(Data.getColumn(VIEW_CARGO_INCOMES, COL_SERVICE));
      values.add(BeeUtils.toString(cargoService));

      columns.add(Data.getColumn(VIEW_CARGO_INCOMES, COL_AMOUNT));
      values.add(BeeUtils.toString(amount, Data.getColumnScale(VIEW_CARGO_INCOMES, COL_AMOUNT)));

      if (DataUtils.isId(currency)) {
        columns.add(Data.getColumn(VIEW_CARGO_INCOMES, AdministrationConstants.COL_CURRENCY));
        values.add(currency);
      }

      if (vatPlus) {
        columns.add(Data.getColumn(VIEW_CARGO_INCOMES, TradeConstants.COL_TRADE_VAT_PLUS));
        values.add(BooleanValue.pack(vatPlus));
      }
      if (BeeUtils.isDouble(vat)) {
        columns.add(Data.getColumn(VIEW_CARGO_INCOMES, TradeConstants.COL_TRADE_VAT));
        values.add(BeeUtils.toString(vat,
            Data.getColumnScale(VIEW_CARGO_INCOMES, TradeConstants.COL_TRADE_VAT)));
      }
      if (vatPercent) {
        columns.add(Data.getColumn(VIEW_CARGO_INCOMES, TradeConstants.COL_TRADE_VAT_PERC));
        values.add(BooleanValue.pack(vatPercent));
      }

      Queries.insertAndFire(VIEW_CARGO_INCOMES, columns, values);
    }

    IsRow tripRow = getSelectedTrip();

    if (DataUtils.hasId(tripRow) && DataUtils.hasId(result)) {
      List<BeeColumn> columns = new ArrayList<>();
      List<String> values = new ArrayList<>();

      columns.add(Data.getColumn(VIEW_CARGO_TRIPS, COL_CARGO));
      values.add(BeeUtils.toString(result.getId()));

      columns.add(Data.getColumn(VIEW_CARGO_TRIPS, COL_TRIP));
      values.add(BeeUtils.toString(tripRow.getId()));

      Queries.insertAndFire(VIEW_CARGO_TRIPS, columns, values);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new NewSimpleTransportationOrder();
  }

  @Override
  public boolean isWidgetEditable(EditableWidget editableWidget, IsRow row) {
    if (editableWidget != null && !editableWidget.hasColumn()) {
      return !DataUtils.hasId(row);
    } else {
      return super.isWidgetEditable(editableWidget, row);
    }
  }

  @Override
  public void onReadyForInsert(final HasHandlers listener, final ReadyForInsertEvent event) {
    event.consume();

    DateTime orderDate = getOrderDate();

    String customer = null;
    Widget customerWidget = getFormView().getWidgetByName(COL_CUSTOMER);
    if (customerWidget instanceof Editor) {
      customer = ((Editor) customerWidget).getValue();
    }

    if (!DataUtils.isId(customer)) {
      getFormView().notifySevere(Data.getColumnLabel(VIEW_ORDERS, COL_CUSTOMER),
          Localized.dictionary().valueRequired());
      return;
    }

    List<BeeColumn> orderColumns = new ArrayList<>();
    List<String> orderValues = new ArrayList<>();

    if (orderDate != null) {
      orderColumns.add(Data.getColumn(VIEW_ORDERS, COL_ORDER_DATE));
      orderValues.add(orderDate.serialize());
    }

    orderColumns.add(Data.getColumn(VIEW_ORDERS, COL_CUSTOMER));
    orderValues.add(customer);

    IsRow tripRow = getSelectedTrip();
    if (tripRow != null) {
      Long manager = Data.getLong(VIEW_ACTIVE_TRIPS, tripRow, COL_TRIP_MANAGER);

      if (DataUtils.isId(manager)) {
        orderColumns.add(Data.getColumn(VIEW_ORDERS, COL_ORDER_MANAGER));
        orderValues.add(BeeUtils.toString(manager));
      }
    }

    Queries.insert(VIEW_ORDERS, orderColumns, orderValues, null, new RowCallback() {
      @Override
      public void onSuccess(BeeRow orderRow) {
        event.add(Data.getColumn(getViewName(), COL_ORDER), orderRow.getId());

        if (BeeUtils.isPositive(getNumber(COL_AMOUNT))) {
          Global.getParameter(PRM_CARGO_SERVICE, new Consumer<String>() {
            @Override
            public void accept(String input) {
              cargoService = BeeUtils.toLongOrNull(input);
              listener.fireEvent(event);
            }
          });

        } else {
          listener.fireEvent(event);
        }
      }
    });
  }

  private boolean getBoolean(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof HasCheckedness) {
      return ((HasCheckedness) widget).isChecked();
    } else {
      return false;
    }
  }

  private Double getNumber(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof InputNumber) {
      return ((InputNumber) widget).getNumber();
    } else {
      return null;
    }
  }

  private DateTime getOrderDate() {
    if (getFormView() == null) {
      return null;
    }

    Widget dateWidget = getFormView().getWidgetByName(COL_ORDER_DATE);
    if (dateWidget instanceof InputDateTime) {
      return ((InputDateTime) dateWidget).getDateTime();
    } else {
      return null;
    }
  }

  private IsRow getSelectedTrip() {
    Widget widget = getFormView().getWidgetByName(COL_TRIP);

    if (widget instanceof HasRelatedRow) {
      return ((HasRelatedRow) widget).getRelatedRow();
    } else {
      return null;
    }
  }

  private static void getTripFilter(DateTime from, DateTime until,
      final Consumer<Filter> consumer) {

    if (from == null && until == null) {
      consumer.accept(null);
      return;
    }

    if (from != null && until != null && !BeeUtils.isLess(from, until)) {
      consumer.accept(Filter.isFalse());
      return;
    }

    final CompoundFilter filter = Filter.and();

    if (from != null) {
      filter.add(Filter.isLess(COL_TRIP_DATE, new DateTimeValue(TimeUtils.startOfNextDay(from))));
      filter.add(Filter.or(Filter.isNull(COL_TRIP_DATE_FROM),
          Filter.isMoreEqual(COL_TRIP_DATE_FROM, new DateValue(from.getDate()))));

      if (until == null) {
        filter.add(Filter.or(Filter.isNull(COL_TRIP_PLANNED_END_DATE),
            Filter.isMoreEqual(COL_TRIP_PLANNED_END_DATE, new DateValue(from.getDate()))));
      }
    }

    if (until != null) {
      if (from == null) {
        filter.add(Filter.isLess(COL_TRIP_DATE,
            new DateTimeValue(TimeUtils.startOfNextDay(until))));
      }

      filter.add(Filter.or(Filter.isNull(COL_TRIP_DATE_FROM),
          Filter.isLessEqual(COL_TRIP_DATE_FROM, new DateValue(until.getDate()))));
      filter.add(Filter.or(Filter.isNull(COL_TRIP_PLANNED_END_DATE),
          Filter.isMoreEqual(COL_TRIP_PLANNED_END_DATE, new DateValue(until.getDate()))));
    }

    if (from == null || until == null) {
      consumer.accept(filter);
      return;
    }

    Filter intersectionFilter = Filter.and(
        Filter.notNull(ALS_LOADING_DATE),
        Filter.isLessEqual(ALS_LOADING_DATE, new DateTimeValue(until)),
        Filter.notNull(ALS_UNLOADING_DATE),
        Filter.isMoreEqual(ALS_UNLOADING_DATE, new DateTimeValue(from)));

    Queries.getDistinctLongs(VIEW_TRIP_CARGO, COL_TRIP, intersectionFilter,
        new RpcCallback<Set<Long>>() {
          @Override
          public void onSuccess(Set<Long> result) {
            if (!BeeUtils.isEmpty(result)) {
              filter.add(Filter.idNotIn(result));
            }

            consumer.accept(filter);
          }
        });
  }

  private void onRequestTrip(final SelectorEvent event) {
    DateTime from = getDateTimeValue(ALS_LOADING_DATE);
    if (from == null) {
      from = getOrderDate();
    }

    DateTime until = getDateTimeValue(ALS_UNLOADING_DATE);

    if (!Objects.equals(loadingDate, from) || !Objects.equals(unloadingDate, until)) {
      this.loadingDate = from;
      this.unloadingDate = until;

      final DataSelector selector = event.getSelector();
      event.consume();

      getTripFilter(from, until, new Consumer<Filter>() {
        @Override
        public void accept(Filter input) {
          selector.setAdditionalFilter(input);
          event.resumeRequest(selector);
        }
      });
    }
  }
}
