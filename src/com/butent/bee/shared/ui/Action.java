package com.butent.bee.shared.ui;

import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

/**
 * Lists possible actions with a user interface component (like refresh, save, close etc).
 */

public enum Action implements BeeSerializable {
  ADD, BOOKMARK, CLOSE, CONFIGURE, DELETE, REFRESH, REQUERY, SAVE;
  
  public static Set<Action> parse(String s) {
    Set<Action> result = Sets.newHashSet();
    if (!BeeUtils.isEmpty(s)) {
      for (String item : BeeUtils.toList(s)) {
        Action action = restore(item);
        if (action != null) {
          result.add(action);
        }
      }
    }
    return result;
  }
  
  public static Action restore(String s) {
    if (!BeeUtils.isEmpty(s)) {
      for (Action action : Action.values()) {
        if (BeeUtils.same(action.name(), s)) {
          return action;
        }
      }
    }
    return null;
  }
  
  public void deserialize(String s) {
    Assert.untouchable();
  }

  public String serialize() {
    return this.name();
  }
}
