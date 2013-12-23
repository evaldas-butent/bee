package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.websocket.Endpoint;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.websocket.messages.LocationMessage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Users extends Flow {

  private static final class User {

    private final String login;
    private final long userId;

    private final Set<String> sessions = Sets.newHashSet();

    private final UserWidget userWidget;

    private User(String login, long userId, String sessionId, UserWidget userWidget) {
      this.login = login;
      this.userId = userId;

      this.sessions.add(sessionId);

      this.userWidget = userWidget;
    }

    private void refresh() {
      userWidget.updateSessionCount(sessions.size());
    }
  }

  private static final class UserWidget extends Flow {

    private final String infoId;

    private UserWidget(String login) {
      super(STYLE_PREFIX + "user");

      CustomDiv nameWidget = new CustomDiv(STYLE_PREFIX + "name");
      nameWidget.setText(login);
      add(nameWidget);

      CustomDiv infoWidget = new CustomDiv(STYLE_PREFIX + "info");
      add(infoWidget);

      this.infoId = infoWidget.getId();
    }

    private void updateSessionCount(int count) {
      Widget infoWidget = DomUtils.getChildById(this, infoId);
      if (infoWidget != null) {
        infoWidget.getElement().setInnerText(Integer.toString(count));
      }
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(Users.class);

  private static final String STYLE_PREFIX = "bee-UserList";

  private final List<User> users = Lists.newArrayList();

  Users() {
    super(STYLE_PREFIX + "container");
  }

  public void addUser(String login, final long userId, String sessionId) {
    User user = find(userId);

    if (user == null) {
      UserWidget userWidget = new UserWidget(login);
      users.add(new User(login, userId, sessionId, userWidget));
      
      userWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String from = Endpoint.getSessionId();
          User toUser = find(userId);
          
          if (!BeeUtils.isEmpty(from) && toUser != null) {
            for (String to : toUser.sessions) {
              logger.debug("get location of", toUser.login);
              Endpoint.send(LocationMessage.query(from, to));
            }
          }
        }
      });

      add(userWidget);

    } else {
      if (!user.login.equals(login)) {
        logger.warning("user id:", userId, "login does not match:", user.login, login);
      }

      if (user.sessions.contains(sessionId)) {
        logger.warning("user", userId, login, "duplicate session", sessionId);
      } else {
        user.sessions.add(sessionId);
        user.refresh();
      }
    }
  }

  public Set<String> getAllSessions() {
    Set<String> result = Sets.newHashSet();

    for (User user : users) {
      result.addAll(user.sessions);
    }

    return result;
  }

  public Set<String> getSessions(Collection<Long> userIds) {
    Set<String> result = Sets.newHashSet();

    if (!BeeUtils.isEmpty(userIds)) {
      for (User user : users) {
        if (userIds.contains(user.userId)) {
          result.addAll(user.sessions);
        }
      }
    }

    return result;
  }
  
  public String getUserNameBySession(String sessionId) {
    for (User user : users) {
      if (user.sessions.contains(sessionId)) {
        return user.login;
      }
    }
    return null;
  }

  public Long parseUserName(String input) {
    for (User user : users) {
      if (BeeUtils.same(user.login, input)) {
        return user.userId;
      }
    }

    Long result = null;

    for (User user : users) {
      if (BeeUtils.containsSame(user.login, input)) {
        if (result == null) {
          result = user.userId;
        } else {
          result = null;
          break;
        }
      }
    }

    return result;
  }

  public void removeUser(long userId, String sessionId) {
    User user = find(userId);

    if (user == null) {
      logger.warning("user not found:", userId);
    } else {
      boolean removed = user.sessions.remove(sessionId);

      if (removed) {
        if (user.sessions.isEmpty()) {
          remove(user.userWidget);
          users.remove(user);
        } else {
          user.refresh();
        }

      } else {
        logger.warning("user:", userId, user.login, "sessions:", user.sessions,
            "session not found:", sessionId);
      }
    }
  }

  private User find(long userId) {
    for (User user : users) {
      if (user.userId == userId) {
        return user;
      }
    }
    return null;
  }
}
