package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.ArrayUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public enum RightsObjectType implements HasLocalizedCaption {
  FIELD(RightsState.VIEW, RightsState.EDIT, RightsState.REQUIRED) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectField();
    }
  },

  WIDGET(RightsState.VIEW) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectWidget();
    }
  },

  DATA(RightsState.VIEW, RightsState.CREATE, RightsState.EDIT, RightsState.DELETE,
      RightsState.MERGE) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectData();
    }
  },

  MENU(RightsState.VIEW) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectMenu();
    }

    @Override
    public boolean isHierarchical() {
      return true;
    }
  },

  MODULE(RightsState.VIEW) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectModule();
    }

    @Override
    public boolean isHierarchical() {
      return true;
    }
  },

  LIST(RightsState.VIEW, RightsState.EDIT, RightsState.REQUIRED) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.objectList();
    }
  };

  private final Set<RightsState> registeredStates = new LinkedHashSet<>();

  RightsObjectType(RightsState... states) {
    if (!ArrayUtils.isEmpty(states)) {
      Collections.addAll(registeredStates, states);
    }
  }

  public Set<RightsState> getRegisteredStates() {
    return registeredStates;
  }

  public boolean isHierarchical() {
    return false;
  }
}
