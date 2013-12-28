package com.butent.bee.client;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.communication.Chat;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.websocket.Endpoint;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.communication.ChatRoom;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.websocket.messages.ChatMessage;
import com.butent.bee.shared.websocket.messages.RoomStateMessage;
import com.butent.bee.shared.websocket.messages.RoomUserMessage;

import java.util.ArrayList;
import java.util.List;

public class Rooms implements HasInfo {

  private final class RoomsPanel extends Flow {

    private RoomsPanel() {
      super(STYLE_PREFIX + "panel");

      FaLabel plusWidget = new FaLabel(FontAwesome.PLUS_SQUARE_O);
      plusWidget.addStyleName(STYLE_PREFIX + "plus");

      plusWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          createRoom();
        }
      });

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

  private final class RoomWidget extends Flow {

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
          enterRoom(roomId);
        }
      });

      add(nameWidget);

      FaLabel infoWidget = new FaLabel(FontAwesome.INFO_CIRCLE);
      infoWidget.addStyleName(STYLE_PREFIX + "info");

      infoWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showInfo(roomId);
        }
      });

      add(infoWidget);

      if (chatRoom.isOwner(BeeKeeper.getUser().getUserId())) {
        FaLabel settingsWidget = new FaLabel(FontAwesome.GEAR);
        settingsWidget.addStyleName(STYLE_PREFIX + "settings");
        add(settingsWidget);

        FaLabel deleteWidget = new FaLabel(FontAwesome.TRASH_O);
        deleteWidget.addStyleName(STYLE_PREFIX + "delete");

        deleteWidget.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            ChatRoom room = findRoom(roomId);
            if (room != null) {
              Endpoint.send(RoomStateMessage.remove(room));
            }
          }
        });

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

  public static void leaveRoom(long roomId) {
    Endpoint.send(RoomUserMessage.leave(roomId, BeeKeeper.getUser().getUserId()));
  }

  private static void enterRoom(long roomId) {
    Chat chat = findChat(roomId);

    if (chat == null) {
      Endpoint.send(RoomUserMessage.enter(roomId, BeeKeeper.getUser().getUserId()));
    } else {
      BeeKeeper.getScreen().activateWidget(chat);
    }
  }

  private static Chat findChat(long roomId) {
    List<IdentifiableWidget> openWidgets = BeeKeeper.getScreen().getOpenWidgets();
    for (IdentifiableWidget widget : openWidgets) {
      if (widget instanceof Chat && Objects.equal(((Chat) widget).getRoomId(), roomId)) {
        return (Chat) widget;
      }
    }
    return null;
  }

  private final List<ChatRoom> chatRooms = Lists.newArrayList();

  private final RoomsPanel roomsPanel = new RoomsPanel();

  private Label sizeBadge;

  Rooms() {
  }

  public void addMessage(ChatMessage chatMessage) {
    Assert.notNull(chatMessage);

    ChatRoom room = findRoom(chatMessage.getRoomId());
    if (room != null) {
      room.addMessage(chatMessage.getTextMessage());

      Chat chat = findChat(room.getId());
      if (chat != null) {
        chat.addMessage(chatMessage.getTextMessage(), true);
      }
    }
  }

  @Override
  public List<Property> getInfo() {
    List<Property> info = new ArrayList<>();
    info.add(new Property("Client Rooms", BeeUtils.bracket(chatRooms.size())));

    for (ChatRoom room : chatRooms) {
      info.addAll(room.getInfo());

      Chat chat = findChat(room.getId());
      if (chat != null) {
        info.add(new Property("Chat View", chat.getId()));
      }
    }
    return info;
  }

  public IdentifiableWidget getRoomsPanel() {
    return roomsPanel;
  }

  public void onRoomState(RoomStateMessage roomStateMessage) {
    Assert.notNull(roomStateMessage);
    Assert.isTrue(roomStateMessage.isValid());

    ChatRoom room = roomStateMessage.getRoom();
    Chat chat;

    if (roomStateMessage.isLoading()) {
      chat = new Chat(room);
      BeeKeeper.getScreen().showWidget(chat, true);

      updateRoom(room);

    } else if (roomStateMessage.isNew()) {
      if (contains(room.getId())) {
        logger.warning("attempt to add existing room:", room.getId());
      } else {
        chatRooms.add(room);

        RoomWidget widget = new RoomWidget(room);
        roomsPanel.addRoomWidget(widget);

        updateHeader();
        logger.info("added room", room.getName());
      }

    } else if (roomStateMessage.isUpdated()) {
      updateRoom(room);

      chat = findChat(room.getId());
      if (chat != null) {
        chat.onRoomUpdate(room);
      }

    } else if (roomStateMessage.isRemoved()) {
      removeRoom(room.getId());

    } else {
      logger.warning("unrecongnized room state:", roomStateMessage.getState());
    }
  }

  private void updateRoom(ChatRoom source) {
    ChatRoom target = findRoom(source.getId());

    if (target != null) {
      target.setName(source.getName());
      target.setType(source.getType());

      BeeUtils.overwrite(target.getOwners(), source.getOwners());
      BeeUtils.overwrite(target.getDwellers(), source.getDwellers());

      BeeUtils.overwrite(target.getUsers(), source.getUsers());

      target.setMessageCount(source.getMessageCount());
      target.setMaxTime(source.getMaxTime());

      RoomWidget widget = roomsPanel.findRoomWidget(source.getId());
      if (widget != null) {
        widget.update(source);
      }
    }
  }

  public void onRoomUser(RoomUserMessage roomUserMessage) {
    Assert.notNull(roomUserMessage);

    ChatRoom room = findRoom(roomUserMessage.getRoomId());

    if (room != null) {
      if (roomUserMessage.join()) {
        room.join(roomUserMessage.getUserId());
      } else {
        room.quit(roomUserMessage.getUserId());
      }

      Chat chat = findChat(room.getId());
      if (chat != null) {
        chat.onRoomUpdate(room);
      }
    }
  }

  public void setRoomData(List<ChatRoom> data) {
    Assert.notNull(data);

    if (!chatRooms.isEmpty()) {
      for (ChatRoom room : chatRooms) {
        RoomWidget widget = roomsPanel.findRoomWidget(room.getId());
        if (widget != null) {
          roomsPanel.remove(widget);
        }
      }

      chatRooms.clear();
    }

    chatRooms.addAll(data);

    for (ChatRoom room : data) {
      RoomWidget widget = new RoomWidget(room);
      roomsPanel.addRoomWidget(widget);
    }

    updateHeader();
    logger.info("rooms", chatRooms.size());
  }

  private void createRoom() {

  }

  private ChatRoom findRoom(long roomId) {
    for (ChatRoom room : chatRooms) {
      if (room.is(roomId)) {
        return room;
      }
    }

    logger.warning("room not found:", roomId);
    return null;
  }

  private boolean contains(long roomId) {
    for (ChatRoom room : chatRooms) {
      if (room.is(roomId)) {
        return true;
      }
    }
    return false;
  }

  private Label getSizeBadge() {
    return sizeBadge;
  }

  private void setSizeBadge(Label sizeBadge) {
    this.sizeBadge = sizeBadge;
  }

  private void removeRoom(long roomId) {
    ChatRoom room = findRoom(roomId);

    if (room != null) {
      chatRooms.remove(room);

      Chat chat = findChat(roomId);
      if (chat != null) {
        BeeKeeper.getScreen().closeWidget(chat);
      }

      updateHeader();
      logger.info("removed room", room.getName());
    }
  }

  private void showInfo(long roomId) {
    ChatRoom room = findRoom(roomId);
    if (room == null) {
      return;
    }

    HtmlTable table = new HtmlTable(STYLE_PREFIX + "details");

    int row = 0;
    table.setText(row, 0, "ID");
    table.setText(row, 1, BeeUtils.toString(room.getId()));

    if (room.getType() != null) {
      row++;
      table.setText(row, 0, "Type");
      table.setText(row, 1, room.getType().getCaption());
    }
    
    if (!BeeUtils.isEmpty(room.getOwners())) {
      row++;
      table.setText(row, 0, "Owners");
      table.setText(row, 1, BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR,
          Global.getUsers().getSignatures(room.getOwners())));
    }
    if (!BeeUtils.isEmpty(room.getDwellers())) {
      row++;
      table.setText(row, 0, "Dwellers");
      table.setText(row, 1, BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR,
          Global.getUsers().getSignatures(room.getDwellers())));
    }

    if (!BeeUtils.isEmpty(room.getUsers())) {
      row++;
      table.setText(row, 0, "Users");
      table.setText(row, 1, BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR,
          Global.getUsers().getSignatures(room.getUsers())));
    }

    row++;
    table.setText(row, 0, "Messages");
    table.setText(row, 1, BeeUtils.toString(room.getMessageCount()));
    
    if (room.getMaxTime() > 0) {
      row++;
      table.setText(row, 0, "Max Time");
      table.setText(row, 1, TimeUtils.toString(room.getMaxTime()));
    }
    
    Global.showModalWidget(room.getName(), table);
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
