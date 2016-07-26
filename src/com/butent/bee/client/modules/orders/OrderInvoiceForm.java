package com.butent.bee.client.modules.orders;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.modules.mail.MailKeeper;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.mail.AccountInfo;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
      confirmAction = new Button(Localized.dictionary().trdInvoice(), new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          Global.confirm(Localized.dictionary().trConfirmProforma(), new ConfirmationCallback() {
            @Override
            public void onConfirm() {

              if (Data.isNull(VIEW_ORDERS_INVOICES, row, COL_TRADE_SALE_SERIES)) {
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
            }
          });
        }
      });
      header.addCommandItem(confirmAction);
    }
    confirmAction.setVisible(proforma && form.isEnabled());
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new PrintOrdersInterceptor();
  }

  @Override
  protected Consumer<FileInfo> getReportCallback() {
    return new Consumer<FileInfo>() {

      @Override
      public void accept(FileInfo input) {
        sendInvoice(input);
      }
    };
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    Queries.getRowSet(VIEW_SALE_ITEMS, null, Filter.equals(COL_SALE, getActiveRowId()),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            ParameterList params = OrdersKeeper.createSvcArgs(SVC_ITEMS_INFO);
            params.addDataItem("view_name", getViewName());
            params.addDataItem("id", getActiveRowId());

            BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

              @Override
              public void onResponse(ResponseObject response) {
                SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());
                Map<Long, String> supTerm = new HashMap<>();

                if (rs.getNumberOfRows() > 0) {
                  for (SimpleRow row : rs) {
                    String term;
                    double reserved =
                        BeeUtils.unbox(row.getDouble(OrdersConstants.COL_RESERVED_REMAINDER));
                    double totRem =
                        BeeUtils.unbox(row.getDouble(PRP_FREE_REMAINDER));

                    if (reserved == 0 && totRem == 0) {
                      if (row.getDate(COL_SUPPLIER_TERM) == null) {
                        DateTime date = row.getDateTime(ProjectConstants.COL_DATES_START_DATE);
                        int weekDay = date.getDow();

                        if (weekDay < 3) {
                          term = new JustDate(date.getDate().getDays() + 9 - weekDay).toString();
                        } else {
                          term = new JustDate(date.getDate().getDays() + 16 - weekDay).toString();
                        }
                      } else {
                        term = row.getDate(COL_SUPPLIER_TERM).toString();
                      }
                    } else {
                      term = "Sandėlyje";
                    }

                    supTerm.put(row.getLong("ItemID"), term);
                  }

                  for (BeeRow r : result) {
                    r.setProperty(PRP_SUPPLIER_TERM, supTerm.get(r.getLong(Data.getColumnIndex(
                        VIEW_SALE_ITEMS, COL_ITEM))));
                  }
                }
                dataConsumer.accept(new BeeRowSet[] {result});
              }
            });
          }
        });
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();

    for (String col : Arrays.asList(COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER)) {
      Long id = getLongValue(col);

      if (DataUtils.isId(id)) {
        companies.put(col, id);
      }
    }
    if (!companies.containsKey(COL_TRADE_SUPPLIER)) {
      companies.put(COL_TRADE_SUPPLIER, BeeKeeper.getUser().getCompany());
    }
    super.getReportParameters((defaultParameters) -> {
      Queries.getRowSet(ClassifierConstants.VIEW_COMPANIES, null, Filter.idIn(companies.values()),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              for (BeeRow row : result) {
                for (Map.Entry<String, Long> entry : companies.entrySet()) {
                  if (Objects.equals(row.getId(), entry.getValue())) {
                    for (BeeColumn column : result.getColumns()) {
                      String value = DataUtils.getString(result, row, column.getId());

                      if (!BeeUtils.isEmpty(value)) {
                        defaultParameters.put(entry.getKey() + column.getId(), value);
                      }
                    }
                  }
                }
              }

              Queries.getRowSet(TBL_COMPANY_BANK_ACCOUNTS, Arrays.asList(COL_BANK_ACCOUNT,
                  ALS_BANK_NAME, COL_BANK_CODE, COL_SWIFT_CODE), Filter.equals(COL_COMPANY,
                  companies.get(COL_TRADE_SUPPLIER)), new Queries.RowSetCallback() {

                @Override
                public void onSuccess(BeeRowSet rowSet) {
                  String banks = "";
                  for (BeeRow row : rowSet) {
                    banks +=
                        BeeUtils.joinWords(row.getString(0), row.getString(1), row.getString(2),
                            row.getString(3)) + "\n";
                  }

                  if (!BeeUtils.isEmpty(banks)) {
                    defaultParameters.put(COL_TRADE_SUPPLIER + COL_BANK, banks);
                  }

                  Queries.getRowCount(VIEW_SALE_ITEMS,
                      Filter.and(Filter.equals(COL_SALE, getActiveRowId()),
                          Filter.notNull(COL_TRADE_DISCOUNT)), new IntCallback() {
                        @Override
                        public void onSuccess(Integer result) {
                          defaultParameters.put(COL_TRADE_DISCOUNT, result.toString());
                          parametersConsumer.accept(defaultParameters);
                        }
                      });
                }
              });
            }
          });
    });
  }

  private String getFileName() {
    IsRow row = getActiveRow();

    String invoicePrefix =
        row.getString(Data.getColumnIndex(OrdersConstants.VIEW_ORDERS_INVOICES,
            COL_TRADE_INVOICE_PREFIX));
    String number =
        row.getString(Data.getColumnIndex(OrdersConstants.VIEW_ORDERS_INVOICES,
            COL_TRADE_INVOICE_NO));

    String name;
    if (!BeeUtils.isEmpty(invoicePrefix)) {
      name = BeeUtils.join("_", invoicePrefix, number);
    } else {
      name = "bee_order";
    }

    return name + ".pdf";
  }

  private void sendInvoice(FileInfo fileInfo) {
    FormView form = getFormView();
    fileInfo.setCaption(getFileName());

    String addr = form.getStringValue(ALS_PAYER_EMAIL);

    if (addr == null) {
      addr = form.getStringValue(ALS_CUSTOMER_EMAIL);
    }
    final Set<String> to = new HashSet<>();

    if (addr != null) {
      to.add(addr);
    }

    MailKeeper.getAccounts(new BiConsumer<List<AccountInfo>, AccountInfo>() {

      @Override
      public void accept(List<AccountInfo> availableAccounts, AccountInfo defaultAccount) {
        if (!BeeUtils.isEmpty(availableAccounts)) {

          List<FileInfo> attach = new ArrayList<>();
          attach.add(fileInfo);

          NewMailMessage.create(availableAccounts, defaultAccount, to, null, null, null, null,
              attach, null, false);
        } else {
          BeeKeeper.getScreen().notifyWarning(Localized.dictionary().mailNoAccountsFound());
        }
      }
    });
  }
}
