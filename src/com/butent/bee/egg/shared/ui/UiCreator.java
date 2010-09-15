package com.butent.bee.egg.shared.ui;

public interface UiCreator {

  Object createButton(UiButton button);

  Object createField(UiField field);

  Object createHorizontalLayout(UiHorizontalLayout horizontalLayout);

  Object createLabel(UiLabel label);

  Object createMenuHorizontal(UiMenuHorizontal uiMenuHorizontal);

  Object createMenuVertical(UiMenuVertical uiMenuVertical);

  Object createPanel(UiPanel panel);

  Object createVerticalLayout(UiVerticalLayout beeVerticalLayout);

  Object createWindow(UiWindow window);
}
