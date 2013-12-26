package com.butent.bee.server.websocket;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.websocket.SessionUser;
import com.butent.bee.shared.websocket.WsUtils;
import com.butent.bee.shared.websocket.messages.HasRecipient;
import com.butent.bee.shared.websocket.messages.InfoMessage;
import com.butent.bee.shared.websocket.messages.LogMessage;
import com.butent.bee.shared.websocket.messages.Message;
import com.butent.bee.shared.websocket.messages.ProgressMessage;
import com.butent.bee.shared.websocket.messages.SessionMessage;
import com.butent.bee.shared.websocket.messages.ShowMessage;
import com.butent.bee.shared.websocket.messages.OnlineMessage;
import com.butent.bee.shared.websocket.messages.ShowMessage.Subject;
import com.butent.bee.shared.websocket.messages.UsersMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Extension;
import javax.websocket.Extension.Parameter;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/{user-id}")
public class Endpoint {

  private static final String PROPERTY_USER_ID = "UserId";

  private static BeeLogger logger = LogUtils.getLogger(Endpoint.class);

  private static Queue<Session> openSessions = new ConcurrentLinkedQueue<>();
  private static Map<String, String> progressToSession = new ConcurrentHashMap<>();
  
  public static boolean updateProgress(String progressId, double value) {
    String sessionId = progressToSession.get(progressId);

    if (BeeUtils.isEmpty(sessionId)) {
      logger.info("ws session not found for progress:", progressId, "value", value);
      return false;
    
    } else {
      Session session = findOpenSession(sessionId, true);
      if (session != null) {
        send(session, ProgressMessage.update(progressId, value));
      }
      
      return true;
    }
  }

  public static void updateUserData(List<UserData> data) {
    if (BeeUtils.isEmpty(data)) {
      logger.severe("user data is empty");
    
    } else if (!openSessions.isEmpty()) {
      UsersMessage message = new UsersMessage(data);

      for (Session session : openSessions) {
        if (session.isOpen()) {
          send(session, message);
        }
      }
    }
  }

  private static void dispatch(Session session, Message message) {
    switch (message.getType()) {
      case ADMIN:
      case LOCATION:
        Session toSession = findOpenSession(((HasRecipient) message).getTo(), true);
        if (toSession != null) {
          send(toSession, message);
        }
        break;
        
      case ECHO:
        send(session, message);
        break;
        
      case LOG:
        LogMessage logMessage = (LogMessage) message;
        LogLevel level = logMessage.getLevel();
        
        if (level == null) {
          WsUtils.onInvalidState(message, toLog(session));
        
        } else if (BeeUtils.isEmpty(logMessage.getText())) {
          logger.setLevel(level);
          logger.log(level, "ws log level set to:", level, toLog(session));

        } else {
          logger.log(level, logMessage.getText());
        }
        break;
        
      case PROGRESS:
        ProgressMessage pm = (ProgressMessage) message;
        String progressId = pm.getProgressId();
        
        if (BeeUtils.isEmpty(progressId)) {
          WsUtils.onEmptyMessage(message, toLog(session));
          
        } else if (pm.isOpen()) {
          progressToSession.put(progressId, session.getId());
          logger.info("ws activated progress:", progressId, "session:", session.getId());
          
          send(session, ProgressMessage.activate(progressId));

        } else if (pm.isClosed() || pm.isCanceled()) {
          String removed = progressToSession.remove(progressId);
          logger.debug("ws remove progress:", progressId, "session:", removed);
        
        } else {
          WsUtils.onInvalidState(message, toLog(session));
        }
        break;

      case SHOW:
        Subject subject = ((ShowMessage) message).getSubject();
        if (subject == null) {
          WsUtils.onEmptyMessage(message, toLog(session));

        } else {
          switch (subject) {
            case ENDPOINT:
              send(session, new InfoMessage(subject.getCaption(), getInfo()));
              break;

            case OPEN_SESSIONS:
              send(session, new InfoMessage(subject.getCaption(),
                  getOpenSessionsInfo(openSessions)));
              break;

            case SESSION:
              send(session, new InfoMessage(subject.getCaption(), getSessionInfo(session)));
              break;
          }
        }
        break;

      case INFO:
      case ONLINE:
      case SESSION:
      case USERS:
        logger.severe("ws message not supported", message, toLog(session));
        break;
    }
  }
  
  private static Session findOpenSession(String sessionId, boolean warn) {
    for (Session session : openSessions) {
      if (session.getId().endsWith(sessionId) && session.isOpen()) {
        return session;
      }
    }
    
    if (warn) {
      logger.warning("ws open session not found", sessionId);
    }
    return null;
  }

  private static List<Property> getExtensionInfo(Extension extension) {
    List<Property> info = new ArrayList<>();

    if (extension == null) {
      info.add(new Property(Extension.class.getSimpleName(), BeeConst.NULL));
    } else {
      info.add(new Property("Name", extension.getName()));

      List<Parameter> parameters = extension.getParameters();
      if (!BeeUtils.isEmpty(parameters)) {
        info.add(new Property("Parameters", BeeUtils.bracket(parameters.size())));

        for (Parameter parameter : parameters) {
          info.add(new Property(parameter.getName(), parameter.getValue()));
        }
      }
    }

    return info;
  }

  private static List<Property> getInfo() {
    List<Property> info = getOpenSessionsInfo(openSessions);
    
    int size = progressToSession.size();
    info.add(new Property("Progress", BeeUtils.bracket(size)));
    if (size > 0) {
      info.addAll(PropertyUtils.createProperties(progressToSession));
    }
    
    LogLevel level = logger.getLevel();
    if (level != null) {
      info.add(new Property("Log Level", level.name()));
    }

    return info;
  }
  
  private static List<Property> getOpenSessionsInfo(Collection<Session> sessions) {
    int size = (sessions == null) ? 0 : sessions.size();

    List<Property> info = new ArrayList<>();
    info.add(new Property("Open Sessions", BeeUtils.bracket(size)));

    if (size > 0) {
      int index = 0;

      for (Session session : sessions) {
        String prefix = "Session " + Integer.toString(index++) + BeeConst.STRING_SPACE;

        PropertyUtils.addProperties(info,
            prefix + "Id", session.getId(),
            prefix + "User Principal", getUserName(session),
            prefix + "User Id", getUserId(session),
            prefix + "Open", session.isOpen());
      }
    }

    return info;
  }

  private static List<Property> getSessionInfo(Session session) {
    List<Property> info = new ArrayList<>();

    if (session == null) {
      info.add(new Property(Session.class.getSimpleName(), BeeConst.NULL));

    } else {
      Async async = session.getAsyncRemote();
      if (async != null) {
        PropertyUtils.addProperties(info,
            "Async Remote: Batching Allowed", async.getBatchingAllowed(),
            "Async Remote: Send Timeout", async.getSendTimeout());
      }

      Basic basic = session.getBasicRemote();
      if (basic != null) {
        PropertyUtils.addProperties(info,
            "Basic Remote: Batching Allowed", basic.getBatchingAllowed());
      }

      WebSocketContainer wsc = session.getContainer();
      if (wsc != null) {
        info.add(new Property("Container", wsc.getClass().getSimpleName()));
        info.addAll(getWebSocketContainerInfo(wsc));
      }

      PropertyUtils.addProperties(info,
          "Id", session.getId(),
          "Max Binary Message Buffer Size", session.getMaxBinaryMessageBufferSize(),
          "Max Idle Timeout", session.getMaxIdleTimeout(),
          "Max Text Message Buffer Size", session.getMaxTextMessageBufferSize());

      List<Extension> extensions = session.getNegotiatedExtensions();
      if (!BeeUtils.isEmpty(extensions)) {
        info.add(new Property("Negotiated Extensions", BeeUtils.bracket(extensions.size())));

        for (Extension extension : extensions) {
          info.addAll(getExtensionInfo(extension));
        }
      }

      PropertyUtils.addProperties(info,
          "Negotiated Subprotocol", session.getNegotiatedSubprotocol(),
          "Open Sessions", session.getOpenSessions().size(),
          "Path Parameters", session.getPathParameters(),
          "Protocol Version", session.getProtocolVersion(),
          "Query String", session.getQueryString(),
          "Request Parameter Map", session.getRequestParameterMap(),
          "Request URI", session.getRequestURI(),
          "User Principal", getUserName(session),
          "User Properties", session.getUserProperties(),
          "Open", session.isOpen(),
          "Secure", session.isSecure());
    }

    return info;
  }
  
  private static SessionUser getSessionUser(Session session) {
    return new SessionUser(session.getId(), getUserId(session));
  }

  private static Long getUserId(Session session) {
    if (session != null && session.getUserProperties() != null) {
      Object value = session.getUserProperties().get(PROPERTY_USER_ID);
      if (value instanceof Long) {
        return (Long) value;
      }
    }
    return null;
  }

  private static String getUserName(Session session) {
    return (session == null || session.getUserPrincipal() == null) ? null
        : session.getUserPrincipal().getName();
  }

  private static List<Property> getWebSocketContainerInfo(WebSocketContainer wsc) {
    List<Property> info = new ArrayList<>();

    if (wsc == null) {
      info.add(new Property(WebSocketContainer.class.getSimpleName(), BeeConst.NULL));
    } else {
      PropertyUtils.addProperties(info,
          "Default Async Send Timeout", wsc.getDefaultAsyncSendTimeout(),
          "Default Max Binary Message Buffer Size", wsc.getDefaultMaxBinaryMessageBufferSize(),
          "Default Max Session Idle Timeout", wsc.getDefaultMaxSessionIdleTimeout(),
          "Default Max Text Message Buffer Size", wsc.getDefaultMaxTextMessageBufferSize());

      Set<Extension> extensions = wsc.getInstalledExtensions();
      if (!BeeUtils.isEmpty(extensions)) {
        info.add(new Property("Installed Extensions", BeeUtils.bracket(extensions.size())));

        for (Extension extension : extensions) {
          info.addAll(getExtensionInfo(extension));
        }
      }
    }

    return info;
  }

  private static void send(Session session, Message message) {
    String text = message.encode();
    session.getAsyncRemote().sendText(message.encode());

    logger.debug("->", message);
    logger.info("->", message.getType(), "length:", text.length(), toLog(session));
  }

  private static void setUserId(Session session, Long userId) {
    session.getUserProperties().put(PROPERTY_USER_ID, userId);
  }

  private static String toLog(Session session) {
    if (session == null) {
      return null;
    } else {
      Long userId = getUserId(session);
      return BeeUtils.joinOptions("session", session.getId(),
          "principal", getUserName(session),
          "user", (userId == null) ? null : userId.toString());
    }
  }

  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    openSessions.remove(session);
    
    if (!progressToSession.isEmpty() && progressToSession.values().contains(session.getId())) {
      Set<String> keys = new HashSet<>();
      
      for (Map.Entry<String, String> entry : progressToSession.entrySet()) {
        if (entry.getValue().equals(session.getId())) {
          keys.add(entry.getKey());
        }
      }
      
      for (String key : keys) {
        progressToSession.remove(key);
      }
    }
    
    String reasonInfo;
    if (closeReason == null) {
      reasonInfo = null;

    } else {
      Integer code = (closeReason.getCloseCode() == null) ? null
          : closeReason.getCloseCode().getCode();
      String codeInfo = null;

      if (code != null) {
        for (CloseCodes cc : CloseCodes.values()) {
          if (code.equals(cc.getCode())) {
            codeInfo = BeeUtils.joinWords(code, cc.name());
            break;
          }
        }

        if (codeInfo == null) {
          codeInfo = code.toString();
        }
      }

      reasonInfo = BeeUtils.joinOptions("code", codeInfo, "phrase", closeReason.getReasonPhrase());
    }

    logger.info("ws close", reasonInfo, toLog(session));
    
    if (!openSessions.isEmpty()) {
      SessionUser sessionUser = getSessionUser(session);

      for (Session openSession : openSessions) {
        if (openSession.isOpen()) {
          send(openSession, SessionMessage.close(sessionUser));
        }
      }
    }
  }

  @OnError
  public void onError(Session session, Throwable thr) {
    openSessions.remove(session);
    logger.error(thr, "ws error", toLog(session));
  }

  @OnMessage
  public void onMessage(Session session, String data) {
    if (BeeUtils.isEmpty(data)) {
      logger.warning("ws received empty message", toLog(session));

    } else {
      Message message = Message.decode(data);
      if (message != null) {
        logger.debug("<-", message);
        logger.info("<-", message.getType(), "length:", data.length(), toLog(session));
        dispatch(session, message);
      }
    }
  }

  @OnOpen
  public void onOpen(@PathParam("user-id") Long userId, Session session) {
    setUserId(session, userId);
    SessionUser sessionUser = getSessionUser(session);

    List<SessionUser> sessionUsers = new ArrayList<>();

    if (!openSessions.isEmpty()) {
      for (Session openSession : openSessions) {
        if (openSession.isOpen()) {
          send(openSession, SessionMessage.open(sessionUser));
          sessionUsers.add(getSessionUser(openSession));
        }
      }
    }

    openSessions.add(session);

    logger.info("ws open", toLog(session));
    
    sessionUsers.add(sessionUser);
    send(session, new OnlineMessage(sessionUsers));
  }
}
