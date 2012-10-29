package com.butent.bee.client.ui;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum UiOption {
  ROOT(EnumSet.of(Type.PAGING, Type.SEARCH, Type.WINDOW)),
  CHILD(null),
  EMBEDDED(EnumSet.of(Type.PAGING, Type.SEARCH)),
  REPORT(EnumSet.of(Type.WINDOW));
  
  private enum Type {
    PAGING, SEARCH, WINDOW
  }

  public static boolean hasPaging(Collection<UiOption> options) {
    return hasType(options, Type.PAGING);
  }

  public static boolean hasSearch(Collection<UiOption> options) {
    return hasType(options, Type.SEARCH);
  }
  
  public static boolean isWindow(Collection<UiOption> options) {
    return hasType(options, Type.WINDOW);
  }
  
  private static boolean hasType(Collection<UiOption> options, Type type) {
    if (options == null) {
      return false;
    }
    
    for (UiOption option : options) {
      if (option.hasType(type)) {
        return true;
      }
    }
    return false;
  }

  private final Set<Type> types;

  private UiOption(Set<Type> types) {
    this.types = types;
  }
  
  private Set<Type> getTypes() {
    return types;
  }

  private boolean hasType(Type type) {
    if (type == null || getTypes() == null) {
      return false;
    }
    return getTypes().contains(type);
  }
}
