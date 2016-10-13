package com.butent.bee.client.modules.orders.ec;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.ElementSize;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.orders.OrdersConstants.OrdersStatus;
import com.butent.bee.shared.modules.orders.ec.OrdEcFinInfo;
import com.butent.bee.shared.modules.orders.ec.OrdEcInvoice;
import com.butent.bee.shared.modules.orders.ec.OrdEcInvoiceItem;
import com.butent.bee.shared.modules.orders.ec.OrdEcOrder;
import com.butent.bee.shared.modules.orders.ec.OrdEcOrderItem;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class OrdEcFinancialInfo extends OrdEcView {

  private static final class OrderPanel extends Flow implements Printable {

    private OrderPanel(String styleName) {
      super(styleName);
    }

    @Override
    public String getCaption() {
      return Localized.dictionary().ecOrder();
    }

    @Override
    public Element getPrintElement() {
      return getElement();
    }

    @Override
    public boolean onPrint(Element source, Element target) {
      if (getId().equals(source.getId())) {
        ElementSize.copyScroll(source, target);
        target.setClassName(BeeConst.STRING_EMPTY);
      }
      return true;
    }
  }

  private static final String STYLE_NAME = "finInfo";

  private static final String STYLE_PREFIX_FIN = EcStyles.name(STYLE_NAME, "fin-");
  private static final String STYLE_PREFIX_ORDER = EcStyles.name(STYLE_NAME, "order-");
  private static final String STYLE_PREFIX_INVOICE = EcStyles.name(STYLE_NAME, "invoice-");

  private static final String STYLE_PREFIX_ORDER_DETAILS = STYLE_PREFIX_ORDER + "details-";
  private static final String STYLE_PREFIX_ORDER_ITEM = STYLE_PREFIX_ORDER + "item-";

  private static final String STYLE_SUFFIX_TABLE = "table";
  private static final String STYLE_SUFFIX_HEADER = "header";
  private static final String STYLE_SUFFIX_DATA = "data";

  private static final String STYLE_SUFFIX_PANEL = "panel";
  private static final String STYLE_SUFFIX_CAPTION = "caption";
  private static final String STYLE_SUFFIX_LABEL = "label";
  private static final String STYLE_SUFFIX_VALUE = "value";
  private static final String STYLE_SUFFIX_EMPTY = "empty";

  private static final String STYLE_ORDER_DATE = STYLE_PREFIX_ORDER + "date";
  private static final String STYLE_ORDER_NUMBER = STYLE_PREFIX_ORDER + "number";
  private static final String STYLE_ORDER_AMOUNT = STYLE_PREFIX_ORDER + "amount";
  private static final String STYLE_ORDER_COMMENT = STYLE_PREFIX_ORDER + "comment";
  private static final String STYLE_ORDER_STATUS = STYLE_PREFIX_ORDER + "status";
  private static final String STYLE_ORDER_MANAGER = STYLE_PREFIX_ORDER + "maneger";

  private static final String STYLE_INVOICE_SERIES = STYLE_PREFIX_INVOICE + "series";
  private static final String STYLE_INVOICE_NUMBER = STYLE_PREFIX_INVOICE + "number";
  private static final String STYLE_INVOICE_DATE = STYLE_PREFIX_INVOICE + "date";
  private static final String STYLE_INVOICE_TERM = STYLE_PREFIX_INVOICE + "termr";
  private static final String STYLE_INVOICE_PAID = STYLE_PREFIX_INVOICE + "paid";
  private static final String STYLE_INVOICE_PAYMENT_TIME = STYLE_PREFIX_INVOICE + "paiymentTime";
  private static final String STYLE_INVOICE_AMOUNT = STYLE_PREFIX_INVOICE + "amount";
  private static final String STYLE_INVOICE_DEBT = STYLE_PREFIX_INVOICE + "debt";
  private static final String STYLE_INVOICE_ID = STYLE_PREFIX_INVOICE + "id";
  private static final String STYLE_INVOICE_CURRENCY = STYLE_PREFIX_INVOICE + "currency";
  private static final String STYLE_INVOICE_MANAGER = STYLE_PREFIX_INVOICE + "manager";

  private static final String STYLE_ORDER_ITEM_PICTURE = STYLE_PREFIX_ORDER_ITEM + "picture";
  private static final String STYLE_ORDER_ITEM_NAME = STYLE_PREFIX_ORDER_ITEM + "name";
  private static final String STYLE_ORDER_ITEM_ARTICLE = STYLE_PREFIX_ORDER_ITEM + "article";
  private static final String STYLE_ORDER_ITEM_QUANTITY = STYLE_PREFIX_ORDER_ITEM + "quantity";
  private static final String STYLE_ORDER_ITEM_PRICE = STYLE_PREFIX_ORDER_ITEM + "price";

  private static final int ORDER_DATE_COL = 0;
  private static final int ORDER_NUMBER_COL = 1;
  private static final int ORDER_AMOUNT_COL = 2;
  private static final int ORDER_COMMENT_COL = 3;
  private static final int ORDER_STATUS_COL = 4;
  private static final int ORDER_MANAGER_COL = 5;

  private static final int INVOICE_ID_COL = 0;
  private static final int INVOICE_DATE_COL = 1;
  private static final int INVOICE_SERIES_COL = 2;
  private static final int INVOICE_NUMBER_COL = 3;
  private static final int INVOICE_AMOUNT_COL = 4;
  private static final int INVOICE_CURRENCY_COL = 5;
  private static final int INVOICE_TERM_COL = 6;
  private static final int INVOICE_PAYMENT_TIME_COL = 7;
  private static final int INVOICE_PAID_COL = 8;
  private static final int INVOICE_DEBT_COL = 9;
  private static final int INVOICE_MANAGER_COL = 10;

  private static final int ORDER_ITEM_PICTURE_COL = 0;
  private static final int ORDER_ITEM_NAME_COL = 1;
  private static final int ORDER_ITEM_ARTICLE_COL = 2;
  private static final int ORDER_ITEM_QUANTITY_COL = 3;
  private static final int ORDER_ITEM_PRICE_COL = 4;

  private static void openInvoice(OrdEcInvoice invoice) {
    final OrderPanel panel = new OrderPanel(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_PANEL);

    HtmlTable invoiceTable = new HtmlTable(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_TABLE);
    int row = 0;
    int col = 0;

    String stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "date-";
    Widget label = renderOrderDetailLabel(Localized.dictionary().ecOrderSubmissionDate());
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    Widget value = renderOrderDetailValue(TimeUtils.renderCompact(invoice.getDate()));
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "invoiceNumber-";
    label = renderOrderDetailLabel(Localized.dictionary().ecInvoiceNumber());
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(BeeUtils.toString(invoice.getInvoiceId()));
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "series-";
    label = renderOrderDetailLabel(Localized.dictionary().trdInvoicePrefix());
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(invoice.getSeries());
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "number-";
    label = renderOrderDetailLabel(Localized.dictionary().number());
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(invoice.getNumber());
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "term-";
    label = renderOrderDetailLabel(Localized.dictionary().trdTerm());
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(TimeUtils.renderCompact(invoice.getTerm()));
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "manager-";
    label = renderOrderDetailLabel(Localized.dictionary().manager());
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(invoice.getManager());
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "amount-";
    label = renderOrderDetailLabel(BeeUtils.joinWords(Localized.dictionary().ecInvoiceAmount(),
        invoice.getCurrency()));
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(BeeUtils.toString(invoice.getTotalAmount()));
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "debt-";
    label = renderOrderDetailLabel(BeeUtils.joinWords(Localized.dictionary().ecInvoiceDebt(),
        invoice.getCurrency()));
    invoiceTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value =
        renderOrderDetailValue(invoice.getDebt() == null ? null : BeeUtils.toString(invoice
            .getDebt()));
    invoiceTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    panel.add(invoiceTable);

    Label itemCaption = new Label(BeeUtils.joinWords(Localized.dictionary().goods(),
        BeeUtils.bracket(invoice.getItems().size())));
    itemCaption.addStyleName(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_CAPTION);
    panel.add(itemCaption);

    HtmlTable itemTable = new HtmlTable(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_TABLE);
    row = 0;

    itemTable.setWidgetAndStyle(row, ORDER_ITEM_NAME_COL,
        renderOrderItemHeader(Localized.dictionary().ecItemName()),
        STYLE_ORDER_ITEM_NAME + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_ARTICLE_COL,
        renderOrderItemHeader(Localized.dictionary().article()),
        STYLE_ORDER_ITEM_ARTICLE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_QUANTITY_COL,
        renderOrderItemHeader(Localized.dictionary().ecItemQuantity()),
        STYLE_ORDER_ITEM_QUANTITY + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_PRICE_COL,
        renderOrderItemHeader(Localized.dictionary().ecItemPrice()),
        STYLE_ORDER_ITEM_PRICE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    itemTable.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_HEADER);
    row++;

    Multimap<Long, OrdEcItemPicture> pictureWidgets = ArrayListMultimap.create();
    Widget widget;

    for (OrdEcInvoiceItem item : invoice.getItems()) {
      OrdEcItemPicture pictureWidget = new OrdEcItemPicture(item.getCaption());
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_PICTURE_COL, pictureWidget,
          STYLE_ORDER_ITEM_PICTURE);
      pictureWidgets.put(item.getItemId(), pictureWidget);

      if (item.getName() != null) {
        widget = new Label(item.getName());
        itemTable.setWidgetAndStyle(row, ORDER_ITEM_NAME_COL, widget, STYLE_ORDER_ITEM_NAME);
      }

      if (item.getArticle() != null) {
        widget = new Label(item.getArticle());
        itemTable.setWidgetAndStyle(row, ORDER_ITEM_ARTICLE_COL, widget, STYLE_ORDER_ITEM_ARTICLE);
      }

      int quantity = BeeUtils.unbox(item.getQuantity());
      widget = new Label(BeeUtils.toString(quantity));
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_QUANTITY_COL, widget, STYLE_ORDER_ITEM_QUANTITY);

      int cents = BeeUtils.round(BeeUtils.unbox(item.getPrice()) * 100);
      widget = new Label(EcUtils.formatCents(cents));
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_PRICE_COL, widget, STYLE_ORDER_ITEM_PRICE);

      itemTable.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_DATA);
      row++;
    }

    if (!pictureWidgets.isEmpty()) {
      OrdEcKeeper.setBackgroundPictures(pictureWidgets);
    }

    panel.add(itemTable);

    DialogBox dialog = DialogBox.withoutCloseBox(Localized.dictionary().ecInvoiceNumber(),
        STYLE_PREFIX_ORDER_DETAILS + "dialog");
    dialog.setWidget(panel);

    FaLabel print = new FaLabel(FontAwesome.PRINT);
    print.addClickHandler(arg0 -> Printer.print(panel));
    dialog.addAction(Action.PRINT, print);
    dialog.addDefaultCloseBox();

    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);
    dialog.center();
  }

  private static void openOrder(OrdEcOrder order) {
    final OrderPanel panel = new OrderPanel(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_PANEL);

    HtmlTable orderTable = new HtmlTable(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_TABLE);
    int row = 0;
    int col = 0;

    String stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "date-";
    Widget label = renderOrderDetailLabel(Localized.dictionary().ecOrderSubmissionDate());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    Widget value = renderOrderDetailValue(TimeUtils.renderCompact(order.getDate()));
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "number-";
    label = renderOrderDetailLabel(Localized.dictionary().ecOrderNumber());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(BeeUtils.toString(order.getOrderId()));
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "status-";
    label = renderOrderDetailLabel(Localized.dictionary().ecOrderStatus());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    OrdersStatus status = OrdersStatus.get(order.getStatus());
    value = renderOrderDetailValue((status == null) ? null : status.getCaption());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "comment-";
    label = renderOrderDetailLabel(Localized.dictionary().comment());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(order.getComment());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "manager-";
    label = renderOrderDetailLabel(Localized.dictionary().ecManager());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(order.getManager());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "amount-";
    label = renderOrderDetailLabel(BeeUtils.joinWords(Localized.dictionary().ecOrderAmount(),
        EcConstants.CURRENCY));
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(EcUtils.formatCents(BeeUtils.round(order.getAmount() * 100)));
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    panel.add(orderTable);

    Label itemCaption = new Label(BeeUtils.joinWords(Localized.dictionary().ecOrderItems(),
        BeeUtils.bracket(order.getItems().size())));
    itemCaption.addStyleName(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_CAPTION);
    panel.add(itemCaption);

    HtmlTable itemTable = new HtmlTable(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_TABLE);
    row = 0;

    itemTable.setWidgetAndStyle(row, ORDER_ITEM_NAME_COL,
        renderOrderItemHeader(Localized.dictionary().ecItemName()),
        STYLE_ORDER_ITEM_NAME + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_ARTICLE_COL,
        renderOrderItemHeader(Localized.dictionary().article()),
        STYLE_ORDER_ITEM_ARTICLE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_QUANTITY_COL,
        renderOrderItemHeader(Localized.dictionary().ecItemQuantity()),
        STYLE_ORDER_ITEM_QUANTITY + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_PRICE_COL,
        renderOrderItemHeader(Localized.dictionary().ecItemPrice()),
        STYLE_ORDER_ITEM_PRICE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    itemTable.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_HEADER);
    row++;

    Multimap<Long, OrdEcItemPicture> pictureWidgets = ArrayListMultimap.create();
    Widget widget;

    for (OrdEcOrderItem item : order.getItems()) {
      OrdEcItemPicture pictureWidget = new OrdEcItemPicture(item.getCaption());
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_PICTURE_COL, pictureWidget,
          STYLE_ORDER_ITEM_PICTURE);
      pictureWidgets.put(item.getItemId(), pictureWidget);

      if (item.getName() != null) {
        widget = new Label(item.getName());
        itemTable.setWidgetAndStyle(row, ORDER_ITEM_NAME_COL, widget, STYLE_ORDER_ITEM_NAME);
      }

      if (item.getArticle() != null) {
        widget = new Label(item.getArticle());
        itemTable.setWidgetAndStyle(row, ORDER_ITEM_ARTICLE_COL, widget, STYLE_ORDER_ITEM_ARTICLE);
      }

      int quantity = BeeUtils.unbox(item.getQuantity());
      widget = new Label(BeeUtils.toString(quantity));
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_QUANTITY_COL, widget, STYLE_ORDER_ITEM_QUANTITY);

      int cents = BeeUtils.round(BeeUtils.unbox(item.getPrice()) * 100);
      widget = new Label(EcUtils.formatCents(cents));
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_PRICE_COL, widget, STYLE_ORDER_ITEM_PRICE);

      itemTable.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_DATA);
      row++;
    }

    if (!pictureWidgets.isEmpty()) {
      OrdEcKeeper.setBackgroundPictures(pictureWidgets);
    }

    panel.add(itemTable);

    DialogBox dialog = DialogBox.withoutCloseBox(Localized.dictionary().ecOrder(),
        STYLE_PREFIX_ORDER_DETAILS + "dialog");
    dialog.setWidget(panel);

    FaLabel print = new FaLabel(FontAwesome.PRINT);
    print.addClickHandler(arg0 -> Printer.print(panel));
    dialog.addAction(Action.PRINT, print);
    dialog.addDefaultCloseBox();

    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);
    dialog.center();
  }

  private static Widget renderFin(OrdEcFinInfo finInfo) {
    if (finInfo == null) {
      return null;
    }

    HtmlTable table = new HtmlTable(STYLE_PREFIX_FIN + STYLE_SUFFIX_TABLE);
    int row = 0;
    int col = 0;

    String stylePrefix = STYLE_PREFIX_FIN + "limit";
    Widget label = renderFinLabel(Localized.dictionary().ecCreditLimit());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    Widget value = renderFinAmount(finInfo.getCreditLimit());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "orders";
    label = renderFinLabel(Localized.dictionary().ecTotalOrdered());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    double total = 0;
    for (OrdEcOrder order : finInfo.getOrders()) {
      if (OrdersStatus.in(order.getStatus(), OrdersStatus.NEW, OrdersStatus.APPROVED)) {
        total += order.getAmount();
      }
    }

    value = renderFinAmount(total);
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);
    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_FIN + "debt";
    label = renderFinLabel(Localized.dictionary().ecDebt());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getDebt());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "overdue";
    label = renderFinLabel(Localized.dictionary().trdOverdue());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getOverdue());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    return table;
  }

  private static Widget renderFinAmount(double amount) {
    CustomDiv widget = new CustomDiv(STYLE_PREFIX_FIN + STYLE_SUFFIX_VALUE);

    int cents = BeeUtils.round(amount * 100);
    String text = (cents == 0) ? BeeConst.STRING_ZERO
        : BeeUtils.joinWords(EcUtils.formatCents(cents), EcConstants.CURRENCY);

    widget.setHtml(text);
    return widget;
  }

  private static Widget renderFinLabel(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_FIN + STYLE_SUFFIX_LABEL);
    return label;
  }

  private static Widget renderInvoiceHeader(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_INVOICE + STYLE_SUFFIX_LABEL);
    return label;
  }

  private static Widget renderInvoices(List<OrdEcInvoice> invoices) {
    if (BeeUtils.isEmpty(invoices)) {
      return null;
    }

    Flow panel = new Flow(STYLE_PREFIX_INVOICE + STYLE_SUFFIX_PANEL);

    Label caption = new Label(Localized.dictionary().ecInvoices());
    caption.addStyleName(STYLE_PREFIX_INVOICE + STYLE_SUFFIX_CAPTION);
    panel.add(caption);

    HtmlTable table = new HtmlTable(STYLE_PREFIX_INVOICE + STYLE_SUFFIX_TABLE);
    int row = 0;

    table.setWidgetAndStyle(row, INVOICE_ID_COL,
        renderInvoiceHeader(Localized.dictionary().ecInvoiceNumber()),
        STYLE_INVOICE_ID + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_DATE_COL,
        renderInvoiceHeader(Localized.dictionary().ecInvoiceDate()),
        STYLE_INVOICE_DATE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_SERIES_COL,
        renderInvoiceHeader(Localized.dictionary().trdInvoicePrefix()),
        STYLE_INVOICE_SERIES + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_NUMBER_COL,
        renderInvoiceHeader(Localized.dictionary().number()),
        STYLE_INVOICE_NUMBER + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_AMOUNT_COL,
        renderInvoiceHeader(BeeUtils.joinWords(Localized.dictionary().ecInvoiceAmount(),
            EcConstants.CURRENCY)),
        STYLE_INVOICE_AMOUNT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_CURRENCY_COL,
        renderInvoiceHeader(Localized.dictionary().currency()),
        STYLE_INVOICE_CURRENCY + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_TERM_COL,
        renderInvoiceHeader(Localized.dictionary().trdTerm()),
        STYLE_INVOICE_TERM + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_PAYMENT_TIME_COL,
        renderInvoiceHeader(Localized.dictionary().trdPaymentTime()),
        STYLE_INVOICE_PAYMENT_TIME + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_PAID_COL,
        renderInvoiceHeader(Localized.dictionary().trdPaid()),
        STYLE_INVOICE_PAID + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_DEBT_COL,
        renderInvoiceHeader(BeeUtils.joinWords(Localized.dictionary().ecInvoiceDebt(),
            EcConstants.CURRENCY)),
        STYLE_INVOICE_DEBT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_MANAGER_COL, renderInvoiceHeader(Localized.dictionary()
        .manager()), STYLE_INVOICE_MANAGER + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.getRowFormatter().addStyleName(row, STYLE_PREFIX_INVOICE + STYLE_SUFFIX_HEADER);
    row++;

    Widget widget;

    for (OrdEcInvoice invoice : invoices) {

      Label idWidget = new Label(BeeUtils.toString(invoice.getInvoiceId()));
      idWidget.addClickHandler(event -> openInvoice(invoice));
      table.setWidgetAndStyle(row, INVOICE_ID_COL, idWidget, STYLE_ORDER_NUMBER);

      if (invoice.getDate() != null) {
        widget = new Label(TimeUtils.renderCompact(invoice.getDate()));
        table.setWidgetAndStyle(row, INVOICE_DATE_COL, widget, STYLE_INVOICE_DATE);
      }

      if (!BeeUtils.isEmpty(invoice.getSeries())) {
        widget = new Label(invoice.getSeries());
        table.setWidgetAndStyle(row, INVOICE_SERIES_COL, widget, STYLE_INVOICE_SERIES);
      }

      if (!BeeUtils.isEmpty(invoice.getNumber())) {
        widget = new Label(invoice.getNumber());
        table.setWidgetAndStyle(row, INVOICE_NUMBER_COL, widget, STYLE_INVOICE_NUMBER);
      }

      if (invoice.getAmount() != null) {
        widget = new Label(EcUtils.formatCents(BeeUtils.round(invoice.getAmount() * 100)));
        table.setWidgetAndStyle(row, INVOICE_AMOUNT_COL, widget, STYLE_INVOICE_AMOUNT);
      }

      if (!BeeUtils.isEmpty(invoice.getCurrency())) {
        widget = new Label(invoice.getCurrency());
        table.setWidgetAndStyle(row, INVOICE_CURRENCY_COL, widget, STYLE_INVOICE_CURRENCY);
      }

      if (invoice.getTerm() != null) {
        widget = new Label(TimeUtils.renderCompact(invoice.getTerm()));
        table.setWidgetAndStyle(row, INVOICE_TERM_COL, widget, STYLE_INVOICE_TERM);
      }

      if (invoice.getPaymentTime() != null) {
        widget = new Label(TimeUtils.renderCompact(invoice.getPaymentTime()));
        table.setWidgetAndStyle(row, INVOICE_PAYMENT_TIME_COL, widget, STYLE_INVOICE_PAYMENT_TIME);
      }

      if (invoice.getPaid() != null) {
        widget = new Label(EcUtils.formatCents(BeeUtils.round(invoice.getPaid() * 100)));
        table.setWidgetAndStyle(row, INVOICE_PAID_COL, widget, STYLE_INVOICE_PAID);
      }

      if (invoice.getDebt() != null) {
        widget = new Label(EcUtils.formatCents(BeeUtils.round(invoice.getDebt() * 100)));
        table.setWidgetAndStyle(row, INVOICE_DEBT_COL, widget, STYLE_INVOICE_DEBT);
      }

      if (!BeeUtils.isEmpty(invoice.getManager())) {
        widget = new Label(invoice.getManager());
        table.setWidgetAndStyle(row, INVOICE_MANAGER_COL, widget, STYLE_INVOICE_MANAGER);
      }

      table.getRowFormatter().addStyleName(row, STYLE_PREFIX_INVOICE + STYLE_SUFFIX_DATA);
      row++;
    }

    panel.add(table);
    return panel;
  }

  private static Widget renderOrderDetailLabel(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_LABEL);
    return label;
  }

  private static Widget renderOrderDetailValue(String value) {
    CustomDiv widget = new CustomDiv(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_VALUE);
    if (BeeUtils.isEmpty(value)) {
      widget.addStyleName(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_EMPTY);
    } else {
      widget.getElement().setInnerText(value);
    }
    return widget;
  }

  private static Widget renderOrderItemHeader(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_LABEL);
    return label;
  }

  private static Widget renderOrders(List<OrdEcOrder> orders) {
    if (BeeUtils.isEmpty(orders)) {
      return null;
    }

    Flow panel = new Flow(STYLE_PREFIX_ORDER + STYLE_SUFFIX_PANEL);

    Label caption = new Label(Localized.dictionary().ecOrdersSubmitted());
    caption.addStyleName(STYLE_PREFIX_ORDER + STYLE_SUFFIX_CAPTION);
    panel.add(caption);

    HtmlTable table = new HtmlTable(STYLE_PREFIX_ORDER + STYLE_SUFFIX_TABLE);
    int row = 0;

    table.setWidgetAndStyle(row, ORDER_DATE_COL,
        renderOrderHeader(Localized.dictionary().ecOrderDate()),
        STYLE_ORDER_DATE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, ORDER_NUMBER_COL,
        renderOrderHeader(Localized.dictionary().ecOrderNumber()),
        STYLE_ORDER_NUMBER + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_AMOUNT_COL,
        renderOrderHeader(Localized.dictionary().ecOrderAmount()),
        STYLE_ORDER_AMOUNT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_COMMENT_COL,
        renderOrderHeader(Localized.dictionary().comment()),
        STYLE_ORDER_COMMENT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_STATUS_COL,
        renderOrderHeader(Localized.dictionary().ecOrderStatus()),
        STYLE_ORDER_STATUS + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, ORDER_MANAGER_COL,
        renderOrderHeader(Localized.dictionary().ecManager()),
        STYLE_ORDER_MANAGER + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER + STYLE_SUFFIX_HEADER);
    row++;

    Widget widget;

    for (final OrdEcOrder order : orders) {
      if (order.getDate() != null) {
        widget = new Label(TimeUtils.renderCompact(order.getDate()));
        table.setWidgetAndStyle(row, ORDER_DATE_COL, widget, STYLE_ORDER_DATE);
      }

      Label numberWidget = new Label(BeeUtils.toString(order.getOrderId()));
      numberWidget.addClickHandler(event -> openOrder(order));

      table.setWidgetAndStyle(row, ORDER_NUMBER_COL, numberWidget, STYLE_ORDER_NUMBER);

      widget = new Label(EcUtils.formatCents(BeeUtils.round(order.getAmount() * 100)));
      table.setWidgetAndStyle(row, ORDER_AMOUNT_COL, widget, STYLE_ORDER_AMOUNT);

      if (order.getComment() != null) {
        widget = new Label(order.getComment());
        table.setWidgetAndStyle(row, ORDER_COMMENT_COL, widget, STYLE_ORDER_COMMENT);
      }

      OrdersStatus status = OrdersStatus.get(order.getStatus());
      if (status != null) {
        widget = new Label(status.getCaption());
        table.setWidgetAndStyle(row, ORDER_STATUS_COL, widget, STYLE_ORDER_STATUS);
      }

      if (order.getManager() != null) {
        widget = new Label(order.getManager());
        table.setWidgetAndStyle(row, ORDER_MANAGER_COL, widget, STYLE_ORDER_MANAGER);
      }

      table.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER + STYLE_SUFFIX_DATA);
      if (status != null) {
        table.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER + status.name().toLowerCase());
      }

      row++;
    }

    panel.add(table);
    return panel;
  }

  private static Widget renderOrderHeader(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_ORDER + STYLE_SUFFIX_LABEL);
    return label;
  }

  OrdEcFinancialInfo() {
    super();
  }

  @Override
  protected void createUi() {
    clear();
    add(new Image(Global.getImages().loading()));

    BeeKeeper.getRpc().makeRequest(OrdEcKeeper.createArgs(SVC_FINANCIAL_INFORMATION),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            OrdEcKeeper.dispatchMessages(response);
            if (response.hasResponse(OrdEcFinInfo.class)) {
              render(OrdEcFinInfo.restore(response.getResponseAsString()));
            }
          }
        });
  }

  @Override
  protected String getPrimaryStyle() {
    return STYLE_NAME;
  }

  private void render(OrdEcFinInfo finInfo) {
    if (!isEmpty()) {
      clear();
    }

    Widget finWidget = renderFin(finInfo);
    if (finWidget != null) {
      add(finWidget);
    }

    Widget orderWidget = renderOrders(finInfo.getOrders());
    if (orderWidget != null) {
      add(orderWidget);
    }

    Widget invoiceWidget = renderInvoices(finInfo.getInvoices());
    if (invoiceWidget != null) {
      add(invoiceWidget);
    }
  }
}