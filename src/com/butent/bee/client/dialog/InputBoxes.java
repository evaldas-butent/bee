package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeFileUpload;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.BeeRadioButton;
import com.butent.bee.client.widget.InputPassword;
import com.butent.bee.client.widget.SimpleBoolean;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a user interface component, which enables to produce a input box for information input
 * from the user.
 */

public class InputBoxes {
  
  private class KeyboardHandler implements KeyDownHandler {

    private final DialogBox dialog;
    private final Stage stage;

    private KeyboardHandler(DialogBox dialog, Stage stage) {
      super();
      this.dialog = dialog;
      this.stage = stage;
    }

    public void onKeyDown(KeyDownEvent event) {
      switch (event.getNativeKeyCode()) {
        case KeyCodes.KEY_ESCAPE:
          EventUtils.eatEvent(event);
          getDialog().hide();
          break;

        case KeyCodes.KEY_ENTER:
          Widget widget = getWidget(event);
          if (widget instanceof Button || widget instanceof FileUpload) {
            break;
          }

          if (EventUtils.hasModifierKey(event.getNativeEvent())) {
            EventUtils.eatEvent(event);
            EventUtils.getEventTargetElement(event).blur();
            if (getStage() != null) {
              BeeKeeper.getBus().dispatchService(getStage(), event);
            } else {
              getDialog().hide();
            }
            break;
          }
          
          if (widget instanceof InputText || widget instanceof ListBox) {
            if (navigate(EventUtils.getEventTargetElement(event), true)) {
              EventUtils.eatEvent(event);
            }
          } else if (widget instanceof BeeRadioButton) {
            if (!((BeeRadioButton) widget).getValue()) {
              EventUtils.eatEvent(event);
              ((BeeRadioButton) widget).setValue(true, true);
            }
          }
          break;
        
        case KeyCodes.KEY_DOWN:
        case KeyCodes.KEY_UP:
          if (!(getWidget(event) instanceof ListBox)) {
            if (navigate(EventUtils.getEventTargetElement(event),
                event.getNativeKeyCode() == KeyCodes.KEY_DOWN)) {
              EventUtils.eatEvent(event);
            }
          }
          break;
      }
    }

    private DialogBox getDialog() {
      return dialog;
    }

    private Stage getStage() {
      return stage;
    }
    
    private Widget getWidget(HasNativeEvent event) {
      return DomUtils.getWidget(getDialog(), EventUtils.getEventTargetElement(event));
    }
    
    private boolean navigate(Element current, boolean forward) {
      if (current == null) {
        return false;
      }
      List<Widget> children = DomUtils.getFocusableChildren(getDialog());
      if (children == null || children.size() <= 1) {
        return false;
      }
      
      int index = BeeConst.UNDEF;
      for (int i = 0; i < children.size(); i++) {
        if (children.get(i).getElement().isOrHasChild(current)) {
          index = i;
          break;
        }
      }
      if (BeeConst.isUndef(index)) {
        return false;
      }
      
      if (forward) {
        index++;
      } else {
        index--;
      }
      
      if (index >= 0 && index < children.size()) {
        Widget child = children.get(index);
        if (child instanceof FocusWidget) {
          ((FocusWidget) child).setFocus(true);
        } else {
          child.getElement().focus();
        }
        return true;
      } else {
        return false;
      }
    }
  }
  
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
          case PASSWORD:
            inp = new InputPassword(var);
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
              inp = new SimpleBoolean(var);
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
        inp = new InputText(var);
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

    BeeButton confirm;
    if (bst == null) {
      confirm = new BeeButton("OK", Service.CONFIRM_DIALOG);
    } else {
      confirm = new BeeButton("OK", bst);
    }
    BeeButton cancel = new BeeButton("Cancel", Service.CANCEL_DIALOG);

    ft.setWidget(r, 0, confirm);
    ft.setWidget(r, 1, cancel);

    ft.getCellFormatter().setHorizontalAlignment(r, 0, HasHorizontalAlignment.ALIGN_LEFT);
    ft.getCellFormatter().setHorizontalAlignment(r, 1, HasHorizontalAlignment.ALIGN_RIGHT);

    DialogBox dialog = new DialogBox();

    if (!BeeUtils.isEmpty(cap)) {
      dialog.setText(cap);
    }

    dialog.setAnimationEnabled(true);
    dialog.addDomHandler(new KeyboardHandler(dialog, bst), KeyDownEvent.getType());

    dialog.setWidget(ft);
    dialog.center();

    if (fw != null) {
      fw.setFocus(true);
    }
  }
}
