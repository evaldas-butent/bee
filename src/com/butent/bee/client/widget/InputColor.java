package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.HasInputHandlers;
import com.butent.bee.client.event.InputHandler;
import com.butent.bee.client.style.Color;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.utils.BeeUtils;

import elemental.client.Browser;

import elemental.html.InputElement;

public class InputColor extends Widget implements HasEnabled, IdentifiableWidget, HasInputHandlers,
    HasMouseDownHandlers {

  public InputColor() {
    super();

    InputElement inputElement = Browser.getDocument().createInputElement();
    inputElement.setType("color");

    setElement((Element) inputElement);
    init();
  }

  @Override
  public HandlerRegistration addInputHandler(InputHandler handler) {
    return Binder.addInputHandler(this, handler);
  }

  @Override
  public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
    return Binder.addMouseDownHandler(this, handler);
  }

  public void click() {
    getInputElement().click();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "color-pick";
  }

  public String getValue() {
    return getInputElement().getValue();
  }

  @Override
  public boolean isEnabled() {
    return !getInputElement().isDisabled();
  }

  public void setColor(String color) {
    setValue(Color.normalize(color));
  }

  @Override
  public void setEnabled(boolean enabled) {
    getInputElement().setDisabled(!enabled);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setValue(String value) {
    getInputElement().setValue(BeeUtils.trim(value));
  }

  protected void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-InputColor");
  }

  private InputElement getInputElement() {
    return (InputElement) getElement();
  }
}
