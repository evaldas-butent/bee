package com.butent.bee.client;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ChatRoom;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.websocket.messages.ChatMessage;

import java.util.List;

public class Rooms {

  private static final class RoomsPanel extends Flow {

    private RoomsPanel() {
      super(STYLE_PREFIX + "panel");

      FaLabel plusWidget = new FaLabel(FontAwesome.PLUS_SQUARE_O);
      plusWidget.addStyleName(STYLE_PREFIX + "plus");
      add(plusWidget);
    }

    private void addRoomWidget(RoomWidget roomWidget) {
      int count = getWidgetCount();
      insert(roomWidget, count - 1);
    }

    private RoomWidget findRoomWidget(long roomId) {
      for (Widget widget : this) {
        if (widget instanceof RoomWidget
            && Objects.equal(((RoomWidget) widget).getRoomId(), roomId)) {
          return (RoomWidget) widget;
        }
      }

      logger.warning("widget not found for room", roomId);
      return null;
    }
  }

  private static final class RoomWidget extends Flow {

    private static final int NAME_INDEX = 0;

    private final long roomId;

    private RoomWidget(ChatRoom chatRoom) {
      super(STYLE_PREFIX + "item");
      this.roomId = chatRoom.getId();

      CustomDiv nameWidget = new CustomDiv(STYLE_PREFIX + "name");
      nameWidget.setText(chatRoom.getName());

      nameWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
        }
      });

      add(nameWidget);

      FaLabel infoWidget = new FaLabel(FontAwesome.INFO_CIRCLE);
      infoWidget.addStyleName(STYLE_PREFIX + "info");
      add(infoWidget);

      if (chatRoom.isOwner(BeeKeeper.getUser().getUserId())) {
        FaLabel settingsWidget = new FaLabel(FontAwesome.GEAR);
        settingsWidget.addStyleName(STYLE_PREFIX + "settings");
        add(settingsWidget);

        FaLabel deleteWidget = new FaLabel(FontAwesome.TRASH_O);
        deleteWidget.addStyleName(STYLE_PREFIX + "delete");
        add(deleteWidget);
      }
    }

    private long getRoomId() {
      return roomId;
    }

    private void update(ChatRoom chatRoom) {
      if (chatRoom != null) {
        getWidget(NAME_INDEX).getElement().setInnerText(chatRoom.getName());
      }
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(Rooms.class);

  private static final String STYLE_PREFIX = "bee-Rooms-";
  private static final String STYLE_UPDATED = STYLE_PREFIX + "updated";

  private final List<ChatRoom> chatRooms = Lists.newArrayList();

  private final RoomsPanel roomsPanel = new RoomsPanel();

  private final List<ChatMessage> pendingMessages = Lists.newArrayList();
  private boolean initialized;

  private Label sizeBadge;

  Rooms() {
  }

  public void addMessage(ChatMessage chatMessage) {
    Assert.notNull(chatMessage);

    if (isInitialized()) {
      ChatRoom room = findRoom(chatMessage.getRoomId(), true);
      if (room != null) {
        room.addMessage(chatMessage.getTextMessage());
      }

    } else {
      pendingMessages.add(chatMessage);
    }
  }

  public IdentifiableWidget getRoomsPanel() {
    return roomsPanel;
  }

  public void setRoomData(List<ChatRoom> data) {
    Assert.notNull(data);

    if (isInitialized()) {

    } else {
      if (!chatRooms.isEmpty()) {
        chatRooms.clear();
      }
      chatRooms.addAll(data);

      for (ChatRoom room : data) {
        RoomWidget widget = new RoomWidget(room);
        roomsPanel.addRoomWidget(widget);
      }

      setInitialized(true);
    }

    if (!pendingMessages.isEmpty()) {
      for (ChatMessage chatMessage : pendingMessages) {
        ChatRoom room = findRoom(chatMessage.getRoomId(), true);
        if (room != null) {
          room.addMessage(chatMessage.getTextMessage());
        }
      }

      pendingMessages.clear();
    }
    
    updateHeader();
    logger.info("rooms", chatRooms.size());
  }

  private ChatRoom findRoom(long roomId, boolean warn) {
    for (ChatRoom room : chatRooms) {
      if (Objects.equal(room.getId(), roomId)) {
        return room;
      }
    }

    if (warn) {
      logger.warning("room not found:", roomId);
    }
    return null;
  }

  private Label getSizeBadge() {
    return sizeBadge;
  }

  private boolean isInitialized() {
    return initialized;
  }

  private void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  private void setSizeBadge(Label sizeBadge) {
    this.sizeBadge = sizeBadge;
  }

  private void updateHeader() {
    Flow header = BeeKeeper.getScreen().getDomainHeader(Domain.ROOMS, null);
    if (header == null) {
      logger.warning(Domain.ROOMS, "header not available");
      return;
    }

    if (getSizeBadge() == null) {
      Label badge = new Label();
      badge.addStyleName(STYLE_PREFIX + "size");
      header.add(badge);

      setSizeBadge(badge);
    }

    getSizeBadge().setText(BeeUtils.toString(chatRooms.size()));

    getSizeBadge().removeStyleName(STYLE_UPDATED);
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        getSizeBadge().addStyleName(STYLE_UPDATED);
      }
    });
  }
}
