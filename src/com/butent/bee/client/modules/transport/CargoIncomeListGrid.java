package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

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
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.widget.Button;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CargoIncomeListGrid extends AbstractGridInterceptor {

  private UnboundSelector mainItem = null;

  @Override
  public GridInterceptor getInstance() {
    return new CargoIncomeListGrid();
  }

  @Override
  public void onShow(final GridPresenter presenter) {
    presenter.getHeader().clearCommandPanel();
    presenter.getHeader().addCommandItem(new Button("Formuoti sąskaitą", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        CompoundFilter flt = CompoundFilter.or();
        final List<Long> idList = Lists.newArrayList();

        for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
          flt.add(ComparisonFilter.compareId(row.getId()));
          idList.add(row.getId());
        }
        if (flt.isEmpty()) {
          presenter.getGridView().notifyWarning("Pažymėkite bent vieną pajamų eilutę");
          return;
        }
        Queries.getRowSet(VIEW_CARGO_INCOME_LIST, null, flt, new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            Set<String> orders = Sets.newHashSet();

            Map<Long, String> currencies = Maps.newHashMap();
            int customerId = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, COL_CUSTOMER);
            int customerName = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, COL_CUSTOMER_NAME);
            int payerId = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, "Payer");
            int payerName = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, "PayerName");
            int companyId = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, "Company");
            int companyName = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, "CompanyName");
            Long clientId = null;
            String clientName = null;
            boolean clientUnique = true;
            boolean itemEmpty = false;

            int item = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, CommonsConstants.COL_ITEM);
            int order = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, COL_ORDER_NO);
            int currId = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, ExchangeUtils.FLD_CURRENCY);
            int currName = Data.getColumnIndex(VIEW_CARGO_INCOME_LIST, ExchangeUtils.FLD_CURRENCY
                + ExchangeUtils.FLD_CURRENCY_NAME);

            for (BeeRow row : result.getRows()) {
              if (!itemEmpty) {
                itemEmpty = (row.getLong(item) == null);
              }
              if (clientUnique) {
                Long id = BeeUtils.nvl(row.getLong(companyId), row.getLong(payerId),
                    row.getLong(customerId));
                String name = BeeUtils.nvl(row.getString(companyName), row.getString(payerName),
                    row.getString(customerName));

                if (clientId == null || Objects.equal(clientId, id)) {
                  clientId = id;
                  clientName = name;
                } else {
                  clientId = null;
                  clientName = null;
                  clientUnique = false;
                }
              }
              orders.add(row.getString(order));
              currencies.put(row.getLong(currId), row.getString(currName));
            }
            final boolean mainRequired = itemEmpty;
            final String turnovers = TradeConstants.TBL_SALES;
            DataInfo dataInfo = Data.getDataInfo(turnovers);

            BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);

            newRow.setValue(Data.getColumnIndex(turnovers, COL_NUMBER), BeeUtils.join(",", orders));
            newRow.setValue(Data.getColumnIndex(turnovers, COL_CUSTOMER), clientId);
            newRow.setValue(Data.getColumnIndex(turnovers, COL_CUSTOMER_NAME), clientName);

            if (currencies.size() == 1) {
              for (Entry<Long, String> entry : currencies.entrySet()) {
                newRow.setValue(Data.getColumnIndex(turnovers, ExchangeUtils.FLD_CURRENCY),
                    entry.getKey());
                newRow.setValue(Data.getColumnIndex(turnovers, ExchangeUtils.FLD_CURRENCY
                    + ExchangeUtils.FLD_CURRENCY_NAME), entry.getValue());
              }
            }
            RowFactory.createRow("NewCargoInvoice", null, dataInfo, newRow, null,
                new AbstractFormInterceptor() {
                  @Override
                  public FormInterceptor getInstance() {
                    return this;
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
                  }
                },
                new RowCallback() {
                  @Override
                  public void onCancel() {
                    mainItem = null;
                  }

                  @Override
                  public void onSuccess(final BeeRow row) {
                    ParameterList args = TransportHandler.createArgs(SVC_CREATE_INVOICE_ITEMS);
                    args.addDataItem(TradeConstants.COL_SALE, row.getId());
                    args.addDataItem(ExchangeUtils.FLD_CURRENCY,
                        row.getLong(Data.getColumnIndex(turnovers, ExchangeUtils.FLD_CURRENCY)));
                    args.addDataItem("IdList", Codec.beeSerialize(idList));

                    if (mainItem != null && DataUtils.isId(mainItem.getRelatedId())) {
                      args.addDataItem(CommonsConstants.COL_ITEM, mainItem.getRelatedId());
                    }
                    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
                      @Override
                      public void onResponse(ResponseObject response) {
                        response.notify(presenter.getGridView());

                        if (response.hasErrors()) {
                          return;
                        }
                        presenter.refresh(true);
                        presenter.getGridView().getGrid().reset();
                        RowEditor.openRow("CargoInvoice", Data.getDataInfo(turnovers), row.getId());
                      }
                    });
                    mainItem = null;
                  }
                });
          }
        });
      }
    }));
  }
}
