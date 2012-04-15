package com.butent.bee.client.decorator;

import com.google.gwt.xml.client.Element;

import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.utils.BeeUtils;

class Handler {
  
  static Handler getHandler(Element element) {
    if (element == null) {
      return null;
    }
    
    String event = element.getAttribute(DecoratorConstants.ATTR_EVENT);
    String text = XmlUtils.getText(element);
    
    if (BeeUtils.isEmpty(event) || BeeUtils.isEmpty(text)) {
      return null;
    } else {
      return new Handler(event, text);
    }
  }
  
  private final String type;
  private final String body;

  Handler(String type, String body) {
    super();
    this.type = type;
    this.body = body;
  }

  String getType() {
    return type;
  }

  String getBody() {
    return body;
  }
}
