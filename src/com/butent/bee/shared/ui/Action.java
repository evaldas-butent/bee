package com.butent.bee.shared.ui;

import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Set;

/**
 * Lists possible actions with a user interface component (like refresh, save, close etc).
 */

public enum Action implements BeeSerializable, HasCaption {
  ADD("naujas", "add"),
  BOOKMARK("bookmark", "bookmark"),
  CLOSE("uždaryti", "close"),
  CONFIGURE("nustatymai", "configure"),
  DELETE("išmesti", "delete"),
  EDIT("koreguoti", "edit"),
  PRINT("spausdinti", "print"),
  REFRESH("atnaujinti", "refresh"),
  SAVE("išsaugoti", "save"),
  FILTER("filtruoti", "filter"),
  REMOVE_FILTER("išvalyti filtrą", "removeFilter");
  
  public static final Set<Action> NO_ACTIONS = Sets.newHashSet(); 
  
  public static Set<Action> parse(String s) {
    Set<Action> result = Sets.newHashSet();
    if (!BeeUtils.isEmpty(s)) {
      for (String item : NameUtils.NAME_SPLITTER.split(s)) {
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
  
  private final String caption;
  private final String styleSuffix;

  private Action(String caption, String styleSuffix) {
    this.caption = caption;
    this.styleSuffix = styleSuffix;
  }

  @Override
  public void deserialize(String s) {
    Assert.untouchable();
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public String getStyleName() {
    return "bee-Action-" + styleSuffix;
  }
  
  @Override
  public String serialize() {
    return this.name();
  }
}
