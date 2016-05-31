package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.Modality;
import com.butent.bee.client.modules.trade.InvoiceForm;
import com.butent.bee.client.modules.trade.InvoicesGrid;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CargoPurchaseInvoicesGrid extends InvoicesGrid {

  private Long operationId;
  private String operation;
  private Long operation2Id;
  private String operation2;
  private Button joinAction = new Button("Apjungti", this);

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    Global.getRelationParameter(PRM_ACCUMULATION_OPERATION, (opId, opName) -> {
      if (DataUtils.isId(opId)) {
        Global.getRelationParameter(PRM_ACCUMULATION2_OPERATION, (op2Id, op2Name) -> {
          if (DataUtils.isId(op2Id)) {
            operation2Id = op2Id;
            operation2 = op2Name;
          }
        });
        operationId = opId;
        operation = opName;
        presenter.getHeader().addCommandItem(joinAction);
      }
    });
    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoPurchaseInvoicesGrid();
  }

  @Override
  public void onClick(ClickEvent event) {
    if (Objects.equals(event.getSource(), joinAction)) {
      GridView view = getGridView();
      Set<Long> ids = new HashSet<>();

      for (RowInfo row : view.getSelectedRows(GridView.SelectedRows.ALL)) {
        ids.add(row.getId());
      }
      if (ids.size() < 2) {
        view.notifyWarning("Pažymėkite bent dvi sąskaitas");
        return;
      }
      joinAction.setVisible(false);
      List<String> cols = Arrays.asList(COL_TRADE_NUMBER, COL_TRADE_OPERATION, COL_TRADE_SUPPLIER,
          COL_TRADE_SUPPLIER + "Name", COL_TRADE_CURRENCY, COL_TRADE_CURRENCY + "Name",
          COL_TRADE_NOTES);

      Queries.getRowSet(getViewName(), cols, Filter.idIn(ids), new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          joinAction.setVisible(true);

          for (String col : new String[] {COL_TRADE_SUPPLIER, COL_TRADE_CURRENCY}) {
            int idx = result.getColumnIndex(col);

            if (result.getDistinctStrings(idx).size() > 1) {
              view.notifyWarning("Daugiau negu viena reikšmė:",
                  Localized.getLabel(result.getColumn(idx)));
              return;
            }
          }
          for (Long op : result.getDistinctLongs(result.getColumnIndex(COL_TRADE_OPERATION))) {
            if (!BeeUtils.in(op, operationId, operation2Id)) {
              view.notifyWarning("Apjungti leidžiama tik dokumentus su operacijomis", operation,
                  operation2);
              return;
            }
          }
          DataInfo dataInfo = Data.getDataInfo(getViewName());
          BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);

          newRow.setValue(dataInfo.getColumnIndex(COL_TRADE_MANAGER),
              BeeKeeper.getUser().getUserId());
          newRow.setValue(dataInfo.getColumnIndex(COL_TRADE_MANAGER + COL_PERSON),
              BeeKeeper.getUser().getUserData().getCompanyPerson());
          newRow.setValue(dataInfo.getColumnIndex(COL_TRADE_MANAGER + COL_FIRST_NAME),
              BeeKeeper.getUser().getFirstName());
          newRow.setValue(dataInfo.getColumnIndex(COL_TRADE_MANAGER + COL_LAST_NAME),
              BeeKeeper.getUser().getLastName());

          for (String col : cols) {
            if (!Objects.equals(col, COL_TRADE_OPERATION)) {
              newRow.setValue(dataInfo.getColumnIndex(col),
                  BeeUtils.joinItems(result.getDistinctStrings(result.getColumnIndex(col))));
            }
          }
          RowFactory.createRow(dataInfo.getNewRowForm(), dataInfo.getNewRowCaption(), dataInfo,
              newRow, Modality.ENABLED, null, new InvoiceForm(null), null, new RowCallback() {
                @Override
                public void onSuccess(BeeRow newInvoice) {
                  Queries.update(VIEW_PURCHASE_ITEMS, Filter.any(COL_PURCHASE, ids), COL_PURCHASE,
                      BeeUtils.toString(newInvoice.getId()), new Queries.IntCallback() {
                        @Override
                        public void onSuccess(Integer res1) {
                          Data.onViewChange(getViewName(), DataChangeEvent.RESET_REFRESH);
                          Data.onViewChange(VIEW_PURCHASE_ITEMS, DataChangeEvent.RESET_REFRESH);

                          Queries.update(TBL_CARGO_EXPENSES,
                              Filter.any(COL_PURCHASE, ids), COL_PURCHASE,
                              BeeUtils.toString(newInvoice.getId()), new Queries.IntCallback() {
                                @Override
                                public void onSuccess(Integer res2) {
                                  Data.onViewChange(TBL_CARGO_EXPENSES,
                                      DataChangeEvent.RESET_REFRESH);
                                }
                              });
                        }
                      });
                }
              });
        }
      });
    } else {
      super.onClick(event);
    }
  }
}
