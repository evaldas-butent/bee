package com.butent.bee.client.websocket;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.shared.data.PropertiesData;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.websocket.InfoMessage;
import com.butent.bee.shared.websocket.Message;
import com.butent.bee.shared.websocket.SessionMessage;
import com.butent.bee.shared.websocket.SessionUser;
import com.butent.bee.shared.websocket.ShowMessage;
import com.butent.bee.shared.websocket.ShowMessage.Subject;
import com.butent.bee.shared.websocket.EchoMessage;
import com.butent.bee.shared.websocket.UsersMessage;

import java.util.List;

class MessageDispatcher {

  private static BeeLogger logger = LogUtils.getLogger(MessageDispatcher.class);
  
  MessageDispatcher() {
  }
  
  void dispatch(Message message) {
    switch (message.getType()) {
      case ECHO:
        BeeKeeper.getScreen().notifyInfo(((EchoMessage) message).getText());
        break;

      case INFO:
        String caption = ((InfoMessage) message).getCaption();
        List<Property> info = ((InfoMessage) message).getInfo();
        
        if (BeeUtils.isEmpty(info)) {
          logger.warning(caption, "message is empty");
        } else {
          Global.showGrid(caption, new PropertiesData(info));
        }
        break;
        
      case SESSION:
        SessionMessage sm = (SessionMessage) message;
        SessionUser su = sm.getSessionUser();
      
        if (sm.isOpen()) {
          Global.getUsers().addUser(su.getLogin(), su.getUserId(), su.getSessionId());
        } else if (sm.isClosed()) {
          Global.getUsers().removeUser(su.getUserId(), su.getSessionId());
        } else {
          logger.warning("unrecognized session state:", sm.getState(),
              "user:", su.getUserId(), su.getLogin(), "session", su.getSessionId());
        }
        break;
        
      case SHOW:
        Subject subject = ((ShowMessage) message).getSubject();
        if (subject == Subject.SESSION) {
          Global.showGrid(subject.getCaption(), new PropertiesData(Endpoint.getInfo()));
        } else {
          logger.warning(message.getClass().getSimpleName(), "subject", subject, "not supported");
        }
        break;
        
      case USERS:
        List<SessionUser> users = ((UsersMessage) message).getUsers();
        if (users.size() > 1) {
          for (int i = 0; i < users.size() - 1; i++) {
            SessionUser user = users.get(i);
            Global.getUsers().addUser(user.getLogin(), user.getUserId(), user.getSessionId());
          }
        }
        
        if (users.isEmpty()) {
          logger.warning(message.getClass().getSimpleName(), "user list empty");
        } else {
          Endpoint.setSessionId(users.get(users.size() - 1).getSessionId());
        }
        break;
    }
  }
}
