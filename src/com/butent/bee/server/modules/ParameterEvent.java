package com.butent.bee.server.modules;

import com.butent.bee.shared.utils.BeeUtils;

public class ParameterEvent {

  private final String module;
  private final String parameter;

  public ParameterEvent(String module, String parameter) {
    this.module = module;
    this.parameter = parameter;
  }

  public String getModule() {
    return module;
  }

  public String getParameter() {
    return parameter;
  }

  public boolean sameParameter(String mod, String prm) {
    return BeeUtils.same(this.module, mod) && BeeUtils.same(this.parameter, prm);
  }
}
