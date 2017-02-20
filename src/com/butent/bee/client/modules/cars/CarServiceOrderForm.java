package com.butent.bee.client.modules.cars;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_DOCUMENT_VAT_MODE;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.administration.Stage;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HasStages;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.widget.CustomSpan;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.FontWeight;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.cars.CarsConstants;
import com.butent.bee.shared.modules.trade.ItemStock;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CarServiceOrderForm extends PrintFormInterceptor implements HasStages,
    SelectorEvent.Handler {

  private HasWidgets stageContainer;
  private List<Stage> orderStages;

  Widget customerWarning;
  List<String> customerMessages = new ArrayList<>();
  Widget carWarning;
  List<String> carMessages = new ArrayList<>();

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (Objects.equals(editableWidget.getColumnId(), COL_CAR) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(this);
    }
    if (Objects.equals(editableWidget.getColumnId(), COL_CUSTOMER)
        && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isChanged()) {
          showCustomerWarning();
        }
      });
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreatePresenter(Presenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    Image invoice = new Image(Global.getImages().silverInvoice());
    invoice.setTitle(Localized.dictionary().createInvoice());
    invoice.addClickHandler(clickEvent -> selectServicesAndJobs());

    if (Data.isViewEditable(VIEW_CAR_SERVICE_ORDERS)) {
      header.addCommandItem(invoice);
    }

    super.afterCreatePresenter(presenter);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      switch (name) {
        case TBL_SERVICE_ORDER_JOBS:
          ((ChildGrid) widget).setGridInterceptor(new CarServiceJobsGrid());
          break;
        case TBL_SERVICE_ORDER_ITEMS:
          ((ChildGrid) widget).setGridInterceptor(new CarServiceItemsGrid());
          break;
        case TBL_SERVICE_EVENTS:
          ((ChildGrid) widget).setGridInterceptor(new CarServiceEventsGrid());
          break;
        case TBL_SERVICE_JOB_PROGRESS:
          ((ChildGrid) widget).setGridInterceptor(new CarJobProgressGrid());
          break;
      }
    }
    if (Objects.equals(name, COL_CUSTOMER + "Warning") && widget instanceof HasClickHandlers) {
      customerWarning = widget.asWidget();
      ((HasClickHandlers) customerWarning).addClickHandler(clickEvent ->
          Global.showInfo(Localized.dictionary().serviceOrders(), customerMessages));
    }
    if (Objects.equals(name, COL_CAR + "Warning") && widget instanceof HasClickHandlers) {
      carWarning = widget.asWidget();
      ((HasClickHandlers) carWarning).addClickHandler(clickEvent ->
          Global.showInfo(Localized.dictionary().recalls(), carMessages));
    }
    if (Objects.equals(name, TBL_STAGES) && widget instanceof HasWidgets) {
      stageContainer = (HasWidgets) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    refreshStages();
    showCustomerWarning();
    showCarWarning();
    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CarServiceOrderForm();
  }

  @Override
  public HasWidgets getStageContainer() {
    return stageContainer;
  }

  @Override
  public List<Stage> getStages() {
    return orderStages;
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    DataInfo eventInfo = Data.getDataInfo(getViewName());
    DataInfo carInfo = Data.getDataInfo(event.getRelatedViewName());
    Long owner = getLongValue(COL_CUSTOMER);

    if (event.isNewRow()) {
      RelationUtils.copyWithDescendants(eventInfo, COL_CUSTOMER, getActiveRow(),
          carInfo, COL_OWNER, event.getNewRow());

    } else if (event.isOpened()) {
      event.getSelector().setAdditionalFilter(Objects.isNull(owner) ? null
          : Filter.equals(COL_OWNER, owner));

    } else if (event.isChanged() && Objects.isNull(owner)) {
      RelationUtils.copyWithDescendants(carInfo, COL_OWNER, event.getRelatedRow(),
          eventInfo, COL_CUSTOMER, getActiveRow());
      getFormView().refresh();
    }
  }

  @Override
  public void onSourceChange(IsRow row, String source, String value) {
    if (!DataUtils.isNewRow(row) && !BeeUtils.isEmpty(source)) {
      switch (source) {
        case COL_TRADE_DOCUMENT_VAT_MODE:
          BeeRowSet rs = DataUtils.getUpdated(getViewName(), row.getId(), row.getVersion(),
              DataUtils.getColumn(source, getDataColumns()),
              getFormView().getOldRow().getString(getDataIndex(source)), value);

          if (!DataUtils.isEmpty(rs)) {
            Queries.updateRow(rs, RowUpdateCallback.refreshRow(getViewName(), true));
          }
          break;
      }
    }
    super.onSourceChange(row, source, value);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    Global.getParameterRelation(PRM_SERVICE_WAREHOUSE, (aLong, s) -> {
      if (DataUtils.isId(aLong)) {
        newRow.setValue(getDataIndex(COL_WAREHOUSE), aLong);
        newRow.setValue(Data.getColumnIndex(VIEW_CAR_SERVICE_ORDERS, ALS_WAREHOUSE_CODE), s);
        form.refreshBySource(COL_WAREHOUSE);
      }
    });
    super.onStartNewRow(form, oldRow, newRow);
  }

  @Override
  public void setStages(List<Stage> stages) {
    orderStages = stages;
  }

  private static void renderInvoiceTable(Collection<BeeRowSet> result, Map<Long, Double> stockMap) {
    HtmlTable table = new HtmlTable(StyleUtils.NAME_INFO_TABLE);
    Totalizer totalizer;
    Dictionary d = Localized.dictionary();
    Table<Long, Pair<Long, String>, InputNumber> inputMap = HashBasedTable.create();
    Map<Long, Double> stockMapCopy = new HashMap<>(stockMap);
    int r = 0;
    int c = 0;

    table.setText(r, c++, d.productService(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.article(), StyleUtils.className(FontWeight.BOLD));

    Flow widget = new Flow(StyleUtils.NAME_FLEX_BOX_HORIZONTAL);

    Label qtyLabel = new Label(d.quantity());
    qtyLabel.setStyleName(StyleUtils.className(FontWeight.BOLD));

    CustomSpan removeAll = new CustomSpan("bee-cars-quantity-remove");
    removeAll.addClickHandler(clickEvent -> {
      for (InputNumber input : inputMap.values()) {
        input.setValue(0.0);
      }
    });

    widget.add(qtyLabel);
    widget.add(removeAll);

    table.setWidget(r, c++, widget);
    table.setText(r, c++, d.unitShort(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.ordCompleted(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.ordUncompleted(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.price(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.discount(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.discountIsPercent(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.vat(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.vatIsPercent(), StyleUtils.className(FontWeight.BOLD));
    table.setText(r, c++, d.total(), StyleUtils.className(FontWeight.BOLD));

    r++;
    for (BeeRowSet rs : result) {
      totalizer = new Totalizer(rs.getColumns());
      String viewName = rs.getViewName();
      c = 0;

      for (BeeRow row : rs) {
        Long item = row.getLong(Data.getColumnIndex(viewName, COL_ITEM));
        double quantity = BeeUtils.unbox(row.getDouble(Data.getColumnIndex(viewName,
            COL_TRADE_ITEM_QUANTITY)));
        double completed = BeeUtils.unbox(row.getDouble(Data.getColumnIndex(viewName,
            ALS_COMPLETED)));

        InputNumber inputQty = new InputNumber();
        StyleUtils.setWidth(inputQty, 100, CssUnit.PX);
        inputQty.setMinValue("0");
        inputMap.put(item, Pair.of(row.getId(), viewName), inputQty);

        String itemName = row.getString(Data.getColumnIndex(viewName, ALS_ITEM_NAME));
        if (Objects.equals(viewName, VIEW_CAR_SERVICE_JOBS)) {
          inputQty.setValue(quantity);
          inputQty.setEnabled(false);
          itemName = BeeUtils.join(" ", itemName, BeeUtils.parenthesize(row.getString(
              Data.getColumnIndex(viewName, ALS_JOB_NAME))));
        } else {
          if (stockMapCopy.containsKey(item)) {
            double stock = BeeUtils.unbox(stockMapCopy.get(item));
            if (stock <= (quantity - completed)) {
              inputQty.setValue(stock);
              inputQty.setMaxValue(BeeUtils.toString(stock));
              stockMapCopy.put(item, 0.0);
            } else {
              inputQty.setValue(quantity - completed);
              inputQty.setMaxValue(BeeUtils.toString(quantity - completed));
              stockMapCopy.put(item, stockMapCopy.get(item) - (quantity - completed));
            }
          } else {
            inputQty.setValue(0.0);
            inputQty.setMaxValue("0");
          }
        }

        table.setText(r, c++, itemName);
        table.setText(r, c++, row.getString(Data.getColumnIndex(viewName,
            COL_ITEM_ARTICLE)));

        CustomSpan remove = new CustomSpan("bee-cars-quantity-remove");
        remove.addClickHandler(clickEvent -> {
          inputQty.setValue(0.0);
        });

        widget = new Flow(StyleUtils.NAME_FLEX_BOX_HORIZONTAL);
        widget.add(inputQty);
        widget.add(remove);

        table.setWidget(r, c++, widget, StyleUtils.className(TextAlign.RIGHT));
        table.setText(r, c++, row.getString(Data.getColumnIndex(viewName, ALS_UNIT_NAME)),
            StyleUtils.className(TextAlign.LEFT));
        table.setText(r, c++, BeeUtils.toString(completed), StyleUtils.className(TextAlign.CENTER));
        table.setText(r, c++, BeeUtils.toString(quantity - completed),
            StyleUtils.className(TextAlign.CENTER));
        table.setText(r, c++, row.getString(Data.getColumnIndex(viewName, COL_PRICE)),
            StyleUtils.className(TextAlign.RIGHT));
        table.setText(r, c++, row.getString(Data.getColumnIndex(viewName,
            COL_TRADE_DISCOUNT)), StyleUtils.className(TextAlign.RIGHT));
        table.setText(r, c++, BeeUtils.unbox(row.getBoolean(Data.getColumnIndex(viewName,
            COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT))) ? "%" : "",
            StyleUtils.className(TextAlign.CENTER));
        table.setText(r, c++, row.getString(Data.getColumnIndex(viewName,
            COL_TRADE_VAT)), StyleUtils.className(TextAlign.RIGHT));
        table.setText(r, c++, BeeUtils.unbox(row.getBoolean(Data.getColumnIndex(viewName,
            COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT))) ? "%" : "",
            StyleUtils.className(TextAlign.CENTER));

        if (BeeUtils.isPositive(totalizer.getTotal(row))) {
          table.setText(r, c++, BeeUtils.toString(BeeUtils.round(totalizer.getTotal(row), 2)));
        } else {
          table.setText(r, c++, BeeConst.STRING_EMPTY);
        }

        r++;
        c = 0;
      }
    }

    Global.inputWidget(d.createInvoice(), table, new InputCallback() {
      @Override
      public String getErrorMessage() {
        boolean hasData = false;

        for (InputNumber inputNumber : inputMap.values()) {
          if (BeeUtils.unbox(inputNumber.getNumber()) > 0) {
            hasData = true;
          }

          if (BeeUtils.unbox(inputNumber.getNumber()) < 0) {
            inputNumber.setFocus(true);
            return d.minValue() + " 0";
          }
        }

        if (!hasData) {
          return d.noData();
        }

        for (Long key : stockMap.keySet()) {
          double stock = BeeUtils.unbox(stockMap.get(key));

          for (Map.Entry<Pair<Long, String>, InputNumber> entry : inputMap.row(key).entrySet()) {
            if (Objects.equals(entry.getKey().getB(), VIEW_CAR_SERVICE_ITEMS)) {
              double inputValue = BeeUtils.unbox(entry.getValue().getNumber());
              double uncompleted = BeeUtils.toDouble(entry.getValue().getMaxValue());
              InputNumber inputNumber = entry.getValue();

              if (inputValue > uncompleted) {
                inputNumber.setMaxValue(BeeUtils.toString(uncompleted));
              } else if (stock < inputValue) {
                inputNumber.setMaxValue(BeeUtils.toString(stock));
              } else {
                stock -= inputValue;
              }
            }
          }
        }

        for (InputNumber inputNumber : inputMap.values()) {
          List<String> messages = inputNumber.validate(true);
          if (!BeeUtils.isEmpty(messages)) {
            inputNumber.setFocus(true);
            return BeeUtils.join(", ", messages);
          }
        }

        return InputCallback.super.getErrorMessage();
      }

      @Override
      public void onSuccess() {
      }
    });
  }

  private void selectServicesAndJobs() {
    Filter filter = Filter.equals(COL_SERVICE_ORDER, getActiveRowId());
    Map<String, Filter> filters = new HashMap<>();
    filters.put(VIEW_CAR_SERVICE_ITEMS, filter);
    filters.put(VIEW_CAR_SERVICE_JOBS, Filter.and(filter, Filter.notNull(ALS_ITEM_NAME)));

    Queries.getData(Arrays.asList(VIEW_CAR_SERVICE_ITEMS, VIEW_CAR_SERVICE_JOBS), filters,
        CachingPolicy.NONE, new Queries.DataCallback() {
          @Override
          public void onSuccess(Collection<BeeRowSet> result) {

            result.forEach(rowSet -> {
              if (Objects.equals(rowSet.getViewName(), VIEW_CAR_SERVICE_ITEMS)) {
                Long warehouse = getActiveRow().getLong(getDataIndex(COL_WAREHOUSE));

                TradeKeeper.getStock(warehouse, rowSet.getDistinctLongs(Data.getColumnIndex(
                    VIEW_CAR_SERVICE_ITEMS, COL_ITEM)), stockMultimap -> {
                  Map<Long, Double> stockMap = new HashMap<>();
                  for (Map.Entry<Long, ItemStock> entry : stockMultimap.entries()) {
                    if (stockMap.containsKey(entry.getKey())) {
                      double qty = stockMap.get(entry.getKey());
                      stockMap.put(entry.getKey(), entry.getValue().getQuantity() + qty);
                    } else {
                      stockMap.put(entry.getKey(), entry.getValue().getQuantity());
                    }
                  }

                  renderInvoiceTable(result, stockMap);
                });
              }
            });
          }
    });
  }

  private void showCarWarning() {
    if (Objects.isNull(carWarning)) {
      return;
    }
    carMessages.clear();
    Long car = getLongValue(COL_CAR);

    if (DataUtils.isId(car)) {
      Queries.getRowSet(VIEW_CAR_RECALLS, null, Filter.and(Filter.equals(COL_VEHICLE, car),
          Filter.isNull(COL_CHECKED)), new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          result.forEach(beeRow -> carMessages.add(BeeUtils.joinWords(
              beeRow.getString(result.getColumnIndex(COL_CODE)),
              beeRow.getString(result.getColumnIndex(CarsConstants.COL_DESCRIPTION)))));

          carWarning.setVisible(!carMessages.isEmpty());
        }
      });
    } else {
      carWarning.setVisible(false);
    }
  }

  private void showCustomerWarning() {
    if (Objects.isNull(customerWarning)) {
      return;
    }
    customerMessages.clear();
    Long customer = getLongValue(COL_CUSTOMER);

    if (DataUtils.isId(customer)) {
      Filter filter = Filter.equals(COL_CUSTOMER, customer);

      if (DataUtils.isId(getActiveRowId())) {
        filter = Filter.and(filter, Filter.compareId(Operator.NE, getActiveRowId()));
      }
      Queries.getRowSet(getViewName(), null, filter, new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          result.forEach(beeRow -> customerMessages.add(BeeUtils.joinWords(
              beeRow.getDateTime(result.getColumnIndex(COL_ORDER_DATE)),
              beeRow.getString(result.getColumnIndex(COL_ORDER_NO)),
              beeRow.getString(result.getColumnIndex(COL_STAGE_NAME)))));

          customerWarning.setVisible(!customerMessages.isEmpty());
        }
      });
    } else {
      customerWarning.setVisible(false);
    }
  }
}
