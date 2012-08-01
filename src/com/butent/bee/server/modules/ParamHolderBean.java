package com.butent.bee.server.modules;

import com.google.common.collect.Maps;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class ParamHolderBean {
  private static final String TBL_PARAMS = "Parameters";
  private static final String TBL_USER_PARAMS = "UserParameters";
  private static final String FLD_MODULE = "Module";
  private static final String FLD_NAME = "Name";
  private static final String FLD_TYPE = "Type";
  private static final String FLD_DESCRIPTION = "Description";
  private static final String FLD_USER_MODE = "UserMode";
  private static final String FLD_VALUE = "Value";
  private static final String FLD_PARAM = "Parameter";

  @EJB
  ModuleHolderBean moduleBean;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @EJB
  SystemBean sys;

  private final Map<String, Map<String, BeeParameter>> modules = Maps.newHashMap();

  @Lock(LockType.WRITE)
  public void createParameter(BeeParameter param) {
    Assert.notNull(param);
    boolean exists = hasModuleParameter(param.getModule(), param.getName());

    if (exists) {
      BeeParameter orig = getModuleParameter(param.getModule(), param.getName());

      SqlUpdate su = new SqlUpdate(TBL_PARAMS)
          .setWhere(SqlUtils.and(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, param.getModule()),
              SqlUtils.equal(TBL_PARAMS, FLD_NAME, param.getName())));

      if (!BeeUtils.equals(param.getType(), orig.getType())) {
        orig.setType(param.getType());
        su.addConstant(FLD_TYPE, param.getType());
      }
      if (!BeeUtils.equals(param.getDescription(), orig.getDescription())) {
        orig.setDescription(param.getDescription());
        su.addConstant(FLD_DESCRIPTION, param.getDescription());
      }
      if (!BeeUtils.equals(param.supportsUsers(), orig.supportsUsers())) {
        orig.setUserMode(param.supportsUsers());
        su.addConstant(FLD_USER_MODE, param.supportsUsers());
      }
      if (!BeeUtils.equals(param.getValue(), orig.getValue())) {
        orig.setValue(param.getValue());
        su.addConstant(FLD_VALUE, param.getValue());
      }
      if (!su.isEmpty()) {
        exists = BeeUtils.isPositive(qs.updateData(su));
      }
    }
    if (!exists) {
      storeParameter(param);
    }
  }

  public String getParameter(String module, String name) {
    String value = null;
    BeeParameter parameter = getModuleParameter(module, name);

    if (parameter.supportsUsers()) {
      value = parameter.getUserValue(usr.getCurrentUserId());
    } else {
      value = parameter.getValue();
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
  public void removeParameters(String module, String... names) {
    Assert.noNulls((Object[]) names);
    HasConditions wh = SqlUtils.or();

    for (String name : names) {
      wh.add(SqlUtils.equal(TBL_PARAMS, FLD_NAME, name));
    }
    if (BeeUtils.isPositive(qs.updateData(new SqlDelete(TBL_PARAMS)
        .setWhere(SqlUtils.and(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, module), wh))))) {
      refreshParameters(module);
    }
  }

  @Lock(LockType.WRITE)
  public void setParameter(String module, String name, String value) {
    BeeParameter parameter = getModuleParameter(module, name);

    IsCondition wh = SqlUtils.and(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, module),
        SqlUtils.equal(TBL_PARAMS, FLD_NAME, name));

    if (parameter.supportsUsers()) {
      Long userId = usr.getCurrentUserId();
      parameter.setUserValue(userId, value);

      Long prmId = qs.getLong(new SqlSelect()
          .addFields(TBL_PARAMS, sys.getIdName(TBL_PARAMS))
          .addFrom(TBL_PARAMS)
          .setWhere(wh));

      if (prmId == null) {
        prmId = storeParameter(parameter);
      }
      wh = SqlUtils.and(SqlUtils.equal(TBL_USER_PARAMS, FLD_PARAM, prmId),
          SqlUtils.equal(TBL_USER_PARAMS, UserServiceBean.FLD_USER, userId));

      if (value == null) {
        qs.updateData(new SqlDelete(TBL_USER_PARAMS)
            .setWhere(wh));
      } else {
        if (!BeeUtils.isPositive(qs.updateData(new SqlUpdate(TBL_USER_PARAMS)
            .addConstant(FLD_VALUE, value)
            .setWhere(wh)))) {

          qs.insertData(new SqlInsert(TBL_USER_PARAMS)
              .addConstant(FLD_PARAM, prmId)
              .addConstant(UserServiceBean.FLD_USER, userId)
              .addConstant(FLD_VALUE, value));
        }
      }
    } else {
      parameter.setValue(value);

      if (!BeeUtils.isPositive(qs.updateData(new SqlUpdate(TBL_PARAMS)
          .addConstant(FLD_VALUE, value)
          .setWhere(wh)))) {
        storeParameter(parameter);
      }
    }
  }

  private BeeParameter getModuleParameter(String module, String name) {
    Assert.notEmpty(name);
    Assert.state(hasModuleParameter(module, name),
        "Unknown parameter: " + BeeUtils.concat(".", module, name));
    Map<String, BeeParameter> params = getParameters(module);

    return params.get(BeeUtils.normalize(name));
  }

  private boolean hasModuleParameter(String module, String name) {
    Map<String, BeeParameter> params = getParameters(module);
    return params.containsKey(BeeUtils.normalize(name));
  }

  private void putModuleParameter(BeeParameter parameter) {
    Map<String, BeeParameter> params = getParameters(parameter.getModule());
    params.put(BeeUtils.normalize(parameter.getName()), parameter);
  }

  private void refreshParameters(String module) {
    modules.put(module, new HashMap<String, BeeParameter>());
    Collection<BeeParameter> defaults = moduleBean.getModuleDefaultParameters(module);

    if (!BeeUtils.isEmpty(defaults)) {
      for (BeeParameter param : defaults) {
        putModuleParameter(param);
      }
    }
    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_PARAMS, FLD_NAME, FLD_TYPE, FLD_DESCRIPTION, FLD_USER_MODE, FLD_VALUE)
        .addFrom(TBL_PARAMS)
        .setWhere(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, module)));

    boolean hasUserParameters = false;

    for (Map<String, String> row : data) {
      BeeParameter parameter = new BeeParameter(module, row.get(FLD_NAME), row.get(FLD_TYPE),
          row.get(FLD_DESCRIPTION), BeeUtils.toBoolean(row.get(FLD_USER_MODE)), row.get(FLD_VALUE));
      putModuleParameter(parameter);

      if (!hasUserParameters) {
        hasUserParameters = parameter.supportsUsers();
      }
    }
    if (hasUserParameters) {
      data = qs.getData(new SqlSelect()
          .addFields(TBL_PARAMS, FLD_NAME)
          .addFields(TBL_USER_PARAMS, UserServiceBean.FLD_USER, FLD_VALUE)
          .addFrom(TBL_PARAMS)
          .addFromInner(TBL_USER_PARAMS,
              SqlUtils.join(TBL_PARAMS, sys.getIdName(TBL_PARAMS), TBL_USER_PARAMS, FLD_PARAM))
          .setWhere(SqlUtils.and(SqlUtils.equal(TBL_PARAMS, FLD_MODULE, module),
              SqlUtils.notNull(TBL_PARAMS, FLD_USER_MODE))));

      for (Map<String, String> row : data) {
        getModuleParameter(module, row.get(FLD_NAME))
            .setUserValue(BeeUtils.toLong(row.get(UserServiceBean.FLD_USER)), row.get(FLD_VALUE));
      }
    }
  }

  private long storeParameter(BeeParameter parameter) {
    Assert.notNull(parameter);
    putModuleParameter(parameter);

    return qs.insertData(new SqlInsert(TBL_PARAMS)
        .addConstant(FLD_MODULE, parameter.getModule())
        .addConstant(FLD_NAME, parameter.getName())
        .addConstant(FLD_TYPE, parameter.getType())
        .addConstant(FLD_DESCRIPTION, parameter.getDescription())
        .addConstant(FLD_USER_MODE, parameter.supportsUsers())
        .addConstant(FLD_VALUE, parameter.getValue()));
  }
}
