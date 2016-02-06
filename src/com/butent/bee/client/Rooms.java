package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.communication.ChatConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.communication.Chat;
import com.butent.bee.client.communication.ChatUtils;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.websocket.Endpoint;
import com.butent.bee.client.widget.Badge;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ChatRoom;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.websocket.messages.ChatMessage;
import com.butent.bee.shared.websocket.messages.RoomStateMessage;
import com.butent.bee.shared.websocket.messages.RoomUserMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Rooms implements HasInfo, HasEnabled {

  private final class ChatSettings {

    private String name;

    private final List<Long> users = new ArrayList<>();

    private ChatSettings() {
    }

    private ChatSettings(ChatRoom room) {
      this.name = room.getName();

      if (!BeeUtils.isEmpty(room.getUsers())) {
        this.users.addAll(room.getUsers());
      }
    }

    private ChatSettings(ChatSettings other) {
      this.name = other.getName();

      if (!BeeUtils.isEmpty(other.getUsers())) {
        this.users.addAll(other.getUsers());
      }
    }

    private String getName() {
      return name;
    }

    private List<Long> getUsers() {
      return users;
    }

    private boolean isValid() {
      return !getUsers().isEmpty();
    }

    private void setName(String name) {
      this.name = name;
    }
  }

  private final class ChatsPanel extends Flow {

    private ChatsPanel(List<ChatRoom> list) {
      super(STYLE_CHATS_PANEL);

      Flow wrapper = new Flow(STYLE_CHATS_WRAPPER);
      if (!list.isEmpty()) {
        for (ChatRoom chat : list) {
          wrapper.add(new ChatWidget(chat));
        }
      }
      add(wrapper);

      Flow actions = new Flow(STYLE_CHATS_ACTIONS);

      CustomDiv plus = new CustomDiv(STYLE_CHATS_PLUS);
      plus.setText(Localized.getConstants().chatStartNew());

      plus.addClickHandler(event -> createChat());
      actions.add(plus);

      if (!list.isEmpty()) {
        CustomDiv showAll = new CustomDiv(STYLE_CHATS_SHOW);
        showAll.setText(Localized.getConstants().chatsShowAll());

        showAll.addClickHandler(event -> showAll());
        actions.add(showAll);
      }

      add(actions);
    }

    private void addChatWidget(ChatWidget chatWidget) {
      getWrapper().add(chatWidget);
    }

    private ChatWidget findChatWidget(long chatId) {
      for (Widget widget : getWrapper()) {
        if (widget instanceof ChatWidget
            && Objects.equals(((ChatWidget) widget).getChatId(), chatId)) {
          return (ChatWidget) widget;
        }
      }

      logger.warning("widget not found for chat", chatId);
      return null;
    }

    private Flow getWrapper() {
      return (Flow) getWidget(0);
    }
  }

  private final class ChatWidget extends Flow {

    private final long chatId;

    private final CustomDiv nameWidget;

    private final Badge usersBadge;
    private final Flow usersPanel;

    private final CustomDiv timeLabel;

    private ChatWidget(ChatRoom chatRoom) {
      super(STYLE_CHAT_PREFIX + "container");
      this.chatId = chatRoom.getId();

      Flow headerPanel = new Flow(STYLE_CHAT_PREFIX + "headerPanel");

      this.nameWidget = new CustomDiv(STYLE_CHAT_PREFIX + "nameLabel");
      nameWidget.setText(chatRoom.getName());

      nameWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          enterChat(chatId);
          removeStyleName(STYLE_CHAT_UPDATED);
        }
      });

      headerPanel.add(nameWidget);

      FaLabel infoWidget = new FaLabel(FontAwesome.INFO_CIRCLE);
      infoWidget.addStyleName(STYLE_CHAT_PREFIX + "infoCommand");

      infoWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showInfo(chatId);
        }
      });

      headerPanel.add(infoWidget);

      add(headerPanel);

      Flow infoPanel = new Flow(STYLE_CHAT_PREFIX + "infoPanel");

      int userCount = ChatUtils.countOtherUsers(chatRoom.getUsers());

      this.usersBadge = new Badge(userCount, STYLE_CHAT_PREFIX + "usersBadge");
      infoPanel.add(usersBadge);

      this.usersPanel = new Flow(STYLE_CHAT_PREFIX + "usersPanel");
      if (userCount > 0) {
        ChatUtils.renderOtherUsers(usersPanel, chatRoom.getUsers(), STYLE_CHAT_USER);
      }

      infoPanel.add(usersPanel);

      this.timeLabel = new CustomDiv(STYLE_CHAT_PREFIX + "maxTime");
      if (chatRoom.getMaxTime() > 0) {
        ChatUtils.updateTime(timeLabel, chatRoom.getMaxTime());
      }

      infoPanel.add(timeLabel);
      add(infoPanel);
    }

    private long getChatId() {
      return chatId;
    }

    private void update(ChatRoom chatRoom) {
      if (chatRoom != null) {
        nameWidget.getElement().setInnerText(chatRoom.getName());

        updateUsers(chatRoom.getUsers());
        updateTime(chatRoom.getMaxTime());
      }
    }

    private void updateTime(long maxTime) {
      ChatUtils.updateTime(timeLabel, maxTime);
    }

    private void updateUsers(Collection<Long> users) {
      int count = ChatUtils.countOtherUsers(users);
      usersBadge.update(count);

      if (count > 0) {
        ChatUtils.renderOtherUsers(usersPanel, users, STYLE_CHAT_USER);
      } else {
        usersPanel.clear();
      }
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(Rooms.class);

  private static final String STYLE_CHATS_PREFIX = BeeConst.CSS_CLASS_PREFIX + "Chats-";

  private static final String STYLE_CHATS_COMMAND = STYLE_CHATS_PREFIX + "command";
  private static final String STYLE_CHATS_UNREAD = STYLE_CHATS_PREFIX + "unread";
  private static final String STYLE_CHATS_ICON = STYLE_CHATS_PREFIX + "icon";
  private static final String STYLE_CHATS_DISABLED = STYLE_CHATS_PREFIX + "disabled";

  private static final String STYLE_CHATS_POPUP = STYLE_CHATS_PREFIX + "popup";
  private static final String STYLE_CHATS_PANEL = STYLE_CHATS_PREFIX + "panel";
  private static final String STYLE_CHATS_WRAPPER = STYLE_CHATS_PREFIX + "wrapper";
  private static final String STYLE_CHATS_ACTIONS = STYLE_CHATS_PREFIX + "actions";
  private static final String STYLE_CHATS_PLUS = STYLE_CHATS_PREFIX + "plus";
  private static final String STYLE_CHATS_SHOW = STYLE_CHATS_PREFIX + "show";

  private static final String STYLE_CHAT_PREFIX = BeeConst.CSS_CLASS_PREFIX + "Chat-";
  private static final String STYLE_CHAT_USER = STYLE_CHAT_PREFIX + "user";
  private static final String STYLE_CHAT_UPDATED = STYLE_CHAT_PREFIX + "updated";

  private static final int TIMER_PERIOD = 10_000;

  private static void enterChat(long chatId) {
    Chat chatView = findChatView(chatId);

    if (chatView == null) {
      Endpoint.send(RoomUserMessage.enter(chatId, BeeKeeper.getUser().getUserId()));
    } else {
      BeeKeeper.getScreen().activateWidget(chatView);
    }
  }

  private static Chat findChatView(long chatId) {
    List<IdentifiableWidget> openWidgets = BeeKeeper.getScreen().getOpenWidgets();
    for (IdentifiableWidget widget : openWidgets) {
      if (widget instanceof Chat && Objects.equals(((Chat) widget).getRoomId(), chatId)) {
        return (Chat) widget;
      }
    }
    return null;
  }

  private static void onDelete(final ChatRoom chat) {
    List<String> messages = Lists.newArrayList(Localized.getConstants().roomDeleteQuestion());

    Global.confirmDelete(chat.getName(), Icon.WARNING, messages, new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        Endpoint.send(RoomStateMessage.remove(chat));
      }
    });
  }

  private final List<ChatRoom> chats = new ArrayList<>();

  private Widget chatsCommand;
  private Widget unreadBadge;

  private ChatsPanel chatsPanel;

  private Timer timer;

  private Map<Long, ViewCallback> viewCallbacks = new HashMap<>();

  private boolean enabled;

  Rooms() {
  }

  public void addMessage(ChatMessage chatMessage) {
    Assert.notNull(chatMessage);

    ChatRoom chat = findChat(chatMessage.getChatId());

    if (chat != null && chatMessage.isValid()) {
      chat.incrementMessageCount();
      chat.setLastMessage(chatMessage.getChatItem());

      ChatWidget chatWidget = findChatWidget(chat.getId());
      if (chatWidget != null) {
        chatWidget.updateTime(chat.getMaxTime());

        if (!BeeKeeper.getUser().is(chatMessage.getChatItem().getUserId())) {
          chatWidget.addStyleName(STYLE_CHAT_UPDATED);
        }
      }

      Chat chatView = findChatView(chat.getId());
      if (chatView != null) {
        chatView.addMessage(chatMessage.getChatItem(), true);
      }
    }
  }

  public Widget createCommand() {
    Flow command = new Flow(STYLE_CHATS_COMMAND);
    command.setTitle(Localized.getConstants().chats());

    CustomDiv unread = new CustomDiv(STYLE_CHATS_UNREAD);
    StyleUtils.setEmptiness(unread, true);
    command.add(unread);

    FaLabel icon = new FaLabel(FontAwesome.COMMENTS, STYLE_CHATS_ICON);
    command.add(icon);

    command.addClickHandler(event -> {
      if (isEnabled()) {
        showChats();
      }
    });

    setChatsCommand(command);
    setUnreadBadge(unread);

    return command;
  }

  public void configure(long chatId) {
    ChatRoom chat = findChat(chatId);
    if (chat == null) {
      return;
    }

    if (chat.isOwner(BeeKeeper.getUser().getUserId())) {
      editSetting(chatId);
    } else {
      showInfo(chatId);
    }
  }

  @Override
  public List<Property> getInfo() {
    List<Property> info = new ArrayList<>();
    info.add(new Property("Chats", BeeUtils.bracket(chats.size())));

    for (ChatRoom chat : chats) {
      info.addAll(chat.getInfo());

      Chat chatView = findChatView(chat.getId());
      if (chatView != null) {
        info.add(new Property("Chat View", chatView.getId()));
      }
    }
    return info;
  }

  public Long getMaxTime(long chatId) {
    ChatRoom chat = findChat(chatId);
    return (chat == null) ? null : chat.getMaxTime();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public void leaveChat(long chatId) {
    Endpoint.send(RoomUserMessage.leave(chatId, BeeKeeper.getUser().getUserId()));

    ChatRoom chat = findChat(chatId);
    if (chat != null) {
      chat.quit(BeeKeeper.getUser().getUserId());
    }
  }

  public void load(String serialized) {
    if (!chats.isEmpty()) {
      chats.clear();
    }

    String[] arr = Codec.beeDeserializeCollection(serialized);

    if (!ArrayUtils.isEmpty(arr)) {
      for (String s : arr) {
        ChatRoom cr = ChatRoom.restore(s);
        chats.add(cr);
      }

      logger.info("loaded", chats.size(), "chats");
    }

    updateUnreadBadge();
  }

  public void onRoomState(RoomStateMessage roomStateMessage) {
    Assert.notNull(roomStateMessage);
    Assert.isTrue(roomStateMessage.isValid());

    ChatRoom room = roomStateMessage.getRoom();
    Chat chatView;

    if (roomStateMessage.isLoading()) {
      chatView = new Chat(room);

      ViewCallback callback = viewCallbacks.remove(room.getId());
      if (callback == null) {
        BeeKeeper.getScreen().show(chatView);
      } else {
        callback.onSuccess(chatView);
      }

      updateChat(room);

    } else if (roomStateMessage.isNew()) {
      if (contains(room.getId())) {
        logger.warning("attempt to add existing room:", room.getId());
      } else if (room.hasUser(BeeKeeper.getUser().getUserId())) {
        addChat(room);
      }

    } else if (roomStateMessage.isUpdated()) {
      boolean visible = room.hasUser(BeeKeeper.getUser().getUserId());

      if (contains(room.getId())) {
        if (visible) {
          updateChat(room);

          chatView = findChatView(room.getId());
          if (chatView != null) {
            chatView.onRoomUpdate(room);
          }

        } else {
          removeChat(room.getId());
        }

      } else if (visible) {
        addChat(room);
      }

    } else if (roomStateMessage.isRemoved()) {
      removeChat(room.getId());

    } else {
      logger.warning("unrecongnized room state:", roomStateMessage.getState());
    }
  }

  public void onRoomUser(RoomUserMessage roomUserMessage) {
    Assert.notNull(roomUserMessage);

    ChatRoom room = findChat(roomUserMessage.getRoomId());

    if (room != null) {
      boolean ok;
      if (roomUserMessage.join()) {
        ok = room.join(roomUserMessage.getUserId());
      } else if (roomUserMessage.quit()) {
        ok = room.quit(roomUserMessage.getUserId());
      } else {
        ok = false;
      }

      if (ok) {
        ChatWidget widget = findChatWidget(room.getId());
        if (widget != null) {
          widget.updateUsers(room.getUsers());
          widget.updateTime(room.getMaxTime());
        }

        Chat chatView = findChatView(room.getId());
        if (chatView != null) {
          chatView.onRoomUpdate(room);
        }
      }
    }
  }

  public void open(long roomId, ViewCallback callback) {
    Assert.notNull(callback);

    ChatRoom room = findChat(roomId);
    if (room != null) {
      viewCallbacks.put(roomId, callback);
      Endpoint.send(RoomUserMessage.enter(roomId, BeeKeeper.getUser().getUserId()));
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void start() {
    setEnabled(getChatsCommand() != null && Endpoint.isOpen());

    if (isEnabled()) {
      updateUnreadBadge();

      if (timer == null) {
        this.timer = new Timer() {
          @Override
          public void run() {
            if (!chats.isEmpty()) {
              onTimer();
            }
          }
        };

        timer.scheduleRepeating(TIMER_PERIOD);
      }

    } else if (getChatsCommand() != null) {
      getChatsCommand().addStyleName(STYLE_CHATS_DISABLED);
    }
  }

  private void addChat(ChatRoom chat) {
    chats.add(chat);

    if (getChatsPanel() != null) {
      ChatWidget widget = new ChatWidget(chat);
      getChatsPanel().addChatWidget(widget);
    }

    updateUnreadBadge();
    logger.info("added chat", chat.getId(), chat.getName());
  }

  private void closeChatsPopup() {
    if (getChatsPanel() != null) {
      UiHelper.closeDialog(getChatsPanel());
    }
  }

  private boolean contains(long roomId) {
    for (ChatRoom room : chats) {
      if (room.is(roomId)) {
        return true;
      }
    }
    return false;
  }

  private void createChat() {
    ChatSettings settings = new ChatSettings();

    openSettings(settings, true, input -> {
      if (input.isValid()) {
        closeChatsPopup();

        ParameterList params = BeeKeeper.getRpc().createParameters(Service.CREATE_CHAT);
        if (!BeeUtils.isEmpty(input.getName())) {
          params.addDataItem(COL_CHAT_NAME, input.getName());
        }
        params.addDataItem(TBL_CHAT_USERS, DataUtils.buildIdList(input.getUsers()));

        BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (response.hasResponse(ChatRoom.class)) {
              ChatRoom chat = ChatRoom.restore(response.getResponseAsString());
              addChat(chat);

              Endpoint.send(RoomStateMessage.add(chat));
            }
          }
        });
      }
    });
  }

  private void editSetting(final long roomId) {
    ChatRoom original = findChat(roomId);
    if (original == null) {
      return;
    }

    ChatSettings settings = new ChatSettings(original);
    openSettings(settings, false, new Consumer<ChatSettings>() {
      @Override
      public void accept(ChatSettings input) {
        if (!input.isValid()) {
          return;
        }

        ChatRoom room = findChat(roomId);
        if (room == null) {
          return;
        }

        boolean changed = false;

        if (!BeeUtils.equalsTrimRight(room.getName(), input.getName())) {
          room.setName(input.getName());
          changed = true;
        }

        if (!BeeUtils.sameElements(room.getUsers(), input.getUsers())) {
          BeeUtils.overwrite(room.getUsers(), input.getUsers());
          changed = true;
        }

        if (changed) {
          Endpoint.send(RoomStateMessage.update(room));
        } else {
          logger.debug("settings not changed");
        }
      }
    });
  }

  private ChatRoom findChat(long chatId) {
    for (ChatRoom room : chats) {
      if (room.is(chatId)) {
        return room;
      }
    }

    logger.warning("chat not found:", chatId);
    return null;
  }

  private ChatWidget findChatWidget(long chatId) {
    if (getChatsPanel() == null) {
      return null;
    } else {
      return getChatsPanel().findChatWidget(chatId);
    }
  }

  private Widget getChatsCommand() {
    return chatsCommand;
  }

  private ChatsPanel getChatsPanel() {
    return chatsPanel;
  }

  private Widget getUnreadBadge() {
    return unreadBadge;
  }

  private void onTimer() {
    for (ChatRoom chat : chats) {
      ChatWidget widget = findChatWidget(chat.getId());
      if (widget != null) {
        widget.updateTime(chat.getMaxTime());
      }
    }
  }

  private void openSettings(ChatSettings settings, final boolean isNew,
      final Consumer<ChatSettings> consumer) {

    final ChatSettings result = new ChatSettings(settings);

    String stylePrefix = STYLE_CHAT_PREFIX + "editor-";

    String caption = isNew ? Localized.getConstants().chatNew()
        : Localized.getConstants().chatSettings();
    final DialogBox dialog = DialogBox.create(caption, stylePrefix + "dialog");

    HtmlTable table = new HtmlTable(stylePrefix + "table");
    int row = 0;

    Label nameLabel = new Label(Localized.getConstants().chatName());
    table.setWidgetAndStyle(row, 0, nameLabel, stylePrefix + "nameLabel");

    final InputText nameInput = new InputText();
    nameInput.setMaxLength(Data.getColumnPrecision(TBL_CHATS, COL_CHAT_NAME));
    if (!BeeUtils.isEmpty(settings.getName())) {
      nameInput.setValue(settings.getName());
    }
    table.setWidgetAndStyle(row, 1, nameInput, stylePrefix + "nameInput");

    row++;
    Label usersLabel = new Label(Localized.getConstants().users());
    usersLabel.addStyleName(StyleUtils.NAME_REQUIRED);
    table.setWidgetAndStyle(row, 0, usersLabel, stylePrefix + "usersLabel");

    final MultiSelector usersWidget = MultiSelector.autonomous(AdministrationConstants.VIEW_USERS,
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME));
    if (isNew) {
      usersWidget.getOracle().setExclusions(Collections.singleton(BeeKeeper.getUser().getUserId()));
    } else if (!BeeUtils.isEmpty(settings.getUsers())) {
      usersWidget.setIds(settings.getUsers());
    }
    table.setWidgetAndStyle(row, 1, usersWidget, stylePrefix + "usersInput");

    row++;
    Flow commands = new Flow();

    Button save = new Button(Localized.getConstants().actionSave());
    save.addStyleName(stylePrefix + "save");

    save.addClickHandler(event -> {
      List<Long> users = usersWidget.getIds();
      int minSize = isNew ? 1 : 2;

      if (BeeUtils.size(users) < minSize) {
        usersWidget.setFocus(true);
        return;
      }

      result.setName(BeeUtils.trim(nameInput.getValue()));
      BeeUtils.overwrite(result.getUsers(), users);

      dialog.close();
      consumer.accept(result);
    });

    commands.add(save);

    Button cancel = new Button(Localized.getConstants().actionCancel());
    cancel.addStyleName(stylePrefix + "cancel");

    cancel.addClickHandler(event -> dialog.close());
    commands.add(cancel);

    table.setWidgetAndStyle(row, 1, commands, stylePrefix + "commands");

    dialog.setWidget(table);

    dialog.setAnimationEnabled(true);
    dialog.focusOnOpen(usersWidget);

    dialog.center();
  }

  private void removeChat(long chatId) {
    ChatRoom chat = findChat(chatId);

    if (chat != null) {
      chats.remove(chat);

      ChatWidget widget = findChatWidget(chatId);
      if (widget != null) {
        widget.removeFromParent();
      }

      Chat chatView = findChatView(chatId);
      if (chatView != null) {
        BeeKeeper.getScreen().closeWidget(chatView);
      }

      updateUnreadBadge();
      logger.info("removed chat", chat.getId(), chat.getName());
    }
  }

  private void setChatsCommand(Widget chatsCommand) {
    this.chatsCommand = chatsCommand;
  }

  private void setChatsPanel(ChatsPanel chatsPanel) {
    this.chatsPanel = chatsPanel;
  }

  private void setUnreadBadge(Widget unreadBadge) {
    this.unreadBadge = unreadBadge;
  }

  private void showAll() {
  }

  private void showChats() {
    List<ChatRoom> list = new ArrayList<>();
    if (!chats.isEmpty()) {
      list.addAll(chats);

      if (list.size() > 1) {
        Collections.sort(list);
      }
    }

    setChatsPanel(new ChatsPanel(list));

    Popup popup = new Popup(OutsideClick.CLOSE, STYLE_CHATS_POPUP);
    popup.setWidget(getChatsPanel());

    popup.setHideOnEscape(true);
    popup.addCloseHandler(event -> setChatsPanel(null));

    popup.showRelativeTo(getChatsCommand().getElement());
  }

  private void showInfo(long chatId) {
    ChatRoom room = findChat(chatId);
    if (room == null) {
      return;
    }

    HtmlTable table = new HtmlTable(STYLE_CHAT_PREFIX + "details");
    int row = 0;

    table.setText(row, 0, Localized.getConstants().captionId());
    table.setText(row, 2, BeeUtils.toString(chatId));

    row++;
    table.setText(row, 0, Localized.getConstants().users());
    table.setText(row, 1, BeeUtils.bracket(BeeUtils.size(room.getUsers())));
    if (!BeeUtils.isEmpty(room.getUsers())) {
      table.setText(row, 2, BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR,
          Global.getUsers().getSignatures(room.getUsers())));
    }

    row++;
    table.setText(row, 0, Localized.getConstants().roomUpdateTime());
    table.setText(row, 1, BeeUtils.bracket(room.getMessageCount()));
    if (room.getMaxTime() > 0) {
      table.setText(row, 2, BeeUtils.joinWords(ChatUtils.elapsed(room.getMaxTime()),
          TimeUtils.renderDateTime(room.getMaxTime(), true)));
    }

    Global.showModalWidget(room.getName(), table);
  }

  private void updateUnreadBadge() {
    if (getUnreadBadge() != null) {
      int count = 0;

      for (ChatRoom chat : chats) {
        if (chat.getUnreadCount() > 0) {
          count += chat.getUnreadCount();
        }
      }

      String text = (count > 0) ? BeeUtils.toString(count) : BeeConst.STRING_EMPTY;
      getUnreadBadge().getElement().setInnerText(text);

      StyleUtils.setEmptiness(getUnreadBadge(), count <= 0);
    }
  }

  private void updateChat(ChatRoom source) {
    ChatRoom target = findChat(source.getId());

    if (target != null) {
      target.setName(source.getName());

      BeeUtils.overwrite(target.getUsers(), source.getUsers());

      target.setMessageCount(source.getMessageCount());
      target.setLastMessage(source.getLastMessage());

      ChatWidget widget = findChatWidget(source.getId());
      if (widget != null) {
        widget.update(source);
      }
    }
  }
}
