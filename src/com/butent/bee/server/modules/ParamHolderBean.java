package com.butent.bee.server.modules;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

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

  private static BeeLogger logger = LogUtils.getLogger(ParamHolderBean.class);

  private static final String TBL_PARAMS = "Parameters";
  private static final String TBL_USER_PARAMS = "UserParameters";
  private static final String FLD_MODULE = "Module";
  private static final String FLD_USER = "User";
  private static final String FLD_NAME = "Name";
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

  private final Map<String, BeeParameter> parameters = Maps.newTreeMap();
  private final EventBus parameterEventBus = new EventBus();

  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_GET_PARAMETERS)) {
      response = ResponseObject.response(getModuleParameters(reqInfo
          .getParameter(VAR_PARAMETERS_MODULE)));

    } else if (BeeUtils.same(svc, SVC_GET_PARAMETER)) {
      response = ResponseObject.response(getValue(reqInfo.getParameter(VAR_PARAMETER)));

    } else if (BeeUtils.same(svc, SVC_SET_PARAMETER)) {
      setParameter(reqInfo.getParameter(VAR_PARAMETER), reqInfo.getParameter(VAR_PARAMETER_VALUE));
      response = ResponseObject.emptyResponse();

    } else if (BeeUtils.same(svc, SVC_RESET_PARAMETER)) {
      resetParameter(reqInfo.getParameter(VAR_PARAMETER));
      response = ResponseObject.emptyResponse();
    }
    if (response == null) {
      String msg = BeeUtils.joinWords("Parameters service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  public Boolean getBoolean(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getBoolean(usr.getCurrentUserId()) : parameter.getBoolean();
  }

  public Collection<String> getCollection(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getCollection(usr.getCurrentUserId()) : parameter.getCollection();
  }

  public JustDate getDate(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getDate(usr.getCurrentUserId()) : parameter.getDate();
  }

  public DateTime getDateTime(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getDateTime(usr.getCurrentUserId()) : parameter.getDateTime();
  }

  public Double getDouble(String name) {
    Number n = getNumber(name);

    if (n != null) {
      return n.doubleValue();
    }
    return null;
  }

  public Integer getInteger(String name) {
    Number n = getNumber(name);

    if (n != null) {
      return n.intValue();
    }
    return null;
  }

  public Long getLong(String name) {
    Number n = getNumber(name);

    if (n != null) {
      return n.longValue();
    }
    return null;
  }

  public Map<String, String> getMap(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getMap(usr.getCurrentUserId()) : parameter.getMap();
  }

  public Collection<BeeParameter> getModuleParameters(String module) {
    Assert.notEmpty(module);
    Collection<BeeParameter> params = Lists.newArrayList();

    for (BeeParameter param : parameters.values()) {
      if (BeeUtils.same(param.getModule(), module)) {
        params.add(param);
      }
    }
    return params;
  }

  public Number getNumber(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getNumber(usr.getCurrentUserId()) : parameter.getNumber();
  }

  public Long getRelation(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getRelation(usr.getCurrentUserId()) : parameter.getRelation();
  }

  public String getText(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getText(usr.getCurrentUserId()) : parameter.getText();
  }

  public Long getTime(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getTime(usr.getCurrentUserId()) : parameter.getTime();
  }

  public String getValue(String name) {
    BeeParameter parameter = getParameter(name);
    return parameter.supportsUsers()
        ? parameter.getUserValue(usr.getCurrentUserId()) : parameter.getValue();
  }

  public boolean hasParameter(String name) {
    return parameters.containsKey(BeeUtils.normalize(name));
  }

  public void postParameterEvent(ParameterEvent event) {
    parameterEventBus.post(event);
  }

  public void refreshModuleParameters(String module) {
    Collection<BeeParameter> defaults = moduleBean.getModuleDefaultParameters(module);

    if (!BeeUtils.isEmpty(defaults)) {
      for (BeeParameter param : defaults) {
        putParameter(param);
      }
    } else {
      defaults = getModuleParameters(module);
    }
    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_PARAMS, FLD_NAME, FLD_VALUE)
        .addField(TBL_PARAMS, sys.getIdName(TBL_PARAMS), FLD_PARAM)
        .addFrom(TBL_PARAMS)
        .setWhere(SqlUtils.equals(TBL_PARAMS, FLD_MODULE, module)));

    boolean hasUserParameters = false;

    for (BeeParameter param : defaults) {
      if (!hasUserParameters) {
        hasUserParameters = param.supportsUsers();
      }
      String name = param.getName();
      String value = data.getValueByKey(FLD_NAME, name, FLD_VALUE);

      if (value != null) {
        param.setValue(value);
      }
      param.setId(BeeUtils.toLongOrNull(data.getValueByKey(FLD_NAME, name, FLD_PARAM)));
    }
    if (hasUserParameters) {
      data = qs.getData(new SqlSelect()
          .addFields(TBL_USER_PARAMS, FLD_PARAM, FLD_USER, FLD_VALUE)
          .addFrom(TBL_USER_PARAMS)
          .addFromInner(TBL_PARAMS, sys.joinTables(TBL_PARAMS, TBL_USER_PARAMS, FLD_PARAM))
          .setWhere(SqlUtils.equals(TBL_PARAMS, FLD_MODULE, module)));

      for (BeeParameter param : defaults) {
        if (DataUtils.isId(param.getId())) {
          String id = BeeUtils.toString(param.getId());
          param.setUserValue(BeeUtils.toLong(data.getValueByKey(FLD_PARAM, id, FLD_USER)),
              data.getValueByKey(FLD_PARAM, id, FLD_VALUE));
        }
      }
    }
  }

  @Lock(LockType.WRITE)
  public void registerParameterEventHandler(ParameterEventHandler eventHandler) {
    parameterEventBus.register(eventHandler);
  }

  @Lock(LockType.WRITE)
  public void resetParameter(String name) {
    BeeParameter param = getParameter(name);

    if (DataUtils.isId(param.getId())) {
      qs.updateData(new SqlDelete(TBL_PARAMS)
          .setWhere(sys.idEquals(TBL_PARAMS, param.getId())));

      param.reset();
      postParameterEvent(new ParameterEvent(name));
    }
  }

  @Lock(LockType.WRITE)
  public void setParameter(String name, String value) {
    BeeParameter param = getParameter(name);

    if (!DataUtils.isId(param.getId())) {
      param.setId(qs.insertData(new SqlInsert(TBL_PARAMS)
          .addConstant(FLD_MODULE, param.getModule())
          .addConstant(FLD_NAME, param.getName())
          .addConstant(FLD_VALUE, param.getValue())));
    }
    if (param.supportsUsers()) {
      Long userId = usr.getCurrentUserId();
      IsCondition wh = SqlUtils.equals(TBL_USER_PARAMS, FLD_PARAM, param.getId(), FLD_USER, userId);

      if (value == null) {
        qs.updateData(new SqlDelete(TBL_USER_PARAMS)
            .setWhere(wh));
      } else {
        if (!BeeUtils.isPositive(qs.updateData(new SqlUpdate(TBL_USER_PARAMS)
            .addConstant(FLD_VALUE, value)
            .setWhere(wh)))) {

          qs.insertData(new SqlInsert(TBL_USER_PARAMS)
              .addConstant(FLD_PARAM, param.getId())
              .addConstant(FLD_USER, userId)
              .addConstant(FLD_VALUE, value));
        }
      }
      param.setUserValue(userId, value);

    } else {
      qs.updateData(new SqlUpdate(TBL_PARAMS)
          .addConstant(FLD_VALUE, value)
          .setWhere(sys.idEquals(TBL_PARAMS, param.getId())));

      param.setValue(value);
    }
    postParameterEvent(new ParameterEvent(name));
  }

  private BeeParameter getParameter(String name) {
    Assert.notEmpty(name);
    Assert.state(hasParameter(name), "Unknown parameter: " + name);

    return parameters.get(BeeUtils.normalize(name));
  }

  private void putParameter(BeeParameter parameter) {
    String name = BeeUtils.normalize(parameter.getName());
    BeeParameter oldParam = parameters.get(name);

    if (oldParam != null) {
      Assert.state(BeeUtils.same(oldParam.getModule(), parameter.getModule()),
          "Dublicate parameter name: " + name
              + " (modules: " + oldParam.getModule() + ", " + parameter.getModule() + ")");
    }
    parameters.put(name, parameter);
  }
}
