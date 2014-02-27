package com.butent.bee.client;

import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * gets user login status, session ID and stores them.
 * 
 */

public class UserInfo implements HasInfo {

  private String sessionId;
  private UserData userData;

  public Long getCompany() {
    return isLoggedIn() ? userData.getCompany() : null;
  }

  public Filter getFilter(String column) {
    if (isLoggedIn() && !BeeUtils.isEmpty(column)) {
      return Filter.equals(column, getUserId());
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
        "Signature", getUserSign());

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

  public boolean hasDataRight(String object, RightsState state) {
    if (!isLoggedIn()) {
      return false;
    }
    return userData.hasDataRight(object, state);
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

  public boolean isMenuVisible(String object) {
    if (!isLoggedIn()) {
      return false;
    }
    return userData.isMenuVisible(object);
  }

  public boolean isModuleVisible(String object) {
    return isLoggedIn() ? userData.isModuleVisible(object) : false;
  }

  public boolean is(Long id) {
    return id != null && id.equals(getUserId());
  }

  public boolean isLoggedIn() {
    return userData != null;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setUserData(UserData userData) {
    this.userData = userData;
  }
}
