package com.butent.bee.server.modules;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

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
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Lock(LockType.READ)
public class ParamHolderBean {
  private static final String TBL_PARAMS = "Parameters";
  private static final String TBL_USER_PARAMS = "UserParameters";
  private static final String FLD_MODULE = "Module";
  private static final String FLD_USER = "User";
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
  private final EventBus parameterEventBus = new EventBus();

  @Lock(LockType.WRITE)
  public void createParameter(BeeParameter param) {
    Assert.notNull(param);
    boolean exists = hasModuleParameter(param.getModule(), param.getName());

    if (exists) {
      BeeParameter orig = getModuleParameter(param.getModule(), param.getName());

      SqlUpdate su = new SqlUpdate(TBL_PARAMS)
          .setWhere(SqlUtils.equals(TBL_PARAMS, FLD_MODULE, param.getModule(),
              FLD_NAME, param.getName()));

      if (!Objects.equal(param.getType(), orig.getType())) {
        orig.setType(param.getType());
        su.addConstant(FLD_TYPE, param.getType().name());
      }
      if (!Objects.equal(param.getDescription(), orig.getDescription())) {
        orig.setDescription(param.getDescription());
        su.addConstant(FLD_DESCRIPTION, param.getDescription());
      }
      if (!Objects.equal(param.supportsUsers(), orig.supportsUsers())) {
        orig.setUserMode(param.supportsUsers());
        su.addConstant(FLD_USER_MODE, param.supportsUsers());
      }
      if (!Objects.equal(param.getValue(), orig.getValue())) {
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

  public Boolean getBoolean(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getBoolean(usr.getCurrentUserId()) : parameter.getBoolean();
  }

  public JustDate getDate(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getDate(usr.getCurrentUserId()) : parameter.getDate();
  }

  public DateTime getDateTime(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getDateTime(usr.getCurrentUserId()) : parameter.getDateTime();
  }

  public Double getDouble(String module, String name) {
    Number n = getNumber(module, name);

    if (n != null) {
      return n.doubleValue();
    }
    return null;
  }

  public Integer getInteger(String module, String name) {
    Number n = getNumber(module, name);

    if (n != null) {
      return n.intValue();
    }
    return null;
  }

  public List<String> getList(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getList(usr.getCurrentUserId()) : parameter.getList();
  }

  public Long getLong(String module, String name) {
    Number n = getNumber(module, name);

    if (n != null) {
      return n.longValue();
    }
    return null;
  }

  public Map<String, String> getMap(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getMap(usr.getCurrentUserId()) : parameter.getMap();
  }

  public Number getNumber(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getNumber(usr.getCurrentUserId()) : parameter.getNumber();
  }

  public Map<String, BeeParameter> getParameters(String module) {
    Assert.notEmpty(module);

    if (!modules.containsKey(module)) {
      refreshParameters(module);
    }
    return modules.get(module);
  }

  public Set<String> getSet(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getSet(usr.getCurrentUserId()) : parameter.getSet();
  }

  public String getText(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getText(usr.getCurrentUserId()) : parameter.getText();
  }

  public Long getTime(String module, String name) {
    BeeParameter parameter = getModuleParameter(module, name);
    return parameter.supportsUsers()
        ? parameter.getTime(usr.getCurrentUserId()) : parameter.getTime();
  }

  public void postParameterEvent(ParameterEvent event) {
    parameterEventBus.post(event);
  }

  public void refreshParameters(String module) {
    modules.put(module, new TreeMap<String, BeeParameter>());
    Collection<BeeParameter> defaults = moduleBean.getModuleDefaultParameters(module);

    if (!BeeUtils.isEmpty(defaults)) {
      for (BeeParameter param : defaults) {
        putModuleParameter(param);
      }
    }
    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_PARAMS, FLD_NAME, FLD_TYPE, FLD_DESCRIPTION, FLD_USER_MODE, FLD_VALUE)
        .addFrom(TBL_PARAMS)
        .setWhere(SqlUtils.equals(TBL_PARAMS, FLD_MODULE, module)));

    boolean hasUserParameters = false;

    for (SimpleRow row : data) {
      BeeParameter parameter = new BeeParameter(module, row.getValue(FLD_NAME),
          NameUtils.getEnumByName(ParameterType.class, row.getValue(FLD_TYPE)),
          row.getValue(FLD_DESCRIPTION), BeeUtils.unbox(row.getBoolean(FLD_USER_MODE)),
          row.getValue(FLD_VALUE));
      putModuleParameter(parameter);

      if (!hasUserParameters) {
        hasUserParameters = parameter.supportsUsers();
      }
    }
    if (hasUserParameters) {
      data = qs.getData(new SqlSelect()
          .addFields(TBL_PARAMS, FLD_NAME)
          .addFields(TBL_USER_PARAMS, FLD_USER, FLD_VALUE)
          .addFrom(TBL_PARAMS)
          .addFromInner(TBL_USER_PARAMS, sys.joinTables(TBL_PARAMS, TBL_USER_PARAMS, FLD_PARAM))
          .setWhere(SqlUtils.and(SqlUtils.equals(TBL_PARAMS, FLD_MODULE, module),
              SqlUtils.notNull(TBL_PARAMS, FLD_USER_MODE))));

      for (SimpleRow row : data) {
        getModuleParameter(module, row.getValue(FLD_NAME))
            .setUserValue(row.getLong(FLD_USER), row.getValue(FLD_VALUE));
      }
    }
  }

  @Lock(LockType.WRITE)
  public void registerParameterEventHandler(ParameterEventHandler eventHandler) {
    parameterEventBus.register(eventHandler);
  }

  @Lock(LockType.WRITE)
  public void removeParameters(String module, String... names) {
    Assert.noNulls((Object[]) names);
    HasConditions wh = SqlUtils.or();

    for (String name : names) {
      wh.add(SqlUtils.equals(TBL_PARAMS, FLD_NAME, name));
    }
    if (BeeUtils.isPositive(qs.updateData(new SqlDelete(TBL_PARAMS)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_PARAMS, FLD_MODULE, module), wh))))) {
      refreshParameters(module);
    }
    for (String name : names) {
      postParameterEvent(new ParameterEvent(module, name));
    }
  }

  @Lock(LockType.WRITE)
  public void setParameter(String module, String name, String value) {
    BeeParameter parameter = getModuleParameter(module, name);

    IsCondition wh = SqlUtils.equals(TBL_PARAMS, FLD_MODULE, module, FLD_NAME, name);

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
      wh = SqlUtils.equals(TBL_USER_PARAMS, FLD_PARAM, prmId, FLD_USER, userId);

      if (value == null) {
        qs.updateData(new SqlDelete(TBL_USER_PARAMS)
            .setWhere(wh));
      } else {
        if (!BeeUtils.isPositive(qs.updateData(new SqlUpdate(TBL_USER_PARAMS)
            .addConstant(FLD_VALUE, value)
            .setWhere(wh)))) {

          qs.insertData(new SqlInsert(TBL_USER_PARAMS)
              .addConstant(FLD_PARAM, prmId)
              .addConstant(FLD_USER, userId)
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
    postParameterEvent(new ParameterEvent(module, name));
  }

  private BeeParameter getModuleParameter(String module, String name) {
    Assert.notEmpty(name);
    Assert.state(hasModuleParameter(module, name),
        "Unknown parameter: " + BeeUtils.join(".", module, name));
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

  private long storeParameter(BeeParameter parameter) {
    Assert.notNull(parameter);
    putModuleParameter(parameter);

    return qs.insertData(new SqlInsert(TBL_PARAMS)
        .addConstant(FLD_MODULE, parameter.getModule())
        .addConstant(FLD_NAME, parameter.getName())
        .addConstant(FLD_TYPE, parameter.getType().name())
        .addConstant(FLD_DESCRIPTION, parameter.getDescription())
        .addConstant(FLD_USER_MODE, parameter.supportsUsers())
        .addConstant(FLD_VALUE, parameter.getValue()));
  }
}
