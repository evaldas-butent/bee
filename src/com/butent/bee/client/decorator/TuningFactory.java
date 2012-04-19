package com.butent.bee.client.decorator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TuningFactory {

  private static final Map<String, Decorator> decorators = Maps.newHashMap();

  private static boolean enabled = true;

  public static void clear() {
    decorators.clear();
  }

  public static Widget decorate(String decoratorId, Element description, Widget widget) {
    if (widget == null || BeeUtils.isEmpty(decoratorId) || !isEnabled()) {
      return widget;
    }

    Decorator decorator = decorators.get(normalize(decoratorId));
    if (decorator == null) {
      BeeKeeper.getLog().warning("decorator not found:", decoratorId);
      return widget;
    }
    if (!decorator.isEnabled()) {
      return widget;
    }
    if (decorator.isAbstract()) {
      BeeKeeper.getLog().warning("decorator is abstract:", decoratorId);
      return widget;
    }
    
    if (!decorator.isInitialized()) {
      if (!initialize(decorator, null)) {
        decorator.setEnabled(false);
        return widget;
      }
    }

    return decorator.decorate(widget, getOptions(description));
  }

  public static List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> result = Lists.newArrayList();
    PropertyUtils.addExtended(result, DecoratorConstants.TAG_DECORATORS,
        BeeUtils.bracket(decorators.size()));

    if (!decorators.isEmpty()) {
      int idx = 0;
      for (Map.Entry<String, Decorator> entry : decorators.entrySet()) {
        PropertyUtils.addExtended(result, DecoratorConstants.TAG_DECORATOR,
            BeeUtils.progress(++idx, decorators.size()), entry.getKey());
        result.addAll(entry.getValue().getExtendedInfo());
      }
    }

    return result;
  }

  public static Map<String, String> getOptions(Element element) {
    Map<String, String> result = Maps.newHashMap();
    if (element == null) {
      return result;
    }

    NamedNodeMap attributes = element.getAttributes();
    if (attributes == null || attributes.getLength() <= 0) {
      return result;
    }

    Attr attr;
    for (int i = 0; i < attributes.getLength(); i++) {
      attr = (Attr) attributes.item(i);
      if (BeeUtils.same(attr.getNamespaceURI(), DecoratorConstants.NAMESPACE)) {
        result.put(XmlUtils.getLocalName(attr), attr.getValue());
      }
    }
    return result;
  }

  public static void getTools() {
    BeeKeeper.getRpc().makeGetRequest(Service.GET_DECORATORS, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(String.class)) {
          parseXml((String) response.getResponse());
        }
      }
    });
  }

  public static boolean isEnabled() {
    return enabled;
  }

  public static void refresh() {
    getTools();
  }
  
  public static void setEnabled(boolean enabled) {
    TuningFactory.enabled = enabled;
  }

  private static boolean initialize(Decorator decorator, Collection<String> relatives) {
    String ext = decorator.getParent();
    if (BeeUtils.isEmpty(ext)) {
      decorator.init(null);
      return true;
    }
    
    String id = decorator.getId();
    if (BeeUtils.same(id, ext) || relatives != null && relatives.contains(normalize(ext))) {
      BeeKeeper.getLog().severe("incest detected:", id, ext,
          BeeUtils.transformCollection(relatives, BeeConst.DEFAULT_LIST_SEPARATOR));
      return false;
    }
    
    Decorator parent = decorators.get(normalize(ext));
    if (parent == null) {
      BeeKeeper.getLog().severe("parent decorator not found id:", id, "extends:", ext);
      return false;
    }
    
    if (!parent.isInitialized()) {
      List<String> children = Lists.newArrayList(normalize(id));
      if (relatives != null) {
        children.addAll(relatives);
      }
      if (!initialize(parent, children)) {
        return false;
      }
    }

    decorator.init(parent.getFields());
    return true;
  }

  private static String normalize(String key) {
    return BeeUtils.normalize(key);
  }
  
  private static void parseXml(String xml) {
    Document document = XmlUtils.parse(xml);
    if (document == null) {
      return;
    }

    List<Element> elements = XmlUtils.getChildrenElements(document.getDocumentElement());
    if (elements.isEmpty()) {
      BeeKeeper.getLog().warning("no decorators found");
    }

    clear();

    String tag;
    String id;
    String ext;

    List<Parameter> params = Lists.newArrayList();
    Map<String, String> constants = Maps.newHashMap();
    Map<String, String> css = Maps.newHashMap();
    Lifecycle lc;
    List<Handler> handlers = Lists.newArrayList();

    Template template;

    for (Element element : elements) {
      tag = XmlUtils.getLocalName(element);
      if (!BeeUtils.inListSame(tag, DecoratorConstants.TAG_ABSTRACT,
          DecoratorConstants.TAG_DECORATOR)) {
        BeeKeeper.getLog().warning("unrecognized decorator tag:", tag);
        continue;
      }

      id = element.getAttribute(DecoratorConstants.ATTR_ID);
      if (BeeUtils.isEmpty(id)) {
        BeeKeeper.getLog().warning("decorator id not found");
        continue;
      }

      ext = element.getAttribute(DecoratorConstants.ATTR_EXTENDS);

      params.clear();
      constants.clear();
      css.clear();
      lc = null;
      handlers.clear();
      template = null;

      for (Element child : XmlUtils.getChildrenElements(element)) {
        String childTag = XmlUtils.getLocalName(child);

        if (BeeUtils.inListSame(childTag, DecoratorConstants.TAG_REQUIRED_PARAM,
            DecoratorConstants.TAG_OPTIONAL_PARAM)) {
          params.addAll(Parameter.getParameters(child));

        } else if (BeeUtils.same(childTag, DecoratorConstants.TAG_CONST)) {
          String name = child.getAttribute(DecoratorConstants.ATTR_NAME);
          String value = XmlUtils.getInnerXml(child);
          if (!BeeUtils.isEmpty(name)) {
            constants.put(name.trim(), BeeUtils.trim(value));
          }

        } else if (BeeUtils.same(childTag, DecoratorConstants.TAG_STYLE)) {
          String name = child.getAttribute(DecoratorConstants.ATTR_ID);
          String text = XmlUtils.getText(child);
          if (!BeeUtils.isEmpty(name) && !BeeUtils.isEmpty(text)) {
            css.put(name.trim(), text.trim());
          }

        } else if (BeeUtils.same(childTag, DecoratorConstants.TAG_LIFECYCLE)) {
          lc = Lifecycle.getLifecycle(child);

        } else if (BeeUtils.same(childTag, DecoratorConstants.TAG_HANDLER)) {
          Handler handler = Handler.getHandler(child);
          if (handler != null) {
            handlers.add(handler);
          }

        } else if (BeeUtils.same(childTag, DecoratorConstants.TAG_TEMPLATE)) {
          template = Template.getTemplate(child);
        }
      }

      String key = normalize(id);
      if (decorators.containsKey(key)) {
        BeeKeeper.getLog().warning("duplicate decorator id:", id);
      } else {
        decorators.put(key, new Decorator(id, ext, BeeUtils.same(tag,
            DecoratorConstants.TAG_ABSTRACT), params, constants, css, lc, handlers, template));
      }
    }
    BeeKeeper.getLog().info("loaded", decorators.size(), "decorators");
  }

  private TuningFactory() {
  }
}
