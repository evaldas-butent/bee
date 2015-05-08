package com.butent.bee.client.modules.transport;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

public class PrintInvoiceInterceptor extends AbstractFormInterceptor {

  Map<String, Widget> companies = new HashMap<>();
  HtmlTable invoiceDetails;
  List<Widget> totals = new ArrayList<>();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.inListSame(name, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER)) {
      companies.put(name, widget.asWidget());

    } else if (BeeUtils.same(name, "InvoiceDetails") && widget instanceof HtmlTable) {
      invoiceDetails = (HtmlTable) widget;

    } else if (BeeUtils.startsSame(name, "TotalInWords")) {
      totals.add(widget.asWidget());
    }
  }

  @Override
  public void beforeRefresh(final FormView form, final IsRow row) {
    for (String name : companies.keySet()) {
      Long id = form.getLongValue(name);

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      ClassifierUtils.getCompanyInfo(id, companies.get(name));
    }
    if (invoiceDetails != null) {
      if (BeeUtils.containsSame(form.getFormName(), "short")) {
        Relation relation = Relation.create(TBL_ITEMS, Arrays.asList(COL_ITEM_NAME));
        relation.disableNewRow();

        final UnboundSelector selector = UnboundSelector.create(relation);
        final CheckBox enableGrouping = new CheckBox(Localized.getConstants().primaryOnly());
        enableGrouping.setChecked(false);

        Flow flow = new Flow(StyleUtils.NAME_FLEX_BOX_VERTICAL);
        flow.add(selector);
        flow.add(enableGrouping);

        Global.inputWidget(Localized.getConstants().itemOrService(), flow,
            new InputCallback() {
              @Override
              public void onCancel() {
                onSuccess();
              }

              @Override
              public void onSuccess() {
                TradeUtils.getDocumentItems(getViewName(), row.getId(),
                    form.getStringValue(ALS_CURRENCY_NAME), invoiceDetails,
                    new Function<SimpleRowSet, SimpleRowSet>() {
                      @Override
                      public SimpleRowSet apply(SimpleRowSet data) {
                        SimpleRowSet rs;

                        if (selector.getRelatedRow() != null) {
                          rs = new SimpleRowSet(data.getColumnNames());

                          String[] group = new String[] {
                              COL_CURRENCY, COL_CURRENCY_RATE + COL_CURRENCY,
                              COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC};

                          Map<String, Multimap<String, String>> map = new HashMap<>();
                          Map<String, Double> amounts = new HashMap<>();

                          for (SimpleRow simpleRow : data) {
                            String key = "";

                            for (String fld : group) {
                              if (data.hasColumn(fld)) {
                                key += fld + simpleRow.getValue(fld);
                              }
                            }
                            if (!map.containsKey(key)) {
                              Multimap<String, String> m = TreeMultimap.create();
                              map.put(key, m);
                            }
                            Multimap<String, String> valueMap = map.get(key);
                            double qty = 0;
                            double prc = 0;

                            for (String fld : data.getColumnNames()) {
                              switch (fld) {
                                case COL_TRADE_ITEM_QUANTITY:
                                  qty = BeeUtils.unbox(simpleRow.getDouble(fld));
                                  break;

                                case COL_TRADE_ITEM_PRICE:
                                  prc = BeeUtils.unbox(simpleRow.getDouble(fld));
                                  break;

                                case COL_TRADE_ITEM_NOTE:
                                  Document xml = null;

                                  try {
                                    xml = XMLParser.parse(simpleRow.getValue(COL_TRADE_ITEM_NOTE));
                                  } catch (DOMParseException ex) {
                                    xml = null;
                                  }
                                  if (xml != null) {
                                    boolean primary = BeeUtils.isEmpty(XmlUtils
                                        .getChildrenText(xml.getDocumentElement(),
                                            "Parent" + COL_ASSESSMENT));

                                    for (Element el : XmlUtils
                                        .getChildrenElements(xml.getDocumentElement())) {
                                      String name = el.getNodeName();

                                      if (primary || !BeeUtils.inListSame(name, COL_ASSESSMENT,
                                          COL_NUMBER, COL_ORDER_NOTES)) {
                                        valueMap.put(name, XmlUtils.getText(el));
                                      }
                                    }
                                  }
                                  break;

                                default:
                                  String viewName = selector.getOracle().getViewName();
                                  String value;

                                  if (!ArrayUtils.containsSame(group, fld)
                                      && Data.containsColumn(viewName, fld)) {
                                    value = Data.getString(viewName, selector.getRelatedRow(), fld);
                                  } else {
                                    value = simpleRow.getValue(fld);
                                  }
                                  if (!BeeUtils.isEmpty(value)) {
                                    valueMap.put(fld, value);
                                  }
                                  break;
                              }
                            }
                            amounts.put(key, BeeUtils.unbox(amounts.get(key)) + qty * prc);
                          }
                          for (Entry<String, Multimap<String, String>> entry : map.entrySet()) {
                            SimpleRow newRow = rs.addEmptyRow();
                            Multimap<String, String> values = entry.getValue();
                            Map<String, String> info = new HashMap<>();

                            for (String fld : values.keySet()) {
                              if (data.hasColumn(fld)) {
                                newRow.setValue(fld, BeeUtils.joinItems(values.get(fld)));
                              } else {
                                info.put(fld, BeeUtils.joinItems(values.get(fld)));
                              }
                            }
                            if (!BeeUtils.isEmpty(info)) {
                              newRow.setValue(COL_TRADE_ITEM_NOTE,
                                  XmlUtils.createString("CargoInfo", info));
                            }
                            newRow.setValue(COL_TRADE_ITEM_QUANTITY, "1");
                            newRow.setValue(COL_TRADE_ITEM_PRICE,
                                BeeUtils.toString(amounts.get(entry.getKey()), 2));
                          }
                        } else if (enableGrouping.isChecked()) {
                          rs = groupByPrimaryAssessements(data);
                        } else {
                          rs = data;
                        }
                        collectWidgetInfo(form, rs);
                        return rs;
                      }
                    });
              }
            });
      } else {
        TradeUtils.getDocumentItems(getViewName(), row.getId(),
            form.getStringValue(ALS_CURRENCY_NAME), invoiceDetails,
            new Function<SimpleRowSet, SimpleRowSet>() {
              @Override
              public SimpleRowSet apply(SimpleRowSet data) {
                collectWidgetInfo(form, data);
                return null;
              }
            });
      }
    }
    for (Widget total : totals) {
      TradeUtils.getTotalInWords(form.getDoubleValue(COL_TRADE_AMOUNT),
          form.getStringValue(ALS_CURRENCY_NAME),
          form.getStringValue("MinorName"), total);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintInvoiceInterceptor();
  }

  private static void collectWidgetInfo(FormView form, SimpleRowSet rs) {
    final Map<String, Pair<Widget, String>> widgets = new HashMap<>();

    for (String name : new String[] {"Documents", COL_VEHICLE, COL_LOADING_PLACE,
        COL_UNLOADING_PLACE, COL_ASSESSMENT, COL_NUMBER}) {
      widgets.put(name, Pair.of(form.getWidgetByName(name), (String) null));
    }
    for (SimpleRow simpleRow : rs) {
      Document xml;

      try {
        xml = XMLParser.parse(simpleRow.getValue(COL_TRADE_ITEM_NOTE));
      } catch (DOMParseException ex) {
        xml = null;
      }
      if (xml != null) {
        for (Entry<String, Pair<Widget, String>> entry : widgets.entrySet()) {
          Widget widget = entry.getValue().getA();

          if (widget != null) {
            entry.getValue().setB(BeeUtils.joinItems(entry.getValue().getB(),
                BeeUtils.joinItems(XmlUtils.getChildrenText(xml
                    .getDocumentElement(), entry.getKey()))));
          }
        }
      }
    }
    Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();

    for (Pair<Widget, String> pair : widgets.values()) {
      if (pair.getA() != null) {
        pair.getA().getElement().setInnerText(BeeUtils
            .joinItems(new TreeSet<>(splitter.splitToList(pair.getB()))));
      }
    }
  }

  private static SimpleRowSet groupByPrimaryAssessements(SimpleRowSet data) {
    return data;
  }
}
