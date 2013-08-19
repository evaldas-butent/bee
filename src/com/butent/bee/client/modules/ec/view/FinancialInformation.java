package com.butent.bee.client.modules.ec.view;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.modules.ec.widget.ItemPicture;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.modules.ec.EcFinInfo;
import com.butent.bee.shared.modules.ec.EcInvoice;
import com.butent.bee.shared.modules.ec.EcOrder;
import com.butent.bee.shared.modules.ec.EcOrderItem;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

class FinancialInformation extends EcView {

  private static final String STYLE_NAME = "finInfo";

  private static final String STYLE_PREFIX_FIN = EcStyles.name("finInfo", "fin-");
  private static final String STYLE_PREFIX_ORDER = EcStyles.name("finInfo", "order-");
  private static final String STYLE_PREFIX_INVOICE = EcStyles.name("finInfo", "invoice-");

  private static final String STYLE_PREFIX_ORDER_DETAILS = STYLE_PREFIX_ORDER + "details-";
  private static final String STYLE_PREFIX_ORDER_ITEM = STYLE_PREFIX_ORDER + "item-";

  private static final String STYLE_SUFFIX_TABLE = "table";
  private static final String STYLE_SUFFIX_HEADER = "header";
  private static final String STYLE_SUFFIX_DATA = "data";

  private static final String STYLE_SUFFIX_PANEL = "panel";
  private static final String STYLE_SUFFIX_CAPTION = "caption";
  private static final String STYLE_SUFFIX_LABEL = "label";
  private static final String STYLE_SUFFIX_VALUE = "value";

  private static final String STYLE_ORDER_DATE = STYLE_PREFIX_ORDER + "date";
  private static final String STYLE_ORDER_NUMBER = STYLE_PREFIX_ORDER + "number";
  private static final String STYLE_ORDER_AMOUNT = STYLE_PREFIX_ORDER + "amount";
  private static final String STYLE_ORDER_COMMENT = STYLE_PREFIX_ORDER + "comment";
  private static final String STYLE_ORDER_DELIVERY_METHOD = STYLE_PREFIX_ORDER + "dm";
  private static final String STYLE_ORDER_DELIVERY_ADDRESS = STYLE_PREFIX_ORDER + "da";
  private static final String STYLE_ORDER_STATUS = STYLE_PREFIX_ORDER + "status";
  private static final String STYLE_ORDER_MANAGER = STYLE_PREFIX_ORDER + "maneger";

  private static final String STYLE_INVOICE_NUMBER = STYLE_PREFIX_INVOICE + "number";
  private static final String STYLE_INVOICE_DATE = STYLE_PREFIX_INVOICE + "date";
  private static final String STYLE_INVOICE_TERM = STYLE_PREFIX_INVOICE + "termr";
  private static final String STYLE_INVOICE_INDULGENCE = STYLE_PREFIX_INVOICE + "indulgence";
  private static final String STYLE_INVOICE_OVERDUE = STYLE_PREFIX_INVOICE + "overdue";
  private static final String STYLE_INVOICE_AMOUNT = STYLE_PREFIX_INVOICE + "amount";
  private static final String STYLE_INVOICE_DEBT = STYLE_PREFIX_INVOICE + "debt";

  private static final String STYLE_ORDER_ITEM_PICTURE = STYLE_PREFIX_ORDER_ITEM + "picture";
  private static final String STYLE_ORDER_ITEM_NAME = STYLE_PREFIX_ORDER_ITEM + "name";
  private static final String STYLE_ORDER_ITEM_CODE = STYLE_PREFIX_ORDER_ITEM + "code";
  private static final String STYLE_ORDER_ITEM_QUANTITY = STYLE_PREFIX_ORDER_ITEM + "quantity";
  private static final String STYLE_ORDER_ITEM_PRICE = STYLE_PREFIX_ORDER_ITEM + "price";

  private static final int ORDER_DATE_COL = 0;
  private static final int ORDER_NUMBER_COL = 1;
  private static final int ORDER_AMOUNT_COL = 2;
  private static final int ORDER_COMMENT_COL = 3;
  private static final int ORDER_DELIVERY_METHOD_COL = 4;
  private static final int ORDER_DELIVERY_ADDRESS_COL = 5;
  private static final int ORDER_STATUS_COL = 6;
  private static final int ORDER_MANAGER_COL = 7;

  private static final int INVOICE_NUMBER_COL = 0;
  private static final int INVOICE_DATE_COL = 1;
  private static final int INVOICE_TERM_COL = 2;
  private static final int INVOICE_INDULGENCE_COL = 3;
  private static final int INVOICE_OVERDUE_COL = 4;
  private static final int INVOICE_AMOUNT_COL = 5;
  private static final int INVOICE_DEBT_COL = 6;

  private static final int ORDER_ITEM_PICTURE_COL = 0;
  private static final int ORDER_ITEM_NAME_COL = 1;
  private static final int ORDER_ITEM_CODE_COL = 2;
  private static final int ORDER_ITEM_QUANTITY_COL = 3;
  private static final int ORDER_ITEM_PRICE_COL = 4;

  private static void openOrder(EcOrder order) {
    Flow panel = new Flow(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_PANEL);

    HtmlTable orderTable = new HtmlTable(STYLE_PREFIX_ORDER_DETAILS + STYLE_SUFFIX_TABLE);
    int row = 0;
    int col = 0;

    String stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "date";
    Widget label = renderOrderDetailLabel(Localized.getConstants().ecOrderSubmissionDate());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    Widget value = renderOrderDetailValue(TimeUtils.renderCompact(order.getDate()));
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "number";
    label = renderOrderDetailLabel(Localized.getConstants().ecOrderNumber());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(BeeUtils.toString(order.getOrderId()));
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "dm";
    label = renderOrderDetailLabel(Localized.getConstants().ecDeliveryMethod());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(order.getDeliveryMethod());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "da";
    label = renderOrderDetailLabel(Localized.getConstants().ecDeliveryAddress());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(order.getDeliveryAddress());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "status";
    label = renderOrderDetailLabel(Localized.getConstants().ecOrderStatus());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    EcOrderStatus status = NameUtils.getEnumByIndex(EcOrderStatus.class, order.getStatus());
    value = renderOrderDetailValue((status == null) ? null : status.getCaption());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "manager";
    label = renderOrderDetailLabel(Localized.getConstants().ecManager());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(order.getManager());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "amount";
    label = renderOrderDetailLabel(BeeUtils.joinWords(Localized.getConstants().ecOrderAmount(),
        EcConstants.CURRENCY));
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(EcUtils.renderCents(BeeUtils.round(order.getAmount() * 100)));
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "weight";
    label = renderOrderDetailLabel(BeeUtils.joinWords(Localized.getConstants().weight(), "kg"));
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(BeeUtils.toString(order.getWeight()));
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "comment";
    label = renderOrderDetailLabel(Localized.getConstants().comment());
    orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderOrderDetailValue(order.getComment());
    orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);
    
    if (EcOrderStatus.REJECTED == status) {
      stylePrefix = STYLE_PREFIX_ORDER_DETAILS + "rr";
      label = renderOrderDetailLabel(Localized.getConstants().ecRejectionReason());
      orderTable.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

      value = renderOrderDetailValue(order.getRejectionReason());
      orderTable.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);
    }

    panel.add(orderTable);
    
    Label itemCaption = new Label(Localized.getConstants().ecOrderItems());
    itemCaption.addStyleName(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_CAPTION);
    panel.add(itemCaption);

    HtmlTable itemTable = new HtmlTable(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_TABLE);
    row = 0;
    
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_NAME_COL,
        renderOrderItemHeader(Localized.getConstants().ecItemName()),
        STYLE_ORDER_ITEM_NAME + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_CODE_COL,
        renderOrderItemHeader(Localized.getConstants().ecItemCode()),
        STYLE_ORDER_ITEM_CODE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    itemTable.setWidgetAndStyle(row, ORDER_ITEM_QUANTITY_COL,
        renderOrderItemHeader(Localized.getConstants().quantity()),
        STYLE_ORDER_ITEM_QUANTITY + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    itemTable.setWidgetAndStyle(row, ORDER_ITEM_PRICE_COL,
        renderOrderItemHeader(Localized.getConstants().price()),
        STYLE_ORDER_ITEM_PRICE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    
    itemTable.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_HEADER);
    row++;

    Multimap<Long, ItemPicture> pictureWidgets = ArrayListMultimap.create();
    Widget widget;
    
    for (EcOrderItem item : order.getItems()) {
      ItemPicture pictureWidget = new ItemPicture();
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_PICTURE_COL, pictureWidget,
          STYLE_ORDER_ITEM_PICTURE);
      pictureWidgets.put(item.getArticleId(), pictureWidget);
      
      if (item.getName() != null) {
        widget = new Label(item.getName());
        itemTable.setWidgetAndStyle(row, ORDER_ITEM_NAME_COL, widget, STYLE_ORDER_ITEM_NAME);
      }

      if (item.getCode() != null) {
        widget = new Label(item.getCode());
        itemTable.setWidgetAndStyle(row, ORDER_ITEM_CODE_COL, widget, STYLE_ORDER_ITEM_CODE);
      }

      widget = new Label(BeeUtils.toString(BeeUtils.unbox(item.getQuantity())));
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_QUANTITY_COL, widget, STYLE_ORDER_ITEM_QUANTITY);

      int cents = BeeUtils.round(BeeUtils.unbox(item.getPrice()) * 100);
      widget = new Label(EcUtils.renderCents(cents));
      itemTable.setWidgetAndStyle(row, ORDER_ITEM_PRICE_COL, widget, STYLE_ORDER_ITEM_PRICE);
    
      itemTable.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_DATA);
      row++;
    }
    
    if (!pictureWidgets.isEmpty()) {
      EcKeeper.setBackgroundPictures(pictureWidgets);
    }
    
    panel.add(itemTable);
    
    DialogBox dialog = DialogBox.create(Localized.getConstants().ecOrder(),
        STYLE_PREFIX_ORDER_DETAILS + "dialog");
    dialog.setWidget(panel);

    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);
    dialog.center();
  }

  private static Widget renderFin(EcFinInfo finInfo) {
    if (finInfo == null) {
      return null;
    }

    HtmlTable table = new HtmlTable(STYLE_PREFIX_FIN + STYLE_SUFFIX_TABLE);
    int row = 0;
    int col = 0;

    String stylePrefix = STYLE_PREFIX_FIN + "limit";
    Widget label = renderFinLabel(Localized.getConstants().ecCreditLimit());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    Widget value = renderFinAmount(finInfo.getCreditLimit());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "days";
    label = renderFinLabel(Localized.getConstants().ecDaysForPayment());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = new CustomDiv(STYLE_PREFIX_FIN + STYLE_SUFFIX_VALUE);
    value.getElement().setInnerText(BeeUtils.toString(BeeUtils.unbox(finInfo.getDaysForPayment())));
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_FIN + "orders";
    label = renderFinLabel(Localized.getConstants().ecTotalOrdered());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    double total = 0;
    for (EcOrder order : finInfo.getOrders()) {
      if (EcOrderStatus.in(order.getStatus(), EcOrderStatus.NEW, EcOrderStatus.ACTIVE)) {
        total += order.getAmount();
      }
    }

    value = renderFinAmount(total);
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "taken";
    label = renderFinLabel(Localized.getConstants().ecTotalTaken());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getTotalTaken());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_FIN + "debt";
    label = renderFinLabel(Localized.getConstants().ecDebt());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getDebt());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "maxed";
    label = renderFinLabel(Localized.getConstants().ecMaxedOut());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getMaxedOut());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    return table;
  }

  private static Widget renderFinAmount(Double amount) {
    CustomDiv widget = new CustomDiv(STYLE_PREFIX_FIN + STYLE_SUFFIX_VALUE);

    int cents = (amount == null) ? 0 : BeeUtils.round(amount * 100);
    String text = (cents == 0) ? BeeConst.STRING_ZERO
        : BeeUtils.joinWords(EcUtils.renderCents(cents), EcConstants.CURRENCY);

    widget.setText(text);
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

  private static Widget renderInvoices(List<EcInvoice> invoices) {
    if (BeeUtils.isEmpty(invoices)) {
      return null;
    }

    Flow panel = new Flow(STYLE_PREFIX_INVOICE + STYLE_SUFFIX_PANEL);

    Label caption = new Label(Localized.getConstants().ecInvoices());
    caption.addStyleName(STYLE_PREFIX_INVOICE + STYLE_SUFFIX_CAPTION);
    panel.add(caption);

    HtmlTable table = new HtmlTable(STYLE_PREFIX_INVOICE + STYLE_SUFFIX_TABLE);
    int row = 0;

    table.setWidgetAndStyle(row, INVOICE_NUMBER_COL,
        renderInvoiceHeader(Localized.getConstants().ecInvoiceNumber()),
        STYLE_INVOICE_NUMBER + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, INVOICE_DATE_COL,
        renderInvoiceHeader(Localized.getConstants().ecInvoiceDate()),
        STYLE_INVOICE_DATE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_TERM_COL,
        renderInvoiceHeader(Localized.getConstants().ecInvoiceTerm()),
        STYLE_INVOICE_TERM + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, INVOICE_INDULGENCE_COL,
        renderInvoiceHeader(Localized.getConstants().ecInvoiceIndulgence()),
        STYLE_INVOICE_INDULGENCE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, INVOICE_OVERDUE_COL,
        renderInvoiceHeader(Localized.getConstants().ecInvoiceOverdue()),
        STYLE_INVOICE_OVERDUE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, INVOICE_AMOUNT_COL,
        renderInvoiceHeader(BeeUtils.joinWords(Localized.getConstants().ecInvoiceAmount(),
            EcConstants.CURRENCY)),
        STYLE_INVOICE_AMOUNT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, INVOICE_DEBT_COL,
        renderInvoiceHeader(BeeUtils.joinWords(Localized.getConstants().ecInvoiceDebt(),
            EcConstants.CURRENCY)),
        STYLE_INVOICE_DEBT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.getRowFormatter().addStyleName(row, STYLE_PREFIX_INVOICE + STYLE_SUFFIX_HEADER);
    row++;

    Widget widget;

    for (EcInvoice invoice : invoices) {
      if (invoice.getNumber() != null) {
        widget = new Label(invoice.getNumber());
        table.setWidgetAndStyle(row, INVOICE_NUMBER_COL, widget, STYLE_INVOICE_NUMBER);
      }

      if (invoice.getDate() != null) {
        widget = new Label(TimeUtils.renderCompact(invoice.getDate()));
        table.setWidgetAndStyle(row, INVOICE_DATE_COL, widget, STYLE_INVOICE_DATE);
      }

      if (invoice.getTerm() != null) {
        widget = new Label(TimeUtils.renderCompact(invoice.getTerm()));
        table.setWidgetAndStyle(row, INVOICE_TERM_COL, widget, STYLE_INVOICE_TERM);

        if (invoice.getDate() != null) {
          int indulgence = TimeUtils.dayDiff(invoice.getDate(), invoice.getTerm());
          widget = new Label(BeeUtils.toString(indulgence));
          table.setWidgetAndStyle(row, INVOICE_INDULGENCE_COL, widget, STYLE_INVOICE_INDULGENCE);
        }

        if (BeeUtils.isPositive(invoice.getDebt())) {
          int overdue = TimeUtils.dayDiff(TimeUtils.today(), invoice.getTerm());
          widget = new Label((overdue > 0) ? (BeeConst.STRING_PLUS + overdue)
              : BeeUtils.toString(overdue));
          table.setWidgetAndStyle(row, INVOICE_OVERDUE_COL, widget, STYLE_INVOICE_OVERDUE);
        }
      }

      if (invoice.getAmount() != null) {
        widget = new Label(EcUtils.renderCents(BeeUtils.round(invoice.getAmount() * 100)));
        table.setWidgetAndStyle(row, INVOICE_AMOUNT_COL, widget, STYLE_INVOICE_AMOUNT);
      }

      widget = new Label();
      if (invoice.getDebt() != null) {
        String text = EcUtils.renderCents(BeeUtils.round(invoice.getDebt() * 100));
        widget.getElement().setInnerText(text);
      }
      table.setWidgetAndStyle(row, INVOICE_DEBT_COL, widget, STYLE_INVOICE_DEBT);

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
    if (value != null) {
      widget.setHTML(value);
    }
    return widget;
  }

  private static Widget renderOrderHeader(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_ORDER + STYLE_SUFFIX_LABEL);
    return label;
  }

  private static Widget renderOrderItemHeader(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_ORDER_ITEM + STYLE_SUFFIX_LABEL);
    return label;
  }
  
  private static Widget renderOrders(List<EcOrder> orders) {
    if (BeeUtils.isEmpty(orders)) {
      return null;
    }

    Flow panel = new Flow(STYLE_PREFIX_ORDER + STYLE_SUFFIX_PANEL);

    Label caption = new Label(Localized.getConstants().ecOrdersSubmitted());
    caption.addStyleName(STYLE_PREFIX_ORDER + STYLE_SUFFIX_CAPTION);
    panel.add(caption);

    HtmlTable table = new HtmlTable(STYLE_PREFIX_ORDER + STYLE_SUFFIX_TABLE);
    int row = 0;

    table.setWidgetAndStyle(row, ORDER_DATE_COL,
        renderOrderHeader(Localized.getConstants().ecOrderDate()),
        STYLE_ORDER_DATE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, ORDER_NUMBER_COL,
        renderOrderHeader(Localized.getConstants().ecOrderNumber()),
        STYLE_ORDER_NUMBER + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_AMOUNT_COL,
        renderOrderHeader(BeeUtils.joinWords(Localized.getConstants().ecOrderAmount(),
            EcConstants.CURRENCY)),
        STYLE_ORDER_AMOUNT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_COMMENT_COL,
        renderOrderHeader(Localized.getConstants().comment()),
        STYLE_ORDER_COMMENT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_DELIVERY_METHOD_COL,
        renderOrderHeader(Localized.getConstants().ecDeliveryMethod()),
        STYLE_ORDER_DELIVERY_METHOD + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, ORDER_DELIVERY_ADDRESS_COL,
        renderOrderHeader(Localized.getConstants().ecDeliveryAddress()),
        STYLE_ORDER_DELIVERY_ADDRESS + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_STATUS_COL,
        renderOrderHeader(Localized.getConstants().ecOrderStatus()),
        STYLE_ORDER_STATUS + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);
    table.setWidgetAndStyle(row, ORDER_MANAGER_COL,
        renderOrderHeader(Localized.getConstants().ecManager()),
        STYLE_ORDER_MANAGER + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER + STYLE_SUFFIX_HEADER);
    row++;

    Widget widget;

    for (final EcOrder order : orders) {
      if (order.getDate() != null) {
        widget = new Label(TimeUtils.renderCompact(order.getDate()));
        table.setWidgetAndStyle(row, ORDER_DATE_COL, widget, STYLE_ORDER_DATE);
      }

      Label numberWidget = new Label(BeeUtils.toString(order.getOrderId()));
      numberWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          openOrder(order);
        }
      });

      table.setWidgetAndStyle(row, ORDER_NUMBER_COL, numberWidget, STYLE_ORDER_NUMBER);

      widget = new Label(EcUtils.renderCents(BeeUtils.round(order.getAmount() * 100)));
      table.setWidgetAndStyle(row, ORDER_AMOUNT_COL, widget, STYLE_ORDER_AMOUNT);

      if (order.getComment() != null) {
        widget = new Label(order.getComment());
        table.setWidgetAndStyle(row, ORDER_COMMENT_COL, widget, STYLE_ORDER_COMMENT);
      }

      if (order.getDeliveryMethod() != null) {
        widget = new Label(order.getDeliveryMethod());
        table.setWidgetAndStyle(row, ORDER_DELIVERY_METHOD_COL, widget,
            STYLE_ORDER_DELIVERY_METHOD);
      }
      if (order.getDeliveryAddress() != null) {
        widget = new Label(order.getDeliveryAddress());
        table.setWidgetAndStyle(row, ORDER_DELIVERY_ADDRESS_COL, widget,
            STYLE_ORDER_DELIVERY_ADDRESS);
      }

      EcOrderStatus status = NameUtils.getEnumByIndex(EcOrderStatus.class, order.getStatus());
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

  FinancialInformation() {
    super();
  }

  @Override
  protected void createUi() {
    BeeKeeper.getRpc().makeRequest(EcKeeper.createArgs(EcConstants.SVC_FINANCIAL_INFORMATION),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            EcKeeper.dispatchMessages(response);
            if (response.hasResponse(EcFinInfo.class)) {
              render(EcFinInfo.restore(response.getResponseAsString()));
            }
          }
        });
  }

  @Override
  protected String getPrimaryStyle() {
    return STYLE_NAME;
  }

  private void render(EcFinInfo finInfo) {
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
