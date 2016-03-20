package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_CUSTOMER;

import com.butent.bee.client.Global;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.modules.trade.InvoiceForm;
import com.butent.bee.client.modules.trade.PrintInvoiceInterceptor;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.Collections;

public class CargoInvoiceForm extends InvoiceForm implements ClickHandler {

  private Button confirmAction;

  public CargoInvoiceForm() {
    super(null);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    int idx = form.getDataIndex(COL_SALE_PROFORMA);
    boolean proforma = !BeeConst.isUndef(idx) && row != null && BeeUtils.unbox(row.getBoolean(idx));

    form.getViewPresenter().getHeader().setCaption(proforma
        ? Localized.dictionary().trProformaInvoice()
        : Localized.dictionary().trdInvoice());

    if (confirmAction == null) {
      confirmAction = new Button(Localized.dictionary().trdInvoice(), this);
      form.getViewPresenter().getHeader().addCommandItem(confirmAction);
    }
    confirmAction.setVisible(proforma && form.isEnabled());
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoInvoiceForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new PrintInvoiceInterceptor() {
      @Override
      public void onLoad(FormView form) {
        HeaderView header = form.getViewPresenter().getHeader();

        if (header != null) {
          FaLabel mail = new FaLabel(FontAwesome.ENVELOPE_O);

          mail.addClickHandler((clickEvent) -> {
            Long id = form.getActiveRowId();

            ReportUtils.getPdf(form.getPrintElement().getString(), (fileInfo) -> {
              String invoice = BeeUtils.join("", form.getStringValue(COL_TRADE_INVOICE_PREFIX),
                  form.getStringValue(COL_TRADE_INVOICE_NO));

              if (!BeeUtils.isEmpty(invoice)) {
                fileInfo.setCaption(invoice + ".pdf");
              }
              Queries.getValue(VIEW_COMPANIES, BeeUtils.unbox(form.getLongValue(COL_CUSTOMER)),
                  COL_EMAIL, new RpcCallback<String>() {
                    @Override
                    public void onSuccess(String email) {
                      NewMailMessage.create(email, invoice, Localized.dictionary().trdInvoice(),
                          Collections.singleton(fileInfo), (messageId, saveMode) -> {
                            DataInfo info = Data.getDataInfo(VIEW_SALE_FILES);

                            Queries.insert(info.getViewName(),
                                Arrays.asList(info.getColumn(COL_SALE),
                                    info.getColumn(AdministrationConstants.COL_FILE)),
                                Arrays.asList(BeeUtils.toString(id),
                                    BeeUtils.toString(fileInfo.getId())), null,
                                new RowInsertCallback(info.getViewName()) {
                                  @Override
                                  public void onSuccess(BeeRow result) {
                                    Data.onTableChange(info.getTableName(),
                                        DataChangeEvent.RESET_REFRESH);
                                    super.onSuccess(result);
                                  }
                                });

                          });
                    }
                  });
            });
          });
          header.addCommandItem(mail);
        }
        super.afterCreate(form);
      }
    };
  }

  @Override
  public void onClick(ClickEvent event) {
    Global.confirm(Localized.dictionary().trConfirmProforma(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
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
}
