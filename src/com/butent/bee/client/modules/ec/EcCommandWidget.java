package com.butent.bee.client.modules.ec;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.event.Binder;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;

class EcCommandWidget {

  enum Type {
    LINK("link") {
      @Override
      Widget create(String html) {
        return new InternalLink(html);
      }
    },
    LABEL("label") {
      @Override
      Widget create(String html) {
        return new Label(html);
      }
    };

    private final String style;

    Type(String style) {
      this.style = style;
    }

    abstract Widget create(String html);

    private String getStyle() {
      return style;
    }
  }

  private static final String STYLE_NAME = "Command";
  private static final String STYLE_ACTIVE = "-active";

  private final String service;

  private final Type type;
  private final Widget widget;

  EcCommandWidget(String service, String html, Type type) {
    this.service = service;

    this.type = type;
    this.widget = type.create(html);

    EcStyles.add(widget, STYLE_NAME);
    EcStyles.add(widget, STYLE_NAME, type.getStyle());
    EcStyles.add(widget, STYLE_NAME, service);

    Binder.addClickHandler(widget, new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        EcKeeper.doCommand(EcCommandWidget.this);
      }
    });
  }

  void activate() {
    EcStyles.add(widget, STYLE_NAME, type.getStyle() + STYLE_ACTIVE);
  }

  void deactivate() {
    EcStyles.remove(widget, STYLE_NAME, type.getStyle() + STYLE_ACTIVE);
  }

  String getService() {
    return service;
  }

  Widget getWidget() {
    return widget;
  }
}
