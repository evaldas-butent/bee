package com.butent.bee.egg.shared.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public abstract class UiComponent {

  private static Logger logger = Logger.getLogger(UiComponent.class.getName());

  public static UiComponent createComponent(String oClass, String oName) {
    Assert.notEmpty(oClass);
    Assert.notEmpty(oName);

    if (getClassName(UiWindow.class).equals(oClass)) {
      return new UiWindow(oName);
    }

    if (getClassName(UiPanel.class).equals(oClass)) {
      return new UiPanel(oName);
    }

    if (getClassName(UiHorizontalLayout.class).equals(oClass)) {
      return new UiHorizontalLayout(oName);
    }

    if (getClassName(UiVerticalLayout.class).equals(oClass)) {
      return new UiVerticalLayout(oName);
    }
    if (getClassName(UiLabel.class).equals(oClass)) {
      return new UiLabel(oName);
    }

    if (getClassName(UiField.class).equals(oClass)) {
      return new UiField(oName);
    }

    if (getClassName(UiButton.class).equals(oClass)) {
      return new UiButton(oName);
    }

    logger.severe("Unsupported class name: " + oClass);
    return null;
  }

  private static String getClassName(Class<? extends UiComponent> cls) {
    String c = cls.getName();
    return c.substring(c.lastIndexOf(".") + 1);
  }

  private String id;
  private UiComponent parent;
  private Map<String, String> properties = new HashMap<String, String>();
  private LinkedHashMap<String, UiComponent> childs;

  protected UiComponent(String id) {
    this.id = id.trim();
  }

  public String getId() {
    return id;
  }

  public UiComponent getParent() {
    return parent;
  }

  public String getProperty(String key) {
    return properties.get(key);
  }

  public void setProperty(String key, Object value) {
    properties.put(key, (String) value);
  }

  public void loadProperties(String props) {
    // TODO: reikia protingesnio varianto
    if (!BeeUtils.isEmpty(props)) {
      String[] rows = props.split(";");

      for (String row : rows) {
        String[] pair = row.split("=", 2);

        if (pair.length == 2) {
          properties.put(pair[0], pair[1]);
        }
      }
    }
  }

  public Collection<UiComponent> getChilds() {
    if (hasChilds()) {
      return Collections.unmodifiableCollection(childs.values());
    }
    return null;
  }

  public void addChild(UiComponent child) {
    Assert.notEmpty(child);

    String childId = child.getId();
    UiComponent childParent = child.getParent();

    if (!BeeUtils.isEmpty(childParent)) {
      throw new IllegalArgumentException(
          "Component already belongs to another parent: " + childParent.getId()
              + "." + childId);
    }
    for (UiComponent parent = this; parent != null; parent = parent.getParent()) {
      if (parent == child) {
        throw new IllegalArgumentException(
            "Component cannot be added inside it's own content: " + childId);
      }
    }
    if (!BeeUtils.isEmpty(getRoot().findChild(childId))) {
      throw new IllegalArgumentException("Dublicate component name: " + childId);
    }
    if (!hasChilds()) {
      childs = new LinkedHashMap<String, UiComponent>();
    }
    childs.put(childId, child);
    child.setParent(this);
  }

  public UiComponent findChild(String childId) {
    UiComponent child = null;

    if (getId().equals(childId)) {
      child = this;
    } else if (hasChilds()) {
      child = childs.get(childId);

      if (BeeUtils.isEmpty(child)) {
        for (UiComponent c : getChilds()) {
          child = c.findChild(childId);

          if (!BeeUtils.isEmpty(child)) {
            break;
          }
        }
      }
    }
    return child;
  }

  public boolean hasChilds() {
    return (childs != null);
  }

  public UiComponent getRoot() {
    UiComponent parent = getParent();

    if (!BeeUtils.isEmpty(parent)) {
      return parent.getRoot();
    }
    return this;
  }

  public abstract Object createInstance(UiCreator creator);

  private void setParent(UiComponent component) {
    parent = component;
  }
}
