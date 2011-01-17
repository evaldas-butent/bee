package com.butent.bee.shared.ui;

public interface UiCreator {

  Object createButton(UiButton button);

  Object createCheckBox(UiCheckBox checkBox);

  Object createField(UiField field);

  Object createGrid(UiGrid uiGrid);

  Object createHorizontalLayout(UiHorizontalLayout horizontalLayout);

  Object createLabel(UiLabel label);

  Object createListBox(UiListBox listBox);

  Object createMenuHorizontal(UiMenuHorizontal uiMenuHorizontal);

  Object createMenuVertical(UiMenuVertical uiMenuVertical);

  Object createPanel(UiPanel panel);

  Object createRadioButton(UiRadioButton radioButton);

  Object createStack(UiStack stack);

  Object createTab(UiTab uiTab);

  Object createTextArea(UiTextArea textArea);

  Object createTree(UiTree tree);

  Object createVerticalLayout(UiVerticalLayout beeVerticalLayout);

  Object createWindow(UiWindow window);
}
