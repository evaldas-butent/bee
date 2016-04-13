package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class TripCostsGrid extends AbstractGridInterceptor
    implements ClickHandler, SelectorEvent.Handler {

  Long trip;
  Long cargo;

  final Flow invoice = new Flow();
  final FaLabel finance = new FaLabel(FontAwesome.CLOUD_UPLOAD);
  final FaLabel dailyCosts = new FaLabel(FontAwesome.MONEY);

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (editor instanceof DataSelector) {
      ((DataSelector) editor).addSelectorHandler(this);
    }
    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    presenter.getHeader().addCommandItem(invoice);

    finance.setTitle("Reiso išlaidos apskaitai");
    finance.addClickHandler(clickEvent -> {
      GridPanel grid = new GridPanel(VIEW_ERP_TRIP_COSTS,
          GridFactory.GridOptions.forFilter(Filter.equals(COL_TRIP, trip)), false);

      StyleUtils.setSize(grid, 800, 600);

      DialogBox dialog = DialogBox.create(null);
      dialog.setWidget(grid);
      dialog.setAnimationEnabled(true);
      dialog.setHideOnEscape(true);
      dialog.center();
    });
    presenter.getHeader().addCommandItem(finance);

    dailyCosts.setTitle(Localized.dictionary().trGenerateDailyCosts());
    dailyCosts.addClickHandler(this);
    presenter.getHeader().addCommandItem(dailyCosts);

    super.afterCreatePresenter(presenter);
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter,
      IsRow activeRow, Collection<RowInfo> selectedRows, DeleteMode defMode) {

    if (activeRow.getDateTime(DataUtils.getColumnIndex("Exported",
        presenter.getDataColumns())) != null && !BeeKeeper.getUser().isAdministrator()) {

      presenter.getGridView().notifyWarning(Localized.dictionary().rowIsNotRemovable());
      return DeleteMode.CANCEL;
    }
    return DeleteMode.SINGLE;
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    Global.confirm(Localized.dictionary().trGenerateDailyCosts(), () -> {
      ParameterList args = TransportHandler.createArgs(SVC_GENERATE_DAILY_COSTS);
      args.addDataItem(COL_TRIP, trip);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(getGridView());

          if (response.hasErrors()) {
            return;
          }
          getGridPresenter().refresh(false, false);
        }
      });
    });
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), TBL_TRIP_DRIVERS) && event.isOpened()) {
      event.getSelector().setAdditionalFilter(Filter.equals(COL_TRIP, trip));

    } else if (BeeUtils.same(event.getRelatedViewName(), VIEW_TRIPS) && event.isOpened()) {
      event.getSelector().setAdditionalFilter(Filter.in(COL_TRIP_ID, VIEW_CARGO_TRIPS, COL_TRIP,
          Filter.equals(COL_CARGO, cargo)));
    }
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (Objects.equals(event.getColumnId(), TradeConstants.VAR_TOTAL)) {
      event.consume();
      amountEntry(event.getRowValue());
      return;
    }
    if (DataUtils.isId(trip) && Objects.equals(event.getColumnId(), COL_TRIP)) {
      event.consume();
      return;
    }
    super.onEditStart(event);
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    switch (event.getViewName()) {
      case VIEW_TRIPS:
        trip = event.getRowId();
        break;
      case VIEW_ORDER_CARGO:
        cargo = event.getRowId();
        break;
    }
    invoice.clear();

    if (DataUtils.isId(trip) && Data.isViewEditable(VIEW_TRIP_PURCHASE_INVOICES)) {
      invoice.add(new InvoiceCreator(VIEW_TRIP_PURCHASES, Filter.equals(COL_TRIP, trip)));
    }
    finance.setVisible(DataUtils.isId(trip));
    dailyCosts.setVisible(DataUtils.isId(trip));

    super.onParentRow(event);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    FormView tripForm = ViewHelper.getForm(gridView);
    JustDate date = TimeUtils.today();

    if (tripForm != null && !BeeConst.isUndef(tripForm.getDataIndex(COL_TRIP_DATE_TO))) {
      date = BeeUtils.nvl(tripForm.getDateValue(COL_TRIP_DATE_TO), date);
    }
    newRow.setValue(gridView.getDataIndex(COL_DATE), date);
    return super.onStartNewRow(gridView, oldRow, newRow);
  }

  private void amountEntry(IsRow row) {
    Totalizer totalizer = new Totalizer(getDataColumns());

    InputNumber input = new InputNumber();
    Double total = totalizer.getTotal(row);

    if (BeeUtils.isDouble(total)) {
      input.setValue(BeeUtils.round(total, 2));
    }
    Global.inputWidget(Localized.dictionary().amount(), input, () -> {
      Double amount = input.getNumber();
      String price = null;

      if (BeeUtils.isDouble(amount)) {
        if (!totalizer.isVatInclusive(row)) {
          row.clearCell(getDataIndex(TradeConstants.COL_TRADE_VAT_PLUS));
          amount -= BeeUtils.unbox(totalizer.getVat(row, amount));
          row.setValue(getDataIndex(TradeConstants.COL_TRADE_VAT_PLUS), 1);
        }
        Double qty = row.getDouble(getDataIndex(COL_COSTS_QUANTITY));
        price = BeeUtils.toString(amount / (BeeUtils.isZero(qty) ? 1 : qty), 5);
      }
      List<BeeColumn> columns = new ArrayList<>();
      List<String> oldValues = new ArrayList<>();
      List<String> newValues = new ArrayList<>();

      columns.add(DataUtils.getColumn(COL_COSTS_PRICE, getDataColumns()));
      oldValues.add(row.getString(getDataIndex(COL_COSTS_PRICE)));
      newValues.add(price);

      String oldCurrency = row.getString(getDataIndex(COL_COSTS_CURRENCY));
      String newCurrency = null;

      if (!BeeUtils.isEmpty(price)) {
        newCurrency = BeeUtils.notEmpty(oldCurrency,
            DataUtils.isId(ClientDefaults.getCurrency())
                ? BeeUtils.toString(ClientDefaults.getCurrency()) : null);
      }
      columns.add(DataUtils.getColumn(COL_COSTS_CURRENCY, getDataColumns()));
      oldValues.add(oldCurrency);
      newValues.add(newCurrency);

      Queries.update(getViewName(), row.getId(), row.getVersion(), columns, oldValues, newValues,
          null, new RowUpdateCallback(getViewName()));
    });
  }
}
