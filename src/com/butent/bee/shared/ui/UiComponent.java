package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Creates and manages instances of user interface components with specified parameters.
 */

public abstract class UiComponent implements HasId, BeeSerializable {

  /**
   * Contains a list of component's parts going through serialization.
   */

  private enum Serial {
    ID, CAPTION, PROPERTIES, CHILDS
  }

  private static Logger logger = Logger.getLogger(UiComponent.class.getName());
  private static UiCreator uiCreator;

  public static UiComponent createComponent(String oClass, String oName) {
    Assert.notEmpty(oName);

    UiComponent c = createComponent(oClass);
    if (!BeeUtils.isEmpty(c)) {
      c.setId(oName);
    }
    return c;
  }

  public static UiComponent restore(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);

    UiComponent root = createComponent(arr[0]);
    root.deserialize(arr[1]);
    return root;
  }

  public static void setUiCreator(UiCreator uiCreator) {
    UiComponent.uiCreator = uiCreator;
  }

  private static UiComponent createComponent(String oClass) {
    Assert.notEmpty(oClass);

    if (NameUtils.getClassName(UiWindow.class).equals(oClass)) {
      return new UiWindow();
    } else if (NameUtils.getClassName(UiPanel.class).equals(oClass)) {
      return new UiPanel();
    } else if (NameUtils.getClassName(UiHorizontalLayout.class).equals(oClass)) {
      return new UiHorizontalLayout();
    } else if (NameUtils.getClassName(UiVerticalLayout.class).equals(oClass)) {
      return new UiVerticalLayout();
    } else if (NameUtils.getClassName(UiLabel.class).equals(oClass)) {
      return new UiLabel();
    } else if (NameUtils.getClassName(UiField.class).equals(oClass)) {
      return new UiField();
    } else if (NameUtils.getClassName(UiButton.class).equals(oClass)) {
      return new UiButton();
    } else if (NameUtils.getClassName(UiMenuHorizontal.class).equals(oClass)) {
      return new UiMenuHorizontal();
    } else if (NameUtils.getClassName(UiMenuVertical.class).equals(oClass)) {
      return new UiMenuVertical();
    } else if (NameUtils.getClassName(UiListBox.class).equals(oClass)) {
      return new UiListBox();
    } else if (NameUtils.getClassName(UiStack.class).equals(oClass)) {
      return new UiStack();
    } else if (NameUtils.getClassName(UiTree.class).equals(oClass)) {
      return new UiTree();
    } else if (NameUtils.getClassName(UiTab.class).equals(oClass)) {
      return new UiTab();
    } else if (NameUtils.getClassName(UiCheckBox.class).equals(oClass)) {
      return new UiCheckBox();
    } else if (NameUtils.getClassName(UiTextArea.class).equals(oClass)) {
      return new UiTextArea();
    } else if (NameUtils.getClassName(UiRadioButton.class).equals(oClass)) {
      return new UiRadioButton();
    } else if (NameUtils.getClassName(UiGrid.class).equals(oClass)) {
      return new UiGrid();
    }

    logger.severe("Unsupported class name: " + oClass);
    return null;
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

  public Object createInstance() {
    return createInstance(uiCreator);
  }

  public abstract Object createInstance(UiCreator creator);

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case ID:
          setId(value);
          break;
        case CAPTION:
          setCaption(value);
          break;
        case PROPERTIES:
          String[] props = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(props)) {
            for (int j = 0; j < props.length; j += 2) {
              setProperty(props[j], props[j + 1]);
            }
          }
          break;
        case CHILDS:
          String[] chlds = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(chlds)) {
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

  @Override
  public String getIdPrefix() {
    return "ui";
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
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ID:
          arr[i++] = id;
          break;
        case CAPTION:
          arr[i++] = caption;
          break;
        case PROPERTIES:
          arr[i++] = properties;
          break;
        case CHILDS:
          arr[i++] = childs;
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerialize(new Object[] {NameUtils.getClassName(this.getClass()), arr});
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
