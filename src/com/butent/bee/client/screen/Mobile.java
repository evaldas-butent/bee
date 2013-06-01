package com.butent.bee.client.screen;

import com.google.common.base.Objects;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.butent.bee.client.Bee;
import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.cli.CliWidget;
import com.butent.bee.client.cli.CliWorker;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.LayoutPanel;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles mobile phone size screen implementation.
 */

public class Mobile extends ScreenImpl {

  public Mobile() {
    super();
  }

  @Override
  public boolean activateDomainEntry(Domain domain, Long key) {
    return false;
  }
  
  @Override
  public void activateWidget(IdentifiableWidget widget) {
    notifyWarning("The white zone is for loading and unloading only");
  }

  @Override
  public void addDomainEntry(Domain domain, IdentifiableWidget widget, Long key, String caption) {
    notifyWarning("The white zone is for loading and unloading only");
  }

  @Override
  public void closeWidget(IdentifiableWidget widget) {
    Assert.notNull(widget, "closeWidget: widget is null");

    if (Objects.equal(widget, getActiveWidget())) {
      getScreenPanel().remove(widget);
    } else {
      notifyWarning("closeWidget: widget not found");
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
    Global.inform(String.valueOf(getActivePanelWidth()), String.valueOf(getActivePanelHeight()));
  }

  @Override
  public void showWidget(IdentifiableWidget widget, boolean newPlace) {
    getScreenPanel().updateCenter(widget);
  }

  @Override
  public void start() {
    Element loading = DomUtils.getElement("loading");
    if (loading != null) {
      Document.get().getBody().removeChild(loading);
    }

    createUi();
    notifyInfo(BeeUtils.joinWords("Start Time:",
        System.currentTimeMillis() - Settings.getStartMillis(), "ms"));
  }

  @Override
  public void updateActivePanel(IdentifiableWidget widget) {
    showWidget(widget, false);
  }

  protected int addLogToggle(LayoutPanel panel) {
    final BeeCheckBox toggle = new BeeCheckBox("Log");

    toggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        ClientLogManager.setPanelVisible(event.getValue());
      }
    });

    panel.addRightWidthTop(toggle, 3, 48, 2);
    return 50;
  }

  @Override
  protected void createUi() {
    IdentifiableWidget w;
    Split p = new Split(1);

    w = initNorth();
    if (w != null) {
      p.addNorth(w, 60);
    }

    w = initSouth();
    if (w != null) {
      p.addSouth(w, 30);
    }

    w = initWest();
    if (w != null) {
      p.addWest(w, getWestWidth());
    }

    w = initEast();
    if (w != null) {
      p.addEast(w, BeeUtils.clamp(DomUtils.getClientWidth() / 5, 128, 300));
    }

    w = initCenter();
    if (w != null) {
      p.add(w);
    }

    getRootPanel().add(p);
    setScreenPanel(p);

    ClientLogManager.setPanelVisible(false);
  }

  protected int getWestWidth() {
    return BeeUtils.clamp(DomUtils.getClientWidth() / 5, 100, 200);
  }

  @Override
  protected IdentifiableWidget initCenter() {
    return null;
  }
  
  @Override
  protected IdentifiableWidget initNorth() {
    Complex panel = new Complex();
    panel.addStyleName("bee-NorthContainer");
    
    panel.addLeftTop(Global.getSearchWidget(), 40, 2);
    
    Flow menuContainer = new Flow();
    menuContainer.addStyleName("bee-MainMenu");
    panel.addLeftTop(menuContainer, 10, 30);
    setMenuPanel(menuContainer);
    
    setNotification(new Notification());
    panel.addRightTop(getNotification(), 1, 1);

    return panel;
  }

  @Override
  protected IdentifiableWidget initSouth() {
    LayoutPanel p = new LayoutPanel();

    int width = DomUtils.getClientWidth();
    int pct = BeeUtils.toInt(BeeUtils.rescale(width, 320, 800, 28, 50));

    final CliWidget cli = new CliWidget();
    p.addLeftWidthTop(cli, 3, CssUnit.PX, pct, CssUnit.PCT, 3, CssUnit.PX);

    Image play = new Image(Global.getImages().play(), new Command() {
      @Override
      public void execute() {
        CliWorker.execute(cli.getValue());
      }
    });
    p.addLeftTop(play, pct + 4, CssUnit.PCT, 2, CssUnit.PX);

    Horizontal hor = new Horizontal();

    Button auth = new Button("Exit", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Bee.exit();
      }
    });

    auth.setId("auth-button");
    hor.add(auth);

    Label user = new Label();
    StyleUtils.setHorizontalPadding(user, 5);
    hor.add(user);
    setSignature(user);

    int right = addLogToggle(p);
    p.addLeftRightTop(hor, pct + 12, CssUnit.PCT, right, CssUnit.PX, 1, CssUnit.PX);

    return p;
  }

  @Override
  protected IdentifiableWidget initWest() {
    return null;
  }
}
