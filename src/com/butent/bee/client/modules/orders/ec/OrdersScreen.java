package com.butent.bee.client.modules.orders.ec;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.cli.Shell;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.modules.administration.PasswordService;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.orders.ec.OrdEcCommandWidget.Type;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.ScreenImpl;
import com.butent.bee.client.ui.AutocompleteProvider;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrdersScreen extends ScreenImpl {

  private static void createCommands(HasWidgets container) {
    String panelStyle = "commandPanel";

    Horizontal info = new Horizontal();
    EcStyles.add(info, panelStyle);
    EcStyles.add(info, panelStyle, "info");

    info.add(createCommandWidget(EcConstants.SVC_FINANCIAL_INFORMATION,
        Localized.dictionary().ecFinancialInformation(), Type.LINK));
    info.add(createCommandWidget(EcConstants.SVC_SHOW_CONTACTS,
        Localized.dictionary().ecContacts(), Type.LINK));

    container.add(info);

    String styleName = "searchBy";

    Horizontal searchBy = new Horizontal();
    EcStyles.add(searchBy, panelStyle);
    EcStyles.add(searchBy, panelStyle, styleName);

    Label label = new Label(Localized.dictionary().ecSearchBy());
    EcStyles.add(label, styleName, "label");
    searchBy.add(label);

    searchBy.add(createCommandWidget(SVC_EC_SEARCH_BY_ITEM_ARTICLE,
        Localized.dictionary().ordSearchByItemArticle(), Type.LABEL));
    searchBy.add(createCommandWidget(SVC_EC_SEARCH_BY_ITEM_CATEGORY,
        Localized.dictionary().ordSearchByItemCategory(), Type.LABEL));
    searchBy.add(createCommandWidget(SVC_EC_GET_NOT_SUBMITTED_ORDERS,
        Localized.dictionary().ordNotSubmittedOrders(), Type.LABEL));

    container.add(searchBy);

    Horizontal carts = new Horizontal();
    EcStyles.add(carts, "carts");

    Image image = new Image(EcUtils.imageUrl("cart.png"));
    EcStyles.add(image, "cartPicture");

    carts.add(image);
    carts.add(OrdEcKeeper.getCartlist());

    container.add(carts);
  }

  private static Widget createCommandWidget(String service, String html, Type type) {
    OrdEcCommandWidget commandWidget = new OrdEcCommandWidget(service, html, type);
    return commandWidget.getWidget();
  }

  public OrdersScreen() {
    super();
  }

  @Override
  public boolean activateDomainEntry(Domain domain, Long key) {
    return false;
  }

  @Override
  public void activateWidget(IdentifiableWidget widget) {
  }

  @Override
  public void addDomainEntry(Domain domain, IdentifiableWidget widget, Long key, String caption) {
  }

  @Override
  public void closeAll() {
    IdentifiableWidget widget = getActiveWidget();
    if (widget != null) {
      getScreenPanel().remove(widget);
    }
  }

  @Override
  public boolean closeWidget(IdentifiableWidget widget) {
    if (widget == null) {
      return false;

    } else if (UiHelper.isModal(widget.asWidget())) {
      return UiHelper.closeDialog(widget.asWidget());

    } else {
      return Objects.equals(widget, getActiveWidget()) && getScreenPanel().remove(widget);
    }
  }

  @Override
  public boolean containsDomainEntry(Domain domain, Long key) {
    return false;
  }

  @Override
  public int getActivePanelHeight() {
    return getScreenPanel().getCenterHeight();
  }

  @Override
  public int getActivePanelWidth() {
    return getScreenPanel().getCenterWidth();
  }

  @Override
  public IdentifiableWidget getActiveWidget() {
    return getScreenPanel().getCenter();
  }

  @Override
  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> info = new ArrayList<>();

    info.add(new ExtendedProperty("Center Width", BeeUtils.toString(getActivePanelWidth())));
    info.add(new ExtendedProperty("Center Height", BeeUtils.toString(getActivePanelHeight())));

    return info;
  }

  @Override
  public List<IdentifiableWidget> getOpenWidgets() {
    List<IdentifiableWidget> result = new ArrayList<>();
    if (getActiveWidget() != null) {
      result.add(getActiveWidget());
    }
    return result;
  }

  @Override
  public UserInterface getUserInterface() {
    return UserInterface.ORDERS;
  }

  @Override
  public void onLoad() {
    if (OrdEcKeeper.showGlobalSearch()) {
      OrdEcKeeper.getSearchBox().setFocus(true);
    }

    OrdEcKeeper.showPromo(false);
    OrdEcKeeper.restoreShoppingCarts(null);
  }

  @Override
  protected void onUserSignatureClick(ClickEvent event) {
    PasswordService.change();
  }

  @Override
  public void onWidgetChange(IdentifiableWidget widget) {
  }

  @Override
  public boolean removeDomainEntry(Domain domain, Long key) {
    return false;
  }

  @Override
  public void showInNewPlace(IdentifiableWidget widget) {
    updateActivePanel(widget);
  }

  @Override
  public void updateActivePanel(IdentifiableWidget widget) {
    getScreenPanel().updateCenter(widget);
  }

  @Override
  public void updateMenu(IdentifiableWidget widget) {
  }

  @Override
  protected String getScreenStyle() {
    return EcStyles.name("Screen");
  }

  @Override
  protected void hideProgressPanel() {
  }

  @Override
  protected IdentifiableWidget initCenter() {
    return new OrdEcItemPanel(false, null);
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initEast() {
    return Pair.of(ClientLogManager.getLogPanel(), 0);
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initNorth() {
    Flow panel = new Flow();
    EcStyles.add(panel, "NorthContainer");

    Widget logo = createLogo(() -> OrdEcKeeper.showPromo(false));

    if (logo != null) {
      EcStyles.add(logo, "Logo");
      panel.add(logo);
    }

    if (OrdEcKeeper.showGlobalSearch()) {
      panel.add(createGlobalSearch());
    }

    createCommands(panel);

    Widget userContainer = createUserContainer();
    userContainer.addStyleName(BeeConst.CSS_CLASS_PREFIX + "UserContainer");

    panel.add(userContainer);

    Notification nw = new Notification();
    EcStyles.add(nw, "Notifications");
    setNotification(nw);

    panel.add(nw);

    return Pair.of(panel, getNorthHeight(100));
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initSouth() {
    Flow panel = new Flow(EcStyles.name("ProgressPanel"));
    panel.add(createCopyright(BeeConst.CSS_CLASS_PREFIX + "ec-"));
    setProgressPanel(panel);

    return Pair.of(panel, 18);
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initWest() {
    Shell shell = new Shell(EcStyles.name("shell"));
    shell.restore();

    Simple wrapper = new Simple(shell);
    return Pair.of(wrapper, 0);
  }

  @Override
  protected void showProgressPanel() {
  }

  private Widget createGlobalSearch() {
    String styleName = "GlobalSearch";

    final InputText input = OrdEcKeeper.getSearchBox();

    DomUtils.setSearch(input);
    DomUtils.setPlaceholder(input, Localized.dictionary().ecGlobalSearchPlaceholder());
    EcStyles.add(input, styleName, "input");

    AutocompleteProvider.enableAutocomplete(input, EcConstants.NAME_PREFIX + styleName);

    input.addKeyDownHandler(event -> {
      int keyCode = event.getNativeKeyCode();

      switch (keyCode) {
        case KeyCodes.KEY_ENTER:
          String query = BeeUtils.trim(input.getValue());
          if (!BeeUtils.isEmpty(query)) {
            OrdEcKeeper.doGlobalSearch(query, input);
          }
          break;

        case KeyCodes.KEY_LEFT:
        case KeyCodes.KEY_RIGHT:
          if (input.isEmpty()) {
            Direction dir = (keyCode == KeyCodes.KEY_LEFT) ? Direction.WEST : Direction.EAST;

            int oldSize = getScreenPanel().getDirectionSize(dir);
            int newSize = (oldSize > 0) ? 0 : getActivePanelWidth() / 5;

            getScreenPanel().setDirectionSize(dir, newSize, true);
          }
          break;
      }
    });

    Image submit = new Image(EcUtils.imageUrl("search_button.png"));
    EcStyles.add(submit, styleName, "submit");

    submit.addClickHandler(event -> {
      String query = BeeUtils.trim(input.getValue());
      if (!BeeUtils.isEmpty(query)) {
        OrdEcKeeper.doGlobalSearch(query, input);
      }
    });

    Horizontal container = new Horizontal();
    EcStyles.add(container, styleName, "container");

    container.add(input);
    container.add(submit);

    return container;
  }
}