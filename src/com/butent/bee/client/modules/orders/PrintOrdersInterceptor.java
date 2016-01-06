package com.butent.bee.client.modules.orders;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.trade.PrintInvoiceInterceptor;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class PrintOrdersInterceptor extends PrintInvoiceInterceptor {

  boolean sendFromOrder;
  OrderForm orderForm;

  public PrintOrdersInterceptor(boolean value, OrderForm orderForm) {
    this.sendFromOrder = value;
    this.orderForm = orderForm;
  }

  public PrintOrdersInterceptor() {
  }

  @Override
  public void afterRefresh(final FormView form, IsRow row) {
    if (sendFromOrder) {
      HeaderView header = form.getViewPresenter().getHeader();
      header.clearCommandPanel();

      header.addCommandItem(new Button(Localized.getConstants().send(), new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          ParameterList params = OrdersKeeper.createSvcArgs(OrdersConstants.SVC_CREATE_PDF_FILE);
          params.addDataItem(MailConstants.COL_CONTENT, form.getElement().getInnerHTML());

          BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

            @Override
            public void onResponse(ResponseObject response) {
              if (!response.hasErrors()) {
                orderForm.sendMail(FileInfo.restore(response.getResponseAsString()));
              }
            }
          });
        }
      }));
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
    if (invoiceDetails != null) {

      final String typeTable = DomUtils.getDataProperty(invoiceDetails.getElement(), "content");

      OrdersKeeper.getDocumentItems(getViewName(), row.getId(),
          form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME), invoiceDetails,
          new Consumer<SimpleRowSet>() {

            @Override
            public void accept(SimpleRowSet data) {
              switch (typeTable) {

                case "InvoiceItems":
                  double qtyTotal = BeeConst.DOUBLE_ZERO;

                  for (SimpleRow sr : data) {
                    Double qty = BeeUtils.unbox(sr.getDouble(COL_TRADE_ITEM_QUANTITY));
                    qtyTotal += qty;
                  }

                  Widget wQty = form.getWidgetByName(COL_TRADE_TOTAL_ITEMS_QUANTITY);
                  if (wQty instanceof Label) {
                    wQty.getElement().setInnerText(BeeUtils.toString(qtyTotal));
                  }

                  break;
              }
            }
          });
    }
    for (Widget total : totals) {
      TradeUtils.getTotalInWords(form.getDoubleValue(COL_TRADE_AMOUNT),
          form.getLongValue(COL_TRADE_CURRENCY), total);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return this;
  }
}
