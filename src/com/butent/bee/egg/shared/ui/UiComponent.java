package com.butent.bee.egg.shared.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeSerializable;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public abstract class UiComponent implements HasId, BeeSerializable {

  private static Logger logger = Logger.getLogger(UiComponent.class.getName());

  public static UiComponent createComponent(String oClass, String oName) {
    Assert.notEmpty(oName);

    UiComponent c = createComponent(oClass);
    if (!BeeUtils.isEmpty(c)) {
      c.setId(oName);
    }
    return c;
  }

  public static UiComponent createComponent(String oClass) {
    Assert.notEmpty(oClass);

    if (getClassName(UiWindow.class).equals(oClass)) {
      return new UiWindow();
    }

    else if (getClassName(UiPanel.class).equals(oClass)) {
      return new UiPanel();
    }

    else if (getClassName(UiHorizontalLayout.class).equals(oClass)) {
      return new UiHorizontalLayout();
    }

    else if (getClassName(UiVerticalLayout.class).equals(oClass)) {
      return new UiVerticalLayout();
    }

    else if (getClassName(UiLabel.class).equals(oClass)) {
      return new UiLabel();
    }

    else if (getClassName(UiField.class).equals(oClass)) {
      return new UiField();
    }

    else if (getClassName(UiButton.class).equals(oClass)) {
      return new UiButton();
    }

    logger.severe("Unsupported class name: " + oClass);
    return null;
  }

  public static UiComponent restore(String s) {
    String[] arr = BeeUtils.beeDeserialize(s);
    Assert.arrayLength(arr, 2);

    UiComponent root = createComponent(arr[0]);
    root.deserialize(arr[1]);
    return root;
  }

  private static String getClassName(Class<? extends UiComponent> cls) {
    String c = cls.getName();
    return c.substring(c.lastIndexOf(".") + 1);
  }

  private String id;
  private UiComponent parent;
  private Map<String, String> properties = new HashMap<String, String>();
  private List<UiComponent> childs;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
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
      return Collections.unmodifiableCollection(childs);
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
      childs = new ArrayList<UiComponent>();
    }
    childs.add(child);
    child.setParent(this);
  }

  public UiComponent findChild(String childId) {
    UiComponent child = null;

    if (getId().equals(childId)) {
      child = this;
    } else if (hasChilds()) {
      for (UiComponent c : getChilds()) {
        if (childId.equals(c.getId())) {
          child = c;
          break;
        }
      }
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

  @Override
  public String serialize() {
    StringBuilder sb = new StringBuilder();

    sb.append(BeeUtils.beeSerialize(id, properties, childs));

    return BeeUtils.beeSerialize(getClassName(this.getClass()), sb);
  }

  @Override
  public void deserialize(String s) {
    String[] arr = BeeUtils.beeDeserialize(s);
    Assert.arrayLength(arr, 3);

    setId(arr[0]);

    if (!BeeUtils.isEmpty(arr[1])) {
      String[] props = BeeUtils.beeDeserialize(arr[1]);
      for (int i = 0; i < Math.floor(props.length / 2); i++) {
        int j = i * 2;
        setProperty(props[j], props[j + 1]);
      }
    }

    if (!BeeUtils.isEmpty(arr[2])) {
      String[] chlds = BeeUtils.beeDeserialize(arr[2]);

      for (String chld : chlds) {
        UiComponent c = restore(chld);
        addChild(c);
      }
    }
  }

  public abstract Object createInstance(UiCreator creator);

  private void setParent(UiComponent component) {
    parent = component;
  }
}
