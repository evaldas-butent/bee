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
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class TripCostsGrid extends AbstractGridInterceptor
    implements ClickHandler, SelectorEvent.Handler {

  Long trip;

  final Flow invoice = new Flow();
  final FaLabel finance = new FaLabel(FontAwesome.CLOUD_UPLOAD);
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

    finance.setTitle("Reiso išlaidos apskaitai");
    finance.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        GridPanel grid = new GridPanel(VIEW_ERP_TRIP_COSTS,
            GridFactory.GridOptions.forFilter(Filter.equals(COL_TRIP, trip)), false);

        StyleUtils.setSize(grid, 800, 600);

        DialogBox dialog = DialogBox.create(null);
        dialog.setWidget(grid);
        dialog.setAnimationEnabled(true);
        dialog.setHideOnEscape(true);
        dialog.center();
      }
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

      presenter.getGridView().notifyWarning(Localized.getConstants().rowIsNotRemovable());
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
    Global.confirm(Localized.dictionary().trGenerateDailyCosts(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
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
      }
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
    finance.setVisible(DataUtils.isId(trip));
    dailyCosts.setVisible(DataUtils.isId(trip));

    super.onParentRow(event);
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (BeeUtils.same(event.getColumnId(), "Ratio") && BeeUtils.isPositive(event.getRowValue()
        .getDouble(getDataIndex("Old" + COL_COSTS_PRICE)))) {

      final IsRow row = event.getRowValue();
      final Double qty = row.getDouble(getDataIndex(COL_COSTS_QUANTITY));

      if (BeeUtils.isPositive(qty)) {
        final InputNumber amount = new InputNumber();

        Global.inputWidget(Localized.getConstants().amount(), amount, new InputCallback() {
          @Override
          public String getErrorMessage() {
            if (!BeeUtils.isPositive(amount.getNumber())) {
              return Localized.getConstants().valueRequired();
            }
            return InputCallback.super.getErrorMessage();
          }

          @Override
          public void onSuccess() {
            Queries.updateCellAndFire(getViewName(), row.getId(), row.getVersion(), COL_COSTS_PRICE,
                row.getString(getDataIndex(COL_COSTS_PRICE)),
                BeeUtils.toString(BeeUtils.round(amount.getNumber() / qty, 2)));
          }
        });
      }
    }
    super.onEditStart(event);
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
}
