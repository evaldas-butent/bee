package com.butent.bee.client.modules;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.modules.commons.CommonsKeeper;
import com.butent.bee.client.modules.crm.CrmKeeper;
import com.butent.bee.client.modules.mail.MailKeeper;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.PasswordService;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Command;
import com.butent.bee.shared.utils.BeeUtils;

public class ModuleManager {

  private static class UserFormInterceptor extends AbstractFormInterceptor {
    @Override
    public void afterCreateWidget(String name, final IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (BeeUtils.same(name, "ChangePassword") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            CompositeService.doService(new PasswordService().name(),
                PasswordService.STG_GET_PASS, UiHelper.getForm(widget.asWidget()));
          }
        });
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return this;
    }
  }

  public static void maybeInitialize(final Command command) {
    CalendarKeeper.ensureData(command);
  }

  public static void onLoad() {
    FormFactory.registerFormInterceptor("User", new UserFormInterceptor());

    TransportHandler.register();
    CommonsKeeper.register();
    MailKeeper.register();

    CrmKeeper.register();
    CalendarKeeper.register();
    MailKeeper.register();
  }

  private ModuleManager() {
  }
}
