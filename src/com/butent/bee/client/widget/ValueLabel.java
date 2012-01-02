package com.butent.bee.client.widget;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.TakesValue;

/**
 * Enables using value label user interface component.
 */

public class ValueLabel<T> extends BeeLabel implements TakesValue<T> {

  protected T value;
  private final Renderer<? super T> renderer;

  public ValueLabel(Renderer<? super T> renderer, boolean inline) {
    super(inline);
    this.renderer = renderer;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
    setText(renderer.render(value));
  }

  protected Renderer<? super T> getRenderer() {
    return renderer;
  }
}