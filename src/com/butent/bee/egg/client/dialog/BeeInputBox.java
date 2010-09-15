package com.butent.bee.egg.client.dialog;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.composite.ButtonGroup;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.grid.BeeFlexTable;
import com.butent.bee.egg.client.widget.BeeCheckBox;
import com.butent.bee.egg.client.widget.BeeFileUpload;
import com.butent.bee.egg.client.widget.BeeIntegerBox;
import com.butent.bee.egg.client.widget.BeeListBox;
import com.butent.bee.egg.client.widget.BeeSimpleCheckBox;
import com.butent.bee.egg.client.widget.BeeTextBox;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeField;
import com.butent.bee.egg.shared.BeeName;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeInputBox {
  public void inputFields(BeeStage bst, String cap, String... fieldNames) {
    Assert.parameterCount(fieldNames.length + 1, 2);

    BeeFlexTable ft = new BeeFlexTable();

    BeeField fld;
    Widget inp = null;
    String z, w;
    int tp;
    BeeWidget bw;

    int r = 0;
    FocusWidget fw = null;
    boolean ok;

    for (String name : fieldNames) {
      fld = BeeGlobal.getField(name);

      tp = fld.getType();
      bw = fld.getWidget();

      z = fld.getCaption();
      if (!BeeUtils.isEmpty(z) && tp != BeeType.TYPE_BOOLEAN) {
        ft.setText(r, 0, z);
      }

      w = fld.getWidth();

      ok = false;

      if (bw != null) {
        switch (bw) {
          case LIST:
            inp = new BeeListBox(name);
            ok = true;
            break;
          case RADIO:
            inp = new RadioGroup(name);
            ok = true;
            break;
          default:
            ok = false;
        }
      } else {
        switch (tp) {
          case BeeType.TYPE_FILE:
            inp = new BeeFileUpload(name);
            ok = true;
            break;
          case BeeType.TYPE_BOOLEAN:
            if (BeeUtils.isEmpty(z)) {
              inp = new BeeSimpleCheckBox(name);
            } else {
              inp = new BeeCheckBox(new BeeName(name));
            }
            ok = true;
            break;
          case BeeType.TYPE_INT:
            inp = new BeeIntegerBox(name);
            ok = true;
            break;
          default:
            ok = false;
        }
      }

      if (!ok) {
        inp = new BeeTextBox(name);
      }

      if (!BeeUtils.isEmpty(w)) {
        inp.setWidth(w);
      }

      ft.setWidget(r, 1, inp);
      if (fw == null && inp instanceof FocusWidget) {
        fw = (FocusWidget) inp;
      }

      r++;
    }

    ButtonGroup bg = new ButtonGroup();
    if (bst == null) {
      bg.addButton("OK", BeeService.SERVICE_CONFIRM_DIALOG);
    } else {
      bg.addButton("OK", bst);
    }
    bg.addButton("Cancel", BeeService.SERVICE_CANCEL_DIALOG);

    ft.setWidget(r, 0, bg);
    ft.getFlexCellFormatter().setColSpan(r, 0, 2);
    ft.getCellFormatter().setHorizontalAlignment(r, 0,
        HasHorizontalAlignment.ALIGN_CENTER);

    BeeDialogBox dialog = new BeeDialogBox();

    if (!BeeUtils.isEmpty(cap)) {
      dialog.setText(cap);
    }

    dialog.setAnimationEnabled(true);

    dialog.setWidget(ft);
    dialog.center();

    if (fw != null) {
      fw.setFocus(true);
    }
  }

}
