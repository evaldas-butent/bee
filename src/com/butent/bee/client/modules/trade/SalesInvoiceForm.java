package com.butent.bee.client.modules.trade;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.documents.DocumentConstants.COL_DOCUMENT_DATE;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.classifiers.CompanyForm;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.modules.trade.acts.TradeActKeeper;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class SalesInvoiceForm extends PrintFormInterceptor {

  private String legacyReports;

  SalesInvoiceForm() {
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {

    if (BeeUtils.same(editableWidget.getColumnId(), COL_TA_DATE)
        && widget instanceof InputDateTime) {

      ((InputDateTime) widget).addEditStopHandler(event ->
          setTerm(((InputDateTime) event.getSource()).getDate()));

      ((InputDateTime) widget).addValueChangeHandler(valueChangeEvent ->
          setTerm(TimeUtils.parseDate(valueChangeEvent.getValue(),
              BeeKeeper.getUser().getDateTimeFormatInfo().dateOrdering())));
    }
    if (BeeUtils.inList(editableWidget.getColumnId(), COL_CUSTOMER, COL_PAYER)
        && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isChanged()) {
          afterRefresh(getFormView(), getActiveRow());
        }
      });
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    CompanyForm.refreshCreditInfo(form, BeeUtils.nvl(row.getLong(form.getDataIndex(COL_PAYER)),
        row.getLong(form.getDataIndex(COL_CUSTOMER))));
    super.afterRefresh(form, row);
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (BeeUtils.inListSame(name, COL_SALE_PAYER, COL_TRADE_SUPPLIER)) {
      description.setAttribute("editEnabled",
          BeeUtils.toString(!TradeActKeeper.isClientArea()).toLowerCase());
    } else if (BeeUtils.same(name, COL_TRADE_CUSTOMER)) {
      description.setAttribute("editForm", TradeActKeeper.isClientArea()
          ? FORM_NEW_COMPANY : FORM_COMPANY);
    }

    return super.beforeCreateWidget(name, description);
  }

  @Override
  public FormInterceptor getInstance() {
    return new SalesInvoiceForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new TradeDocumentRenderer(VIEW_SALE_ITEMS, COL_SALE) {
      @Override
      public void onLoad(FormView form) {
        TradeActKeeper.ensureSendMailPrintableForm(form,
            Global.getParameterText(PRM_INVOICE_MAIL_SIGNATURE));
        super.afterCreate(form);
      }
    };
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    return super.isRowEditable(row) && isEditable(row.isNull(getDataIndex(COL_TRADE_EXPORTED)));
  }

  @Override
  public void onLoad(FormView form) {
    super.onLoad(form);

    String reports = "reports";
    this.legacyReports = form.getProperties().get(reports);

    FaLabel print = new FaLabel(FontAwesome.PRINT);

    print.addClickHandler(clickEvent -> {
      if (EventUtils.hasModifierKey(clickEvent)) {
        form.getProperties().put(reports, legacyReports);
      } else {
        form.getProperties().remove(reports);
      }
      form.getViewPresenter().handleAction(Action.PRINT);
    });
    getHeaderView().addCommandItem(print);
  }

  @Override
  protected ReportUtils.ReportCallback getReportCallback() {
    return new ReportUtils.ReportCallback() {
      @Override
      public Widget getActionWidget() {
        FaLabel action = new FaLabel(FontAwesome.ENVELOPE_O);
        action.setTitle(Localized.dictionary().trWriteEmail());
        return action;
      }

      @Override
      public void accept(FileInfo fileInfo) {
        FormView form = getFormView();
        Long id = getActiveRowId();

        String invoice = BeeUtils.same(form.getViewName(), VIEW_SALES)
            ? BeeUtils.join("_", Localized.dictionary().trdInvoice(),
            BeeUtils.join("", form.getStringValue(COL_TRADE_INVOICE_PREFIX),
                form.getStringValue(COL_TRADE_INVOICE_NO)))
            : BeeUtils.join("_", form.getCaption(), form.getActiveRowId());

        if (!BeeUtils.isEmpty(invoice)) {
          fileInfo.setCaption(invoice + ".pdf");
        }

        Long companyId = form.getLongValue(COL_COMPANY);

        if (BeeUtils.same(form.getViewName(), VIEW_SALES)) {
          companyId = form.getLongValue(COL_CUSTOMER);
        }

        String content = BeeUtils.same(form.getViewName(), VIEW_SALES)
            ? ""
            : Localized.dictionary().tradeAct();

        TradeActUtils.getInvoiceEmails(companyId, emails ->
            TradeActUtils.getTradeActEmails(Collections.singleton(id), cc ->
                NewMailMessage.create(emails, cc, null, invoice, content,
                    Collections.singleton(fileInfo), null, false, (messageId, saveMode) -> {
                      if (!BeeUtils.same(form.getViewName(), VIEW_SALES)) {
                        return;
                      }
                      Queries.insert(VIEW_SALE_FILES,
                          Data.getColumns(VIEW_SALE_FILES, Arrays.asList(COL_SALE,
                              AdministrationConstants.COL_FILE, COL_NOTES,
                              MailConstants.COL_MESSAGE)),
                          Queries.asList(id, fileInfo.getId(),
                              Format.renderDateTime(TimeUtils.nowMinutes()), messageId),
                          null, new RowInsertCallback(VIEW_SALE_FILES) {
                            @Override
                            public void onSuccess(BeeRow result) {
                              Data.refreshLocal(VIEW_SALE_FILES);
                              form.updateCell(COL_SENT_TO_EMAIL, BeeConst.STRING_TRUE);
                              form.refreshBySource(COL_SENT_TO_EMAIL);
                              form.getViewPresenter().handleAction(Action.SAVE);
                              super.onSuccess(result);
                            }
                          });
                    }, Global.getParameterText(PRM_INVOICE_MAIL_SIGNATURE))));
      }
    };
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    long sale = getActiveRowId();

    Queries.getRowSet(VIEW_SALE_ITEMS, null, Filter.equals(COL_SALE, sale),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet saleItems) {
            Filter filter = Filter.equals(COL_SALE, sale);

            Map<String, Filter> filters = new HashMap<>();
            filters.put(TradeActConstants.VIEW_TRADE_ACT_ITEMS, filter);
            filters.put(TradeActConstants.VIEW_TRADE_ACT_INVOICES, filter);

            Queries.getData(Arrays.asList(TradeActConstants.VIEW_TRADE_ACT_ITEMS,
                TradeActConstants.VIEW_TRADE_ACT_INVOICES), filters, CachingPolicy.NONE,
                new Queries.DataCallback() {
                  @Override
                  public void onSuccess(Collection<BeeRowSet> result) {
                    for (BeeRowSet rSet : result) {
                      if (!DataUtils.isEmpty(rSet)) {

                        if (Objects.equals(TradeActConstants.VIEW_TRADE_ACT_ITEMS,
                            rSet.getViewName())) {

                          fillSaleProperties(false, rSet, saleItems);

                        } else {
                          fillSaleProperties(true, rSet, saleItems);
                        }
                      }
                    }

                    dataConsumer.accept(new BeeRowSet[] {saleItems});
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

    super.getReportParameters(defaultParameters ->
        ClassifierUtils.getCompaniesInfo(companies, companiesInfo -> {
          defaultParameters.putAll(companiesInfo);
          Long saleId = getActiveRowId();

          DateTime t = new DateTime();
          t.setMillis(0);
          t.setSecond(0);

          defaultParameters.put("PrintingDate", Format.renderDateTime(t));
          parametersConsumer.accept(defaultParameters);

          if (!DataUtils.isId(saleId)) {
            return;
          }

          Queries.getRowSet(VIEW_TRADE_ACT_INVOICES, Collections.singletonList("ContractNumber"),
              Filter.equals(COL_SALE, saleId), new Order(COL_DOCUMENT_DATE, false),
              new Queries.RowSetCallback() {
                @Override
                public void onSuccess(BeeRowSet contracts) {
                  String contractNumber = BeeConst.STRING_EMPTY;

                  for (BeeRow contract : contracts) {
                    contractNumber = contract.getString(contracts.getColumnIndex("ContractNumber"));

                    if (!BeeUtils.isEmpty(contractNumber)) {
                      break;
                    }
                  }

                  defaultParameters.put(TradeActConstants.WIDGET_TA_CONTRACT, contractNumber);
                }
              });
        }));
  }

  static boolean isEditable(boolean notExported) {
    return BeeKeeper.getUser().isAdministrator() || notExported;
  }

  private void setTerm(JustDate date) {
    InputDate term = (InputDate) getFormView().getWidgetBySource(COL_TRADE_TERM);
    int creditDays = BeeUtils.unbox(Data.getInteger(VIEW_SALES, getActiveRow(),
        COL_COMPANY_CREDIT_DAYS));

    if (term != null && creditDays > 0) {
      TimeUtils.addDay(date, creditDays);
      getActiveRow().setValue(Data.getColumnIndex(VIEW_SALES, COL_TRADE_TERM), date);
      getFormView().refreshBySource(COL_TRADE_TERM);
    }
  }

  private static void fillSaleProperties(boolean forAllSaleItems, BeeRowSet rowSet,
      BeeRowSet saleItems) {
    int tradeSeriesIdx = rowSet.getColumnIndex("Trade" + COL_SERIES_NAME);
    int tradeNumberIdx = rowSet.getColumnIndex("Trade" + COL_TRADE_NUMBER);

    String tradeAct;

    if (forAllSaleItems) {
      for (BeeRow saleItem : saleItems) {
        BeeRow row = rowSet.findRow(rowSet.getColumnIndex("SaleItem"), saleItem.getId());
        if (row != null) {
          tradeAct =
              BeeUtils.joinWords(row.getString(tradeSeriesIdx), row.getString(tradeNumberIdx));
          saleItem.setProperty(TradeActConstants.COL_TRADE_ACT, tradeAct);
        }

        saleItem.setProperty(COL_ITEM_IS_SERVICE, COL_ITEM_IS_SERVICE);
      }
    } else {
      BeeRow row = rowSet.getRow(0);
      tradeAct = BeeUtils.joinWords(row.getString(tradeSeriesIdx), row.getString(tradeNumberIdx));

      for (BeeRow saleItem : saleItems) {
        saleItem.setProperty(TradeActConstants.COL_TRADE_ACT, tradeAct);
      }
    }
  }
}
