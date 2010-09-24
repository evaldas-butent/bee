package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeSerializable;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class UiComponent implements HasId, BeeSerializable {

  private enum SerializationMembers {
    ID, CAPTION, PROPERTIES, CHILDS
  }

  private static Logger logger = Logger.getLogger(UiComponent.class.getName());
  private static UiCreator creator;

  public static UiComponent createComponent(String oClass) {
    Assert.notEmpty(oClass);

    if (getClassName(UiWindow.class).equals(oClass)) {
      return new UiWindow();
    } else if (getClassName(UiPanel.class).equals(oClass)) {
      return new UiPanel();
    } else if (getClassName(UiHorizontalLayout.class).equals(oClass)) {
      return new UiHorizontalLayout();
    } else if (getClassName(UiVerticalLayout.class).equals(oClass)) {
      return new UiVerticalLayout();
    } else if (getClassName(UiLabel.class).equals(oClass)) {
      return new UiLabel();
    } else if (getClassName(UiField.class).equals(oClass)) {
      return new UiField();
    } else if (getClassName(UiButton.class).equals(oClass)) {
      return new UiButton();
    } else if (getClassName(UiMenuHorizontal.class).equals(oClass)) {
      return new UiMenuHorizontal();
    } else if (getClassName(UiMenuVertical.class).equals(oClass)) {
      return new UiMenuVertical();
    } else if (getClassName(UiListBox.class).equals(oClass)) {
      return new UiListBox();
    } else if (getClassName(UiStack.class).equals(oClass)) {
      return new UiStack();
    } else if (getClassName(UiTree.class).equals(oClass)) {
      return new UiTree();
    } else if (getClassName(UiTab.class).equals(oClass)) {
      return new UiTab();
    } else if (getClassName(UiCheckBox.class).equals(oClass)) {
      return new UiCheckBox();
    } else if (getClassName(UiTextArea.class).equals(oClass)) {
      return new UiTextArea();
    } else if (getClassName(UiRadioButton.class).equals(oClass)) {
      return new UiRadioButton();
    } else if (getClassName(UiGrid.class).equals(oClass)) {
      return new UiGrid();
    }

    logger.severe("Unsupported class name: " + oClass);
    return null;
  }

  public static UiComponent createComponent(String oClass, String oName) {
    Assert.notEmpty(oName);

    UiComponent c = createComponent(oClass);
    if (!BeeUtils.isEmpty(c)) {
      c.setId(oName);
    }
    return c;
  }

  public static UiComponent restore(String s) {
    String[] arr = BeeUtils.beeDeserialize(s);
    Assert.arrayLength(arr, 2);

    UiComponent root = createComponent(arr[0]);
    root.deserialize(arr[1]);
    return root;
  }

  public static void setCreator(UiCreator creator) {
    UiComponent.creator = creator;
  }

  private static String getClassName(Class<? extends UiComponent> cls) {
    String c = cls.getName();
    return c.substring(c.lastIndexOf(".") + 1);
  }

  private String id;
  private String caption;
  private UiComponent parent;
  private Map<String, String> properties = new HashMap<String, String>();
  private List<UiComponent> childs;

  public void addChild(UiComponent child) {
    Assert.notEmpty(child);

    String childId = child.getId();
    UiComponent childParent = child.getParent();

    if (!BeeUtils.isEmpty(childParent)) {
      throw new IllegalArgumentException(
          "Component already belongs to another parent: " + childParent.getId()
              + "." + childId);
    }
    for (UiComponent prnt = this; prnt != null; prnt = prnt.getParent()) {
      if (prnt == child) {
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

  @Override
  public void createId() {
  }

  public Object createInstance() {
    return createInstance(creator);
  }

  public abstract Object createInstance(UiCreator creator);

  @Override
  public void deserialize(String s) {
    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = BeeUtils.beeDeserialize(s);

    Assert.arrayLength(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case ID:
          setId(value);
          break;
        case CAPTION:
          setCaption(value);
          break;
        case PROPERTIES:
          if (!BeeUtils.isEmpty(value)) {
            String[] props = BeeUtils.beeDeserialize(value);
            for (int j = 0; j < props.length; j += 2) {
              setProperty(props[j], props[j + 1]);
            }
          }
          break;
        case CHILDS:
          if (!BeeUtils.isEmpty(value)) {
            String[] chlds = BeeUtils.beeDeserialize(value);

            for (String chld : chlds) {
              UiComponent c = restore(chld);
              addChild(c);
            }
          }
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
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

  public String getCaption() {
    return caption;
  }

  public Collection<UiComponent> getChilds() {
    if (hasChilds()) {
      return Collections.unmodifiableCollection(childs);
    }
    return null;
  }

  @Override
  public String getId() {
    return id;
  }

  public UiComponent getParent() {
    return parent;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public String getProperty(String key) {
    return properties.get(key);
  }

  public UiComponent getRoot() {
    UiComponent prnt = getParent();

    if (!BeeUtils.isEmpty(prnt)) {
      return prnt.getRoot();
    }
    return this;
  }

  public boolean hasChilds() {
    return (childs != null);
  }

  public void loadProperties(String props) {
    if (!BeeUtils.isEmpty(props)) {
      String[] rows = props.replaceAll("\\\\[\r\n]+\\s*", "").split("[\r\n]+");

      for (String row : rows) {
        String[] pair = row.split("[:=]", 2);

        if (pair.length == 2) {
          properties.put(pair[0].trim(), pair[1].trim());
        }
      }
    }
  }

  @Override
  public String serialize() {
    StringBuilder sb = new StringBuilder();
    SerializationMembers[] members = SerializationMembers.values();

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];

      switch (member) {
        case ID:
          sb.append(BeeUtils.beeSerialize(id));
          break;
        case CAPTION:
          sb.append(BeeUtils.beeSerialize(caption));
          break;
        case PROPERTIES:
          sb.append(BeeUtils.beeSerialize(properties));
          break;
        case CHILDS:
          sb.append(BeeUtils.beeSerialize(childs));
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return BeeUtils.beeSerialize(getClassName(this.getClass()), sb);
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public void setProperty(String key, Object value) {
    properties.put(key, (String) value);
  }

  private void setParent(UiComponent component) {
    parent = component;
  }
}
