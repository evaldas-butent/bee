package com.butent.bee.client.decorator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

class Decorator implements HasEnabled, HasExtendedInfo {

  private class Fields implements HasExtendedInfo {

    private final List<Parameter> params = Lists.newArrayList();
    private final Map<String, String> constants = Maps.newHashMap();
    private final Map<String, String> css = Maps.newHashMap();
    private Lifecycle lifecycle = null;
    private final List<Handler> handlers = Lists.newArrayList();
    private Template template = null;

    private Fields(List<Parameter> params, Map<String, String> constants, Map<String, String> css,
        Lifecycle lifecycle, List<Handler> handlers, Template template) {
      super();

      if (params != null && !params.isEmpty()) {
        this.params.addAll(params);
      }
      if (constants != null && !constants.isEmpty()) {
        this.constants.putAll(constants);
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

      int idx;
      if (!params.isEmpty()) {
        PropertyUtils.addExtended(result, "parameters", BeeUtils.bracket(params.size()));
        idx = 0;
        for (Parameter param : params) {
          PropertyUtils.addChildren(result, BeeUtils.concat(1, "param", ++idx),
              "Name", param.getName(),
              "Default", param.getDefaultValue(),
              "Required", param.isRequired());
        }
      }
      if (!constants.isEmpty()) {
        PropertyUtils.addExtended(result, "constants", BeeUtils.bracket(constants.size()));
        idx = 0;
        for (Map.Entry<String, String> entry : constants.entrySet()) {
          result.add(new ExtendedProperty(BeeUtils.concat(1, DecoratorConstants.TAG_CONST, ++idx),
              entry.getKey(), entry.getValue()));
        }
      }
      if (!css.isEmpty()) {
        PropertyUtils.addExtended(result, "styles", BeeUtils.bracket(css.size()));
        idx = 0;
        for (Map.Entry<String, String> entry : css.entrySet()) {
          PropertyUtils.addExtended(result,
              BeeUtils.concat(1, DecoratorConstants.TAG_STYLE, ++idx),
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
          PropertyUtils.addExtended(result, BeeUtils.concat(1, DecoratorConstants.TAG_HANDLER,
              ++idx),
              handler.getType(), handler.getBody());
        }
      }
      if (template != null) {
        PropertyUtils.addExtended(result, DecoratorConstants.TAG_TEMPLATE,
            template.getId(), template.getMarkup());
      }
      return result;
    }

    private void addConstant(String name, String value, boolean override) {
      if (override || !constants.containsKey(name.trim())) {
        constants.put(name.trim(), BeeUtils.trim(value));
      }
    }

    private void addCss(String name, String text, boolean override) {
      if (override || !css.containsKey(name.trim())) {
        css.put(name.trim(), BeeUtils.trim(text));
      }
    }

    private void addParameter(Parameter param, boolean override) {
      int index = getParameterIndex(param);
      if (index >= 0) {
        if (override) {
          params.remove(index);
        } else {
          return;
        }
      }
      params.add(param);
    }

    private Fields getCopy() {
      return new Fields(params, constants, css, lifecycle, handlers, template);
    }

    private Lifecycle getLifecycle() {
      return lifecycle;
    }

    private int getParameterIndex(Parameter param) {
      int index = BeeConst.UNDEF;
      if (params.isEmpty()) {
        return index;
      }

      for (int i = 0; i < params.size(); i++) {
        if (BeeUtils.same(params.get(i).getName(), param.getName())) {
          index = i;
          break;
        }
      }
      return index;
    }

    private Template getTemplate() {
      return template;
    }

    private void setLifecycle(Lifecycle lifecycle) {
      this.lifecycle = lifecycle;
    }
    
    private void setTemplate(Template template) {
      this.template = template;
    }
  }

  private final boolean isAbstract;

  private final String id;
  private final String parent;

  private final Fields definedFields;
  private Fields fields = null;

  private boolean initialized = false;
  private int counter = 0;

  private boolean enabled = true;

  Decorator(String id, String parent, boolean isAbstract, List<Parameter> params,
      Map<String, String> constants, Map<String, String> css, Lifecycle lifecycle,
      List<Handler> handlers, Template template) {
    super();
    this.id = id;
    this.parent = parent;
    this.isAbstract = isAbstract;

    this.definedFields = new Fields(params, constants, css, lifecycle, handlers, template);
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> result = Lists.newArrayList();
    PropertyUtils.addProperties(result, false,
        DecoratorConstants.ATTR_ID, getId(),
        DecoratorConstants.ATTR_EXTENDS, getParent(),
        DecoratorConstants.TAG_ABSTRACT, isAbstract(),
        "initialized", isInitialized(),
        "counter", getCounter(),
        "enabled", isEnabled());

    PropertyUtils.appendWithPrefix(result, "def", definedFields.getExtendedInfo());
    if (getFields() != null) {
      PropertyUtils.appendExtended(result, getFields().getExtendedInfo());
    }
    return result;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  Widget decorate(Widget widget, Map<String, String> options) {
    if (getFields() == null) {
      BeeKeeper.getLog().severe("decorator", getId(), "has no fields");
      return widget;
    }
    if (!hasTemplate()) {
      BeeKeeper.getLog().severe("decorator", getId(), "has no template");
      return widget;
    }
    
    Map<String, String> substitutes = getSubstitutes(options);
    Element root = getElement(substitutes);
    if (root == null) {
      BeeKeeper.getLog().severe("decorator", getId(), "cannot create element");
      return widget;
    }
    if (BeeUtils.same(root.getTagName(), DecoratorConstants.TAG_CONTENT)) {
      BeeKeeper.getLog().severe("decorator", getId(), "template root element cannot be content");
      BeeKeeper.getLog().severe(root.toString());
      return widget;
    }

    NodeList<Element> list = root.getElementsByTagName(DecoratorConstants.TAG_CONTENT);
    int cnt = (list == null) ? 0 : list.getLength();
    if (cnt < 1) {
      BeeKeeper.getLog().severe("decorator", getId(), "template has no content");
      BeeKeeper.getLog().severe(root.toString());
      return widget;
    } else if (cnt > 1) {
      BeeKeeper.getLog().severe("decorator", getId(), "template has", cnt, "contents");
      BeeKeeper.getLog().severe(root.toString());
      return widget;
    }
    
    Element templateContent = list.getItem(0);
    String role = templateContent.getAttribute(DomUtils.ATTRIBUTE_ROLE);
    Style style = templateContent.getStyle();
    String className = templateContent.getClassName();
    
    Element contentParent = templateContent.getParentElement();
    Element widgetElement = widget.getElement();
    contentParent.replaceChild(widgetElement, templateContent);
    
    if (!BeeUtils.isEmpty(role)) {
      widgetElement.setAttribute(DomUtils.ATTRIBUTE_ROLE, role);
    }
    if (style != null) {
      StyleUtils.addStyle(widgetElement, style);
    }
    if (!BeeUtils.isEmpty(className)) {
      widgetElement.addClassName(className);
    }
    
    widgetElement.addClassName(getId() + BeeConst.CHAR_MINUS + DecoratorConstants.TAG_CONTENT);

    if (getCounter() <= 0) {
      addStyleSheets(substitutes);
    }
    setCounter(getCounter() + 1);

    return new DecoratedWidget(widget, root);
  }

  Fields getFields() {
    return fields;
  }

  String getId() {
    return id;
  }

  String getParent() {
    return parent;
  }

  void init(Fields parentFields) {
    setFields(definedFields.getCopy());

    if (parentFields != null) {
      if (!parentFields.params.isEmpty()) {
        for (Parameter param : parentFields.params) {
          getFields().addParameter(param, false);
        }
      }
      if (!parentFields.constants.isEmpty()) {
        for (Map.Entry<String, String> constant : parentFields.constants.entrySet()) {
          getFields().addConstant(constant.getKey(), constant.getValue(), false);
        }
      }
      if (!parentFields.css.isEmpty()) {
        for (Map.Entry<String, String> sheet : parentFields.css.entrySet()) {
          getFields().addCss(sheet.getKey(), sheet.getValue(), false);
        }
      }
      if (parentFields.getLifecycle() != null) {
        if (getFields().getLifecycle() == null) {
          getFields().setLifecycle(parentFields.getLifecycle().getCopy());
        } else {
          getFields().getLifecycle().updateFrom(parentFields.getLifecycle(), false);
        }
      }
      if (!parentFields.handlers.isEmpty()) {
        getFields().handlers.addAll(parentFields.handlers);
      }
      if (getFields().getTemplate() == null && parentFields.getTemplate() != null) {
        getFields().setTemplate(parentFields.getTemplate().getCopy());
      }
    }

    setInitialized(true);
  }

  boolean isAbstract() {
    return isAbstract;
  }

  boolean isInitialized() {
    return initialized;
  }

  void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  private void addStyleSheets(Map<String, String> substitutes) {
    for (Map.Entry<String, String> sheet : getFields().css.entrySet()) {
      String name = sheet.getKey();
      String text = sheet.getValue();
      Global.addStyleSheet(substitute(name, substitutes), substitute(text, substitutes));
    }
  }

  private int getCounter() {
    return counter;
  }

  private Element getElement(Map<String, String> substitutes) {
    DivElement tmpDiv = Document.get().createDivElement();
    tmpDiv.setInnerHTML(substitute(getFields().getTemplate().getMarkup(), substitutes));
    return tmpDiv.getFirstChildElement();
  }

  private Map<String, String> getSubstitutes(Map<String, String> options) {
    Map<String, String> result = Maps.newHashMap();
    for (Parameter param : getFields().params) {
      result.put(wrapSubstitute(param.getName()), param.getValue(options));
    }
    for (Map.Entry<String, String> constant : getFields().constants.entrySet()) {
      result.put(wrapSubstitute(constant.getKey()), BeeUtils.trim(constant.getValue()));
    }

    for (Map.Entry<String, String> entry : result.entrySet()) {
      String value = substitute(entry.getValue(), result);
      if (!BeeUtils.equalsTrim(value, entry.getValue())) {
        entry.setValue(value);
      }
    }
    return result;
  }

  private boolean hasTemplate() {
    return getFields().getTemplate() != null
        && !BeeUtils.isEmpty(getFields().getTemplate().getMarkup());
  }

  private void setCounter(int counter) {
    this.counter = counter;
  }

  private void setFields(Fields fields) {
    this.fields = fields;
  }

  private String substitute(String source, Map<String, String> substitutes) {
    if (BeeUtils.isEmpty(source) || substitutes.isEmpty()) {
      return source;
    }

    String result = source.trim();
    for (Map.Entry<String, String> entry : substitutes.entrySet()) {
      if (entry.getValue() != null) {
        result = result.replace(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  private String wrapSubstitute(String s) {
    return DecoratorConstants.SUBSTITUTE_PREFIX + s.trim() + DecoratorConstants.SUBSTITUTE_SUFFIX;
  }
}
