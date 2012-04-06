package com.butent.bee.server.modules;

import com.google.common.collect.Maps;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class ParamHolderBean {
  private static final String TBL_PARAMS = "Parameters";
  private static final String FLD_MODULE = "Module";
  private static final String FLD_NAME = "Name";
  private static final String FLD_TYPE = "Type";
  private static final String FLD_VALUE = "Value";
  private static final String FLD_DESCRIPTION = "Description";

  @EJB
  ModuleHolderBean moduleBean;
  @EJB
  QueryServiceBean qs;

  private final Map<String, Map<String, BeeParameter>> modules = Maps.newHashMap();

  public String getParameter(String module, String name) {
    Assert.notEmpty(module);
    Assert.notEmpty(name);

    String value = null;
    Map<String, BeeParameter> params = getParameters(module);

    if (!BeeUtils.isEmpty(params)) {
      BeeParameter parameter = params.get(name);

      if (parameter != null) {
        value = parameter.getValue();
      }
    }
    return value;
  }

  public Map<String, BeeParameter> getParameters(String module) {
    Assert.notEmpty(module);

    if (!modules.containsKey(module)) {
      updateParameters(module);
    }
    return modules.get(module);
  }

  @Lock(LockType.WRITE)
  private void updateParameters(String module) {
    Map<String, BeeParameter> params = moduleBean.getModuleDefaultParameters(module);

    if (!BeeUtils.isEmpty(params)) {
      SimpleRowSet data = qs.getData(new SqlSelect()
          .addFields(TBL_PARAMS, FLD_NAME, FLD_TYPE, FLD_VALUE, FLD_DESCRIPTION)
          .addFrom(TBL_PARAMS)
          .setWhere(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, module)));

      for (Map<String, String> row : data) {
        params.put(row.get(FLD_NAME),
            new BeeParameter(module, row.get(FLD_NAME),
                row.get(FLD_TYPE), row.get(FLD_VALUE), row.get(FLD_DESCRIPTION)));
      }
    }
    modules.put(module, params);
  }
}
