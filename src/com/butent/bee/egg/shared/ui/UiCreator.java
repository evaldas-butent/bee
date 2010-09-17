package com.butent.bee.egg.shared.ui;

public interface UiCreator {

  Object createButton(UiButton button);

  Object createField(UiField field);

  Object createHorizontalLayout(UiHorizontalLayout horizontalLayout);

  Object createLabel(UiLabel label);

  Object createListBox(UiListBox listBox);

  Object createMenuHorizontal(UiMenuHorizontal uiMenuHorizontal);

  Object createMenuVertical(UiMenuVertical uiMenuVertical);

  Object createPanel(UiPanel panel);

  Object createStack(UiStack stack);

  Object createTab(UiTab uiTab);

  Object createTree(UiTree tree);

  Object createVerticalLayout(UiVerticalLayout beeVerticalLayout);

  Object createWindow(UiWindow window);
}
