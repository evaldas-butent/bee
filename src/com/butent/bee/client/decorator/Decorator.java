package com.butent.bee.client.decorator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

class Decorator implements HasEnabled, HasExtendedInfo {

  private final String id;
  
  private final List<Parameter> params = Lists.newArrayList();
  private final Map<String, String> css = Maps.newHashMap();
  private final Lifecycle lifecycle;
  private final List<Handler> handlers = Lists.newArrayList();
  
  private final Template template;
  
  private boolean enabled = true;
  private int counter = 0;

  Decorator(String id, List<Parameter> params, Map<String, String> css, Lifecycle lifecycle,
      List<Handler> handlers, Template template) {
    super();
    this.id = id;
    
    if (params != null && !params.isEmpty()) {
      this.params.addAll(params);
    }
    if (css != null && !css.isEmpty()) {
      this.css.putAll(css);
    }

    this.lifecycle = lifecycle;
    
    if (handlers != null && !handlers.isEmpty()) {
      this.handlers.addAll(handlers);
    }
    
    this.template = template;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> result = Lists.newArrayList();
    PropertyUtils.addExtended(result, DecoratorConstants.ATTR_ID, getId());
    PropertyUtils.addExtended(result, "counter", getCounter());
    PropertyUtils.addExtended(result, "enabled", isEnabled());
    
    int idx;
    
    if (!params.isEmpty()) {
      PropertyUtils.addExtended(result, "parameters", BeeUtils.bracket(params.size()));
      idx = 0;
      for (Parameter param : params) {
        PropertyUtils.addChildren(result, BeeUtils.concat(1, DecoratorConstants.TAG_PARAM, ++idx),
            DecoratorConstants.ATTR_NAME, param.getName(),
            DecoratorConstants.ATTR_DEFAULT, param.getDefaultValue(),
            DecoratorConstants.ATTR_REQUIRED, param.isRequired());
      }
    }
    if (!css.isEmpty()) {
      PropertyUtils.addExtended(result, "styles", BeeUtils.bracket(css.size()));
      idx = 0;
      for (Map.Entry<String, String> entry : css.entrySet()) {
        PropertyUtils.addExtended(result, BeeUtils.concat(1, DecoratorConstants.TAG_STYLE, ++idx),
            entry.getKey(), entry.getValue());
      }
    }
    if (lifecycle != null) {
      PropertyUtils.addChildren(result, DecoratorConstants.TAG_LIFECYCLE,
          DecoratorConstants.TAG_CREATED, lifecycle.getCreated(),
          DecoratorConstants.TAG_INSERTED, lifecycle.getInserted(),
          DecoratorConstants.TAG_REMOVED, lifecycle.getRemoved());
    }
    if (!handlers.isEmpty()) {
      PropertyUtils.addExtended(result, "handlers", BeeUtils.bracket(handlers.size()));
      idx = 0;
      for (Handler handler : handlers) {
        PropertyUtils.addExtended(result, BeeUtils.concat(1, DecoratorConstants.TAG_HANDLER, ++idx),
            handler.getType(), handler.getBody());
      }
    }
    if (template != null) {
      PropertyUtils.addExtended(result, DecoratorConstants.TAG_TEMPLATE,
          template.getId(), template.getMarkup());
    }
    return result;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  Widget decorate(Widget widget, JSONObject options) {
    Map<String, String> values = getValues(options);
    Element element = getElement(values);
    
    boolean ok = false;
    NodeList<Element> list = element.getElementsByTagName(DecoratorConstants.TAG_CONTENT);
    if (list != null && list.getLength() == 1) {
      Element content = list.getItem(0);
      Element parent = content.getParentElement();
      if (parent != null) {
        parent.removeChild(content);
        parent.appendChild(widget.getElement());
        ok = true;
      }
    }
    
    if (ok) {
      widget.addStyleName(getId() + "-content");
      if (getCounter() <= 0) {
        addCss(values);
      }
      setCounter(getCounter() + 1);
      
      return new DecoratedWidget(widget, element);
    } else {
      return widget;
    }
  }

  Map<String, String> getCss() {
    return css;
  }

  List<Handler> getHandlers() {
    return handlers;
  }
  
  String getId() {
    return id;
  }
  
  Lifecycle getLifecycle() {
    return lifecycle;
  }

  List<Parameter> getParams() {
    return params;
  }

  Template getTemplate() {
    return template;
  }

  boolean isValid() {
    return template != null;
  }

  private void addCss(Map<String, String> values) {
    for (Map.Entry<String, String> sheet : css.entrySet()) {
      String name = sheet.getKey();
      String text = sheet.getValue();
      Global.addStyleSheet(applyValues(name, values), applyValues(text, values));
    }
  }

  private String applyValues(String source, Map<String, String> values) {
    if (BeeUtils.isEmpty(source) || values.isEmpty()) {
      return source;
    }
    
    String result = source.trim();
    for (Map.Entry<String, String> entry : values.entrySet()) {
      if (entry.getValue() != null) {
        result = result.replace(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
  
  private int getCounter() {
    return counter;
  }
  
  private Element getElement(Map<String, String> values) {
    DivElement tmpDiv = Document.get().createDivElement();
    tmpDiv.setInnerHTML(applyValues(template.getMarkup(), values));
    return tmpDiv.getFirstChildElement();
  }
  
  private Map<String, String> getValues(JSONObject options) {
    Map<String, String> result = Maps.newHashMap();
    for (Parameter param : params) {
      result.put("{" + param.getName() + "}", param.getValue(options));
    }
    return result;
  }
  
  private void setCounter(int counter) {
    this.counter = counter;
  }
}
