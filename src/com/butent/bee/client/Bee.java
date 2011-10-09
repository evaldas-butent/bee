package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputPassword;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

/**
 * The entry point class of the application, initializes <code>BeeKeeper</code> class.
 */

public class Bee implements EntryPoint {
  public void onModuleLoad() {
    BeeConst.setClient();

    BeeKeeper bk = new BeeKeeper(RootLayoutPanel.get(), 
        GWT.getModuleBaseURL() + GWT.getModuleName());

    bk.init();
    bk.start();

    if (GWT.isProdMode()) {
      GWT.setUncaughtExceptionHandler(new ExceptionHandler());
    }
    
    bk.register();
    signIn();
  }
  
  private void signIn() {
    final Popup popup = new Popup(false, true);
    popup.setStyleName("bee-SignIn-Popup");

    Absolute panel = new Absolute(Position.RELATIVE);
    panel.addStyleName("bee-SignIn-Panel");
    
    BeeLabel caption = new BeeLabel("Būtent CRM");
    caption.addStyleName("bee-SignIn-Caption");
    panel.add(caption);
    
    BeeLabel userLabel = new BeeLabel("Prisijungimo vardas");
    userLabel.addStyleName("bee-SignIn-Label");
    userLabel.addStyleName("bee-SignIn-User");
    panel.add(userLabel);

    final InputText userBox = new InputText();
    userBox.addStyleName("bee-SignIn-Input");
    userBox.addStyleName("bee-SignIn-User");
    panel.add(userBox);

    BeeLabel pswdLabel = new BeeLabel("Slaptažodis");
    pswdLabel.addStyleName("bee-SignIn-Label");
    pswdLabel.addStyleName("bee-SignIn-Password");
    panel.add(pswdLabel);

    final InputPassword pswdBox = new InputPassword();
    pswdBox.addStyleName("bee-SignIn-Input");
    pswdBox.addStyleName("bee-SignIn-Password");
    panel.add(pswdBox);
    
    final RadioGroup langWidget = new RadioGroup(false, 0,
        Lists.newArrayList("lt", "lv", "et", "en", "de", "ru"));
    langWidget.addStyleName("bee-SignIn-Language");
    panel.add(langWidget);
    
    BeeButton button = new BeeButton("Prisijungti");
    button.setStyleName("bee-SignIn-Button");
    panel.add(button);
    
    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String userName = userBox.getValue();
        if (BeeUtils.isEmpty(userName)) {
          Global.showError("Įveskite prisijungimo vardą");
          return;
        }
        
        String password = pswdBox.getValue();
        if (BeeUtils.isEmpty(password)) {
          Global.showError("Įveskite slaptažodį");
          return;
        }
        
        BeeKeeper.getRpc().makePostRequest(Service.LOGIN,
            XmlUtils.createString(Service.XML_TAG_DATA,
                Service.VAR_LOGIN, userName, Service.VAR_PASSWORD, Codec.md5(password)));
        
        popup.hide();
        BeeKeeper.getScreen().start();
      }
    });
    
    popup.setWidget(panel);
    popup.center();
    
    userBox.setFocus(true);
  }
}
