package com.butent.bee.egg.client.event;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.butent.bee.egg.client.Global;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.widget.BeeRadioButton;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeValueChangeHandler<I> implements ValueChangeHandler<I> {

  public void onValueChange(ValueChangeEvent<I> event) {
    Object source = event.getSource();
    I value = event.getValue();

    if (source instanceof BeeRadioButton && BeeUtils.isTrue(value)) {
      BeeRadioButton rb = (BeeRadioButton) source;

      String fld = rb.getName();
      if (Global.isField(fld)) {
        Global.setFieldValue(fld,
            Global.getFieldItems(fld).get(BeeUtils.toInt(rb.getFormValue())));
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
