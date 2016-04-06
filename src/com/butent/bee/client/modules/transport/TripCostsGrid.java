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

  final Flow invoice = new Flow();
  final FaLabel dailyCosts = new FaLabel(FontAwesome.MONEY);

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (BeeUtils.same(source, COL_DRIVER) && editor instanceof DataSelector) {
      ((DataSelector) editor).addSelectorHandler(this);
    }
    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    presenter.getHeader().addCommandItem(invoice);

    dailyCosts.setTitle(Localized.dictionary().trGenerateDailyCosts());
    dailyCosts.addClickHandler(this);
    presenter.getHeader().addCommandItem(dailyCosts);

    super.afterCreatePresenter(presenter);
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
    }
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    trip = event.getRowId();

    invoice.clear();

    if (DataUtils.isId(trip) && Data.isViewEditable(VIEW_TRIP_PURCHASE_INVOICES)) {
      invoice.add(new InvoiceCreator(VIEW_TRIP_PURCHASES, Filter.equals(COL_TRIP, trip)));
    }
    dailyCosts.setVisible(DataUtils.isId(trip));

    super.onParentRow(event);
  }

  @Override
  public void onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {
    int idx = DataUtils.getColumnIndex(COL_COSTS_PRICE, event.getColumns());

    if (!BeeConst.isUndef(idx)
        && BeeUtils.isZero(BeeUtils.toDoubleOrNull(event.getValues().get(idx)))) {

      event.consume();

      amountEntry(BeeUtils.toDoubleOrNull(event.getValues()
              .get(DataUtils.getColumnIndex(COL_COSTS_QUANTITY, event.getColumns()))),
          (newPrice) -> {
            event.getValues().set(idx, newPrice);
            event.setConsumed(false);
            gridView.fireEvent(event);
          });
      return;
    }
    super.onReadyForInsert(gridView, event);
  }

  @Override
  public void onReadyForUpdate(GridView gridView, ReadyForUpdateEvent event) {
    if (Objects.equals(event.getColumn().getId(), COL_COSTS_PRICE)
        && BeeUtils.isZero(BeeUtils.toDoubleOrNull(event.getNewValue()))) {

      event.consume();

      amountEntry(DataUtils.getDouble(gridView.getDataColumns(), event.getRowValue(),
          COL_COSTS_QUANTITY),
          (price) -> {
            event.setNewValue(price);
            event.setConsumed(false);
            gridView.fireEvent(event);
          });
      return;
    }
    super.onReadyForUpdate(gridView, event);
  }

  private String amountEntry(Double qty, Consumer<String> amountConsumer) {
    InputNumber input = new InputNumber();

    Global.inputWidget(Localized.dictionary().amount(), input, new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (!BeeUtils.isPositive(input.getNumber())) {
          return Localized.dictionary().valueRequired();
        }
        return InputCallback.super.getErrorMessage();
      }

      @Override
      public void onSuccess() {
        amountConsumer
            .accept(BeeUtils.toString(input.getNumber() / (BeeUtils.isPositive(qty) ? qty : 1), 5));
      }
    });
    return null;
  }
}
