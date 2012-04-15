package com.butent.bee.client.decorator;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.utils.JsonUtils;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.utils.BeeUtils;

class Parameter {
  
  static Parameter getParameter(Element element) {
    if (element == null) {
      return null;
    }
    
    String name = element.getAttribute(DecoratorConstants.ATTR_NAME);
    String defaultValue = element.getAttribute(DecoratorConstants.ATTR_DEFAULT);
    boolean required = BeeUtils.toBoolean(element.getAttribute(DecoratorConstants.ATTR_REQUIRED));
    
    if (BeeUtils.isEmpty(name)) {
      return null;
    } else {
      return new Parameter(name.trim(), defaultValue, required);
    }
  }
  
  private final String name;
  private final String defaultValue;
  private final boolean required;

  Parameter(String name, String defaultValue, boolean required) {
    super();
    this.name = name;
    this.defaultValue = defaultValue;
    this.required = required;
  }

  String getDefaultValue() {
    return defaultValue;
  }

  String getName() {
    return name;
  }

  String getValue(JSONObject options) {
    if (options == null || !options.containsKey(name)) {
      return defaultValue;
    }
    
    String value = JsonUtils.toString(options.get(name));
    return BeeUtils.ifString(value, defaultValue);
  }
  
  boolean isRequired() {
    return required;
  }
}
