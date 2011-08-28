package com.butent.bee.server.data;

import com.butent.bee.shared.Assert;

/**
 * Enables usage of user, role, checked or unchecked related states.
 */
public class BeeState {
  private final String name;
  private final boolean userMode;
  private final boolean roleMode;
  private final boolean checked;

  public BeeState(String name, boolean userMode, boolean roleMode, boolean checked) {
    Assert.notEmpty(name);

    this.name = name;
    this.userMode = userMode;
    this.roleMode = roleMode;
    this.checked = checked;
  }

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
