package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.output.Exporter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.css.values.VerticalAlign;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XRow;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

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

    GridFactory.registerGridInterceptor(VIEW_ORDER_SALES, new OrderInvoiceBuilder());
    GridFactory.registerGridInterceptor(GRID_ORDERS_INVOICES, new OrdersInvoicesGrid());
    GridFactory.registerGridInterceptor(VIEW_ORDER_TMPL_ITEMS, new OrderTmplItemsGrid());
    GridFactory.registerGridInterceptor(VIEW_ORDERS, new OrdersGrid());

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
}