package com.butent.bee.client.decorator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

public class TuningFactory {

  private static final Map<String, Decorator> decorators = Maps.newHashMap();

  private static boolean enabled = true;

  public static void clear() {
    decorators.clear();
  }
  
  public static Widget decorate(Widget widget, String decoratorId, JSONObject options) {
    if (widget == null || BeeUtils.isEmpty(decoratorId) || !isEnabled()) {
      return widget;
    }
    
    Decorator decorator = decorators.get(normalize(decoratorId));
    if (decorator == null) {
      BeeKeeper.getLog().warning("decorator not found id:", decoratorId);
      return widget;
    }
    if (!decorator.isEnabled() || !decorator.isValid()) {
      return widget;
    }
    
    return decorator.decorate(widget, options);
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

  private static String normalize(String key) {
    return BeeUtils.normalize(key);
  }

  private static void parseXml(String xml) {
    Document document = XmlUtils.parse(xml);
    if (document == null) {
      return;
    }

    List<Element> elements = XmlUtils.getElementsByLocalName(document.getDocumentElement(),
        DecoratorConstants.TAG_DECORATOR);
    if (elements.isEmpty()) {
      BeeKeeper.getLog().warning("no decorators found");
    }

    clear();

    String id;

    List<Parameter> params = Lists.newArrayList();
    Map<String, String> css = Maps.newHashMap();
    Lifecycle lc;
    List<Handler> handlers = Lists.newArrayList();

    Template template;

    for (Element element : elements) {
      id = element.getAttribute(DecoratorConstants.ATTR_ID);
      if (BeeUtils.isEmpty(id)) {
        BeeKeeper.getLog().warning("decorator id not found");
        continue;
      }

      params.clear();
      css.clear();
      lc = null;
      handlers.clear();
      template = null;

      for (Element child : XmlUtils.getChildrenElements(element)) {
        String tagName = XmlUtils.getLocalName(child);

        if (BeeUtils.same(tagName, DecoratorConstants.TAG_PARAM)) {
          Parameter param = Parameter.getParameter(child);
          if (param != null) {
            params.add(param);
          }

        } else if (BeeUtils.same(tagName, DecoratorConstants.TAG_STYLE)) {
          String name = child.getAttribute(DecoratorConstants.ATTR_ID);
          String text = XmlUtils.getText(child);
          if (!BeeUtils.isEmpty(name) && !BeeUtils.isEmpty(text)) {
            css.put(name.trim(), text.trim());
          }

        } else if (BeeUtils.same(tagName, DecoratorConstants.TAG_LIFECYCLE)) {
          lc = Lifecycle.getLifecycle(child);

        } else if (BeeUtils.same(tagName, DecoratorConstants.TAG_HANDLER)) {
          Handler handler = Handler.getHandler(child);
          if (handler != null) {
            handlers.add(handler);
          }

        } else if (BeeUtils.same(tagName, DecoratorConstants.TAG_TEMPLATE)) {
          template = Template.getTemplate(child);
        }
      }

      if (template == null) {
        BeeKeeper.getLog().warning("decorator id:", id, "template not found");
      } else {
        decorators.put(normalize(id), new Decorator(id, params, css, lc, handlers, template));
      }
    }
    BeeKeeper.getLog().info("loaded", decorators.size(), "decorators");
  }

  private TuningFactory() {
  }
}
