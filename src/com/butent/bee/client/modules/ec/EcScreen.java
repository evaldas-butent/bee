package com.butent.bee.client.modules.ec;

import com.google.common.base.Objects;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.cli.Shell;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.ScreenImpl;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class EcScreen extends ScreenImpl {

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
  public void onLoad() {
    EcKeeper.getSearchBox().setFocus(true);
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
    Shell shell = new Shell(EcStyles.name("shell"));
    shell.restore();

    Simple wrapper = new Simple(shell);
    return Pair.of(wrapper, 0);
  }

  private void createCommands(HasWidgets container) {
    String panelStyle = "commandPanel";

    Horizontal fin = new Horizontal();
    EcStyles.add(fin, panelStyle);
    EcStyles.add(fin, panelStyle, "fin");

    fin.add(createCommandWidget(EcConstants.SVC_FINANCIAL_INFORMATION,
        Localized.constants.ecFinancialInformation()));

    container.add(fin);

    boolean hasTerms = true;
    boolean hasContacts = true;

    if (hasTerms || hasContacts) {
      Horizontal info = new Horizontal();
      EcStyles.add(info, panelStyle);
      EcStyles.add(info, panelStyle, "info");

      if (hasTerms) {
        info.add(createCommandWidget(EcConstants.SVC_SHOW_TERMS_OF_DELIVERY,
            Localized.constants.ecTermsOfDelivery()));
      }
      if (hasContacts) {
        info.add(createCommandWidget(EcConstants.SVC_SHOW_CONTACTS,
            Localized.constants.ecContacts()));
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

    container.add(EcKeeper.getCartlist());
  }

  private Widget createCommandWidget(String service, String html) {
    EcCommandWidget commandWidget = new EcCommandWidget(service, html);
    return commandWidget.getWidget();
  }

  private Widget createGlobalSearch() {
    final InputText input = EcKeeper.getSearchBox();

    DomUtils.setSearch(input);
    DomUtils.setPlaceholder(input, Localized.constants.ecGlobalSearchPlaceholder());
    EcStyles.add(input, "GlobalSearchBox");

    input.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        
        if (keyCode == KeyCodes.KEY_ENTER) {
          String query = BeeUtils.trim(input.getValue());
          if (!BeeUtils.isEmpty(query)) {
            EcKeeper.doGlobalSearch(query);
          }

        } else if (EventUtils.isArrowKey(keyCode) && input.isEmpty()) {
          Direction direction = (keyCode == KeyCodes.KEY_LEFT || keyCode == KeyCodes.KEY_UP)
              ? Direction.WEST : Direction.EAST;
              
          int oldSize = getScreenPanel().getDirectionSize(direction);
          int newSize = (oldSize > 0) ? 0 : getActivePanelWidth() / 5;
          
          getScreenPanel().setDirectionSize(direction, newSize, true);
        }
      }
    });

    Simple container = new Simple(input);
    EcStyles.add(container, "GlobalSearchContainer");

    return container;
  }
}
