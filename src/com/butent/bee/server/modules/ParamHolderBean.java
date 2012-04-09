package com.butent.bee.server.modules;

import com.google.common.collect.Maps;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
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
  public boolean saveParameters(Collection<BeeParameter> params) {
    boolean ok = true;

    for (BeeParameter param : params) {
      long res = 0;
      BeeParameter orig = getParameters(param.getModule()).get(param.getName());

      if (orig != null) {
        SqlUpdate su = new SqlUpdate(TBL_PARAMS)
            .setWhere(SqlUtils.and(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, param.getModule()),
                SqlUtils.equal(TBL_PARAMS, FLD_NAME, param.getName())));

        if (!BeeUtils.equals(param.getType(), orig.getType())) {
          su.addConstant(FLD_TYPE, param.getType());
        }
        if (!BeeUtils.equals(param.getValue(), orig.getValue())) {
          su.addConstant(FLD_VALUE, param.getValue());
        }
        if (!BeeUtils.equals(param.getDescription(), orig.getDescription())) {
          su.addConstant(FLD_DESCRIPTION, param.getDescription());
        }
        res = qs.updateData(su);
      }
      if (res == 0) {
        res = qs.insertData(new SqlInsert(TBL_PARAMS)
            .addConstant(FLD_MODULE, param.getModule())
            .addConstant(FLD_NAME, param.getName())
            .addConstant(FLD_TYPE, param.getType())
            .addConstant(FLD_VALUE, param.getValue())
            .addConstant(FLD_DESCRIPTION, param.getDescription()));
      }
      if (res <= 0) {
        ok = false;
        break;
      } else {
        getParameters(param.getModule()).put(param.getName(), param);
      }
    }
    return ok;
  }

  @Lock(LockType.WRITE)
  private void updateParameters(String module) {
    Map<String, BeeParameter> params = moduleBean.getModuleDefaultParameters(module);

    if (BeeUtils.isEmpty(params)) {
      params = Maps.newHashMap();
    }
    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_PARAMS, FLD_NAME, FLD_TYPE, FLD_VALUE, FLD_DESCRIPTION)
        .addFrom(TBL_PARAMS)
        .setWhere(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, module)));

    for (Map<String, String> row : data) {
      params.put(row.get(FLD_NAME),
          new BeeParameter(module, row.get(FLD_NAME),
              row.get(FLD_TYPE), row.get(FLD_VALUE), row.get(FLD_DESCRIPTION)));
    }
    modules.put(module, params);
  }
}
