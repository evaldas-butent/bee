package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.BeeRadioButton;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.client.widget.InputPassword;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.SimpleBoolean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.State;
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

  public static final String WIDGET_PANEL = "panel";
  public static final String WIDGET_PROMPT = "prompt";
  public static final String WIDGET_INPUT = "input";
  public static final String WIDGET_ERROR = "error";
  public static final String WIDGET_COMMAND_GROUP = "commandGroup";
  public static final String WIDGET_CONFIRM = "confirm";
  public static final String WIDGET_CANCEL = "cancel";

  private static final String STYLE_INPUT_PANEL = "bee-InputPanel";
  private static final String STYLE_INPUT_PROMPT = "bee-InputPrompt";
  private static final String STYLE_INPUT_STRING = "bee-InputString";
  private static final String STYLE_INPUT_ERROR = "bee-InputError";
  private static final String STYLE_INPUT_COMMAND_GROUP = "bee-InputCommandGroup";
  private static final String STYLE_INPUT_COMMAND = "bee-InputCommand";
  private static final String STYLE_INPUT_CONFIRM = "bee-InputConfirm";
  private static final String STYLE_INPUT_CANCEL = "bee-InputCancel";

  public void inputString(String caption, String prompt, final StringCallback callback,
      String defaultValue, int maxLength, double width, Unit widthUnit, final int timeout,
      String confirmHtml, String cancelHtml, WidgetInitializer initializer) {
    Assert.notNull(callback);

    final Holder<State> state = new Holder<State>(State.OPEN);

    final DialogBox dialog = new DialogBox();
    if (!BeeUtils.isEmpty(caption)) {
      dialog.setText(caption.trim());
    }

    final Timer timer = (timeout > 0) ? new Timer() {
      @Override
      public void run() {
        state.setValue(State.EXPIRED);
        dialog.hide();
      }
    } : null;

    Flow panel = new Flow();
    panel.addStyleName(STYLE_INPUT_PANEL);

    if (!BeeUtils.isEmpty(prompt)) {
      BeeLabel label = new BeeLabel(prompt.trim());
      label.addStyleName(STYLE_INPUT_PROMPT);

      if (initializer != null) {
        initializer.initialize(WIDGET_PROMPT, label);
      }
      panel.add(label);
    }

    final BeeLabel error = new BeeLabel();
    error.addStyleName(STYLE_INPUT_ERROR);
    error.addStyleName(StyleUtils.NAME_ERROR);

    final InputText input = new InputText();
    input.addStyleName(STYLE_INPUT_STRING);

    if (!BeeUtils.isEmpty(defaultValue)) {
      input.setValue(defaultValue.trim());
    }
    if (maxLength > 0) {
      input.setMaxLength(maxLength);
    }
    if (width > 0) {
      StyleUtils.setWidth(input, width, widthUnit);
    }

    input.addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          EventUtils.eatEvent(event);
          state.setValue(State.CANCELED);
          dialog.hide();
          return;
        }

        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          String message = callback.getMessage(input.getValue());
          if (BeeUtils.isEmpty(message)) {
            EventUtils.eatEvent(event);
            state.setValue(State.CONFIRMED);
            dialog.hide();
            return;
          } else {
            error.setText(message);
          }
        }

        if (timer != null) {
          timer.schedule(timeout);
        }
      }
    });

    if (initializer != null) {
      initializer.initialize(WIDGET_INPUT, input);
    }
    panel.add(input);

    if (initializer != null) {
      initializer.initialize(WIDGET_ERROR, error);
    }
    panel.add(error);

    if (!BeeUtils.allEmpty(confirmHtml, cancelHtml)) {
      Flow commandGroup = new Flow();
      commandGroup.addStyleName(STYLE_INPUT_COMMAND_GROUP);

      if (!BeeUtils.isEmpty(confirmHtml)) {
        BeeButton confirm = new BeeButton(confirmHtml);
        confirm.addStyleName(STYLE_INPUT_COMMAND);
        confirm.addStyleName(STYLE_INPUT_CONFIRM);

        confirm.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            String message = callback.getMessage(input.getValue());
            if (BeeUtils.isEmpty(message)) {
              state.setValue(State.CONFIRMED);
              dialog.hide();
            } else {
              error.setText(message);
            }
          }
        });

        if (initializer != null) {
          initializer.initialize(WIDGET_CONFIRM, confirm);
        }
        commandGroup.add(confirm);
      }

      if (!BeeUtils.isEmpty(cancelHtml)) {
        BeeButton cancel = new BeeButton(cancelHtml);
        cancel.addStyleName(STYLE_INPUT_COMMAND);
        cancel.addStyleName(STYLE_INPUT_CANCEL);

        cancel.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            state.setValue(State.CANCELED);
            dialog.hide();
          }
        });

        if (initializer != null) {
          initializer.initialize(WIDGET_CANCEL, cancel);
        }
        commandGroup.add(cancel);
      }

      if (initializer != null) {
        initializer.initialize(WIDGET_COMMAND_GROUP, commandGroup);
      }
      panel.add(commandGroup);
    }

    if (initializer != null) {
      initializer.initialize(WIDGET_PANEL, panel);
    }

    dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        if (timer != null) {
          timer.cancel();
        }

        switch (state.getValue()) {
          case CONFIRMED:
            callback.onSuccess(BeeUtils.trim(input.getValue()));
            break;
          case EXPIRED:
            callback.onTimeout(BeeUtils.trim(input.getValue()));
            break;
          default:
            callback.onCancel();
        }
      }
    });

    dialog.setAnimationEnabled(true);

    dialog.setWidget(panel);
    dialog.center();

    input.setFocus(true);

    if (timer != null) {
      timer.schedule(timeout);
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
