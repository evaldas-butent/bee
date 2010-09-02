package com.butent.bee.egg.shared.ui;

public interface UiCreator {

  public Object createLabel(UiLabel label);

  public Object createWindow(UiWindow window);

  public Object createHorizontalLayout(UiHorizontalLayout horizontalLayout);

  public Object createVerticalLayout(UiVerticalLayout beeVerticalLayout);

  public Object createField(UiField field);

  public Object createPanel(UiPanel panel);

  public Object createButton(UiButton button);
}
