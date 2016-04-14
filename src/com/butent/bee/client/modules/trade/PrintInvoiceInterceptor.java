package com.butent.bee.client.modules.trade;

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

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
  public void beforeRefresh(FormView form, IsRow row) {
    for (String name : companies.keySet()) {
      Long id = form.getLongValue(name);

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      ClassifierUtils.getCompanyInfo(id, companies.get(name));
    }
    if (invoiceDetails != null) {
      if (BeeUtils.containsSame(form.getFormName(), "short")) {
        summarizeItems(row.getId());
      } else {
        TradeUtils.getDocumentItems(getViewName(), row.getId(),
            form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME), invoiceDetails, null);
      }
    }
    for (Widget total : totals) {
      TradeUtils.getTotalInWords(form.getDoubleValue(COL_TRADE_AMOUNT),
          form.getLongValue(COL_TRADE_CURRENCY), total);
    }
  }

  private void summarizeItems(long id) {
    Relation relation = Relation.create(TBL_ITEMS, Collections.singletonList(COL_ITEM_NAME));
    relation.disableNewRow();

    UnboundSelector selector = UnboundSelector.create(relation);

    Global.inputWidget(Localized.dictionary().itemOrService(), selector, new InputCallback() {
      @Override
      public void onCancel() {
        onSuccess();
      }

      @Override
      public void onSuccess() {
        TradeUtils.getDocumentItems(getViewName(), id,
            getFormView().getStringValue(AdministrationConstants.ALS_CURRENCY_NAME), invoiceDetails,
            data -> {
              SimpleRowSet rs;

              if (selector.getRelatedRow() != null) {
                rs = new SimpleRowSet(data.getColumnNames());

                String[] group = new String[] {
                    COL_CURRENCY, COL_CURRENCY_RATE + COL_CURRENCY,
                    COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC};

                Map<String, Multimap<String, String>> map = new HashMap<>();
                Map<String, Double> amounts = new HashMap<>();

                for (SimpleRowSet.SimpleRow simpleRow : data) {
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
                          for (Element e : XmlUtils.getChildrenElements(xml.getDocumentElement())) {
                            valueMap.put(e.getNodeName(), XmlUtils.getText(e));
                          }
                        }
                        break;

                      default:
                        String viewName = selector.getOracle().getViewName();
                        String value;

                        if (!ArrayUtils.containsSame(group, fld)
                            && !Objects.equals(fld, COL_ITEM_ARTICLE)
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
                for (Map.Entry<String, Multimap<String, String>> entry : map.entrySet()) {
                  SimpleRowSet.SimpleRow newRow = rs.addEmptyRow();
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
              } else {
                rs = data;
              }
              return rs;
            });
      }
    });
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintInvoiceInterceptor();
  }
}
