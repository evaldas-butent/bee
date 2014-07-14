package com.butent.bee.client.modules.trade;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.modules.administration.AdministrationKeeper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Link;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.TradeDocumentData;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TradeDocumentRenderer extends AbstractFormInterceptor {

  private static final String NAME_DOC_TITLE = "DocTitle";
  private static final String NAME_DOC_NUMBER = "DocNumber";
  private static final String NAME_DOC_ID = "DocId";

  private static final String PREFIX_SUPPLIER = "Supplier";
  private static final String PREFIX_CUSTOMER = "Customer";
  private static final String PREFIX_PAYER = "Payer";

  private static final String SUFFIX_NAME = "Name";
  private static final String SUFFIX_CODE = "Code";
  private static final String SUFFIX_VAT_CODE = "VatCode";
  private static final String SUFFIX_ADDRESS = "Address";
  private static final String SUFFIX_PHONE = "Phone";
  private static final String SUFFIX_BANK = "Bank";
  private static final String SUFFIX_WEBSITE = "Website";

  private static Long getCompany(FormView form, IsRow row, String colName, boolean checkDefault) {
    int index = form.getDataIndex(colName);

    if (BeeConst.isUndef(index)) {
      return null;

    } else {
      Long company = row.getLong(index);

      if (company == null && checkDefault) {
        company = AdministrationKeeper.getCompany();
        if (company == null) {
          company = BeeKeeper.getUser().getCompany();
        }
      }
      return company;
    }
  }

  private static String join(String label, String value) {
    if (BeeUtils.isEmpty(value)) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinWords(label, value);
    }
  }

  private static String renderBankInfo(long id, TradeDocumentData tdd) {
    BeeRowSet bankAccounts = tdd.getCompanyBankAccounts(id);

    if (DataUtils.isEmpty(bankAccounts)) {
      return BeeConst.STRING_EMPTY;

    } else {
      List<String> info = new ArrayList<>();

      int accountIndex = bankAccounts.getColumnIndex(COL_BANK_ACCOUNT);
      int nameIndex = bankAccounts.getColumnIndex(ALS_BANK_NAME);
      int codeIndex = bankAccounts.getColumnIndex(COL_BANK_CODE);
      int swiftIndex = bankAccounts.getColumnIndex(COL_SWIFT_CODE);

      for (IsRow row : bankAccounts) {
        info.add(BeeUtils.joinItems(
            join(Localized.getConstants().printBankAccount(), row.getString(accountIndex)),
            row.getString(nameIndex),
            join(Localized.getConstants().printBankCode(), row.getString(codeIndex)),
            join(Localized.getConstants().printBankSwift(), row.getString(swiftIndex))));
      }

      return BeeUtils.buildLines(info);
    }
  }

  private static void renderCompany(long id, TradeDocumentData tdd, String prefix,
      Map<String, Widget> widgets) {

    Widget widget = widgets.get(prefix + SUFFIX_NAME);
    if (widget != null) {
      setHtml(widget, tdd.getCompanyValue(id, COL_COMPANY_NAME));
    }

    widget = widgets.get(prefix + SUFFIX_CODE);
    if (widget != null) {
      setHtml(widget, join(Localized.getConstants().companyCode(),
          tdd.getCompanyValue(id, COL_COMPANY_CODE)));
    }

    widget = widgets.get(prefix + SUFFIX_VAT_CODE);
    if (widget != null) {
      setHtml(widget, join(Localized.getConstants().companyVATCode(),
          tdd.getCompanyValue(id, COL_COMPANY_VAT_CODE)));
    }

    widget = widgets.get(prefix + SUFFIX_ADDRESS);
    if (widget != null) {
      setHtml(widget, BeeUtils.joinItems(tdd.getCompanyValue(id, COL_ADDRESS),
          tdd.getCompanyValue(id, COL_POST_INDEX), tdd.getCompanyValue(id, ALS_CITY_NAME),
          tdd.getCompanyValue(id, ALS_COUNTRY_NAME)));
    }

    widget = widgets.get(prefix + SUFFIX_PHONE);
    if (widget != null) {
      setHtml(widget, BeeUtils.joinItems(
          join(Localized.getConstants().phone(), tdd.getCompanyValue(id, COL_PHONE)),
          join(Localized.getConstants().mobile(), tdd.getCompanyValue(id, COL_MOBILE)),
          join(Localized.getConstants().fax(), tdd.getCompanyValue(id, COL_FAX))));
    }

    widget = widgets.get(prefix + SUFFIX_WEBSITE);
    if (widget != null) {
      String website = tdd.getCompanyValue(id, COL_WEBSITE);

      if (widget instanceof Link) {
        if (!BeeUtils.isEmpty(website)) {
          ((Link) widget).setHref(website);
          ((Link) widget).setText(website);
        }
      } else {
        setHtml(widget, website);
      }
    }

    widget = widgets.get(prefix + SUFFIX_BANK);
    if (widget != null) {
      setHtml(widget, renderBankInfo(id, tdd));
    }
  }

  private static String renderDocId(long id) {
    return join(Localized.getConstants().captionId(), BeeUtils.toString(id));
  }

  private static void setHtml(Widget widget, String html) {
    widget.getElement().setInnerHTML(html);
  }

  private final Map<String, Long> companies = new HashMap<>();

  private final Set<String> currencies = new HashSet<>();

  private final String itemViewName;

  private final String itemRelation;

  public TradeDocumentRenderer(String itemViewName, String itemRelation) {
    this.itemViewName = itemViewName;
    this.itemRelation = itemRelation;
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    setCompanies(form, row);
    setCurrencies(form, row);

    getDocumentData(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeDocumentRenderer(itemViewName, itemRelation);
  }

  private void getDocumentData(FormView form, IsRow row) {
    if (form != null && DataUtils.hasId(row)) {
      final long id = row.getId();

      ParameterList params = TradeKeeper.createArgs(SVC_GET_DOCUMENT_DATA);

      params.addDataItem(Service.VAR_ID, id);

      params.addDataItem(Service.VAR_VIEW_NAME, itemViewName);
      params.addDataItem(Service.VAR_COLUMN, itemRelation);

      if (!companies.isEmpty()) {
        Set<Long> companyIds = new HashSet<>(companies.values());
        params.addDataItem(VIEW_COMPANIES, DataUtils.buildIdList(companyIds));
      }

      if (!currencies.isEmpty()) {
        params.addDataItem(AdministrationConstants.VIEW_CURRENCIES,
            Codec.beeSerialize(currencies));
      }

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasResponse(TradeDocumentData.class)
              && Objects.equals(getActiveRowId(), id)) {
            TradeDocumentData tdd = TradeDocumentData.restore(response.getResponseAsString());
            refresh(tdd);
          }
        }
      });
    }
  }

  private String getString(IsRow row, String colName) {
    int index = getDataIndex(colName);
    return BeeConst.isUndef(index) ? null : row.getString(index);
  }

  private void refresh(TradeDocumentData tdd) {
    FormView form = getFormView();
    IsRow row = getActiveRow();

    if (form == null || row == null) {
      return;
    }

    Map<String, Widget> namedWidgets = form.getNamedWidgets();

    Widget widget = namedWidgets.get(NAME_DOC_TITLE);
    if (widget != null) {
      setHtml(widget, renderDocTitle(row, tdd));
    }

    widget = namedWidgets.get(NAME_DOC_NUMBER);
    if (widget != null) {
      setHtml(widget, renderDocNumber(row));
    }

    widget = namedWidgets.get(NAME_DOC_ID);
    if (widget != null) {
      setHtml(widget, renderDocId(row.getId()));
    }

    Long supplier = companies.get(COL_TRADE_SUPPLIER);
    if (DataUtils.isId(supplier) && tdd.containsCompany(supplier)) {
      renderCompany(supplier, tdd, PREFIX_SUPPLIER, namedWidgets);
    }

    Long customer = companies.get(COL_TRADE_CUSTOMER);
    if (DataUtils.isId(customer) && tdd.containsCompany(customer)) {
      renderCompany(customer, tdd, PREFIX_CUSTOMER, namedWidgets);
    }

    Long payer = companies.get(COL_SALE_PAYER);
    if (DataUtils.isId(payer) && tdd.containsCompany(payer)) {
      renderCompany(payer, tdd, PREFIX_PAYER, namedWidgets);
    }
  }

  private String renderDocNumber(IsRow row) {
    String series = getString(row, COL_TRADE_INVOICE_PREFIX);
    String number = getString(row, COL_TRADE_INVOICE_NO);

    if (BeeUtils.isEmpty(number)) {
      return BeeConst.STRING_EMPTY;
    } else if (BeeUtils.isEmpty(series)) {
      return join(Localized.getConstants().printDocumentNumber(), number);
    } else {
      return Localized.getMessages().printDocumentSeriesAndNumber(series, number);
    }
  }

  private String renderDocTitle(IsRow row, TradeDocumentData tdd) {
    if (BeeUtils.anyEmpty(getString(row, COL_TRADE_INVOICE_PREFIX),
        getString(row, COL_TRADE_INVOICE_NO))) {
      return BeeConst.STRING_EMPTY;

    } else if (BeeUtils.isEmpty(tdd.getCompanyValue(companies.get(COL_TRADE_SUPPLIER),
        COL_COMPANY_VAT_CODE))) {
      return Localized.getConstants().printInvoice();

    } else {
      return Localized.getConstants().printInvoiceVat();
    }
  }

  private void setCompanies(FormView form, IsRow row) {
    if (!companies.isEmpty()) {
      companies.clear();
    }

    if (form != null && row != null) {
      Long supplier = getCompany(form, row, COL_TRADE_SUPPLIER, true);
      if (supplier != null) {
        companies.put(COL_TRADE_SUPPLIER, supplier);
      }

      Long customer = getCompany(form, row, COL_TRADE_CUSTOMER, true);
      if (customer != null) {
        companies.put(COL_TRADE_CUSTOMER, customer);
      }

      Long payer = getCompany(form, row, COL_SALE_PAYER, false);
      if (payer != null) {
        companies.put(COL_SALE_PAYER, payer);
      }
    }
  }

  private void setCurrencies(FormView form, IsRow row) {
    if (!currencies.isEmpty()) {
      currencies.clear();
    }

    if (form != null && row != null) {
      int index = form.getDataIndex(AdministrationConstants.ALS_CURRENCY_NAME);

      if (!BeeConst.isUndef(index)) {
        String currency = row.getString(index);
        if (!BeeUtils.isEmpty(currency)) {
          currencies.add(currency);
        }
      }
    }
  }
}
