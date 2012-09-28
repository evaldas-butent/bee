package com.butent.bee.client.modules;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.modules.commons.CommonsEventHandler;
import com.butent.bee.client.modules.crm.CrmKeeper;
import com.butent.bee.client.modules.mail.MailKeeper;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.PasswordService;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Command;
import com.butent.bee.shared.utils.BeeUtils;

public class ModuleManager {

  private static class UserFormCallback extends AbstractFormCallback {
    @Override
    public void afterCreateWidget(String name, final Widget widget,
        WidgetDescriptionCallback callback) {
      if (BeeUtils.same(name, "ChangePassword") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            CompositeService.doService(new PasswordService().name(),
                PasswordService.STG_GET_PASS, UiHelper.getForm(widget));
          }
        });
      }
    }

    @Override
    public FormCallback getInstance() {
      return this;
    }
  }

  public static void maybeInitialize(final Command command) {
    CalendarKeeper.ensureData(command);
  }

  public static void onLoad() {
    FormFactory.registerFormCallback("User", new UserFormCallback());

    TransportHandler.register();
    CommonsEventHandler.register();

    CrmKeeper.register();
    CalendarKeeper.register();
    MailKeeper.register();
  }

  private ModuleManager() {
  }
}
