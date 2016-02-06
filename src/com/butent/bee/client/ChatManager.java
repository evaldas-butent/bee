package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.communication.ChatConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.communication.ChatUtils;
import com.butent.bee.client.communication.ChatView;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.websocket.Endpoint;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.Chat;
import com.butent.bee.shared.communication.ChatItem;
import com.butent.bee.shared.communication.Presence;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.websocket.messages.ChatMessage;
import com.butent.bee.shared.websocket.messages.ChatStateMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatManager implements HasInfo, HasEnabled {

  private final class ChatSettings {

    private String name;

    private final List<Long> users = new ArrayList<>();

    private ChatSettings() {
    }

    private ChatSettings(Chat chat) {
      this.name = chat.getName();

      if (!BeeUtils.isEmpty(chat.getUsers())) {
        this.users.addAll(chat.getUsers());
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

    private ChatsPanel(List<Chat> list) {
      super(STYLE_CHATS_PANEL);

      Flow wrapper = new Flow(STYLE_CHATS_WRAPPER);
      if (!list.isEmpty()) {
        for (Chat chat : list) {
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

    private final CustomDiv timeLabel;

    private ChatWidget(Chat chat) {
      super(STYLE_CHAT_PREFIX + "container");
      this.chatId = chat.getId();

      List<Long> users = ChatUtils.getOtherUsers(chat.getUsers());

      Map<Presence, Integer> presence = ChatUtils.countUserPresence(users);
      if (!BeeUtils.isEmpty(presence)) {
        applyPresence(presence);
      }

      Flow picturePanel = new Flow(STYLE_CHAT_PREFIX + "picturePanel");

      Widget picture = createPicture(users);
      if (picture != null) {
        picturePanel.add(picture);
      }

      add(picturePanel);

      Flow headerPanel = new Flow(STYLE_CHAT_PREFIX + "headerPanel");

      if (BeeUtils.isEmpty(chat.getName())) {
        Flow usersPanel = new Flow(STYLE_CHAT_PREFIX + "usersPanel");

        if (users.size() == 1) {
          CustomDiv userWidget = new CustomDiv(STYLE_CHAT_PREFIX + "userName");
          userWidget.setText(Global.getUsers().getSignature(users.get(0)));

          usersPanel.add(userWidget);

        } else {
          ChatUtils.renderOtherUsers(usersPanel, chat.getUsers(), STYLE_CHAT_USER);
        }

        headerPanel.add(usersPanel);

      } else {
        CustomDiv nameWidget = new CustomDiv(STYLE_CHAT_PREFIX + "nameLabel");
        nameWidget.setText(chat.getName());

        headerPanel.add(nameWidget);
      }

      this.timeLabel = new CustomDiv(STYLE_CHAT_PREFIX + "maxTime");
      if (chat.getMaxTime() > 0) {
        ChatUtils.updateTime(timeLabel, chat.getMaxTime());
      }
      headerPanel.add(timeLabel);

      add(headerPanel);

      Flow infoPanel = new Flow(STYLE_CHAT_PREFIX + "infoPanel");

      if (chat.getLastMessage() != null) {
        String text = chat.getLastMessage().getText();

        if (!BeeUtils.isEmpty(text)) {
          CustomDiv lastMessageWidget = new CustomDiv(STYLE_CHAT_PREFIX + "lastMessage");
          lastMessageWidget.setText(text);

          infoPanel.add(lastMessageWidget);
        }
      }

      add(infoPanel);

      addClickHandler(event -> enterChat(chatId));
    }

    private void applyPresence(Map<Presence, Integer> presence) {
      if (presence.size() == 1) {
        Presence p = BeeUtils.peek(presence.keySet());
        StyleUtils.setBackgroundColor(this, p.getBackground());

      } else if (presence.size() > 1) {
        List<String> colors = new ArrayList<>();

        for (Presence p : presence.keySet()) {
          String color = p.getBackground();

          for (int i = 0; i < presence.get(p); i++) {
            colors.add(color);
          }
        }

        StyleUtils.setBackgroundColor(this, Color.blend(colors));
      }
    }

    private Widget createPicture(List<Long> users) {
      if (users.size() == 1 && Global.getUsers().hasPhoto(users.get(0))) {
        Image photo = Global.getUsers().getPhoto(users.get(0));
        if (photo != null) {
          photo.addStyleName(STYLE_CHAT_PREFIX + "photo");
          return photo;
        }
      }

      CustomDiv badge = new CustomDiv(STYLE_CHAT_PREFIX + "usersBadge");
      badge.setText(BeeUtils.toString(users.size()));

      return badge;
    }

    private long getChatId() {
      return chatId;
    }

    private void update(Chat chat) {
      if (chat != null) {
        updateTime(chat.getMaxTime());
      }
    }

    private void updateTime(long maxTime) {
      ChatUtils.updateTime(timeLabel, maxTime);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(ChatManager.class);

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

  private static final int TIMER_PERIOD = 10_000;

  private static ChatView findChatView(long chatId) {
    List<IdentifiableWidget> openWidgets = BeeKeeper.getScreen().getOpenWidgets();
    for (IdentifiableWidget widget : openWidgets) {
      if (widget instanceof ChatView && Objects.equals(((ChatView) widget).getChatId(), chatId)) {
        return (ChatView) widget;
      }
    }
    return null;
  }

  private static void onDelete(final Chat chat) {
    List<String> messages = Lists.newArrayList(Localized.getConstants().chatDeleteQuestion());

    Global.confirmDelete(chat.getName(), Icon.WARNING, messages, new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        Endpoint.send(ChatStateMessage.remove(chat));
      }
    });
  }

  private final List<Chat> chats = new ArrayList<>();

  private Widget chatsCommand;
  private Widget unreadBadge;

  private AudioElement incomingSound;

  private ChatsPanel chatsPanel;

  private Timer timer;

  private boolean enabled;

  ChatManager() {
  }

  public void addMessage(ChatMessage chatMessage) {
    Assert.notNull(chatMessage);

    Chat chat = findChat(chatMessage.getChatId());

    if (chat != null && chatMessage.isValid()) {
      if (chat.hasMessages() || chat.getMessageCount() == 0) {
        chat.addMessage(chatMessage.getChatItem());
      }
      chat.incrementMessageCount();
      chat.setLastMessage(chatMessage.getChatItem());

      boolean incoming = !BeeKeeper.getUser().is(chatMessage.getChatItem().getUserId());

      ChatWidget chatWidget = findChatWidget(chat.getId());
      if (chatWidget != null) {
        chatWidget.updateTime(chat.getMaxTime());
      }

      ChatView chatView = findChatView(chat.getId());
      if (chatView != null) {
        chatView.addMessage(chatMessage.getChatItem(), true);
      }

      if (incoming && incomingSound != null) {
        incomingSound.play();
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
    Chat chat = findChat(chatId);
    if (chat == null) {
      return;
    }

    if (chat.isOwner(BeeKeeper.getUser().getUserId())) {
      editSetting(chatId);
    } else {
      showInfo(chatId);
    }
  }

  private void enterChat(long chatId) {
    closeChatsPopup();

    ChatView chatView = findChatView(chatId);

    if (chatView != null) {
      BeeKeeper.getScreen().activateWidget(chatView);
    } else {
      open(chatId, view -> BeeKeeper.getScreen().show(view));
    }
  }

  @Override
  public List<Property> getInfo() {
    List<Property> info = new ArrayList<>();
    info.add(new Property("Chats", BeeUtils.bracket(chats.size())));

    for (Chat chat : chats) {
      info.addAll(chat.getInfo());

      ChatView chatView = findChatView(chat.getId());
      if (chatView != null) {
        info.add(new Property("Chat View", chatView.getId()));
      }
    }
    return info;
  }

  public Long getMaxTime(long chatId) {
    Chat chat = findChat(chatId);
    return (chat == null) ? null : chat.getMaxTime();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public void leaveChat(long chatId) {
  }

  public void load(String serialized) {
    if (!chats.isEmpty()) {
      chats.clear();
    }

    String[] arr = Codec.beeDeserializeCollection(serialized);

    if (!ArrayUtils.isEmpty(arr)) {
      for (String s : arr) {
        Chat chat = Chat.restore(s);
        chats.add(chat);
      }

      logger.info("loaded", chats.size(), "chats");
    }

    updateUnreadBadge();
  }

  public void onChatState(ChatStateMessage stateMessage) {
    Assert.notNull(stateMessage);
    Assert.isTrue(stateMessage.isValid());

    Chat chat = stateMessage.getChat();

    if (stateMessage.isNew()) {
      if (contains(chat.getId())) {
        logger.warning("attempt to add existing chat:", chat.getId());
      } else if (chat.hasUser(BeeKeeper.getUser().getUserId())) {
        addChat(chat);
      }

    } else if (stateMessage.isUpdated()) {
      boolean visible = chat.hasUser(BeeKeeper.getUser().getUserId());

      if (contains(chat.getId())) {
        if (visible) {
          updateChat(chat);

          ChatView chatView = findChatView(chat.getId());
          if (chatView != null) {
            chatView.onChatUpdate(chat);
          }

        } else {
          removeChat(chat.getId());
        }

      } else if (visible) {
        addChat(chat);
      }

    } else if (stateMessage.isRemoved()) {
      removeChat(chat.getId());

    } else {
      logger.warning("unrecognized chat state:", stateMessage.getState());
    }
  }

  public void open(long chatId, final ViewCallback callback) {
    Assert.notNull(callback);

    final Chat chat = findChat(chatId);

    if (chat != null) {
      if (chat.getMessageCount() <= 0 || chat.hasMessages()) {
        callback.onSuccess(new ChatView(chat));
        return;
      }

      ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_CHAT_MESSAGES);
      params.addQueryItem(COL_CHAT, chat.getId());

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasResponse()) {
            chat.clearMessages();

            String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());
            for (String s : arr) {
              chat.addMessage(ChatItem.restore(s));
            }

            chat.setMessageCount(chat.getMessages().size());
            chat.setLastMessage(BeeUtils.getLast(chat.getMessages()));
          }

          callback.onSuccess(new ChatView(chat));
        }
      });
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
      initSound();

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

  private void addChat(Chat chat) {
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

  private boolean contains(long chatId) {
    for (Chat chat : chats) {
      if (chat.is(chatId)) {
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
            if (response.hasResponse(Chat.class)) {
              Chat chat = Chat.restore(response.getResponseAsString());
              addChat(chat);

              Endpoint.send(ChatStateMessage.add(chat));
            }
          }
        });
      }
    });
  }

  private void editSetting(final long chatId) {
    Chat original = findChat(chatId);
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

        Chat chat = findChat(chatId);
        if (chat == null) {
          return;
        }

        boolean changed = false;

        if (!BeeUtils.equalsTrimRight(chat.getName(), input.getName())) {
          chat.setName(input.getName());
          changed = true;
        }

        if (!BeeUtils.sameElements(chat.getUsers(), input.getUsers())) {
          BeeUtils.overwrite(chat.getUsers(), input.getUsers());
          changed = true;
        }

        if (changed) {
          Endpoint.send(ChatStateMessage.update(chat));
        } else {
          logger.debug("settings not changed");
        }
      }
    });
  }

  private Chat findChat(long chatId) {
    for (Chat chat : chats) {
      if (chat.is(chatId)) {
        return chat;
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

  private void initSound() {
    String fileName = Settings.getIncomingChatMessageSound();

    if (!BeeUtils.isEmpty(fileName) && incomingSound == null) {
      this.incomingSound = Document.get().createAudioElement();
      DomUtils.createId(incomingSound, "icm");

      incomingSound.setAutoplay(false);
      incomingSound.setControls(false);

      Double volume = Settings.getIncomingChatMessageVolume();
      if (volume != null) {
        incomingSound.setVolume(volume);
      }

      incomingSound.setSrc(Paths.getSoundPath(fileName));

      BodyPanel.conceal(incomingSound);
    }
  }

  private void onTimer() {
    for (Chat chat : chats) {
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
    Chat chat = findChat(chatId);

    if (chat != null) {
      chats.remove(chat);

      ChatWidget widget = findChatWidget(chatId);
      if (widget != null) {
        widget.removeFromParent();
      }

      ChatView chatView = findChatView(chatId);
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
    List<Chat> list = new ArrayList<>();
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
    Chat chat = findChat(chatId);
    if (chat == null) {
      return;
    }

    HtmlTable table = new HtmlTable(STYLE_CHAT_PREFIX + "details");
    int row = 0;

    table.setText(row, 0, Localized.getConstants().captionId());
    table.setText(row, 2, BeeUtils.toString(chatId));

    row++;
    table.setText(row, 0, Localized.getConstants().users());
    table.setText(row, 1, BeeUtils.bracket(BeeUtils.size(chat.getUsers())));
    if (!BeeUtils.isEmpty(chat.getUsers())) {
      table.setText(row, 2, BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR,
          Global.getUsers().getSignatures(chat.getUsers())));
    }

    row++;
    table.setText(row, 0, Localized.getConstants().chatUpdateTime());
    table.setText(row, 1, BeeUtils.bracket(chat.getMessageCount()));
    if (chat.getMaxTime() > 0) {
      table.setText(row, 2, BeeUtils.joinWords(ChatUtils.elapsed(chat.getMaxTime()),
          TimeUtils.renderDateTime(chat.getMaxTime(), true)));
    }

    Global.showModalWidget(chat.getName(), table);
  }

  private void updateUnreadBadge() {
    if (getUnreadBadge() != null) {
      int count = 0;

      for (Chat chat : chats) {
        if (chat.getUnreadCount() > 0) {
          count += chat.getUnreadCount();
        }
      }

      String text = (count > 0) ? BeeUtils.toString(count) : BeeConst.STRING_EMPTY;
      getUnreadBadge().getElement().setInnerText(text);

      StyleUtils.setEmptiness(getUnreadBadge(), count <= 0);
    }
  }

  private void updateChat(Chat source) {
    Chat target = findChat(source.getId());

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
