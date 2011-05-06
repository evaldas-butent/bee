package com.butent.bee.server.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables usage of user, role, checked or unchecked related states.
 */

public class BeeState {
  private static final String USER_MODE = "USER";
  private static final String ROLE_MODE = "ROLE";

  private final String name;
  private final String mode;
  private final boolean checked;

  public BeeState(String name, String mode, boolean checked) {
    Assert.notEmpty(name);

    this.name = name;
    this.mode = mode;
    this.checked = checked;
  }

  public String getMode() {
    return mode;
  }

  public String getName() {
    return name;
  }

  public boolean isChecked() {
    return checked;
  }

  public boolean supportsRoles() {
    return BeeUtils.isEmpty(mode) || BeeUtils.same(mode, ROLE_MODE);
  }

  public boolean supportsUsers() {
    return BeeUtils.isEmpty(mode) || BeeUtils.same(mode, USER_MODE);
  }
}
