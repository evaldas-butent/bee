package com.butent.bee.client.modules.transport;

import com.google.common.base.Splitter;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
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
  public void beforeRefresh(FormView form, IsRow row) {
    for (String name : companies.keySet()) {
      Long id = form.getLongValue(name);

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      ClassifierUtils.getCompanyInfo(id, companies.get(name));
    }
    if (invoiceDetails != null) {
      TradeUtils.getDocumentItems(getViewName(), row.getId(),
          form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME), invoiceDetails);
    }
    for (Widget total : totals) {
      TradeUtils.getTotalInWords(form.getDoubleValue(COL_TRADE_AMOUNT),
          form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME),
          form.getStringValue("MinorName"), total);
    }
    final Map<String, Pair<Widget, String>> widgets = new HashMap<>();

    for (String name : new String[] {TransportConstants.COL_VEHICLE,
        TransportConstants.COL_LOADING_PLACE, TransportConstants.COL_UNLOADING_PLACE,
        TransportConstants.COL_ASSESSMENT, TransportConstants.COL_NUMBER, "Documents"}) {
      widgets.put(name, Pair.of(form.getWidgetByName(name), (String) null));
    }
    Queries.getRowSet(TBL_SALE_ITEMS, Arrays.asList(COL_TRADE_ITEM_NOTE),
        Filter.equals(COL_SALE, row.getId()), new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            for (BeeRow beeRow : result) {
              Document xml;

              try {
                xml = XMLParser.parse(beeRow.getString(0));
              } catch (DOMParseException ex) {
                xml = null;
              }
              if (xml != null) {
                for (Entry<String, Pair<Widget, String>> entry : widgets.entrySet()) {
                  Widget widget = entry.getValue().getA();

                  if (widget != null) {
                    entry.getValue().setB(BeeUtils.joinItems(entry.getValue().getB(),
                        BeeUtils.joinItems(XmlUtils
                            .getChildrenText(xml.getDocumentElement(), entry.getKey()))));
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
        });
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintInvoiceInterceptor();
  }
}
