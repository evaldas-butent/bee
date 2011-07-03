package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.cli.CliWidget;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.FontSize;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.grid.TextCellType;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Scroll;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

/**
 * Handles mobile phone size screen implementation.
 */

public class Mobile extends ScreenImpl {

  private class OpenViewCommand extends BeeCommand {
    private final DataInfo dataInfo;

    private OpenViewCommand(DataInfo dataInfo) {
      super();
      this.dataInfo = dataInfo;
    }

    @Override
    public void execute() {
      Global.getDataExplorer().openView(dataInfo);
    }
  }

  private Widget loadingWidget = null;
  private BeeButton authButton = null;

  public Mobile() {
    super();
  }

  @Override
  public String getDsn() {
    return Settings.getDsn();
  }

  public void start() {
    Element loading = DomUtils.getElement("loading");
    if (loading != null) {
      Document.get().getBody().removeChild(loading);
    }

    createUi();
    notifyInfo(BeeUtils.concat(1, "Start Time:", 
        System.currentTimeMillis() - Settings.getStartMillis(), "ms"));
  }

  @Override
  public void updateActivePanel(Widget w, ScrollBars scroll) {
    if (scroll == null || scroll == ScrollBars.NONE || w instanceof ScrollPanel) {
      super.updateActivePanel(w, scroll);
    } else {
      super.updateActivePanel(new Scroll(w), ScrollBars.NONE);
    }
  }

  @Override
  public void updateSignature(boolean init) {
    if (getSignature() == null) {
      return;
    }

    String usr = BeeKeeper.getUser().getLogin();
    if (BeeUtils.isEmpty(usr)) {
      updateAuthWidget(true);
      usr = Global.constants.notLoggedIn();
      if (!init) {
        getDataPanel().clear();
        closePanel();
      }
    } else {
      updateAuthWidget(false);
      loadDataInfo();
    }

    getSignature().getElement().setInnerHTML(usr);
  }

  @Override
  protected void createUi() {
    Widget w;
    Split p = new Split(1);

    w = initNorth();
    if (w != null) {
      p.addNorth(w, 40);
    }

    w = initSouth();
    if (w != null) {
      p.addSouth(w, 30);
    }

    w = initWest();
    if (w != null) {
      p.addWest(w, BeeUtils.limit(DomUtils.getClientWidth() / 5, 100, 200));
    }

    w = initEast();
    if (w != null) {
      p.addEast(w, BeeUtils.limit(DomUtils.getClientWidth() / 5, 128, 300), ScrollBars.BOTH);
    }

    w = initCenter();
    if (w != null) {
      p.add(w, ScrollBars.BOTH);
    }

    getRootPanel().add(p);
    setScreenPanel(p);

    BeeKeeper.getLog().hide();
  }

  @Override
  protected TextCellType getDefaultCellType() {
    return TextCellType.TEXT;
  }

  @Override
  protected int getDefaultGridType() {
    return Settings.getGridType();
  }

  @Override
  protected Widget initNorth() {
    Complex p = new Complex();

    Horizontal data = new Horizontal();
    setDataPanel(data);
    p.addLeftTop(data, 1, Unit.EM, 4, Unit.PX);

    setNotification(new Notification());
    p.addRightTop(getNotification(), 1, 1);

    return p;
  }

  @Override
  protected Widget initSouth() {
    BeeLayoutPanel p = new BeeLayoutPanel();

    CliWidget cli = new CliWidget();
    p.addLeftWidthTop(cli, 3, Unit.PX, 40, Unit.PCT, 3, Unit.PX);

    Horizontal hor = new Horizontal();
    
    BeeButton auth = new BeeButton(true);
    auth.setId("auth-button");
    hor.add(auth);
    setAuthButton(auth);

    BeeLabel user = new BeeLabel();
    StyleUtils.setHorizontalPadding(user, 5);
    hor.add(user);
    setSignature(user);

    updateSignature(true);

    p.addLeftRightTop(hor, 43, Unit.PCT, 80, Unit.PX, 1, Unit.PX);

    final Toggle log = new Toggle("Hide Log", "Show Log", "toggleLog");
    StyleUtils.setFontSize(log, FontSize.SMALL);
    StyleUtils.setHorizontalPadding(log, 2);

    log.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        if (log.isDown()) {
          BeeKeeper.getLog().show();
        } else {
          BeeKeeper.getLog().hide();
        }
        log.invert();
      }
    });

    p.addRightWidthTop(log, 3, 76, 1);
    log.setDown(true);

    return p;
  }

  @Override
  protected Widget initWest() {
    return null;
  }

  private Widget createViewWidget(String caption, DataInfo info) {
    BeeButton button = new BeeButton(caption, new OpenViewCommand(info));
    button.setStyleName("viewButton");
    return button;
  }

  private Widget ensureLoadingWidget() {
    if (getLoadingWidget() == null) {
      setLoadingWidget(new BeeImage(Global.getImages().loading()));
    }
    return getLoadingWidget();
  }

  private BeeButton getAuthButton() {
    return authButton;
  }

  private Widget getLoadingWidget() {
    return loadingWidget;
  }

  private void loadDataInfo() {
    HasWidgets panel = getDataPanel();
    panel.clear();
    panel.add(new BeeLabel("Loading data info..."));
    panel.add(ensureLoadingWidget());

    BeeKeeper.getRpc().makeGetRequest(Service.GET_VIEW_LIST, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);
        String[] arr = Codec.beeDeserialize((String) response.getResponse());

        List<DataInfo> dataInfos = Lists.newArrayList();
        for (String s : arr) {
          DataInfo info = DataInfo.restore(s);
          if (info != null && info.getRowCount() >= 0) {
            dataInfos.add(info);
          }
        }
        showViews(dataInfos);
      }
    });
  }

  private void setAuthButton(BeeButton authButton) {
    this.authButton = authButton;
  }
  
  private void setLoadingWidget(Widget loadingWidget) {
    this.loadingWidget = loadingWidget;
  }
  
  private void showViews(List<DataInfo> dataInfos) {
    HasWidgets panel = getDataPanel();
    if (panel == null) {
      return;
    }
    panel.clear();
    if (BeeUtils.isEmpty(dataInfos)) {
      return;
    }
    
    Map<String, String> userViews = BeeKeeper.getUser().getViews();
    if (BeeUtils.isEmpty(userViews)) {
      for (DataInfo info : dataInfos) {
        panel.add(createViewWidget(info.getName(), info));
      }
    } else {
      for (Map.Entry<String, String> entry : userViews.entrySet()) {
        String name = entry.getKey();
        if (BeeUtils.isEmpty(name)) {
          continue;
        }
        String caption = BeeUtils.ifString(entry.getValue(), name);
        
        boolean found = false;
        for (DataInfo info : dataInfos) {
          if (BeeUtils.same(info.getName(), name)) {
            panel.add(createViewWidget(caption, info));
            found = true;
            break;
          }
        }
        if (!found) {
          BeeKeeper.getLog().warning("user view", name, caption, "not found");
        }
      }
    }
  }
  
  private void updateAuthWidget(boolean login) {
    if (getAuthButton() == null) {
      return;
    }
    getAuthButton().setHTML(login ? Global.constants.login() : Global.constants.logout());
    getAuthButton().setService(login ? Service.GET_LOGIN : Service.LOGOUT);
    getAuthButton().setStage(login ? Stage.STAGE_GET_PARAMETERS : null);
  }
}
