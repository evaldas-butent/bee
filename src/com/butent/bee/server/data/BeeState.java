package com.butent.bee.server.data;

import com.butent.bee.shared.Assert;

/**
 * Enables usage of user, role, checked or unchecked related states.
 */
public class BeeState implements BeeObject {
  private final String moduleName;
  private final String name;
  private final boolean userMode;
  private final boolean roleMode;
  private final boolean checked;

  public BeeState(String moduleName, String name, boolean userMode, boolean roleMode,
      boolean checked) {
    Assert.notEmpty(name);

    this.moduleName = moduleName;
    this.name = name;
    this.userMode = userMode;
    this.roleMode = roleMode;
    this.checked = checked;
  }

  @Override
  public String getModuleName() {
    return moduleName;
  }

  @Override
  public String getName() {
    return name;
  }

  public boolean isChecked() {
    return checked;
  }

  public boolean supportsRoles() {
    return roleMode;
  }

  public boolean supportsUsers() {
    return userMode;
  }
}
