package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.websocket.Endpoint;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.websocket.messages.LocationMessage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Users {

  private static final class OnlinePanel extends Flow {
    
    private OnlinePanel() {
      super(STYLE_PREFIX + "panel");
    }

    private OnlineWidget findBySession(String sessionId) {
      for (Widget widget : this) {
        if (widget instanceof OnlineWidget && ((OnlineWidget) widget).sessionId.equals(sessionId)) {
          return (OnlineWidget) widget;
        }
      }

      logger.warning("widget not found for session", sessionId);
      return null;
    }
  }

  private static final class OnlineWidget extends Flow {
    
    private static final int NAME_INDEX = 0; 

    private final String sessionId;

    private OnlineWidget(String sessionId, UserData userData) {
      super(STYLE_PREFIX + "item");
      this.sessionId = sessionId;

      CustomDiv nameWidget = new CustomDiv(STYLE_PREFIX + "name");
      nameWidget.setText(userData.getUserSign());
      add(nameWidget);
    }
    
    private void updateData(UserData userData) {
      if (userData != null) {
        getWidget(NAME_INDEX).getElement().setInnerText(userData.getUserSign());
      }
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(Users.class);

  private static final String STYLE_PREFIX = "bee-Online-";

  private final Map<Long, UserData> users = Maps.newHashMap();
  private final Map<String, Long> openSessions = Maps.newHashMap();

  private final OnlinePanel onlinePanel = new OnlinePanel();

  Users() {
  }
  
  public void addSession(final String sessionId, long userId) {
    Assert.notEmpty(sessionId, "attempt to add empty session");
    if (openSessions.containsKey(sessionId)) {
      logger.severe("session", sessionId, "already exists");
      return;
    }

    UserData userData = users.get(userId);
    if (userData == null) {
      logger.severe("user data not available", userId);
      return;
    }

    openSessions.put(sessionId, userId);

    OnlineWidget widget = new OnlineWidget(sessionId, userData);

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String from = Endpoint.getSessionId();
        if (!BeeUtils.isEmpty(from)) {
          Endpoint.send(LocationMessage.query(from, sessionId));
        }
      }
    });

    onlinePanel.add(widget);
  }
  
  public Set<String> getAllSessions() {
    return openSessions.keySet();
  }

  public IdentifiableWidget getOnlinePanel() {
    return onlinePanel;
  }

  public Set<String> getSessions(Collection<Long> userIds) {
    Set<String> result = Sets.newHashSet();

    if (!BeeUtils.isEmpty(userIds) && !openSessions.isEmpty()) {
      for (Map.Entry<String, Long> entry : openSessions.entrySet()) {
        if (userIds.contains(entry.getValue())) {
          result.add(entry.getKey());
        }
      }
    }
    return result;
  }

  public String getUserNameBySession(String sessionId) {
    Long userId = openSessions.get(sessionId);
    UserData userData = (userId == null) ? null : users.get(userId);

    return (userData == null) ? null : userData.getUserSign();
  }

  public void loadUserData(String serialized) {
    String[] arr = Codec.beeDeserializeCollection(serialized);
    if (ArrayUtils.isEmpty(arr)) {
      logger.severe("cannot deserialize user data");
      return;
    }
    
    List<UserData> data = Lists.newArrayList();
    for (String s : arr) {
      data.add(UserData.restore(s));
    }
    
    updateUserData(data);
  }

  public Long parseUserName(String input) {
    if (users.isEmpty() || BeeUtils.isEmpty(input)) {
      return null;
    }

    List<Long> loginEquals = Lists.newArrayList();
    List<Long> loginContains = Lists.newArrayList();

    List<Long> firstNameEquals = Lists.newArrayList();
    List<Long> firstNameContains = Lists.newArrayList();

    List<Long> lastNameEquals = Lists.newArrayList();
    List<Long> lastNameContains = Lists.newArrayList();

    for (UserData userData : users.values()) {
      if (BeeUtils.same(userData.getLogin(), input)) {
        loginEquals.add(userData.getUserId());
      }
      if (BeeUtils.containsSame(userData.getLogin(), input)) {
        loginContains.add(userData.getUserId());
      }

      if (BeeUtils.same(userData.getFirstName(), input)) {
        firstNameEquals.add(userData.getUserId());
      }
      if (BeeUtils.containsSame(userData.getFirstName(), input)) {
        firstNameContains.add(userData.getUserId());
      }

      if (BeeUtils.same(userData.getLastName(), input)) {
        lastNameEquals.add(userData.getUserId());
      }
      if (BeeUtils.containsSame(userData.getLastName(), input)) {
        lastNameContains.add(userData.getUserId());
      }
    }

    if (firstNameEquals.size() == 1) {
      return firstNameEquals.get(0);
    }
    if (lastNameEquals.size() == 1) {
      return lastNameEquals.get(0);
    }
    if (loginEquals.size() == 1) {
      return loginEquals.get(0);
    }

    if (firstNameContains.size() == 1) {
      return firstNameContains.get(0);
    }
    if (lastNameContains.size() == 1) {
      return lastNameContains.get(0);
    }
    if (loginContains.size() == 1) {
      return loginContains.get(0);
    }

    if (loginContains.isEmpty() && firstNameContains.isEmpty() && lastNameContains.isEmpty()) {
      logger.warning("cannot parse user name:", input);
    } else {
      logger.warning("ambiguous user name:", input);
    }
    return null;
  }

  public void removeSession(String sessionId) {
    if (BeeUtils.isEmpty(sessionId)) {
      logger.severe("remove session: id is empty");

    } else if (openSessions.containsKey(sessionId)) {
      openSessions.remove(sessionId);

      OnlineWidget widget = onlinePanel.findBySession(sessionId);
      if (widget != null) {
        onlinePanel.remove(widget);
      }

    } else {
      logger.warning("session not found:", sessionId);
    }
  }

  public void updateUserData(Collection<UserData> data) {
    users.clear();
    if (BeeUtils.isEmpty(data)) {
      openSessions.clear();
      onlinePanel.clear();
      
      logger.warning("user data is empty");
      return;
    }
    
    for (UserData userData : data) {
      users.put(userData.getUserId(), userData);
    }
    
    if (!openSessions.isEmpty()) {
      Set<String> sessionsToRemove = Sets.newHashSet();
      for (Map.Entry<String, Long> entry : openSessions.entrySet()) {
        if (!users.containsKey(entry.getValue())) {
          sessionsToRemove.add(entry.getKey());
        }
      }
      
      if (!sessionsToRemove.isEmpty()) {
        for (String sessionId : sessionsToRemove) {
          removeSession(sessionId);
        }
      }
    }
    
    if (!openSessions.isEmpty()) {
      for (Map.Entry<String, Long> entry : openSessions.entrySet()) {
        OnlineWidget widget = onlinePanel.findBySession(entry.getKey());
        if (widget != null) {
          widget.updateData(users.get(entry.getValue()));
        }
      }
    }
    
    logger.info("users", users.size());
  }
}
