package com.butent.bee.client.modules.transport;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintInvoiceInterceptor extends AbstractFormInterceptor {

  Map<String, Widget> companies = new HashMap<>();
  List<HtmlTable> invoiceDetails = new ArrayList<>();
  List<Widget> totals = new ArrayList<>();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils
        .inListSame(name, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER,
            ClassifierConstants.COL_COMPANY)) {
      companies.put(name, widget.asWidget());

    } else if (BeeUtils.startsSame(name, "InvoiceDetails") && widget instanceof HtmlTable) {
      invoiceDetails.add((HtmlTable) widget.asWidget());

    } else if (BeeUtils.startsSame(name, "TotalInWords")) {
      totals.add(widget.asWidget());
    }
  }

  @Override
  public void beforeRefresh(final FormView form, IsRow row) {

    for (String name : companies.keySet()) {
      Long id = form.getLongValue(name);

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      ClassifierUtils.getCompanyInfo(id, companies.get(name));
    }
    for (HtmlTable invoiceDetail : invoiceDetails) {
      final String typeTable = DomUtils.getDataProperty(invoiceDetail.getElement(), "content");

      TradeUtils.getDocumentItems(getViewName(), row.getId(),
          form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME), invoiceDetail,
          new Consumer<SimpleRowSet>() {
            @Override
            public void accept(SimpleRowSet data) {
              switch (typeTable) {

                case "TradeActItems":

                  double totSvor = BeeConst.DOUBLE_ZERO;
                  double totArea = BeeConst.DOUBLE_ZERO;
                  double totRem = BeeConst.DOUBLE_ZERO;
                  double qtyTotal = BeeConst.DOUBLE_ZERO;

                  for (SimpleRow simpleRow : data) {
                    double qty = BeeUtils.unbox(simpleRow.getDouble(COL_TRADE_ITEM_QUANTITY));
                    double ret = BeeUtils.unbox(simpleRow.getDouble(COL_TA_RETURNED_QTY));
                    double rem = qty - ret;
                    simpleRow.setValue(COL_TRADE_ITEM_NOTE, "<root><rem>"
                        + rem + "</rem></root>");
                    double sv = BeeUtils.unbox(simpleRow.getDouble(COL_TRADE_WEIGHT));
                    double area =
                        BeeUtils.unbox(simpleRow.getDouble(ClassifierConstants.COL_ITEM_AREA));
                    double rowSv = BeeUtils.round(rem * sv, 3);
                    double rowArea = BeeUtils.round(rem * area, 3);

                    totArea += rowArea;
                    totSvor += rowSv;
                    totRem += rem;
                    qtyTotal += qty;
                  }

                  Widget ww = form.getWidgetByName(COL_TRADE_TOTAL_WEIGHT);
                  Widget wa = form.getWidgetByName(COL_TRADE_TOTAL_AREA);
                  Widget wRem = form.getWidgetByName(COL_TRADE_TOTAL_REMAINING);
                  Widget wQty = form.getWidgetByName(COL_TRADE_TOTAL_ITEMS_QUANTITY);

                  if (ww instanceof Label) {
                    ww.getElement().setInnerText(BeeUtils.toString(totSvor, 3));
                  }
                  if (wa instanceof Label) {
                    wa.getElement().setInnerText(BeeUtils.toString(totArea, 3));
                  }
                  if (wRem instanceof Label) {
                    wRem.getElement().setInnerText(BeeUtils.toString(totRem, 3));
                  }
                  if (wQty instanceof Label) {
                    wQty.getElement().setInnerText(BeeUtils.toString(qtyTotal, 3));
                  }

                  break;

                case "TradeActServices":

                  for (SimpleRow simpleRow : data) {
                    int ind = BeeUtils.unbox(simpleRow.getInt(COL_TRADE_TIME_UNIT));
                    simpleRow.setValue(COL_TRADE_ITEM_NOTE, "<root><tu>"
                        + EnumUtils.getCaption("TradeActTimeUnit", ind) + "</tu></root>");
                  }
              }
            }
          });
    }
    for (Widget total : totals) {
      TradeUtils.getTotalInWords(form.getDoubleValue(COL_TRADE_AMOUNT),
          form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME),
          form.getStringValue("MinorName"), total);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintInvoiceInterceptor();
  }
}
