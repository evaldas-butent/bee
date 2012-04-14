package com.butent.bee.client.communication;

import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

/**
 * Contains necessary properties of a particular RPC parameter and methods to get and set them.
 */
public class RpcParameter implements Transformable {
  /**
   * Contains available sections of RPC parameters.
   */
  public static enum SECTION {
    QUERY, HEADER, DATA
  }

  public static SECTION defaultSection = SECTION.HEADER;

  private SECTION section = defaultSection;
  private String name = null;
  private String value;

  public RpcParameter(Object value) {
    this(defaultSection, null, BeeUtils.transform(value));
  }

  public RpcParameter(SECTION section, Object value) {
    this(section, null, BeeUtils.transform(value));
  }

  public RpcParameter(SECTION section, String value) {
    this(section, null, value);
  }

  public RpcParameter(SECTION section, String name, Object value) {
    this(section, name, BeeUtils.transform(value));
  }

  public RpcParameter(SECTION section, String name, String value) {
    super();
    this.section = section;
    this.name = name;
    this.value = value;
  }

  public RpcParameter(String value) {
    this(defaultSection, null, value);
  }

  public RpcParameter(String name, Object value) {
    this(defaultSection, name, BeeUtils.transform(value));
  }

  public RpcParameter(String name, String value) {
    this(defaultSection, name, value);
  }

  protected RpcParameter() {
  }

  public String getName() {
    return name;
  }

  public SECTION getSection() {
    return section;
  }

  public String getValue() {
    return value;
  }

  public boolean isNamed() {
    return !BeeUtils.isEmpty(getName());
  }

  public boolean isReady() {
    return isValid() && isNamed();
  }

  public boolean isValid() {
    return getSection() != null && !BeeUtils.isEmpty(getValue());
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSection(SECTION section) {
    this.section = section;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String transform() {
    return NameUtils.addName(BeeUtils.concat(1, BeeUtils.bracket(getSection()), getName()),
        getValue());
  }
}
