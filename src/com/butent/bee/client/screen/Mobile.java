package com.butent.bee.client.screen;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.cli.CliWidget;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.FontSize;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.grid.TextCellType;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Horizontal;
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
  
  public Mobile() {
    super();
  }

  @Override
  public String getDsn() {
    return Settings.getDsn();
  }

  public void start() {
    createUi();
    loadDataInfo();
  }

  @Override
  public void updateSignature() {
    if (getSignature() == null) {
      return;
    }

    String usr = BeeKeeper.getUser().getLogin();
    if (BeeUtils.isEmpty(usr)) {
      usr = Global.constants.notLoggedIn();
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
      p.addWest(w, 100);
    }

    w = initEast();
    if (w != null) {
      p.addEast(w, 128, ScrollBars.BOTH);
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

    Absolute data = new Absolute();
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

    hor.add(new BeeButton(Global.constants.login(), Service.GET_LOGIN, Stage.STAGE_GET_PARAMETERS));

    BeeLabel user = new BeeLabel();
    StyleUtils.setHorizontalPadding(user, 5);
    hor.add(user);

    setSignature(user);
    updateSignature();

    hor.add(new BeeButton(Global.constants.logout(), Service.LOGOUT));
    
    p.addLeftRightTop(hor, 43, Unit.PCT, 80, Unit.PX, 1, Unit.PX);
    
    final Toggle log = new Toggle("Hide Log", "Show Log");
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

  private Widget ensureLoadingWidget() {
    if (getLoadingWidget() == null) {
      setLoadingWidget(new BeeImage(Global.getImages().loading()));
    }
    return getLoadingWidget();
  }

  private Widget getLoadingWidget() {
    return loadingWidget;
  }
  
  private void loadDataInfo() {
    final HasWidgets panel = getDataPanel();
    panel.clear();
    panel.add(new BeeLabel("Loading data info..."));
    panel.add(ensureLoadingWidget());
    
    BeeKeeper.getRpc().makeGetRequest(Service.GET_VIEW_LIST, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);
        String[] arr = Codec.beeDeserialize((String) response.getResponse());
        panel.clear();

        for (String s : arr) {
          DataInfo info = DataInfo.restore(s);
          if (info != null && info.getRowCount() >= 0) {
            panel.add(new BeeButton(info.getName(), new OpenViewCommand(info)));
          }
        }
      }
    });
  }
  
  private void setLoadingWidget(Widget loadingWidget) {
    this.loadingWidget = loadingWidget;
  }
}
