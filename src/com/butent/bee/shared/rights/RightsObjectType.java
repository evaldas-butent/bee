package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

import java.util.EnumSet;
import java.util.Set;

public enum RightsObjectType implements HasLocalizedCaption {
  FIELD(EnumSet.of(RightsState.VIEW, RightsState.EDIT)) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectField();
    }
  },
  WIDGET(EnumSet.of(RightsState.VIEW)) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectWidget();
    }
  },
  DATA(EnumSet.allOf(RightsState.class)) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectData();
    }
  },
  MENU(EnumSet.of(RightsState.VIEW)) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectMenu();
    }
  },
  MODULE(EnumSet.of(RightsState.VIEW)) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectModule();
    }
  };

  private final Set<RightsState> registeredStates;

  RightsObjectType(Set<RightsState> states) {
    this.registeredStates = states;
  }

  public Set<RightsState> getRegisteredStates() {
    return registeredStates;
  }
}
