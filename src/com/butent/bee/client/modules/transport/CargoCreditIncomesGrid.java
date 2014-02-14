package com.butent.bee.client.modules.transport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.COL_ITEM;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CargoCreditIncomesGrid extends AbstractGridInterceptor implements ClickHandler {

  private UnboundSelector mainItem;
  private InputNumber creditAmount;

  @Override
  public GridInterceptor getInstance() {
    return new CargoCreditIncomesGrid();
  }

  @Override
  public Map<String, Filter> getInitialParentFilters() {
    return ImmutableMap.of("pyp",
        Filter.and(Filter.isNull(COL_SALE_PROFORMA), Filter.isNull(COL_PURCHASE)));
  }

  @Override
  public void onClick(ClickEvent event) {
    final GridPresenter presenter = getGridPresenter();
    CompoundFilter flt = CompoundFilter.or();
    final Set<Long> ids = Sets.newHashSet();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      flt.add(Filter.compareId(row.getId()));
      ids.add(row.getId());
    }
    if (flt.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }
    Queries.getRowSet(VIEW_CARGO_CREDIT_INCOMES, null, flt, new RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        Set<String> orders = Sets.newHashSet();
        Map<Long, String> customers = Maps.newHashMap();
        Map<Long, String> currencies = Maps.newHashMap();

        boolean itemEmpty = false;
        DataInfo info = Data.getDataInfo(VIEW_CARGO_CREDIT_INCOMES);

        int item = info.getColumnIndex(COL_ITEM);
        int order = info.getColumnIndex(COL_ORDER_NO);
        int custId = info.getColumnIndex(COL_CUSTOMER);
        int custName = info.getColumnIndex(COL_CUSTOMER_NAME);
        int currId = info.getColumnIndex(ExchangeUtils.COL_CURRENCY);
        int currName = info.getColumnIndex(ExchangeUtils.COL_CURRENCY
            + ExchangeUtils.COL_CURRENCY_NAME);

        for (BeeRow row : result.getRows()) {
          if (!itemEmpty) {
            itemEmpty = row.getLong(item) == null;
          }
          orders.add(row.getString(order));

          Long id = row.getLong(custId);

          if (DataUtils.isId(id)) {
            customers.put(id, row.getString(custName));
          }
          id = row.getLong(currId);

          if (DataUtils.isId(id)) {
            currencies.put(id, row.getString(currName));
          }
        }
        final boolean mainRequired = itemEmpty;
        final DataInfo purchaseInfo = Data.getDataInfo(VIEW_CARGO_CREDIT_INVOICES);

        BeeRow newRow = RowFactory.createEmptyRow(purchaseInfo, true);

        newRow.setValue(purchaseInfo.getColumnIndex(COL_TRADE_NUMBER), BeeUtils.joinItems(orders));

        if (customers.size() == 1) {
          for (Entry<Long, String> entry : customers.entrySet()) {
            newRow.setValue(purchaseInfo.getColumnIndex(COL_TRADE_SUPPLIER), entry.getKey());
            newRow.setValue(purchaseInfo.getColumnIndex("SupplierName"), entry.getValue());
          }
        }
        if (currencies.size() == 1) {
          for (Entry<Long, String> entry : currencies.entrySet()) {
            newRow.setValue(purchaseInfo.getColumnIndex(ExchangeUtils.COL_CURRENCY),
                entry.getKey());
            newRow.setValue(purchaseInfo.getColumnIndex(ExchangeUtils.COL_CURRENCY
                + ExchangeUtils.COL_CURRENCY_NAME), entry.getValue());
          }
        }
        RowFactory.createRow(FORM_NEW_CARGO_CREDIT_INVOICE, null, purchaseInfo, newRow, null,
            new AbstractFormInterceptor() {
              @Override
              public FormInterceptor getInstance() {
                return this;
              }

              @Override
              public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent ev) {
                FormView form = getFormView();
                Widget w = form.getWidgetByName("Cause");

                if (w != null && w instanceof ListBox) {
                  String cause = ((ListBox) w).getValue();
                  int idx = -1;

                  for (int i = 0; i < ev.getColumns().size(); i++) {
                    if (BeeUtils.same(ev.getColumns().get(i).getId(), COL_TRADE_NOTES)) {
                      idx = i;
                      break;
                    }
                  }
                  if (idx >= 0) {
                    ev.getValues().set(idx, cause + "\n" + ev.getValues().get(idx));
                  } else {
                    ev.getColumns().add(Data.getColumn(form.getViewName(), COL_TRADE_NOTES));
                    ev.getValues().add(cause);
                  }
                }
              }

              @Override
              public void onStart(FormView form) {
                Widget w = form.getWidgetByName("MainItem");

                if (w != null && w instanceof UnboundSelector) {
                  mainItem = (UnboundSelector) w;

                  if (mainRequired) {
                    mainItem.setNullable(false);
                    w = form.getWidgetByName("MainItemCaption");

                    if (w != null) {
                      w.addStyleName(StyleUtils.NAME_REQUIRED);
                    }
                  }
                }
                w = form.getWidgetByName(COL_TRADE_AMOUNT);

                if (w != null && w instanceof InputNumber) {
                  creditAmount = (InputNumber) w;
                }
              }
            },
            new RowCallback() {
              @Override
              public void onCancel() {
                mainItem = null;
                creditAmount = null;
              }

              @Override
              public void onSuccess(final BeeRow row) {
                ParameterList args = TransportHandler.createArgs(SVC_CREATE_INVOICE_ITEMS);
                args.addDataItem(TradeConstants.COL_PURCHASE, row.getId());
                args.addDataItem(ExchangeUtils.COL_CURRENCY,
                    row.getLong(purchaseInfo.getColumnIndex(ExchangeUtils.COL_CURRENCY)));
                args.addDataItem("IdList", DataUtils.buildIdList(ids));

                if (mainItem != null && DataUtils.isId(mainItem.getRelatedId())) {
                  args.addDataItem(CommonsConstants.COL_ITEM, mainItem.getRelatedId());
                }
                if (creditAmount != null && BeeUtils.isPositive(creditAmount.getNumber())) {
                  args.addDataItem(COL_TRADE_AMOUNT, creditAmount.getNumber());
                }
                BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    response.notify(presenter.getGridView());

                    if (!response.hasErrors()) {
                      Data.onViewChange(presenter.getViewName(),
                          DataChangeEvent.CANCEL_RESET_REFRESH);
                      RowEditor.openRow(FORM_CARGO_CREDIT_INVOICE, purchaseInfo, row.getId());
                    }
                  }
                });
                onCancel();
              }
            });
      }
    });
  }

  @Override
  public void onShow(GridPresenter presenter) {
    presenter.getHeader().clearCommandPanel();
    presenter.getHeader()
        .addCommandItem(new Button(Localized.getConstants().createCreditInvoice(), this));
  }
}
