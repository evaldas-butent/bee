package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.output.Exporter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.css.values.VerticalAlign;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XRow;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Client-side projects module handler.
 */
public final class OrdersKeeper {

  /**
   * Creates rpc parameters of orders module.
   *
   * @param method name of method.
   * @return rpc parameters to call queries of server-side.
   */
  public static ParameterList createSvcArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.ORDERS, method);
  }

  /**
   * Register orders client-side module handler.
   */
  public static void register() {
    FormFactory.registerFormInterceptor(COL_ORDER, new OrderForm());
    FormFactory.registerFormInterceptor("OrderInvoice", new OrderInvoiceForm());
    FormFactory.registerFormInterceptor(FORM_NEW_ORDER_INVOICE, new NewOrderInvoiceForm());

    GridFactory.registerGridInterceptor(GRID_ORDER_SALES, new OrderInvoiceBuilder());
    GridFactory.registerGridInterceptor(GRID_ORDERS_INVOICES, new OrdersInvoicesGrid());
    GridFactory.registerGridInterceptor(VIEW_ORDER_TMPL_ITEMS, new OrderTmplItemsGrid());
    GridFactory.registerGridInterceptor(VIEW_ORDERS, new OrdersGrid());
    GridFactory.registerGridInterceptor(GRID_ORDER_COMPLECT_ITEMS, new OrderItemsGrid());

    SelectorEvent.register(new OrdersSelectorHandler());

    Global.getParameter(PRM_NOTIFY_ABOUT_DEBTS, input -> {
      if (BeeUtils.toBoolean(input)) {
        OrdersObserver.register();
      }
    });
  }

  private OrdersKeeper() {
  }

  private static void doExport(BeeRowSet rowSet, String caption, final String fileName) {
    Assert.isTrue(Exporter.validateFileName(fileName));

    final int rowCount = rowSet.getNumberOfRows();
    if (rowCount <= 0) {
      String message = Localized.dictionary().noData();
      BeeKeeper.getScreen().notifyWarning(message);
      return;
    }

    List<BeeColumn> gridColumns = rowSet.getColumns();
    int columnCount = gridColumns.size();
    if (columnCount <= 0) {
      String message = "No exportable columns found";
      BeeKeeper.getScreen().notifyWarning(message);
      return;
    }

    int rowIndex = 1;
    XSheet sheet = new XSheet();
    XCell cell;

    if (!BeeUtils.isEmpty(caption)) {
      Exporter.addCaption(sheet, caption, rowIndex++, columnCount);
      rowIndex++;
    }

    XRow headerRow = new XRow(rowIndex++);

    XStyle headerStyle = XStyle.center();
    headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
    headerStyle.setColor(Colors.LIGHTGRAY);
    headerStyle.setFontRef(sheet.registerFont(XFont.bold()));

    int headerStyleRef = sheet.registerStyle(headerStyle);

    for (int i = 0; i < columnCount; i++) {
      cell = new XCell(i, Localized.maybeTranslate(gridColumns.get(i).getLabel()), headerStyleRef);
      headerRow.add(cell);
    }

    sheet.add(headerRow);

    for (BeeRow row : rowSet) {
      XRow xRow = new XRow(rowIndex++);
      for (int i = 0; i < columnCount; i++) {
        cell = new XCell(i, row.getValue(i));
        xRow.add(cell);
      }
      sheet.add(xRow);
    }

    for (int i = 0; i < columnCount; i++) {
      sheet.autoSizeColumn(i);
    }

    Exporter.export(sheet, fileName);
  }

  public static void export(BeeRowSet result, String caption) {
    if (result.getNumberOfRows() == 0) {
      BeeKeeper.getScreen().notifyWarning(Localized.dictionary().noData());
      return;
    }

    Exporter.confirm(caption, new Exporter.FileNameCallback() {
      @Override
      public void onSuccess(final String value) {
        doExport(result, caption, value);
      }
    });
  }

  public static boolean isComplect(IsRow row) {
    return BeeUtils.isPositive(row.getPropertyInteger(ClassifierConstants.PROP_ITEM_COMPONENT));
  }

  public static boolean isComponent(BeeRow row, String viewName) {
    return BeeUtils.isPositive(row.getLong(Data.getColumnIndex(viewName, COL_TRADE_ITEM_PARENT)));
  }

  public static void recalculateComplectPrice(BeeRow component, String viewName, String target) {
    Long complectItem = component.getLong(Data.getColumnIndex(viewName, COL_TRADE_ITEM_PARENT));
    Long order = component.getLong(Data.getColumnIndex(viewName, target));

    recalculateComplectPrice(complectItem, order, viewName, target);
  }

  public static void recalculateComplectPrice(Long complectItem, Long order, String viewName,
      String target) {
    if (!DataUtils.isId(complectItem) || !DataUtils.isId(order)) {
      return;
    }

    Queries.getRowSet(viewName, null, Filter.and(Filter.equals(target, order),
        Filter.equals(COL_TRADE_ITEM_PARENT, complectItem)), new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (result.getNumberOfRows() > 0) {
          Queries.getRow(viewName, complectItem, new RowCallback() {
            @Override
            public void onSuccess(BeeRow row) {
              if (row != null) {
                Totalizer totalizer = new Totalizer(result.getColumns());
                Double complectQty = row.getDouble(Data.getColumnIndex(viewName,
                    COL_TRADE_ITEM_QUANTITY));

                double price = BeeConst.DOUBLE_ZERO;
                double vat = BeeConst.DOUBLE_ZERO;

                for (BeeRow orderItem : result) {
                  price += BeeUtils.unbox(totalizer.getTotal(orderItem));
                  vat += BeeUtils.unbox(totalizer.getVat(orderItem));
                }

                price = price / complectQty;

                Queries.update(viewName, Filter.compareId(row.getId()),
                    Arrays.asList(COL_TRADE_ITEM_PRICE, COL_TRADE_VAT),
                    Arrays.asList(BeeUtils.toString(price), BeeUtils.toString(vat)),
                    new Queries.IntCallback() {
                      @Override
                      public void onSuccess(Integer result) {
                        DataChangeEvent.fireRefresh(BeeKeeper.getBus(), viewName);
                      }
                    });
              }
            }
          });
        }
      }
    });
  }
}