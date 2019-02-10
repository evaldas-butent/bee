package com.butent.bee.client;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.BrowsingContext;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.rights.RegulatedWidget;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class NotificationUserPanel extends SimpleUserPanel {
  private static final String PANEL_NAME = "NotificationUserPanel";

  private static final String STYLE_USER_EXIT = BeeConst.CSS_CLASS_PREFIX + "UserExit";
  private static final String STYLE_USER_EXIT_CONTAINER = STYLE_USER_EXIT + "Container";
  private static final String STYLE_USER_CONTAINER = BeeConst.CSS_CLASS_PREFIX
      + "UserContainer";

  private static final String STYLE_CALENDAR_CONTAINER = BeeConst.CSS_CLASS_PREFIX
      + "CalendarContainer";

  private static final String STYLE_INFO_CONTAINER = BeeConst.CSS_CLASS_PREFIX
      + "InfoContainer";

  private static final String STYLE_INFO_TABLE = BeeConst.CSS_CLASS_PREFIX  + "InfoTable";
  private static final String STYLE_INFO_LABEL = STYLE_INFO_TABLE + "-label";
  private static final String STYLE_INFO_VALUE = STYLE_INFO_TABLE + "-value";

  private static final String STYLE_CALENDAR_MONTH_DAY = BeeConst.CSS_CLASS_PREFIX + "MonthDay";
  private static final String STYLE_CALENDAR_MONTH_DAY_TODAY = STYLE_CALENDAR_MONTH_DAY + "Today";
  private static final String STYLE_CALENDAR_WEEK_DAY = BeeConst.CSS_CLASS_PREFIX + "WeekDay";

  private static final String STYLE_USER_MENU_ICON = BeeConst.CSS_CLASS_PREFIX + "UserMenuIcon";
  private static final String STYLE_USER_MENU_ICON_SELECTED = STYLE_USER_MENU_ICON + "-Selected";

  private static final String STYLE_SETTINGS_CONTAINER = BeeConst.CSS_CLASS_PREFIX
      + "SettingsContainer";

  private static final String STYLE_USER_CONTROL_ICON = BeeConst.CSS_CLASS_PREFIX
      + "UserControlIcon";

  private static final String STYLE_USER_CONTROL_ICON_INFO_CLICKED = STYLE_USER_CONTROL_ICON
      + "-InfoClicked";

  private static final String STYLE_USER_PANEL = BeeConst.CSS_CLASS_PREFIX + "UserPanel";

  private static final String STYLE_NOTIFICATION_BAR  = BeeConst.CSS_CLASS_PREFIX
      + "NotificationBar";

  private final HtmlTable notificationPanel = new HtmlTable(BeeConst.CSS_CLASS_PREFIX
      + "NotificationBar-Content");

  private Flow panelHeader;
  private FaLabel settingsAction;
  private Flow commandPanel;
  private Horizontal userSignatureContainer;
  private FaLabel menuHideAction;
  private Popup popup;
  private FaLabel infoAction;

  @Override
  public String getName() {
    return PANEL_NAME;
  }

  @Override
  protected void createUserPhoto(UserData userData) {
    super.createUserPhoto(userData);

    Image image = getUserPhotoImage();

    if (image == null) {
      return;
    }

    createPanelHeader();

    if (panelHeader != null) {
      notificationPanel.setWidget(0, 0, panelHeader);
    }
    notificationPanel.setWidget(1, 0, createCalendarPanel());

    if (BeeKeeper.getUser().isWidgetVisible(RegulatedWidget.NEWS)) {
      notificationPanel.setWidget(2, 0,
          Global.getNewsAggregator().getNewsPanel().asWidget());
    }

    image.addClickHandler(e -> showPopup());
  }

  @Override
  protected void createExitContainer() {
    if (userSignatureContainer == null || !Settings.showLogout()) {
      return;
    }
    Label signature = new Label(BeeUtils.trim(BeeKeeper.getUser().getUserSign()));
    Label exit = new Label(Localized.dictionary().signOut());

    signature.addStyleName(STYLE_USER_SIGNATURE);

    exit.addStyleName(STYLE_USER_EXIT);
    exit.setTitle(Localized.dictionary().signOut());

    exit.addClickHandler(e -> Bee.exit());

    Flow exitContainer = new Flow(STYLE_USER_EXIT_CONTAINER);

    exitContainer.add(signature);
    exitContainer.add(exit);
    userSignatureContainer.add(exitContainer);
  }

  @Override
  protected void createUserSignatureContainer() {
    if (panelHeader == null) {
      return;
    }

    userSignatureContainer = new Horizontal(STYLE_USER_CONTAINER);
    createExitContainer();

    if (getUserPhotoImage() != null || !Settings.showUserPhoto()) {
      Image image = new Image(getUserPhotoImage().getUrl());

      image.setAlt(BeeKeeper.getUser().getLogin());
      image.addStyleName(STYLE_USER_PHOTO);

      image.addClickHandler(event ->  UiHelper.closeDialog((Widget) event.getSource()));
      userSignatureContainer.add(image);
    }
    panelHeader.add(userSignatureContainer);
  }

  private static Widget createCalendarPanel() {
    Flow userCal = new Flow();
    userCal.setStyleName(STYLE_CALENDAR_CONTAINER);

    JustDate firstDay = TimeUtils.today();
    JustDate day;

    for (int i = 0; i < 5; i++) {

      Label lblD = new Label();
      Label lblWd = new Label();
      Flow cal = new Flow();

      lblD.setStyleName(STYLE_CALENDAR_MONTH_DAY);
      lblWd.setStyleName(STYLE_CALENDAR_WEEK_DAY);

      if (i == 0) {

        lblD.addStyleName(STYLE_CALENDAR_MONTH_DAY_TODAY);
        lblD.setText(String.valueOf(firstDay.getDom()));
        lblWd.setText(String.valueOf(Format.renderDayOfWeekShort(firstDay.getDow())));
      } else {
        day = TimeUtils.toDateOrNull(firstDay.getDays() + i);
        lblD.setText(String.valueOf(day.getDom()));
        lblWd.setText(String.valueOf(Format.renderDayOfWeekShort(day.getDow())));
      }
      cal.add(lblWd);
      cal.add(lblD);
      userCal.add(cal);
    }
    return userCal;
  }

  private static Widget createInfoPanel() {
    Flow infoPanel = new Flow();
    Label lbl = new Label();
    infoPanel.setStyleName(STYLE_INFO_CONTAINER);

    HtmlTable table = new HtmlTable(STYLE_INFO_TABLE);

    int row = 0;
    int col = 0;

    table.setWidget(row, col, new Image("images/bs-logo.png"));

    if (!BeeUtils.isEmpty(Settings.getAppName())) {
      lbl.setText(Settings.getAppName());
    }

    col++;
    table.setWidget(row, col, lbl);
    row++;

    col = 0;

    if (!BeeUtils.isEmpty(Settings.getLicence())) {
      row = appendValue(table, row, col, Localized.dictionary().softwareLicenceNo(),
          Settings.getLicence());
    }

    if (!BeeUtils.isEmpty(Settings.getVersion())) {
      row = appendValue(table, row, col, Localized.dictionary().softwareVersion(),
          Settings.getVersion());
    }

    if (!BeeUtils.isEmpty(Settings.getBuild())) {
      row = appendValue(table, row, col, Localized.dictionary().softwareBuild(),
          Settings.getBuild());
    }

    if (!BeeUtils.isEmpty(Settings.getReleaseDate())) {
      row = appendValue(table, row, col, Localized.dictionary().softwareReleaseDate(),
          Settings.getReleaseDate());
    }

    table.getCellFormatter().setColSpan(0, 1, 2);
    table.getCellFormatter().setStyleName(0, 1, STYLE_INFO_VALUE);
    table.getCellFormatter().setRowSpan(0, 0, row);

    infoPanel.add(table);
    return infoPanel;
  }

  private static int appendValue(HtmlTable table, int row, int col, String key, String val) {
    int r = row;
    int c = col;
    table.getCellFormatter().setStyleName(r, c, STYLE_INFO_LABEL);
    table.setWidget(r, c, new Label(key));

    c++;
    table.getCellFormatter().setStyleName(r, c, STYLE_INFO_VALUE);
    table.setWidget(r, c, new Label(val));

    r++;
    return r;
  }

  private static void styleMenuToggle(Widget toggle, boolean visible) {
    toggle.setStyleName(STYLE_USER_MENU_ICON, visible);
    toggle.setStyleName(STYLE_USER_MENU_ICON_SELECTED, !visible);
  }

  private void createCommandPanel() {
    if (panelHeader == null) {
      return;
    }
    commandPanel = new Flow(STYLE_SETTINGS_CONTAINER);

    createSettingsAction();
    createHelpAction();
    createInfoAction();
    createMenuHideAction();

    panelHeader.add(commandPanel);
  }

  private void createHelpAction() {
    FaLabel helpAction = new FaLabel(FontAwesome.QUESTION, STYLE_USER_CONTROL_ICON);

    helpAction.setTitle(Localized.dictionary().help());
    helpAction.addClickHandler(arg1 -> BrowsingContext.open(UiConstants.helpURL()));

    if (commandPanel != null) {
      commandPanel.add(helpAction);
    }
  }

  private void createInfoAction() {
    infoAction = new FaLabel(FontAwesome.INFO, STYLE_USER_CONTROL_ICON);

    infoAction.setTitle(Localized.dictionary().additionalInfo());
    infoAction.addClickHandler(arg1 -> onInfoActionClick());

    if (commandPanel != null) {
      commandPanel.add(infoAction);
    }
  }

  private void createMenuHideAction() {
    menuHideAction = new FaLabel(FontAwesome.THUMB_TACK);

    menuHideAction.setTitle(Localized.dictionary().hideOrShowMenu());
    styleMenuToggle(menuHideAction, BeeKeeper.getUser().isMenuVisible());


    menuHideAction.addClickHandler(event -> {
      boolean visible = !BeeKeeper.getUser().isMenuVisible();
      BeeKeeper.getUser().setMenuVisible(visible);
      styleMenuToggle((Widget) event.getSource(), visible);
    });

    if (commandPanel != null) {
      commandPanel.add(menuHideAction);
    }
  }


  private void createPanelHeader() {
    panelHeader = new Flow(STYLE_USER_PANEL);
    createCommandPanel();
    createUserSignatureContainer();
  }

  private void createPopup() {
    popup = new Popup(Popup.OutsideClick.CLOSE,
        STYLE_NOTIFICATION_BAR);
    popup.setWidget(notificationPanel);
    popup.setHideOnEscape(true);

    popup.setAnimationEnabled(true);
    popup.setAnimation(new Popup.Animation(300) {
      int left;
      int width;

      @Override
      public void start() {
        this.width = getPopup().getOffsetWidth();
        this.left = DomUtils.getClientWidth() - this.width;

        if (getPopup().isShowing()) {
          StyleUtils.setOpacity(getPopup(), BeeConst.DOUBLE_ZERO);
          StyleUtils.setLeft(getPopup(), this.width);
        }
        super.start();
      }

      @Override
      protected void onComplete() {
        if (getPopup().isShowing()) {
          StyleUtils.setLeft(getPopup(), this.left);
        }
        getPopup().getElement().getStyle().clearOpacity();
        super.onComplete();
      }

      @Override
      protected boolean run(double elapsed) {
        if (isCanceled()) {
          return false;
        } else {
          StyleUtils.setOpacity(getPopup(), getFactor(elapsed));
          double x = this.left + (1 - getFactor(elapsed)) * this.width;
          StyleUtils.setLeft(getPopup(), x, CssUnit.PX);
          return true;
        }
      }
    });

  }

  private void createSettingsAction() {
    settingsAction = new FaLabel(FontAwesome.GEAR, STYLE_USER_CONTROL_ICON);
    settingsAction.setTitle(Localized.dictionary().settings());
    settingsAction.addClickHandler(event -> UiHelper.closeDialog((Widget) event.getSource()));

    if (commandPanel != null) {
      commandPanel.add(settingsAction);
    }
  }

  public FaLabel getMenuHideAction() {
    return menuHideAction;
  }

  public FaLabel getSettingsAction() {
    return settingsAction;
  }

  public FaLabel getInfoAction() {
    return infoAction;
  }

  private void showPopup() {
    if (popup == null) {
      createPopup();
    }

    popup.setPopupPositionAndShow((offsetWidth, offsetHeight)
        -> popup.setPopupPosition(DomUtils.getClientWidth() - offsetWidth, 0));
  }

  private void onInfoActionClick() {
    if (infoAction == null) {
      return;
    }

    infoAction.setStyleName(STYLE_USER_CONTROL_ICON_INFO_CLICKED,
        !StyleUtils.hasClassName(infoAction.getElement(), STYLE_USER_CONTROL_ICON_INFO_CLICKED));

    if (notificationPanel == null) {
      return;
    }

    notificationPanel.setWidget(1, 0,
        StyleUtils.hasClassName(infoAction.getElement(), STYLE_USER_CONTROL_ICON_INFO_CLICKED)
            ? createInfoPanel() : createCalendarPanel());
  }
}
