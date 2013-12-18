package com.butent.bee.client.websocket;

import com.google.common.collect.Lists;

import com.butent.bee.client.dom.Features;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

import elemental.events.MessageEvent;
import elemental.events.CloseEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.client.Browser;
import elemental.html.WebSocket;

public final class Endpoint {
  
  private enum ReadyState {
    CONNECTING(0),
    OPEN(1),
    CLOSING(2),
    CLOSED(3);
    
    private static ReadyState getByValue(int value) {
      for (ReadyState state : values()) {
        if (state.value == value) {
          return state;
        }
      }
      return null;
    }
    
    private final int value; 

    private ReadyState(int value) {
      this.value = value;
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(Endpoint.class);

  private static WebSocket socket;

  public static void close() {
    if (socket != null && !isClosed()) {
      socket.close();
    }
  }
  
  public static List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();
    
    if (socket == null) {
      info.add(new Property(WebSocket.class.getSimpleName(), BeeConst.NULL));
    } else {
      PropertyUtils.addProperties(info,
          "Binary Type", socket.getBinaryType(),
          "Buffered Amount", socket.getBufferedAmount(),
          "Extensions", socket.getExtensions(),
          "Protocol", socket.getProtocol(),
          "Ready State", getReadyState(),
          "Url", socket.getUrl());
    }
    
    return info;
  }

  public static boolean isClosed() {
    return socket != null && socket.getReadyState() == ReadyState.CLOSED.value;
  }
  
  public static boolean isOpen() {
    return socket != null && socket.getReadyState() == ReadyState.OPEN.value;
  }

  public static void open() {
    if (Features.supportsWebSockets() && (socket == null || isClosed())) {
      socket = Browser.getWindow().newWebSocket(url());

      if (socket != null) {
        socket.setOnopen(new EventListener() {
          @Override
          public void handleEvent(Event evt) {
            onOpen();
          }
        });

        socket.setOnclose(new EventListener() {
          @Override
          public void handleEvent(Event evt) {
            onClose((CloseEvent) evt);
          }
        });

        socket.setOnerror(new EventListener() {
          @Override
          public void handleEvent(Event evt) {
            onError(evt);
          }
        });

        socket.setOnmessage(new EventListener() {
          @Override
          public void handleEvent(Event evt) {
            onMessage((MessageEvent) evt);
          }
        });
      }
    }
  }
  
  public static void send(String data) {
    if (isOpen()) {
      socket.send(data);
    }
  }
  
  private static String getReadyState() {
    if (socket == null) {
      return null;

    } else {
      int value = socket.getReadyState();
      ReadyState readyState = ReadyState.getByValue(value);
      
      return (readyState == null) ? Integer.toString(value) : readyState.name().toLowerCase();
    }
  }

  private static void onClose(CloseEvent event) {
    if (socket != null) {
      logger.debug(socket.getClass().getSimpleName(), getReadyState());
    }
    if (event != null) {
      logger.debug(NameUtils.getName(event), event.getCode(), event.getReason());
    }
  }

  private static void onError(Event event) {
    logger.severe(Endpoint.class.getSimpleName(), "error", event);
  }

  private static void onMessage(MessageEvent event) {
    Object data = event.getData();
    if (data != null) {
      logger.debug(NameUtils.getName(data), data);
      logger.addSeparator();
    }
  }

  private static void onOpen() {
    if (socket != null) {
      logger.debug(socket.getClass().getSimpleName(), socket.getUrl(), getReadyState());
    }
  }
  
  private static String url() {
    String href = Browser.getWindow().getLocation().getHref();
    return "ws" + Paths.ensureEndSeparator(href.substring(4)) + "ws";
  }

  private Endpoint() {
  }
}
