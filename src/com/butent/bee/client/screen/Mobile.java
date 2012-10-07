package com.butent.bee.client.screen;

import com.google.common.base.Objects;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.cli.CliWidget;
import com.butent.bee.client.cli.CliWorker;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.logging.PanelHandler;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles mobile phone size screen implementation.
 */

public class Mobile extends ScreenImpl {

  public Mobile() {
    super();
  }

  @Override
  public void closeWidget(Widget widget) {
    Assert.notNull(widget, "closeWidget: widget is null");

    if (Objects.equal(widget, getActiveWidget())) {
      getScreenPanel().remove(widget);
    } else {
      notifyWarning("closeWidget: widget not found");
    }
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
  public Widget getActiveWidget() {
    return getScreenPanel().getCenter();
  }

  @Override
  public void showInfo() {
    Global.inform(String.valueOf(getActivePanelWidth()), String.valueOf(getActivePanelHeight()));
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
  public void updateActivePanel(Widget widget, ScrollBars scroll) {
    getScreenPanel().updateCenter(widget, scroll);
  }

  protected int addLogToggle(BeeLayoutPanel panel) {
    final BeeCheckBox toggle = new BeeCheckBox("Log");

    toggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        PanelHandler handler = getLogHandler();
        if (handler != null) {
          handler.setVisible(event.getValue());
        }
      }
    });

    panel.addRightWidthTop(toggle, 3, 48, 2);
    return 50;
  }

  @Override
  protected void createUi() {
    Widget w;
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
      p.addEast(w, BeeUtils.clamp(DomUtils.getClientWidth() / 5, 128, 300), ScrollBars.BOTH);
    }

    w = initCenter();
    if (w != null) {
      p.add(w, ScrollBars.BOTH);
    }

    getRootPanel().add(p);
    setScreenPanel(p);

    PanelHandler handler = getLogHandler();
    if (handler != null) {
      handler.setVisible(false);
    }
  }

  protected int getWestWidth() {
    return BeeUtils.clamp(DomUtils.getClientWidth() / 5, 100, 200);
  }

  @Override
  protected Widget initCenter() {
    return null;
  }
  
  @Override
  protected Widget initNorth() {
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
  protected Widget initSouth() {
    BeeLayoutPanel p = new BeeLayoutPanel();

    int width = DomUtils.getClientWidth();
    int pct = BeeUtils.toInt(BeeUtils.rescale(width, 320, 800, 28, 50));

    final CliWidget cli = new CliWidget();
    p.addLeftWidthTop(cli, 3, Unit.PX, pct, Unit.PCT, 3, Unit.PX);

    BeeImage play = new BeeImage(Global.getImages().play(), new Command() {
      @Override
      public void execute() {
        CliWorker.execute(cli.getValue());
      }
    });
    p.addLeftTop(play, pct + 4, Unit.PCT, 2, Unit.PX);

    Horizontal hor = new Horizontal();

    BeeButton auth = new BeeButton("Exit", Service.LOGOUT);
    auth.setId("auth-button");
    hor.add(auth);

    BeeLabel user = new BeeLabel();
    StyleUtils.setHorizontalPadding(user, 5);
    hor.add(user);
    setSignature(user);

    int right = addLogToggle(p);
    p.addLeftRightTop(hor, pct + 12, Unit.PCT, right, Unit.PX, 1, Unit.PX);

    return p;
  }

  @Override
  protected Widget initWest() {
    return null;
  }
}
