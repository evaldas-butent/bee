package com.butent.bee.shared.menu;

@FunctionalInterface
public interface MenuHandler {
  void onSelection(String parameters);
}
