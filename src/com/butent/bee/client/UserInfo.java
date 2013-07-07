package com.butent.bee.client;

import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * gets user login status, session ID and stores them.
 * 
 */

public class UserInfo implements Module, HasInfo {

  private String sessionId;
  private UserData userData;

  public String getConstant(String name) {
    return (userData == null) ? null : userData.getConstant(name);
  }
  
  public String getDsn() {
    if (!isLoggedIn()) {
      return null;
    }
    return userData.getProperty("dsn");
  }

  public Filter getFilter(String column) {
    if (isLoggedIn()) {
      return ComparisonFilter.isEqual(column, new LongValue(getUserId()));
    } else {
      return null;
    }
  }

  public String getFirstName() {
    return isLoggedIn() ? userData.getFirstName() : null;
  }

  @Override
  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties("Is Logged In", isLoggedIn(),
        "Session Id", getSessionId(),
        "Signature", getUserSign(),
        "Dsn", getDsn());
    
    if (userData != null) {
      info.addAll(userData.getInfo());
    }
    return info;
  }

  public String getLastName() {
    return isLoggedIn() ? userData.getLastName() : null;
  }

  public String getLogin() {
    if (!isLoggedIn()) {
      return null;
    }
    return userData.getLogin();
  }

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public String getProperty(String property) {
    if (!isLoggedIn()) {
      return null;
    }
    return userData.getProperty(property);
  }

  public String getSessionId() {
    return sessionId;
  }

  public UserData getUserData() {
    return userData;
  }

  public Long getUserId() {
    if (!isLoggedIn()) {
      return null;
    }
    return userData.getUserId();
  }

  public String getUserSign() {
    if (!isLoggedIn()) {
      return null;
    }
    return userData.getUserSign();
  }

  public boolean hasEventRight(String object, RightsState state) {
    if (!isLoggedIn()) {
      return false;
    }
    return userData.hasEventRight(object, state);
  }

  public boolean hasFormRight(String object, RightsState state) {
    if (!isLoggedIn()) {
      return false;
    }
    return userData.hasFormRight(object, state);
  }

  public boolean hasGridRight(String object, RightsState state) {
    if (!isLoggedIn()) {
      return false;
    }
    return userData.hasGridRight(object, state);
  }

  public boolean hasMenuRight(String object, RightsState state) {
    if (!isLoggedIn()) {
      return false;
    }
    return userData.hasMenuRight(object, state);
  }

  public boolean hasModuleRight(String object, RightsState state) {
    if (!isLoggedIn()) {
      return false;
    }
    return userData.hasModuleRight(object, state);
  }

  @Override
  public void init() {
  }
  
  public boolean is(Long id) {
    return id != null && id.equals(getUserId());
  }

  public boolean isLoggedIn() {
    return userData != null;
  }

  @Override
  public void onExit() {
  }

  public void setDsn(String dsn) {
    if (isLoggedIn()) {
      userData.setProperty("dsn", dsn);
    }
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setUserData(UserData userData) {
    this.userData = userData;
  }

  @Override
  public void start() {
  }
}
