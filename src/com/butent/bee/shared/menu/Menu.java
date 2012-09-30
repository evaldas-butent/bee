package com.butent.bee.shared.menu;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlSeeAlso({MenuEntry.class, MenuItem.class})
public abstract class Menu implements BeeSerializable {

  private enum Serial {
    NAME, LABEL, SEPARATOR, ORDER, MODULE
  }

  public static Menu restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length + 2);
    String clazz = arr[0];
    String data = arr[1];
    Menu menu = null;

    if (data != null) {
      menu = Menu.getMenu(clazz);

      for (int i = 0; i < members.length; i++) {
        Serial member = members[i];
        String value = arr[i + 2];

        switch (member) {
          case NAME:
            menu.name = value;
            break;
          case LABEL:
            menu.label = value;
            break;
          case SEPARATOR:
            menu.separator = BeeUtils.toBooleanOrNull(value);
            break;
          case ORDER:
            menu.order = BeeUtils.toIntOrNull(value);
            break;
          case MODULE:
            menu.moduleName = value;
            break;
        }
      }
      menu.deserialize(data);
    }
    return menu;
  }

  private static Menu getMenu(String clazz) {
    Menu menu = null;

    if (NameUtils.getClassName(MenuEntry.class).equals(clazz)) {
      menu = new MenuEntry();

    } else if (NameUtils.getClassName(MenuItem.class).equals(clazz)) {
      menu = new MenuItem();

    } else {
      Assert.unsupported("Unsupported menu class name: " + clazz);
    }
    return menu;
  }

  @XmlAttribute
  private String name;
  @XmlAttribute
  private String label;
  @XmlAttribute
  private Boolean separator;
  @XmlAttribute
  private Integer order;
  @XmlAttribute
  private String parent;

  @XmlTransient
  private String moduleName;

  public String getLabel() {
    return label;
  }

  public String getModuleName() {
    return moduleName;
  }

  public String getName() {
    return name;
  }

  public Integer getOrder() {
    return order;
  }

  public String getParent() {
    return parent;
  }

  public Boolean hasSeparator() {
    return BeeUtils.isTrue(separator);
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  protected String serialize(Object obj) {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length + 2];
    int i = 0;
    arr[i++] = NameUtils.getClassName(this.getClass());
    arr[i++] = obj;

    for (Serial member : Serial.values()) {
      switch (member) {
        case NAME:
          arr[i++] = name;
          break;
        case LABEL:
          arr[i++] = label;
          break;
        case SEPARATOR:
          arr[i++] = separator;
          break;
        case ORDER:
          arr[i++] = order;
          break;
        case MODULE:
          arr[i++] = moduleName;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }
}
