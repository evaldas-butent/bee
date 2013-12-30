package com.butent.bee.client.websocket;

import com.google.common.base.Objects;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.Position.Coordinates;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.cli.CliWorker;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.maps.MapUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ChatRoom;
import com.butent.bee.shared.data.PropertiesData;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.websocket.SessionUser;
import com.butent.bee.shared.websocket.WsUtils;
import com.butent.bee.shared.websocket.messages.AdminMessage;
import com.butent.bee.shared.websocket.messages.ChatMessage;
import com.butent.bee.shared.websocket.messages.EchoMessage;
import com.butent.bee.shared.websocket.messages.InfoMessage;
import com.butent.bee.shared.websocket.messages.LocationMessage;
import com.butent.bee.shared.websocket.messages.LogMessage;
import com.butent.bee.shared.websocket.messages.Message;
import com.butent.bee.shared.websocket.messages.NotificationMessage;
import com.butent.bee.shared.websocket.messages.OnlineMessage;
import com.butent.bee.shared.websocket.messages.ProgressMessage;
import com.butent.bee.shared.websocket.messages.RoomStateMessage;
import com.butent.bee.shared.websocket.messages.RoomUserMessage;
import com.butent.bee.shared.websocket.messages.RoomsMessage;
import com.butent.bee.shared.websocket.messages.SessionMessage;
import com.butent.bee.shared.websocket.messages.ShowMessage;
import com.butent.bee.shared.websocket.messages.ShowMessage.Subject;
import com.butent.bee.shared.websocket.messages.UsersMessage;

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
        
      case CHAT:
        ChatMessage chatMessage = (ChatMessage) message;
        
        if (chatMessage.isValid()) {
          Global.getRooms().addMessage(chatMessage);
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
          String title = Global.getUsers().getUserSignatureBySession(from);
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

      case NOTIFICATION:
        NotificationMessage notificationMessage = (NotificationMessage) message;
        
        if (notificationMessage.isValid()) {
          Long fromUser = Global.getUsers().getUserIdBySession(notificationMessage.getFrom());
          String signature = Global.getUsers().getSignature(fromUser);
          String text = notificationMessage.getText();
          
          Icon icon;
          if (text.endsWith(BeeConst.STRING_QUESTION)) {
            icon = Icon.QUESTION;
          } else if (text.endsWith(BeeConst.STRING_EXCLAMATION)) {
            icon = Icon.ALARM;
          } else {
            icon = null;
          }
          
          if (icon == null) {
            BeeKeeper.getScreen().notifyInfo(signature, text);
          } else {
            Global.messageBox(signature, icon, text);
          }

        } else {
          WsUtils.onEmptyMessage(message);
        }
        
        break;
        
      case ONLINE:
        List<SessionUser> sessionUsers = ((OnlineMessage) message).getSessionUsers();
        if (sessionUsers.size() > 1) {
          for (int i = 0; i < sessionUsers.size() - 1; i++) {
            SessionUser sessionUser = sessionUsers.get(i);
            Global.getUsers().addSession(sessionUser.getSessionId(), sessionUser.getUserId(), true);
          }
        }

        if (sessionUsers.isEmpty()) {
          WsUtils.onEmptyMessage(message);
        } else {
          Endpoint.setSessionId(sessionUsers.get(sessionUsers.size() - 1).getSessionId());
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
        
      case ROOM_STATE:
        RoomStateMessage rsm = (RoomStateMessage) message;
        
        if (rsm.isValid()) {
          Global.getRooms().onRoomState(rsm);
        } else {
          WsUtils.onInvalidState(message);
        }
        break;

      case ROOM_USER:
        Global.getRooms().onRoomUser((RoomUserMessage) message);
        break;

      case ROOMS:
        List<ChatRoom> rooms = ((RoomsMessage) message).getData();

        if (BeeUtils.isEmpty(rooms)) {
          WsUtils.onEmptyMessage(message);
        } else {
          Global.getRooms().setRoomData(rooms);
        }
        break;
        
      case SESSION:
        SessionMessage sm = (SessionMessage) message;
        SessionUser su = sm.getSessionUser();

        if (sm.isOpen()) {
          Global.getUsers().addSession(su.getSessionId(), su.getUserId(), false);
        } else if (sm.isClosed()) {
          Global.getUsers().removeSession(su.getSessionId());
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
        List<UserData> users = ((UsersMessage) message).getData();

        if (BeeUtils.isEmpty(users)) {
          WsUtils.onEmptyMessage(message);
        
        } else {
          Global.getUsers().updateUserData(users);
          
          for (UserData userData : users) {
            if (Objects.equal(BeeKeeper.getUser().getUserId(), userData.getUserId())) {
              BeeKeeper.getUser().setUserData(userData);
              BeeKeeper.getScreen().updateUserData(userData);
              break;
            }
          }
        }
        break;
    }
  }
}
