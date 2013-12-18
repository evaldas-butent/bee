package com.butent.bee.server.websocket;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.websocket.InfoMessage;
import com.butent.bee.shared.websocket.Message;
import com.butent.bee.shared.websocket.TextMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.CloseReason;
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

  private static List<Property> getExtensionInfo(Extension extension) {
    List<Property> info = Lists.newArrayList();
    
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
  
  private static List<Property> getOpenSessionsInfo(Collection<Session> sessions) {
    int size = (sessions == null) ? 0 : sessions.size();

    List<Property> info = Lists.newArrayList();
    info.add(new Property("Open Sessions", BeeUtils.bracket(size)));
    
    if (size > 0) {
      int index = 0;

      for (Session session : sessions) {
        String prefix = "Session " + Integer.toString(index++) + BeeConst.STRING_SPACE;
        
        PropertyUtils.addProperties(info,
            prefix + "Id", session.getId(),
            prefix + "User Principal", session.getUserPrincipal(),
            prefix + "User Id", getUserId(session),
            prefix + "Open", session.isOpen());
      }
    }
    
    return info;
  }

  private static List<Property> getSessionInfo(Session session) {
    List<Property> info = Lists.newArrayList();
    
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
          "User Principal", session.getUserPrincipal(),
          "User Properties", session.getUserProperties(),
          "Open", session.isOpen(),
          "Secure", session.isSecure());
    }
    
    return info;
  }
  
  private static Long getUserId(Session session) {
    return (session == null) ? null : (Long) session.getUserProperties().get(PROPERTY_USER_ID);
  }

  private static List<Property> getWebSocketContainerInfo(WebSocketContainer wsc) {
    List<Property> info = Lists.newArrayList();
    
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
    try {
      session.getBasicRemote().sendText(message.encode());
    } catch (IOException ex) {
      logger.error(ex);
    }
  }
  
  private static void setUserId(Session session, Long userId) {
    session.getUserProperties().put(PROPERTY_USER_ID, userId);
  }
  
  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    openSessions.remove(session);
    
//    String reasonInfo;
//    if (closeReason == null) {
//      reasonInfo = null;
//    } else {
//      int code = closeReason.getCloseCode().getCode();
//      
//      for (CloseCodes closeCodes : closeCodes.values()) {
//        
//      }
//   
//    
//    reasonInfo = BeeUtils.joinOptions("code", (code == null) ? null
//            : BeeUtils.joinWords(code.toString(), ""), closeReason.getReasonPhrase());
//    }
    
    logger.info("ws close", session.getId(), closeReason);
  }
  
  @OnError
  public void onError(Session session, Throwable thr) {
    openSessions.remove(session);
    logger.error(thr, "ws error", session.getId());
  }
  
  @OnMessage
  public void onMessage(Session session, String received) {
    logger.debug("ws message", session.getId(), received);
    
    Message message;
    
    if (BeeUtils.same(received, "?")) {
      message = new InfoMessage("Session", getSessionInfo(session));
    } else if (BeeUtils.same(received, "who")) {
      message = new InfoMessage("Open Sessions", getOpenSessionsInfo(openSessions));
    } else {
      message = new TextMessage(received);
    }
    
    send(session, message);
  }
  
  @OnOpen
  public void onOpen(@PathParam("user-id") Long userId, Session session) {
    setUserId(session, userId);
    openSessions.add(session);

    logger.info("ws open", session.getId(), session.getUserPrincipal(), userId);
  }
}
