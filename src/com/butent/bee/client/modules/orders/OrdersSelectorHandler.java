package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.orders.OrdersConstants.OrdersStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class OrdersSelectorHandler implements SelectorEvent.Handler {

  private static final BeeLogger logger = LogUtils.getLogger(OrdersSelectorHandler.class);

  private static boolean isTemplatable(IsRow ordRow, IsRow templRow, String colName) {
    int ordIndex = Data.getColumnIndex(VIEW_ORDERS, colName);
    int templIndex = Data.getColumnIndex(VIEW_ORDERS_TEMPLATES, colName);

    return !BeeConst.isUndef(ordIndex) && !BeeConst.isUndef(templIndex)
        && ordRow.isNull(ordIndex) && !templRow.isNull(templIndex);
  }

  private static void applyOrderTemplate(final IsRow templRow, final FormView form) {
    IsRow targetRow = form.getActiveRow();
    List<BeeColumn> templColumns = Data.getColumns(VIEW_ORDERS_TEMPLATES);

    if (targetRow != null && !BeeUtils.isEmpty(templColumns)) {
      Set<String> updatedColumns = new HashSet<>();

      for (int i = 0; i < templColumns.size(); i++) {
        String colName = templColumns.get(i).getId();
        String newValue = templRow.getString(i);

        int targetIndex = form.getDataIndex(colName);
        boolean upd;

        if (BeeConst.isUndef(targetIndex)) {
          upd = false;

        } else {
          upd = isTemplatable(targetRow, templRow, colName);
        }

        if (upd) {
          targetRow.setValue(targetIndex, newValue);
          if (templColumns.get(i).isEditable()) {
            updatedColumns.add(colName);
          }
        }
      }

      if (!updatedColumns.isEmpty()) {
        for (String colName : updatedColumns) {
          form.refreshBySource(colName);
        }
        logger.debug(updatedColumns);
      }

      ParameterList params = OrdersKeeper.createSvcArgs(SVC_GET_TEMPLATE_ITEMS);
      params.addQueryItem(COL_TEMPLATE, templRow.getId());

      if (DataUtils.hasId(targetRow)) {
        params.addQueryItem(COL_ORDER, targetRow.getId());
      }

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          final Map<String, BeeRowSet> data = new HashMap<>();

          if (response.getSize() > 0) {
            String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

            if (arr != null) {
              for (String s : arr) {
                BeeRowSet rowSet = BeeRowSet.restore(s);

                if (!DataUtils.isEmpty(rowSet)) {
                  data.put(rowSet.getViewName(), rowSet);
                  logger.debug(rowSet.getViewName(), rowSet.getNumberOfRows());
                }
              }
            }
          }

          if (!data.isEmpty()) {
            GridView gridView = ViewHelper.getChildGrid(form, TBL_ORDER_ITEMS);

            if (gridView != null) {
              gridView.ensureRelId(result -> {
                if (DataUtils.isId(result) && DataUtils.idEquals(form.getActiveRow(), result)) {
                  if (data.containsKey(VIEW_ORDER_TMPL_ITEMS)) {
                    addTemplateChildren(TBL_ORDER_ITEMS, form.getActiveRow(),
                        data.get(VIEW_ORDER_TMPL_ITEMS));
                  }
                }
              });
            }
          }
        }
      });
    }
  }

  private static void addTemplateChildren(String viewName, IsRow ordRow, BeeRowSet templChildren) {

    List<BeeColumn> columns = new ArrayList<>();
    Map<Integer, Integer> indexes = new HashMap<>();

    for (BeeColumn column : Data.getColumns(viewName)) {
      if (COL_ORDER.equals(column.getId())) {
        columns.add(column);

      } else if (column.isEditable()) {
        int templIndex = templChildren.getColumnIndex(column.getId());

        if (!BeeConst.isUndef(templIndex)) {
          indexes.put(templIndex, columns.size());
          columns.add(column);
        }
      }
    }

    BeeRowSet orderItems = new BeeRowSet(viewName, columns);
    BeeRowSet items = new BeeRowSet(viewName, columns);

    List<BeeRowSet> orderComplects = new ArrayList<>();
    List<BeeRowSet> complects = new ArrayList<>();

    int ordIndex = orderItems.getColumnIndex(COL_ORDER);
    int qtyIndex = orderItems.getColumnIndex(COL_TRADE_ITEM_QUANTITY);

    boolean qtyNullable = BeeConst.isUndef(qtyIndex) || orderItems.getColumn(qtyIndex).isNullable();

    for (BeeRow templItem : templChildren) {
      if (OrdersKeeper.isComplect(templItem)) {
        BeeRowSet complect = new BeeRowSet(viewName, columns);
        complect.addRow(templItem);
        complect.addRows(DataUtils.filterRows(templChildren, COL_TRADE_ITEM_PARENT,
            templItem.getId()));
        complects.add(complect);
      } else if (!OrdersKeeper.isComponent(templItem, VIEW_ORDER_TMPL_ITEMS)) {
        items.addRow(templItem);
      }
    }

    for (BeeRow templItem : items) {
      BeeRow ordItem = DataUtils.createEmptyRow(orderItems.getNumberOfColumns());
      ordItem.setValue(ordIndex, ordRow.getId());

      for (Map.Entry<Integer, Integer> indexEntry : indexes.entrySet()) {
        if (!templItem.isNull(indexEntry.getKey())) {
          ordItem.setValue(indexEntry.getValue(), templItem.getString(indexEntry.getKey()));
        }
      }

      if (!qtyNullable && ordItem.isNull(qtyIndex)) {
        ordItem.setValue(qtyIndex, 0);
      }

      orderItems.addRow(ordItem);
    }

    for (BeeRowSet rowSet : complects) {
      BeeRowSet complect = new BeeRowSet(viewName, columns);

      for (BeeRow row : rowSet) {
        BeeRow ordItem = DataUtils.createEmptyRow(orderItems.getNumberOfColumns());
        ordItem.setValue(ordIndex, ordRow.getId());

        for (Map.Entry<Integer, Integer> indexEntry : indexes.entrySet()) {
          if (!row.isNull(indexEntry.getKey())) {
            ordItem.setValue(indexEntry.getValue(), row.getString(indexEntry.getKey()));
          }
        }

        if (!qtyNullable && ordItem.isNull(qtyIndex)) {
          ordItem.setValue(qtyIndex, 0);
        }

        complect.addRow(ordItem);
      }

      orderComplects.add(complect);
    }

    if (orderComplects.isEmpty()) {
      if (!orderItems.isEmpty()) {
        Queries.insertRows(orderItems);
      }
    } else {
      ParameterList params = OrdersKeeper.createSvcArgs(SVC_INSERT_COMPLECTS);
      params.addHeaderItem(Service.VAR_DATA, Codec.beeSerialize(orderComplects));

      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (!response.hasErrors()) {
            if (!orderItems.isEmpty()) {
              Queries.insertRows(orderItems);
            }

            DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_ORDER_ITEMS);
          }
        }
      });
    }
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    String relatedViewName = event.getRelatedViewName();
    if (BeeUtils.isEmpty(relatedViewName)) {
      return;
    }

    switch (relatedViewName) {
      case VIEW_ORDERS_TEMPLATES:
        if (event.isChanged()) {
          Dictionary lc = Localized.dictionary();
          IsRow relatedRow = event.getRelatedRow();
          FormView form = ViewHelper.getForm(event.getSelector());

          if (!Objects.equals(form.getIntegerValue(COL_ORDERS_STATUS), OrdersStatus.PREPARED
              .ordinal())) {

            if (!BeeUtils.isPositive(relatedRow.getLong(Data.getColumnIndex(VIEW_ORDERS_TEMPLATES,
                COL_WAREHOUSE))) && !BeeUtils.isPositive(form.getLongValue(COL_WAREHOUSE))) {
              form.notifySevere(lc.warehouse() + " " + lc.valueRequired());
              event.getSelector().clearValue();
              return;
            }
          }

          if (!BeeUtils.isPositive(relatedRow.getLong(Data.getColumnIndex(VIEW_ORDERS_TEMPLATES,
              COL_COMPANY))) && !BeeUtils.isPositive(form.getLongValue(COL_COMPANY))) {
            form.notifySevere(lc.client() + " " + lc.valueRequired());
            event.getSelector().clearValue();
            return;
          }

          if (relatedRow != null) {
            applyOrderTemplate(relatedRow, form);
          }
        }
        break;
    }
  }
}