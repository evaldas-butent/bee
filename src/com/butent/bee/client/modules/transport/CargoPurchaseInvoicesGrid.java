package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.modules.trade.InvoiceForm;
import com.butent.bee.client.modules.trade.InvoicesGrid;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
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
    Global.getParameterRelation(PRM_ACCUMULATION_OPERATION, (opId, opName) -> {
      if (DataUtils.isId(opId)) {
        operationId = opId;
        operation = opName;
        presenter.getHeader().addCommandItem(joinAction);
      }
    });
    Global.getParameterRelation(PRM_PURCHASE_OPERATION, (op2Id, op2Name) -> {
      operation2Id = op2Id;
      operation2 = op2Name;
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
      if (ids.isEmpty()) {
        view.notifyWarning(Localized.dictionary().selectAtLeastOneRow());
        return;
      }
      joinAction.setVisible(false);
      List<String> cols = Arrays.asList(COL_TRADE_NUMBER, COL_TRADE_OPERATION, COL_TRADE_SUPPLIER,
          COL_TRADE_SUPPLIER + "Name", COL_TRADE_CURRENCY, COL_TRADE_CURRENCY + "Name",
          COL_OPERATION_WAREHOUSE_TO, COL_OPERATION_WAREHOUSE_TO + "Code", COL_TRADE_NOTES);

      Queries.getRowSet(getViewName(), cols, Filter.idIn(ids), result -> {
        joinAction.setVisible(true);

        for (String col : new String[] {
            COL_TRADE_SUPPLIER, COL_OPERATION_WAREHOUSE_TO, COL_TRADE_CURRENCY}) {
          int idx = result.getColumnIndex(col);

          if (result.getDistinctStrings(idx).size() > 1) {
            view.notifyWarning("Daugiau negu viena reikšmė:",
                Localized.getLabel(result.getColumn(idx)));
            return;
          }
        }
        for (Long op : result.getDistinctLongs(result.getColumnIndex(COL_TRADE_OPERATION))) {
          if (!Objects.equals(op, operationId)) {
            view.notifyWarning("Apjungti leidžiama tik dokumentus su operacija", operation);
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
        if (DataUtils.isId(operation2Id)) {
          newRow.setValue(dataInfo.getColumnIndex(COL_TRADE_OPERATION), operation2Id);
          newRow.setValue(dataInfo.getColumnIndex(COL_TRADE_OPERATION + "Name"), operation2);
        }
        RowFactory.createRow(dataInfo.getNewRowForm(), dataInfo.getNewRowCaption(), dataInfo,
            newRow, Opener.MODAL, new InvoiceForm(null),
            newInvoice -> Queries.update(VIEW_PURCHASE_ITEMS, Filter.any(COL_PURCHASE, ids),
                COL_PURCHASE, BeeUtils.toString(newInvoice.getId()), res1 -> {
                  Data.resetLocal(getViewName());
                  Data.resetLocal(VIEW_PURCHASE_ITEMS);

                  Queries.update(TBL_CARGO_EXPENSES, Filter.any(COL_PURCHASE, ids), COL_PURCHASE,
                      BeeUtils.toString(newInvoice.getId()),
                      res2 -> Data.resetLocal(TBL_CARGO_EXPENSES));
                }));
      });
    } else {
      super.onClick(event);
    }
  }
}
