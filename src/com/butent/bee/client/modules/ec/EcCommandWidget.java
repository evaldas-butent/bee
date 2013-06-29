package com.butent.bee.client.modules.ec;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.event.Binder;
import com.butent.bee.client.widget.InternalLink;

class EcCommandWidget {

  private static final String STYLE_NAME = "Command";
  private static final String STYLE_ACTIVE = "active";

  private final String service;
  private final Widget widget;

  EcCommandWidget(String service, String html) {
    this.service = service;
    this.widget = new InternalLink(html);

    EcStyles.add(widget, STYLE_NAME);
    EcStyles.add(widget, STYLE_NAME, service);

    Binder.addClickHandler(widget, new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        EcKeeper.doCommand(EcCommandWidget.this);
      }
    });
  }

  void activate() {
    EcStyles.add(widget, STYLE_NAME, STYLE_ACTIVE);
  }

  void deactivate() {
    EcStyles.remove(widget, STYLE_NAME, STYLE_ACTIVE);
  }

  String getService() {
    return service;
  }

  Widget getWidget() {
    return widget;
  }
}
