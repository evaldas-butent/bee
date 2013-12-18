package com.butent.bee.client.websocket;

import com.google.common.collect.Lists;

import com.butent.bee.client.dom.Features;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.websocket.Message;

import java.util.List;

import elemental.js.events.JsEvent;
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

  private static final String LOG_PREFIX = "WS Endpoint";

  private static BeeLogger logger = LogUtils.getLogger(Endpoint.class);

  private static WebSocket socket;

  private static MessageDispatcher dispatcher = new MessageDispatcher();

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

  public static void open(Long userId) {
    if (Features.supportsWebSockets() && (socket == null || isClosed()) && userId != null) {
      socket = Browser.getWindow().newWebSocket(url(userId));

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
            onError((JsEvent) evt);
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
      String eventInfo = (event == null) ? null 
          : BeeUtils.joinOptions("code", Integer.toString(event.getCode()),
              "reason", event.getReason());
      logger.info(LOG_PREFIX, socket.getUrl(), getReadyState(), eventInfo);
    }
  }

  private static void onError(JsEvent event) {
    logger.severe(LOG_PREFIX, "error", JsUtils.toJson(event));
  }

  private static void onMessage(MessageEvent event) {
    Object data = event.getData();

    if (data instanceof String) {
      logger.debug(LOG_PREFIX, "received length:", ((String) data).length());

      Message message = Message.decode((String) data);
      if (message != null) {
        dispatcher.dispatch(message);
      }

    } else if (data == null) {
      logger.warning(LOG_PREFIX, "received data is null");
    } else {
      logger.warning(LOG_PREFIX, "unknown received data type", NameUtils.getName(data));
    }
  }

  private static void onOpen() {
    if (socket != null) {
      logger.info(LOG_PREFIX, socket.getUrl(), getReadyState());
    }
  }

  private static String url(long userId) {
    String href = Browser.getWindow().getLocation().getHref();
    return "ws" + Paths.ensureEndSeparator(href.substring(4)) + "ws/" + Long.toString(userId);
  }

  private Endpoint() {
  }
}
