package com.butent.bee.client.modules.ec;

import com.google.common.base.Objects;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.ScreenImpl;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class EcScreen extends ScreenImpl {
  
  private static class CommandWidget {
    
    private static final String STYLE_NAME = "Command"; 
    
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
          LogUtils.getRootLogger().debug(CommandWidget.this.service);
        }
      });
    }
  }

  public EcScreen() {
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
    Flow panel = new Flow();
    EcStyles.add(panel, "StartPanel");
    
    String styleLabel = "label";
    String styleContainer = "container";
    String styleItem = "item";

    String style = "Featured";
    
    Label featuredLabel = new Label(Localized.constants.ecFeaturedItems());
    EcStyles.add(featuredLabel, style, styleLabel);
    panel.add(featuredLabel);
    
    Flow featuredContainer = new Flow(); 
    EcStyles.add(featuredContainer, style, styleContainer);
    
    int count = BeeUtils.randomInt(2, 10);
    for (int i = 0; i < count; i++) {
      Widget item = EcUtils.randomPicture(60, 200);
      EcStyles.add(item, style, styleItem);
      featuredContainer.add(item);
    }
    
    panel.add(featuredContainer);
    
    style = "Novelty";

    Label noveltyLabel = new Label(Localized.constants.ecNoveltyItems());
    EcStyles.add(noveltyLabel, style, styleLabel);
    panel.add(noveltyLabel);

    Flow noveltyContainer = new Flow(); 
    EcStyles.add(noveltyContainer, style, styleContainer);
    
    count = BeeUtils.randomInt(5, 25);
    for (int i = 0; i < count; i++) {
      Widget item = EcUtils.randomPicture(30, 120);
      EcStyles.add(item, style, styleItem);
      noveltyContainer.add(item);
    }
    
    panel.add(noveltyContainer);
    
    return panel;
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initEast() {
    return Pair.of(ClientLogManager.getLogPanel(), 0);
  }
  
  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initNorth() {
    Flow panel = new Flow();
    EcStyles.add(panel, "NorthContainer");

    Widget logo = createLogo();
    if (logo != null) {
      EcStyles.add(logo, "Logo");
      panel.add(logo);
    }

    panel.add(createGeneralSearch());
    
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
  
  private void createCommands(HasWidgets container) {
    String panelStyle = "commandPanel";
    
    Horizontal info = new Horizontal();
    EcStyles.add(info, panelStyle);
    EcStyles.add(info, panelStyle, "info");
    
    info.add(createCommandWidget(EcConstants.SVC_FINANCIAL_INFORMATION,
        Localized.constants.ecFinancialInformation()));
    info.add(createCommandWidget(EcConstants.SVC_TERMS_OF_DELIVERY,
        Localized.constants.ecTermsOfDelivery()));

    container.add(info);
    
    Horizontal contact = new Horizontal();
    EcStyles.add(contact, panelStyle);
    EcStyles.add(contact, panelStyle, "contact");
    
    contact.add(createCommandWidget(EcConstants.SVC_CONTACTS, Localized.constants.ecContacts()));
    
    container.add(contact);

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
    
    HtmlTable cart = new HtmlTable();
    EcStyles.add(cart, panelStyle);
    EcStyles.add(cart, panelStyle, "cart");
    
    cart.setWidget(0, 0, createCommandWidget(EcConstants.SVC_SHOPPING_CART_MAIN,
        Localized.constants.ecShoppingCartMain()));
    cart.setText(0, 1, "(0)"); 

    cart.setWidget(1, 0, createCommandWidget(EcConstants.SVC_SHOPPING_CART_ALTERNATIVE,
        Localized.constants.ecShoppingCartAlternative()));
    cart.setText(1, 1, "(0)"); 
    
    container.add(cart);
  }
  
  private Widget createCommandWidget(String service, String html) {
    CommandWidget commandWidget = new CommandWidget(service, html);
    return commandWidget.widget;
  }
  
  private Widget createGeneralSearch() {
    InputText input = new InputText();
    DomUtils.setSearch(input);
    DomUtils.setPlaceholder(input, Localized.constants.ecSearchGeneralPlaceholder());
    EcStyles.add(input, "GlobalSearchBox");
    
    Simple container = new Simple(input);
    EcStyles.add(container, "GlobalSearchContainer");
    
    return container;
  }
}
