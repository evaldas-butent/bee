package com.butent.bee.client.dom;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

public class Stacking {
  
  private static int maxLevel = 0;
  
  private static final Map<String, Integer> widgetLevels = Maps.newHashMap();
  
  public static int addContext(Element el) {
    Assert.notNull(el, "Stacking add: element is null");
    int level = push(el.getId());
    el.getStyle().setZIndex(level);
    return level;
  }

  public static int addContext(UIObject obj) {
    Assert.notNull(obj, "Stacking add: ui object is null");
    return addContext(obj.getElement());
  }
  
  public static void ensureLevel(int level) {
    if (level > maxLevel) {
      maxLevel = level;
    }
  }

  public static List<Property> getInfo() {
    List<Property> lst = PropertyUtils.createProperties("Max Level", getMaxLevel(),
      "Widget Levels", BeeUtils.bracket(widgetLevels.size()));
    
    for (Map.Entry<String, Integer> entry : widgetLevels.entrySet()) {
      PropertyUtils.addProperty(lst, entry.getKey(), entry.getValue());
    }
    
    return lst;
  }
  
  public static int getMaxLevel() {
    return maxLevel;
  }
  
  public static int getWidgetCount() {
    return widgetLevels.size();
  }

  public static int nextLevel() {
    return ++maxLevel;
  }
  
  public static void pop(String id) {
    Assert.notEmpty(id, "Stacking pop: id is empty");
    Integer level = widgetLevels.remove(id);
    
    if (level == null) {
      BeeKeeper.getLog().severe("Stacking pop: id " + id + " not in widgetLevels");
      for (Property prop : getInfo()) {
        BeeKeeper.getLog().debug(prop.getName(), prop.getValue());
      }
      BeeKeeper.getLog().addSeparator();
    } else {
      maxLevel = level;
    }
  }
  
  public static int push(String id) {
    Assert.notEmpty(id, "Stacking push: id is empty");

    Integer level = widgetLevels.get(id);
    if (level != null && level == maxLevel - 1) {
      return maxLevel;
    } else {
      widgetLevels.put(id, maxLevel);
      return nextLevel();
    }
  }

  public static void removeContext(Element el) {
    Assert.notNull(el, "Stacking remove: element is null");
    pop(el.getId());
  }

  public static void removeContext(UIObject obj) {
    Assert.notNull(obj, "Stacking remove: ui object is null");
    removeContext(obj.getElement());
  }
  
  private Stacking() {
  }
}
