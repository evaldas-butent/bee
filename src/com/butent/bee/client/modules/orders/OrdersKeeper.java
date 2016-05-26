package com.butent.bee.client.modules.orders;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.ClientDefaults;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.trade.InvoicesGrid;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.Link;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Client-side projects module handler.
 */
public final class OrdersKeeper {

  private static final String STYLE_ITEMS = TradeKeeper.STYLE_PREFIX + "itemsInfo-";
  private static final String STYLE_ITEMS_TABLE = STYLE_ITEMS + "table";
  private static final String STYLE_ITEMS_HEADER = STYLE_ITEMS + "header";
  private static final String STYLE_ITEMS_DATA = STYLE_ITEMS + "data";
  private static final String STYLE_ITEMS_FOOTER = STYLE_ITEMS + "footer";

  private static final String COL_NAME = "Name";
  private static final String COL_ORDINAL = "Ordinal";
  private static final String COL_TOTAL = "Total";
  private static final String COL_RATE_AMOUNT = COL_CURRENCY_RATE + COL_TRADE_AMOUNT;
  private static final String COL_RATE_VAT = COL_CURRENCY_RATE + COL_TRADE_VAT;
  private static final String COL_RATE_TOTAL = COL_CURRENCY_RATE + COL_TOTAL;
  private static final String COL_RATE_CURRENCY = COL_CURRENCY_RATE + COL_CURRENCY;

  public static ParameterList createSvcArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.ORDERS, method);
  }

  /**
   * Register orders client-side module handler.
   */
  public static void register() {
    FormFactory.registerFormInterceptor(COL_ORDER, new OrderForm());
    FormFactory.registerFormInterceptor("OrderInvoice", new OrderInvoiceForm());

    GridFactory.registerGridInterceptor(VIEW_ORDER_SALES, new OrderInvoiceBuilder());
    GridFactory.registerGridInterceptor(VIEW_ORDERS_INVOICES, new InvoicesGrid());
    GridFactory.registerGridInterceptor(VIEW_ORDER_TMPL_ITEMS, new OrderTmplItemsGrid());
    GridFactory.registerGridInterceptor(VIEW_ORDERS, new OrdersGrid());

    SelectorEvent.register(new OrdersSelectorHandler());
  }

  private OrdersKeeper() {

  }

  public static void getDocumentItems(final String viewName, long tradeId, String currencyName,
      final HtmlTable table, final Consumer<SimpleRowSet> consumer) {

    Assert.notNull(table);

    if (BeeUtils.isEmpty(viewName) || !DataUtils.isId(tradeId)) {
      return;
    }
    ParameterList args = createSvcArgs(SVC_ITEMS_INFO);
    args.addDataItem("view_name", viewName);
    args.addDataItem("id", tradeId);

    String currencyTo = DomUtils.getDataProperty(table.getElement(), COL_RATE_CURRENCY);

    if (!BeeUtils.isEmpty(currencyTo)
        && !BeeUtils.same(currencyName, ClientDefaults.getCurrencyName())) {
      currencyTo = ClientDefaults.getCurrencyName();
    }
    final boolean rateExists = !BeeUtils.isEmpty(currencyTo);

    if (rateExists) {
      args.addDataItem(COL_CURRENCY, currencyTo);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        if (table.getRowCount() == 0) {
          return;
        }
        int headerRowCount = Math.max(table.getRowCount() - 1, 1);
        boolean footer = table.getRowCount() > 1;

        table.addStyleName(STYLE_ITEMS_TABLE);

        NumberFormat formater = NumberFormat.getFormat("0.00");

        String currency = null;
        double qtyTotal = 0;
        double vatTotal = 0;
        double sumTotal = 0;
        String rateCurrency = null;
        double rate = 0;
        double currVatTotal = 0;
        double currSumTotal = 0;

        SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());
        int ordinal = 0;

        if (rs.getNumberOfRows() > 0) {
          consumer.accept(rs);
        }

        for (SimpleRow row : rs) {
          ordinal++;
          if (BeeUtils.isEmpty(currency)) {
            currency = row.getValue(COL_CURRENCY);
          }
          double qty = BeeUtils.unbox(row.getDouble(COL_TRADE_ITEM_QUANTITY));
          qtyTotal += qty;
          double price = BeeUtils.unbox(row.getDouble(COL_TRADE_ITEM_PRICE));
          double discount = BeeConst.DOUBLE_ZERO;

          if (BeeUtils.same(viewName, VIEW_ORDERS)) {
            discount = BeeUtils.unbox(row.getDouble(COL_TRADE_DISCOUNT));
            formater = NumberFormat.getFormat("0.0000");
          }

          double sum = qty * price - discount * qty * price / 100;
          double currSum = 0;
          double vat = BeeUtils.unbox(row.getDouble(COL_TRADE_VAT));
          boolean vatInPercents = BeeUtils.unbox(row.getBoolean(COL_TRADE_VAT_PERC));
          double reserved =
              BeeUtils.unbox(row.getDouble(OrdersConstants.COL_RESERVED_REMAINDER));
          double totRem =
              BeeUtils.unbox(row.getDouble(PRP_FREE_REMAINDER));

          if (BeeUtils.unbox(row.getBoolean(COL_TRADE_VAT_PLUS))) {
            if (vatInPercents) {
              vat = sum / 100 * vat;
            }
          } else {
            if (vatInPercents) {
              vat = sum - sum / (1 + vat / 100);
            }
            sum -= vat;
          }
          sum = BeeUtils.round(sum, 2);
          sumTotal += sum;
          vatTotal += vat;

          if (rateExists) {
            if (BeeUtils.isEmpty(rateCurrency)) {
              rateCurrency = row.getValue(COL_RATE_CURRENCY);
              rate = BeeUtils.unbox(row.getDouble(COL_CURRENCY_RATE));
            }
            currSum = BeeUtils.round(sum * rate, 2);
            currSumTotal += currSum;
            currVatTotal += vat * rate;
          }
          Document xml = null;

          for (int i = 0; i < headerRowCount; i++) {
            int x = table.insertRow(table.getRowCount() - (footer ? 1 : 0));
            table.getRow(x).setInnerHTML(table.getRow(i).getInnerHTML());
            table.getRowFormatter().addStyleName(x, STYLE_ITEMS_DATA);

            for (int j = 0; j < table.getCellCount(x); j++) {
              Multimap<String, Element> elements = getNamedElements(table.getCellFormatter()
                  .getElement(x, j).getFirstChildElement());

              for (String fld : elements.keySet()) {
                String value;

                if (BeeUtils.same(fld, COL_TRADE_ITEM_QUANTITY)) {
                  value = BeeUtils.toString(qty);

                } else if (BeeUtils.same(fld, COL_TRADE_ITEM_PRICE)) {
                  value = formater.format(sum / qty);

                } else if (BeeUtils.same(fld, COL_TRADE_VAT)) {
                  value = row.getValue(fld);

                  if (value != null && vatInPercents) {
                    value = BeeUtils.removeTrailingZeros(value) + "%";
                  }
                } else if (BeeUtils.same(fld, COL_TRADE_AMOUNT)) {
                  value = formater.format(sum);

                } else if (BeeUtils.same(fld, COL_RATE_AMOUNT)) {
                  value = formater.format(currSum);

                } else if (BeeUtils.same(fld, COL_ORDINAL)) {
                  value = BeeUtils.toString(ordinal);

                } else if (BeeUtils.same(fld, COL_CURRENCY)) {
                  value = currency;

                } else if (BeeUtils.same(fld, COL_RATE_CURRENCY)) {
                  value = rateCurrency;

                } else if (BeeUtils.same(fld, COL_CURRENCY_RATE)) {
                  value = BeeUtils.toString(rate, 7);

                } else if (BeeUtils.same(fld, COL_TRADE_PRICE_WITH_VAT)) {
                  value = formater.format((sum + vat) / qty);

                } else if (BeeUtils.same(fld, COL_TRADE_AMAOUNT_WITH_VAT)) {
                  value = formater.format(sum + vat);

                } else if (BeeUtils.same(fld, COL_RESERVED_REMAINDER)) {
                  value = formater.format(reserved);

                } else if (BeeUtils.same(fld, COL_SUPPLIER_TERM)) {
                  if (reserved == 0 && reserved - totRem == 0) {
                    if (row.getDate(COL_SUPPLIER_TERM) == null) {
                      DateTime date = row.getDateTime(ProjectConstants.COL_DATES_START_DATE);
                      int weekDay = date.getDow();

                      if (weekDay < 4) {
                        value = new JustDate(date.getDate().getDays() + 9 - weekDay).toString();
                      } else {
                        value = new JustDate(date.getDate().getDays() + 16 - weekDay).toString();
                      }
                    } else {
                      value = row.getDate(COL_SUPPLIER_TERM).toString();
                    }
                  } else {
                    value = "Sandėlyje";
                  }

                } else if (!rs.hasColumn(fld)) {

                  if (xml == null) {
                    try {
                      xml = XMLParser.parse(row.getValue(COL_TRADE_ITEM_NOTE));
                    } catch (DOMParseException ex) {
                      xml = null;
                    }
                  }
                  if (xml != null) {
                    value = BeeUtils.joinItems(XmlUtils.getChildrenText(xml.getDocumentElement(),
                        fld));
                  } else {
                    value = null;
                  }
                } else {
                  value = row.getValue(fld);
                }
                for (Element element : elements.get(fld)) {
                  element.addClassName(STYLE_ITEMS + fld);

                  if (BeeUtils.same(fld, ClassifierConstants.COL_ITEM_LINK)) {
                    String itemLink = row.getValue(ClassifierConstants.COL_ITEM_LINK);
                    if (!BeeUtils.isEmpty(itemLink)) {
                      Link link = new Link("WWW", itemLink);
                      element.setInnerHTML(link.asWidget().toString());

                    } else {
                      element.setInnerText("");
                    }
                  } else {
                    element.setInnerText(value);
                  }
                }
              }
            }
          }
        }
        for (int i = 0; i < headerRowCount; i++) {
          for (int j = 0; j < table.getCellCount(i); j++) {
            Multimap<String, Element> elements = getNamedElements(table.getCellFormatter()
                .getElement(i, j).getFirstChildElement());

            for (String fld : elements.keySet()) {
              for (Element element : elements.get(fld)) {
                element.addClassName(STYLE_ITEMS + fld);
              }
            }
          }
          table.getRowFormatter().addStyleName(i, STYLE_ITEMS_HEADER);
        }
        if (footer) {
          vatTotal = BeeUtils.round(vatTotal, 2);
          currVatTotal = BeeUtils.round(currVatTotal, 2);

          int x = table.getRowCount() - 1;

          for (int j = 0; j < table.getCellCount(x); j++) {
            Multimap<String, Element> elements = getNamedElements(table.getCellFormatter()
                .getElement(x, j).getFirstChildElement());

            for (String fld : elements.keySet()) {
              String value;

              if (BeeUtils.same(fld, COL_TRADE_ITEM_QUANTITY + COL_TOTAL)) {
                value = BeeUtils.removeTrailingZeros(BeeUtils.toString(qtyTotal));

              } else if (BeeUtils.same(fld, COL_TRADE_AMOUNT + COL_TOTAL)) {
                value = formater.format(sumTotal);

              } else if (BeeUtils.same(fld, COL_TRADE_VAT + COL_TOTAL)) {
                value = formater.format(vatTotal);

              } else if (BeeUtils.same(fld, COL_TOTAL)) {
                value = formater.format(sumTotal + vatTotal);

              } else if (BeeUtils.same(fld, COL_CURRENCY)) {
                value = currency;

              } else if (BeeUtils.same(fld, COL_RATE_AMOUNT + COL_TOTAL)) {
                value = formater.format(currSumTotal);

              } else if (BeeUtils.same(fld, COL_RATE_VAT + COL_TOTAL)) {
                value = formater.format(currVatTotal);

              } else if (BeeUtils.same(fld, COL_RATE_TOTAL)) {
                value = formater.format(currSumTotal + currVatTotal);

              } else if (BeeUtils.same(fld, COL_RATE_CURRENCY)) {
                value = rateCurrency;

              } else if (BeeUtils.same(fld, COL_CURRENCY_RATE)) {
                value = BeeUtils.toString(rate, 7);

              } else if (BeeUtils.same(fld, COL_TRADE_TOTAL_LTL)) {
                value = formater.format((sumTotal + vatTotal) * 3.4528);

              } else {
                value = null;
              }
              for (Element element : elements.get(fld)) {
                element.addClassName(STYLE_ITEMS + fld);
                element.setInnerText(value);
              }
            }
          }
          table.getRowFormatter().addStyleName(x, STYLE_ITEMS_FOOTER);
        }
      }
    });
  }

  private static Multimap<String, Element> getNamedElements(Element element) {

    Multimap<String, Element> elements = HashMultimap.create();
    String name = DomUtils.getDataProperty(element, COL_NAME);

    if (!BeeUtils.isEmpty(name)) {
      elements.put(name, element);
    } else {
      NodeList<Element> children = DomUtils.getChildren(element);

      if (children != null) {
        for (int i = 0; i < children.getLength(); i++) {
          elements.putAll(getNamedElements(children.getItem(i)));
        }
      }
    }
    return elements;
  }
}