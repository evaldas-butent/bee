package com.butent.bee.client.dialog;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.composite.ButtonGroup;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeFileUpload;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.BeeSimpleCheckBox;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

public class InputBox {
  public void inputVars(Stage bst, String cap, Variable... vars) {
    Assert.notNull(vars);
    Assert.parameterCount(vars.length + 1, 2);

    FlexTable ft = new FlexTable();

    Widget inp = null;
    String z, w;
    BeeType tp;
    BeeWidget bw;

    int r = 0;
    FocusWidget fw = null;
    boolean ok;

    for (Variable var : vars) {
      tp = var.getType();
      bw = var.getWidget();

      z = var.getCaption();
      if (!BeeUtils.isEmpty(z) && tp != BeeType.BOOLEAN) {
        ft.setText(r, 0, z);
      }

      w = var.getWidth();

      ok = false;

      if (bw != null) {
        switch (bw) {
          case LIST:
            inp = new BeeListBox(var);
            ok = true;
            break;
          case RADIO:
            inp = new RadioGroup(var);
            ok = true;
            break;
          default:
            ok = false;
        }
      } else {
        switch (tp) {
          case FILE:
            inp = new BeeFileUpload(var);
            ok = true;
            break;
          case BOOLEAN:
            if (BeeUtils.isEmpty(z)) {
              inp = new BeeSimpleCheckBox(var);
            } else {
              inp = new BeeCheckBox(var);
            }
            ok = true;
            break;
          case INT:
            inp = new InputInteger(var);
            ok = true;
            break;
          default:
            ok = false;
        }
      }

      if (!ok) {
        inp = new BeeTextBox(var);
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
      bg.addButton("OK", Service.CONFIRM_DIALOG);
    } else {
      bg.addButton("OK", bst);
    }
    bg.addButton("Cancel", Service.CANCEL_DIALOG);

    ft.setWidget(r, 0, bg);
    ft.getFlexCellFormatter().setColSpan(r, 0, 2);
    ft.getCellFormatter().setHorizontalAlignment(r, 0, HasHorizontalAlignment.ALIGN_CENTER);

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
