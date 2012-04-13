package com.butent.bee.server.modules;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
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
      refreshParameters(module);
    }
    return modules.get(module);
  }

  @Lock(LockType.WRITE)
  public ResponseObject removeParameters(String module, String... names) {
    Assert.noNulls((Object[]) names);
    HasConditions wh = SqlUtils.or();

    for (String name : names) {
      wh.add(SqlUtils.equal(TBL_PARAMS, FLD_NAME, name));
    }
    ResponseObject resp = qs.updateDataWithResponse(new SqlDelete(TBL_PARAMS)
        .setWhere(SqlUtils.and(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, module), wh)));

    if (!resp.hasErrors()) {
      Map<String, BeeParameter> params = moduleBean.getModuleDefaultParameters(module);
      List<BeeParameter> defs = Lists.newArrayList();

      for (String name : names) {
        BeeParameter def = null;

        if (!BeeUtils.isEmpty(params)) {
          def = params.get(name);
        }
        if (def != null) {
          defs.add(def);
          getParameters(module).put(name, def);
        } else {
          getParameters(module).remove(name);
        }
      }
      resp.setResponse(defs);
    }
    return resp;
  }

  @Lock(LockType.WRITE)
  public ResponseObject saveParameter(BeeParameter param) {
    ResponseObject response = null;
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
      if (su.isEmpty()) {
        response = ResponseObject.response(-1);
      } else {
        response = qs.updateDataWithResponse(su);
      }
    }
    if (response == null || BeeUtils.isEmpty(response.getResponse(-1, null))) {
      response = qs.insertDataWithResponse(new SqlInsert(TBL_PARAMS)
          .addConstant(FLD_MODULE, param.getModule())
          .addConstant(FLD_NAME, param.getName())
          .addConstant(FLD_TYPE, param.getType())
          .addConstant(FLD_VALUE, param.getValue())
          .addConstant(FLD_DESCRIPTION, param.getDescription()));
    }
    if (!response.hasErrors()) {
      getParameters(param.getModule()).put(param.getName(), param);
      response.setResponse(param);
    }
    return response;
  }

  @Lock(LockType.WRITE)
  private void refreshParameters(String module) {
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
