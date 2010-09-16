package com.butent.bee.egg.client.event;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.widget.BeeRadioButton;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class BeeValueChangeHandler<I> implements ValueChangeHandler<I> {

  public void onValueChange(ValueChangeEvent<I> event) {
    Object source = event.getSource();
    I value = event.getValue();

    if (source instanceof BeeRadioButton && BeeUtils.isTrue(value)) {
      BeeRadioButton rb = (BeeRadioButton) source;

      String fld = rb.getName();
      if (BeeGlobal.isField(fld)) {
        BeeGlobal.setFieldValue(fld,
            BeeGlobal.getFieldItems(fld).get(rb.getTabIndex()));
      }

      BeeCommand cmnd = rb.getCommand();
      if (cmnd != null) {
        cmnd.execute();
      }

      List<Widget> sib = DomUtils.getSiblings((Widget) source);
      if (sib != null) {
        for (int i = 0; i < sib.size(); i++) {
          Widget w = sib.get(i);

          if (w instanceof BeeRadioButton) {
            if (((BeeRadioButton) w).getValue()) {
              w.addStyleName(BeeStyle.RADIO_BUTTON_SELECTED);
            } else {
              w.removeStyleName(BeeStyle.RADIO_BUTTON_SELECTED);
            }
          }
        }
      }

      return;
    }

    if (source instanceof HasBeeValueChangeHandler) {
      ((HasBeeValueChangeHandler<I>) source).onValueChange(value);
      return;
    }
  }

}
