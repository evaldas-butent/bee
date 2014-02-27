package com.butent.bee.client.rights;

import com.butent.bee.shared.rights.RightsUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

class RightsObject implements HasCaption {

  private final String name;
  private final String caption;

  private final String module;

  private final int level;
  private final String parent;

  private boolean hasChildren;

  RightsObject(String name, String caption, String module) {
    this(name, caption, module, 0, null);
  }

  RightsObject(String name, String caption, String module, int level, String parent) {
    this.name = RightsUtils.buildName(parent, name);
    this.caption = caption;

    this.module = module;

    this.level = level;
    this.parent = BeeUtils.isEmpty(parent) ? null : RightsUtils.normalizeName(parent);
  }

  @Override
  public String getCaption() {
    return caption;
  }

  int getLevel() {
    return level;
  }

  String getModule() {
    return module;
  }

  String getName() {
    return name;
  }

  String getParent() {
    return parent;
  }

  boolean hasChildren() {
    return hasChildren;
  }
  
  boolean hasParent() {
    return parent != null;
  }

  void setHasChildren(boolean hasChildren) {
    this.hasChildren = hasChildren;
  }
}
