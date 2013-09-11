package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class CargoInvoiceForm extends AbstractFormInterceptor implements ClickHandler {

  private final Button confirmAction = new Button(Localized.getConstants().trInvoice(), this);

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid && BeeUtils.same(name, TBL_SALE_ITEMS)) {
      ((ChildGrid) widget).setGridInterceptor(new AbstractGridInterceptor() {
        @Override
        public void afterDeleteRow(long rowId) {
          refreshTotals();
        }

        @Override
        public void afterInsertRow(IsRow result) {
          refreshTotals();
        }

        @Override
        public void afterUpdateCell(IsColumn column, IsRow result, boolean rowMode) {
          if (BeeUtils.inListSame(column.getId(), COL_SALE_ITEM_QUANTITY, COL_SALE_ITEM_PRICE,
              COL_SALE_ITEM_VAT, COL_SALE_ITEM_VAT_PERC)) {
            refreshTotals();
          }
        }

        private void refreshTotals() {
          final FormView form = getFormView();

          Queries.getRow(form.getViewName(), form.getActiveRow().getId(), new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              form.updateRow(result, false);
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(form.getViewName(), result));
            }
          });
        }
      });
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      RowEditor.openRow("PrintCargoInvoice", Data.getDataInfo(getFormView().getViewName()),
          getFormView().getActiveRow(), true, null, null, null, new PrintCargoInvoiceForm());
      return false;
    }
    return super.beforeAction(action, presenter);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    boolean proforma = BeeUtils.unbox(row.getBoolean(DataUtils.getColumnIndex(COL_SALE_PROFORMA,
        form.getDataColumns())));

    form.getViewPresenter().getHeader().setCaption(proforma
        ? Localized.getConstants().trProformaInvoice()
        : Localized.getConstants().trInvoice());

    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (proforma) {
      header.addCommandItem(confirmAction);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoInvoiceForm();
  }

  @Override
  public void onClick(ClickEvent event) {
    final FormView form = getFormView();

    Global.confirm(Localized.getConstants().trConfirmProforma(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        Queries.update(form.getViewName(),
            IdFilter.compareId(form.getActiveRow().getId()),
            COL_SALE_PROFORMA, BooleanValue.getNullValue(), new IntCallback() {
              @Override
              public void onSuccess(Integer result) {
                if (BeeUtils.isPositive(result)) {
                  Presenter presenter = form.getViewPresenter();
                  presenter.handleAction(Action.CLOSE);

                  if (presenter instanceof HasGridView) {
                    GridView gridView = ((HasGridView) presenter).getGridView();

                    gridView.getGrid().reset();
                    gridView.getViewPresenter().handleAction(Action.REFRESH);
                  }
                }
              }
            });
      }
    });
  }
}
