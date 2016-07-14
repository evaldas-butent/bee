package com.butent.bee.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.render.PhotoRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.Presence;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

public class OnlineUsers extends Flow {
  public static final String STYLE_USERS_COUNT = BeeConst.CSS_CLASS_PREFIX + "OnlineUsersCount";
  public static final String STYLE_USERS_ICON = BeeConst.CSS_CLASS_PREFIX + "OnlineUsersIcon";
  public static final String STYLE_POPUP_USERS = BeeConst.CSS_CLASS_PREFIX + "PopupUsers";

  private static final String STYLE_POPUP_USERS_PHOTO = STYLE_POPUP_USERS + "Photo";
  private static final String STYLE_POPUP_USERS_LABEL = STYLE_POPUP_USERS + "Label";
  private static final String STYLE_POPUP_USERS_CONTENT = STYLE_POPUP_USERS + "Content";
  private static final String STYLE_POPUP_USERS_PRESENCE = STYLE_POPUP_USERS + "Presence";

  private static final String STYLE_POPUP_USERS_CHAT = STYLE_POPUP_USERS + "Chat";

  private final Users users = Global.getUsers();

  private CustomDiv userCountDiv = new CustomDiv(STYLE_USERS_COUNT);
  private FaLabel icon = new FaLabel(FontAwesome.USER, STYLE_USERS_ICON);
  private Popup onlineUsersPopup = new Popup(Popup.OutsideClick.CLOSE, STYLE_POPUP_USERS);

  /**
   * Create a widget where shows online (in session) users.
   * @return Html div element where on click shows online users.
   */
  public static Flow createWidget(String style) {
    OnlineUsers instance = new OnlineUsers(style);
    instance.create();
    return instance;
  }

  public OnlineUsers(String style) {
    super(style);
  }

  public void create() {
    StyleUtils.setEmptiness(userCountDiv, true);

    add(userCountDiv);

    StyleUtils.setEmptiness(icon, true);
    icon.addClickHandler(this::showOnlineUsers);

    add(icon);
  }

  private static int addRow(HtmlTable table, int rowId, UserData userData, Widget ... widgets) {
    int colId = 0;

    for (Widget widget : widgets) {
      if (widget != null) {
        table.setWidget(rowId, colId, widget);
      }

      colId++;
    }

    DomUtils.setDataIndex(table.getRow(rowId), userData.getPerson());

    return rowId + 1;
  }

  private static Image renderUserPhoto(Long fileId) {
    Image photo = new Image(DataUtils.isId(fileId) ? PhotoRenderer.getUrl(fileId)
        : PhotoRenderer.DEFAULT_PHOTO_IMAGE);

    photo.addStyleName(STYLE_POPUP_USERS_PHOTO);

    return photo;
  }

  /* Asynchronous method call (onChatButtonClick) */
  private void openChat(Long userId) {
    UiHelper.closeDialog(onlineUsersPopup.getWidget());
    Global.getChatManager().chatWithUser(userId);
  }

  /* Asynchronous method call (onUsersTableClick) */
  private void openPersonForm(ClickEvent event) {
    Element targetElement = EventUtils.getEventTargetElement(event);
    TableRowElement rowElement = DomUtils.getParentRow(targetElement, true);
    Long id = DomUtils.getDataIndexLong(rowElement);

    if (!DataUtils.isId(id)) {
      return;
    }

    if (!BeeKeeper.getUser().isDataVisible(ClassifierConstants.VIEW_PERSONS)) {
      return;
    }

    UiHelper.closeDialog(onlineUsersPopup.getWidget());
    RowEditor.open(ClassifierConstants.VIEW_PERSONS, id, Opener.NEW_TAB);
  }

  private FaLabel renderChatButton(Long userId) {
    FaLabel button = null;

    if (!Global.getChatManager().isEnabled()) {
      return button;
    }

    button = new FaLabel(FontAwesome.COMMENT_O, STYLE_POPUP_USERS_CHAT);
    button.setTitle(Localized.dictionary().chat());

    button.addClickHandler(event -> openChat(userId));

    return button;
  }

  private FaLabel renderUserPresence(String session) {
    FaLabel widget = null;
    Presence presence = users.getPresenceBySession(session);

    if (presence == null) {
      return widget;
    }

    widget = new FaLabel(presence.getIcon(), presence.getStyleName());
    widget.addStyleName(STYLE_POPUP_USERS_PRESENCE);
    widget.setTitle(presence.getCaption());

    return widget;
  }

  /* Asynchronous method call (onIconClick) */
  private void showOnlineUsers(ClickEvent event) {
    Set<String> sessions = Global.getUsers().getAllSessions();

    if (BeeUtils.isEmpty(sessions)) {
      return;
    }

    int rowId = 0;

    HtmlTable usersTable = new HtmlTable(STYLE_POPUP_USERS_CONTENT);

    for (String session : sessions) {
      UserData userData = users.getUserData(users.getUserIdBySession(session));
      Image userPhoto = renderUserPhoto(userData.getPhotoFile());
      FaLabel userPresence = renderUserPresence(session);
      FaLabel chatButton = renderChatButton(userData.getUserId());
      Label userSign = new Label(userData.getUserSign());


      userSign.addStyleName(STYLE_POPUP_USERS_LABEL);

      rowId = addRow(usersTable, rowId, userData, userPhoto, userPresence, userSign, chatButton);
    }

    onlineUsersPopup.setWidget(usersTable);
    onlineUsersPopup.setHideOnEscape(true);
    onlineUsersPopup.showRelativeTo(icon.getElement());

    if (rowId == 0) {
      return;
    }

    usersTable.addClickHandler(this::openPersonForm);
  }
}
