package com.butent.bee.server.modules.payroll;

import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class PayrollModuleBean implements BeeModule {

  @EJB
  QueryServiceBean qs;

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result = new ArrayList<>();

    if (BeeUtils.isPositiveInt(query)) {
      result.addAll(qs.getSearchResults(VIEW_EMPLOYEES,
          Filter.equals(COL_TAB_NUMBER, BeeUtils.toInt(query))));

    } else {
      result.addAll(qs.getSearchResults(VIEW_EMPLOYEES,
          Filter.anyContains(Sets.newHashSet(COL_FIRST_NAME, COL_LAST_NAME,
              ALS_DEPARTMENT_NAME, ALS_POSITION_NAME), query)));

      result.addAll(qs.getSearchResults(VIEW_LOCATIONS,
          Filter.anyContains(Sets.newHashSet(COL_LOCATION_NAME, ALS_COMPANY_NAME,
              ALS_LOCATION_MANAGER_FIRST_NAME, ALS_LOCATION_MANAGER_LAST_NAME,
              COL_ADDRESS), query)));
    }

    return result;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    return null;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public Module getModule() {
    return Module.PAYROLL;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
  }
}
