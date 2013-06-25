package com.butent.bee.client.modules.ec;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.cli.Shell;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.modules.ec.view.EcView;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.ScreenImpl;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.CartType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumMap;

import elemental.events.KeyboardEvent.KeyCode;

public class EcScreen extends ScreenImpl {

  private class CommandWidget {

    private static final String STYLE_NAME = "Command";
    private static final String STYLE_ACTIVE = "active";

    private final String service;
    private final Widget widget;

    private CommandWidget(String service, String html) {
      this.service = service;
      this.widget = new InternalLink(html);

      EcStyles.add(widget, STYLE_NAME);
      EcStyles.add(widget, STYLE_NAME, service);

      Binder.addClickHandler(widget, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          doCommand(CommandWidget.this);
        }
      });
    }

    private void activate() {
      EcStyles.add(widget, STYLE_NAME, STYLE_ACTIVE);
    }

    private void deactivate() {
      EcStyles.remove(widget, STYLE_NAME, STYLE_ACTIVE);
    }
  }

  private static final int CART_COL_CAPTION = 0;
  private static final int CART_COL_SIZE = 1;
  private static final int CART_COL_ACTIVE = 2;

  private InputText searchBox = null;
  private CommandWidget activeCommand = null;

  private final EnumMap<CartType, Integer> cartSize;
  private CartType activeCart = CartType.MAIN;

  private HtmlTable cartTable = null;

  public EcScreen() {
    super();

    this.cartSize = Maps.newEnumMap(CartType.class);
    for (CartType cartType : CartType.values()) {
      cartSize.put(cartType, 0);
    }
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

  public void addToCart(int count) {
    CartType cartType = getActiveCart();

    int size = cartSize.get(cartType) + count;
    cartSize.put(cartType, size);

    getCartTable().setText(cartType.ordinal(), CART_COL_SIZE, renderCartSize(cartType));
  }

  @Override
  public void closeWidget(IdentifiableWidget widget) {
    if (widget != null && Objects.equal(widget, getActiveWidget())) {
      getScreenPanel().remove(widget);
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
  public void onLoad() {
    if (getSearchBox() != null) {
      getSearchBox().setFocus(true);
    }

    EcKeeper.showFeaturedAndNoveltyItems();
  }

  @Override
  public boolean removeDomainEntry(Domain domain, Long key) {
    return false;
  }

  @Override
  public void showInfo() {
    Global.showModalGrid(getName(),
        new ExtendedPropertiesData(getScreenPanel().getExtendedInfo(), false));
  }

  @Override
  public void showWidget(IdentifiableWidget widget, boolean newPlace) {
    getScreenPanel().updateCenter(widget);
  }

  @Override
  public void start() {
    createUi();
  }

  @Override
  public void updateMenu(IdentifiableWidget widget) {
  }

  @Override
  protected String getScreenStyle() {
    return EcStyles.name("Screen");
  }

  @Override
  protected IdentifiableWidget initCenter() {
    return new CustomDiv();
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initEast() {
    return Pair.of(ClientLogManager.getLogPanel(), 0);
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initNorth() {
    Flow panel = new Flow();
    EcStyles.add(panel, "NorthContainer");

    Widget logo = createLogo(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        EcKeeper.showFeaturedAndNoveltyItems();
      }
    });

    if (logo != null) {
      EcStyles.add(logo, "Logo");
      panel.add(logo);
    }

    panel.add(createGlobalSearch());

    createCommands(panel);

    Widget userContainer = createUserContainer();
    EcStyles.add(userContainer, "UserContainer");

    panel.add(userContainer);

    Notification nw = new Notification();
    EcStyles.add(nw, "Notifications");
    setNotification(nw);

    panel.add(nw);

    return Pair.of(panel, 100);
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initWest() {
    Shell shell = new Shell();
    shell.setStyleName(EcStyles.name("shell"));

    shell.restore();

    Simple wrapper = new Simple(shell);
    return Pair.of(wrapper, 0);
  }

  private HtmlTable createCartTable() {
    String style = "cartTable";
    HtmlTable table = new HtmlTable();
    EcStyles.add(table, style);

    for (CartType cartType : CartType.values()) {
      int row = cartType.ordinal();
      
      Label label = new Label(cartType.getLabel());
      table.setWidgetAndStyle(row, CART_COL_CAPTION, label, EcStyles.name(style, "label"));

      table.setText(row, CART_COL_SIZE, renderCartSize(cartType));
    }

    renderCartActivity(table);

    setCartTable(table);
    return table;
  }

  private void createCommands(HasWidgets container) {
    String panelStyle = "commandPanel";

    Horizontal fin = new Horizontal();
    EcStyles.add(fin, panelStyle);
    EcStyles.add(fin, panelStyle, "fin");

    fin.add(createCommandWidget(EcConstants.SVC_FINANCIAL_INFORMATION,
        Localized.constants.ecFinancialInformation()));

    container.add(fin);

    boolean hasTerms = !BeeUtils.isEmpty(EcKeeper.getTermsUrl());
    boolean hasContacts = !BeeUtils.isEmpty(EcKeeper.getContactsUrl());

    if (hasTerms || hasContacts) {
      Horizontal info = new Horizontal();
      EcStyles.add(info, panelStyle);
      EcStyles.add(info, panelStyle, "info");

      if (hasTerms) {
        info.add(createCommandWidget(EcConstants.SVC_TERMS_OF_DELIVERY,
            Localized.constants.ecTermsOfDelivery()));
      }
      if (hasContacts) {
        info.add(createCommandWidget(EcConstants.SVC_CONTACTS, Localized.constants.ecContacts()));
      }

      container.add(info);
    }

    Horizontal searchBy = new Horizontal();
    EcStyles.add(searchBy, panelStyle);
    EcStyles.add(searchBy, panelStyle, "searchBy");

    searchBy.add(new Label(Localized.constants.ecSearchBy()));
    searchBy.add(createCommandWidget(EcConstants.SVC_SEARCH_BY_ITEM_CODE,
        Localized.constants.ecSearchByItemCode()));
    searchBy.add(createCommandWidget(EcConstants.SVC_SEARCH_BY_OE_NUMBER,
        Localized.constants.ecSearchByOeNumber()));
    searchBy.add(createCommandWidget(EcConstants.SVC_SEARCH_BY_CAR,
        Localized.constants.ecSearchByCar()));
    searchBy.add(createCommandWidget(EcConstants.SVC_SEARCH_BY_MANUFACTURER,
        Localized.constants.ecSearchByManufacturer()));

    container.add(searchBy);

    Horizontal searchOther = new Horizontal();
    EcStyles.add(searchOther, panelStyle);
    EcStyles.add(searchOther, panelStyle, "searchOther");

    searchOther.add(createCommandWidget(EcConstants.SVC_GENERAL_ITEMS,
        Localized.constants.ecGeneralItems()));
    searchOther.add(createCommandWidget(EcConstants.SVC_BIKE_ITEMS,
        Localized.constants.ecBikeItems()));

    container.add(searchOther);

    HtmlTable carts = createCartTable();
    container.add(carts);
  }

  private Widget createCommandWidget(String service, String html) {
    CommandWidget commandWidget = new CommandWidget(service, html);
    return commandWidget.widget;
  }

  private Widget createGlobalSearch() {
    final InputText input = new InputText();
    DomUtils.setSearch(input);
    DomUtils.setPlaceholder(input, Localized.constants.ecGlobalSearchPlaceholder());
    EcStyles.add(input, "GlobalSearchBox");

    input.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCode.ENTER) {
          String query = BeeUtils.trim(input.getValue());
          if (!BeeUtils.isEmpty(query)) {
            EcKeeper.doGlobalSearch(query);
          }
        }
      }
    });

    Simple container = new Simple(input);
    EcStyles.add(container, "GlobalSearchContainer");

    setSearchBox(input);

    return container;
  }

  private void doCommand(CommandWidget commandWidget) {
    EcView ecView = EcView.create(commandWidget.service);
    if (ecView != null) {
      updateActivePanel(ecView);
    }

    if (getActiveCommand() == null || !getActiveCommand().service.equals(commandWidget.service)) {
      if (getActiveCommand() != null) {
        getActiveCommand().deactivate();
      }

      setActiveCommand(commandWidget);
      getActiveCommand().activate();
    }
  }

  private CartType getActiveCart() {
    return activeCart;
  }

  private CommandWidget getActiveCommand() {
    return activeCommand;
  }

  private HtmlTable getCartTable() {
    return cartTable;
  }

  private InputText getSearchBox() {
    return searchBox;
  }

  private Widget renderActiveCart() {
    Image image = new Image("images/shopping_cart.png");
    image.setAlt("cart");

    return image;
  }

  private void renderCartActivity(HtmlTable table) {
    String activeRowStyle = EcStyles.name("cartRowActive");
    String inactiveRowStyle = EcStyles.name("cartRowInactive");

    for (CartType cartType : CartType.values()) {
      int row = cartType.ordinal();

      if (getActiveCart() == cartType) {
        table.setWidgetAndStyle(row, CART_COL_ACTIVE, renderActiveCart(),
            EcStyles.name("cartActive"));
        table.getRowFormatter().removeStyleName(row, inactiveRowStyle);
        table.getRowFormatter().addStyleName(row, activeRowStyle);

      } else {
        table.setWidgetAndStyle(row, CART_COL_ACTIVE, renderInactiveCart(cartType),
            EcStyles.name("cartInactive"));
        table.getRowFormatter().removeStyleName(row, activeRowStyle);
        table.getRowFormatter().addStyleName(row, inactiveRowStyle);
      }
    }
  }

  private String renderCartSize(CartType cartType) {
    return BeeUtils.parenthesize(cartSize.get(cartType));
  }

  private Widget renderInactiveCart(final CartType cartType) {
    CustomDiv widget = new CustomDiv();
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setActiveCart(cartType);
        renderCartActivity(getCartTable());
      }
    });

    return widget;
  }

  private void setActiveCart(CartType activeCart) {
    this.activeCart = activeCart;
  }

  private void setActiveCommand(CommandWidget activeCommand) {
    this.activeCommand = activeCommand;
  }

  private void setCartTable(HtmlTable cartTable) {
    this.cartTable = cartTable;
  }

  private void setSearchBox(InputText searchBox) {
    this.searchBox = searchBox;
  }
}
