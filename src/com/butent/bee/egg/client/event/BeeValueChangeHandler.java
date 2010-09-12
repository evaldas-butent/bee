package com.butent.bee.egg.client.event;

import java.util.List;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.widget.BeeCheckBox;
import com.butent.bee.egg.client.widget.BeeRadioButton;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;

public class BeeValueChangeHandler<I> implements ValueChangeHandler<I> {

  public void onValueChange(ValueChangeEvent<I> event) {
    Object source = event.getSource();
    I value = event.getValue();

    if (source instanceof BeeRadioButton && BeeUtils.isTrue(value)) {
      BeeRadioButton rb = (BeeRadioButton) source;
      String fld = rb.getName();
      if (BeeGlobal.isField(fld))
        BeeGlobal.setFieldValue(fld,
            BeeGlobal.getFieldItems(fld).get(rb.getTabIndex()));

      List<Widget> sib = BeeDom.getSiblings((Widget) source);
      if (sib == null)
        return;

      for (int i = 0; i < sib.size(); i++) {
        Widget w = sib.get(i);

        if (w instanceof BeeRadioButton) {
          if (((BeeRadioButton) w).getValue())
            w.addStyleName(BeeStyle.RADIO_BUTTON_SELECTED);
          else
            w.removeStyleName(BeeStyle.RADIO_BUTTON_SELECTED);
        }
      }

      return;
    }

    if (source instanceof BeeCheckBox) {
      ((BeeCheckBox) source).onValueChange((Boolean) value);
      return;
    }

    if (source instanceof HasBeeValueChangeHandler) {
      ((HasBeeValueChangeHandler<I>) source).onValueChange((I) value);
      return;
    }
  }

}
