package com.butent.bee.client.websocket;

import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.Position.Coordinates;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.cli.CliWorker;
import com.butent.bee.client.maps.MapUtils;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.PropertiesData;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.websocket.SessionUser;
import com.butent.bee.shared.websocket.WsUtils;
import com.butent.bee.shared.websocket.messages.AdminMessage;
import com.butent.bee.shared.websocket.messages.EchoMessage;
import com.butent.bee.shared.websocket.messages.InfoMessage;
import com.butent.bee.shared.websocket.messages.LocationMessage;
import com.butent.bee.shared.websocket.messages.LogMessage;
import com.butent.bee.shared.websocket.messages.Message;
import com.butent.bee.shared.websocket.messages.ProgressMessage;
import com.butent.bee.shared.websocket.messages.SessionMessage;
import com.butent.bee.shared.websocket.messages.ShowMessage;
import com.butent.bee.shared.websocket.messages.UsersMessage;
import com.butent.bee.shared.websocket.messages.ShowMessage.Subject;

import java.util.List;

class MessageDispatcher {

  private static BeeLogger logger = LogUtils.getLogger(MessageDispatcher.class);

  MessageDispatcher() {
  }

  void dispatch(Message message) {
    String caption;

    switch (message.getType()) {
      case ADMIN:
        AdminMessage am = (AdminMessage) message;

        if (!BeeUtils.isEmpty(am.getResponse())) {
          logger.debug(message.getClass().getSimpleName(), "response", am.getResponse());

        } else if (!BeeUtils.isEmpty(am.getCommand())) {
          logger.debug(message.getClass().getSimpleName(), am.getCommand());

          boolean ok = CliWorker.execute(am.getCommand(), false);
          if (!ok) {
            Endpoint.send(AdminMessage.response(Endpoint.getSessionId(), am.getFrom(),
                "command not recognized: " + am.getCommand()));
          }

        } else {
          WsUtils.onEmptyMessage(message);
        }
        break;

      case ECHO:
        BeeKeeper.getScreen().notifyInfo(((EchoMessage) message).getText());
        break;

      case INFO:
        caption = ((InfoMessage) message).getCaption();
        List<Property> info = ((InfoMessage) message).getInfo();

        if (BeeUtils.isEmpty(info)) {
          WsUtils.onEmptyMessage(message);
        } else {
          Global.showGrid(caption, new PropertiesData(info));
        }
        break;
        
      case LOCATION:
        LocationMessage lm = (LocationMessage) message;
        final String from = lm.getFrom();

        if (lm.isQuery()) {
          MapUtils.getCurrentPosition(new Consumer<Position>() {
            @Override
            public void accept(Position input) {
              Coordinates coords = input.getCoordinates();
              Endpoint.send(LocationMessage.coordinates(Endpoint.getSessionId(), from,
                  coords.getLatitude(), coords.getLongitude(), coords.getAccuracy()));
            }
          }, new Consumer<String>() {
            @Override
            public void accept(String input) {
              Endpoint.send(LocationMessage.response(Endpoint.getSessionId(), from, input));
            }
          });

        } else if (lm.hasCoordinates()) {
          String title = Global.getUsers().getUserNameBySession(from);
          if (BeeUtils.isPositive(lm.getAccuracy())) {
            caption = BeeUtils.joinWords(title, "+-", lm.getAccuracy(), "m");
          } else {
            caption = title;
          }

          MapUtils.showPosition(caption, lm.getLatitude(), lm.getLongitude(), title);

        } else if (!BeeUtils.isEmpty(lm.getResponse())) {
          logger.debug(message.getClass().getSimpleName(), "response", lm.getResponse());

        } else {
          WsUtils.onEmptyMessage(message);
        }
        break;
        
      case LOG:
        LogMessage logMessage = (LogMessage) message;
        
        if (logMessage.getLevel() != null && !BeeUtils.isEmpty(logMessage.getText())) {
          logger.log(logMessage.getLevel(), logMessage.getText());
        } else {
          WsUtils.onEmptyMessage(message);
        }
        break;
        
      case PROGRESS:
        ProgressMessage pm = (ProgressMessage) message;
        String progressId = pm.getProgressId();
        
        if (BeeUtils.isEmpty(progressId)) {
          WsUtils.onEmptyMessage(message);
        
        } else if (pm.isActivated()) {
          boolean started = Endpoint.startPropgress(progressId);
          if (started) {
            logger.debug("progress", progressId, "started");
          } else {
            logger.warning("cannot start progress", progressId);
          }

        } else if (pm.isUpdate()) {
          if (Global.isDebug()) {
            logger.debug(message);
          }
          BeeKeeper.getScreen().updateProgress(progressId, pm.getValue());

        } else if (pm.isCanceled() || pm.isClosed()) {
          logger.debug(message);
          BeeKeeper.getScreen().removeProgress(progressId);
        
        } else {
          WsUtils.onInvalidState(message);
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
          WsUtils.onInvalidState(message);
        }
        break;

      case SHOW:
        Subject subject = ((ShowMessage) message).getSubject();
        if (subject == Subject.SESSION) {
          Global.showGrid(subject.getCaption(), new PropertiesData(Endpoint.getInfo()));
        } else {
          WsUtils.onInvalidState(message);
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
          WsUtils.onEmptyMessage(message);
        } else {
          Endpoint.setSessionId(users.get(users.size() - 1).getSessionId());
        }
        break;
    }
  }
}
