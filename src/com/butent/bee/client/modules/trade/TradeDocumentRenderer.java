package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.modules.administration.AdministrationKeeper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TradeDocumentRenderer extends AbstractFormInterceptor {

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

  private void getDocumentData(FormView form, IsRow row) {
    if (form != null && row != null) {
      ParameterList params = TradeKeeper.createArgs(SVC_GET_DOCUMENT_DATA);

      params.addDataItem(Service.VAR_ID, row.getId());

      params.addDataItem(Service.VAR_VIEW_NAME, itemViewName);
      params.addDataItem(Service.VAR_COLUMN, itemRelation);

      if (!companies.isEmpty()) {
        Set<Long> companyIds = new HashSet<>(companies.values());
        params.addDataItem(ClassifierConstants.VIEW_COMPANIES,
            DataUtils.buildIdList(companyIds));
      }

      if (!currencies.isEmpty()) {
        params.addDataItem(AdministrationConstants.VIEW_CURRENCIES,
            Codec.beeSerialize(currencies));
      }

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          LogUtils.getRootLogger().debug(response.getType());
        }
      });
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

  @Override
  public FormInterceptor getInstance() {
    return new TradeDocumentRenderer(itemViewName, itemRelation);
  }
}
