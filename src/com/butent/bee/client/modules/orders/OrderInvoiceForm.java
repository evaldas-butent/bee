package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class OrderInvoiceForm extends PrintFormInterceptor {

  private Button confirmAction;
  private DataSelector series;

  @Override
  public FormInterceptor getInstance() {
    return new OrderInvoiceForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_SERIES)) {
      series = (DataSelector) widget;
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    int idx = form.getDataIndex(COL_SALE_PROFORMA);
    boolean proforma = !BeeConst.isUndef(idx) && row != null && BeeUtils.unbox(row.getBoolean(idx));

    form.getViewPresenter().getHeader().setCaption(proforma
        ? Localized.dictionary().trProformaInvoice()
        : Localized.dictionary().trdInvoice());

    series.setEnabled(proforma);

    if (confirmAction == null) {
      confirmAction = new Button(Localized.dictionary().trdInvoice(),
          arg0 -> Global.confirm(Localized.dictionary().trConfirmProforma(), () -> {

            if (Data.isNull(VIEW_ORDER_CHILD_INVOICES, row, COL_TRADE_SALE_SERIES)) {
              getFormView().notifySevere(
                  Localized.dictionary().trdInvoicePrefix() + " "
                      + Localized.dictionary().valueRequired());
              return;
            }

            Queries.update(getViewName(), IdFilter.compareId(getActiveRowId()),
                COL_SALE_PROFORMA, BooleanValue.getNullValue(), new IntCallback() {
                  @Override
                  public void onSuccess(Integer result) {
                    if (BeeUtils.isPositive(result)) {
                      Data.onViewChange(getViewName(), DataChangeEvent.CANCEL_RESET_REFRESH);
                    }
                  }
                });
          }));
      header.addCommandItem(confirmAction);
    }
    confirmAction.setVisible(proforma && form.isEnabled());
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    Queries.getRowSet(VIEW_SALE_ITEMS, null, Filter.equals(COL_SALE, getActiveRowId()),
        Order.ascending(ClassifierConstants.COL_ITEM_KPN_CODE), new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            dataConsumer.accept(new BeeRowSet[] {result});
          }
        });
  }

  @Override
  protected ReportUtils.ReportCallback getReportCallback() {
    return new ReportUtils.ReportCallback() {
      @Override
      public void accept(FileInfo fileInfo) {
        sendInvoice(fileInfo);
      }

      @Override
      public FontAwesome getIcon() {
        return FontAwesome.ENVELOPE_O;
      }
    };
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();
    companies.put(COL_CUSTOMER, getLongValue(COL_CUSTOMER));
    companies.put(TradeConstants.COL_TRADE_SUPPLIER, BeeKeeper.getUser().getCompany());

    super.getReportParameters(defaultParameters ->
        ClassifierUtils.getCompaniesInfo(companies, companiesInfo -> {
          defaultParameters.putAll(companiesInfo);
          Queries.getRowSet(VIEW_DISTINCT_ORDER_VALUES, Arrays.asList(COL_SERIES_NAME, COL_NUMBER),
              Filter.equals(COL_SALE, getActiveRowId()),
              new Queries.RowSetCallback() {
                @Override
                public void onSuccess(BeeRowSet result) {
                  List<String> orderNr = new ArrayList<>();
                  for (BeeRow row : result) {
                    orderNr.add(BeeUtils.joinWords(row.getString(0), row.getString(1)));
                  }
                  defaultParameters.put(COL_ORDER_NO, BeeUtils.join(",", orderNr));
                  parametersConsumer.accept(defaultParameters);
                }
              });
        }));
  }

  @Override
  protected void print(String report) {
    if (Objects.equals("PrintInvoice_ru", report)) {
      report = "PrintInvoiceRU_ru";
    }
    super.print(report);
  }

  private String getFileName() {
    IsRow row = getActiveRow();

    String invoicePrefix =
        row.getString(Data.getColumnIndex(VIEW_ORDER_CHILD_INVOICES, COL_TRADE_INVOICE_PREFIX));
    String number =
        row.getString(Data.getColumnIndex(VIEW_ORDER_CHILD_INVOICES, COL_TRADE_INVOICE_NO));

    String name;
    if (!BeeUtils.isEmpty(invoicePrefix)) {
      name = BeeUtils.join("_", invoicePrefix, number);
    } else {
      name = "bee_invoice";
    }

    return name + ".pdf";
  }

  private void sendInvoice(FileInfo fileInfo) {
    FormView form = getFormView();
    fileInfo.setCaption(getFileName());

    List<FileInfo> attach = new ArrayList<>();
    attach.add(fileInfo);

    NewMailMessage.create(BeeUtils.notEmpty(form.getStringValue(ALS_PAYER_EMAIL),
        form.getStringValue(ALS_CUSTOMER_EMAIL)), null, null, attach, null);
  }
}

