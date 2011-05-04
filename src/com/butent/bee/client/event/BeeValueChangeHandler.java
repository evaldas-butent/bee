package com.butent.bee.client.event;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeRadioButton;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements value change event handling for user interface components.
 */

public class BeeValueChangeHandler<I> implements ValueChangeHandler<I> {

  public void onValueChange(ValueChangeEvent<I> event) {
    Object source = event.getSource();
    I value = event.getValue();

    if (source instanceof BeeRadioButton && BeeUtils.isTrue(value)) {
      BeeRadioButton rb = (BeeRadioButton) source;

      String name = rb.getName();
      if (Global.isVar(name)) {
        Global.setVarValue(name, Global.getVarItems(name).get(BeeUtils.toInt(rb.getFormValue())));
      }
      if (BeeKeeper.getStorage().hasItem(name)) {
        BeeKeeper.getStorage().setItem(name, rb.getFormValue());
      }

      BeeCommand cmnd = rb.getCommand();
      if (cmnd != null) {
        cmnd.execute();
      }
      return;
    }

    if (source instanceof HasBeeValueChangeHandler) {
      extracted(source).onValueChange(value);
      return;
    }
  }

  @SuppressWarnings("unchecked")
  private HasBeeValueChangeHandler<I> extracted(Object source) {
    return ((HasBeeValueChangeHandler<I>) source);
  }
}
