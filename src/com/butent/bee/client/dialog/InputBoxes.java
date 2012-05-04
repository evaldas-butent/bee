package com.butent.bee.client.dialog;

import com.google.common.base.Supplier;
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
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.UiHelper;
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
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.State;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

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
          event.preventDefault();
          getDialog().hide();
          break;

        case KeyCodes.KEY_ENTER:
          Widget widget = getWidget(event);
          if (widget instanceof Button || widget instanceof FileUpload) {
            break;
          }

          if (EventUtils.hasModifierKey(event.getNativeEvent())) {
            event.preventDefault();
            EventUtils.getEventTargetElement(event).blur();
            if (getStage() != null) {
              BeeKeeper.getBus().dispatchService(getStage(), dialog);
            } else {
              getDialog().hide();
            }
            break;
          }

          if (widget instanceof InputText || widget instanceof ListBox) {
            if (UiHelper.moveFocus(getDialog(), EventUtils.getEventTargetElement(event), true)) {
              event.preventDefault();
            }
          } else if (widget instanceof BeeRadioButton) {
            if (!((BeeRadioButton) widget).getValue()) {
              event.preventDefault();
              ((BeeRadioButton) widget).setValue(true, true);
            }
          }
          break;

        case KeyCodes.KEY_DOWN:
        case KeyCodes.KEY_UP:
          if (!(getWidget(event) instanceof ListBox)) {
            if (UiHelper.moveFocus(getDialog(), EventUtils.getEventTargetElement(event),
                event.getNativeKeyCode() == KeyCodes.KEY_DOWN)) {
              event.preventDefault();
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
      return DomUtils.getChildByElement(getDialog(), EventUtils.getEventTargetElement(event));
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
  private static final String STYLE_INPUT_WIDGET = "bee-InputWidget";
  private static final String STYLE_INPUT_ERROR = "bee-InputError";
  private static final String STYLE_INPUT_COMMAND_GROUP = "bee-InputCommandGroup";
  private static final String STYLE_INPUT_COMMAND = "bee-InputCommand";
  private static final String STYLE_INPUT_CONFIRM = "bee-InputConfirm";
  private static final String STYLE_INPUT_CANCEL = "bee-InputCancel";

  public void inputString(String caption, String prompt, final StringCallback callback,
      String defaultValue, int maxLength, double width, Unit widthUnit, final int timeout,
      String confirmHtml, String cancelHtml, WidgetInitializer initializer) {
    Assert.notNull(callback);

    final Holder<State> state = Holder.of(State.OPEN);

    final DialogBox dialog = new DialogBox(caption);

    final Timer timer = (timeout > 0) ? createTimer(dialog, state) : null;

    Panel panel = new Flow();
    panel.addStyleName(STYLE_INPUT_PANEL);

    if (!BeeUtils.isEmpty(prompt)) {
      BeeLabel label = new BeeLabel(prompt.trim());
      label.addStyleName(STYLE_INPUT_PROMPT);

      UiHelper.add(panel, label, initializer, WIDGET_PROMPT);
    }

    InputText box = new InputText();
    box.addStyleName(STYLE_INPUT_STRING);

    if (!BeeUtils.isEmpty(defaultValue)) {
      box.setValue(defaultValue.trim());
    }
    if (maxLength > 0) {
      box.setMaxLength(maxLength);
    }
    if (width > 0) {
      StyleUtils.setWidth(box, width, widthUnit);
    }

    final Holder<Widget> input = new Holder<Widget>(box);

    BeeLabel errorLabel = new BeeLabel();
    errorLabel.addStyleName(STYLE_INPUT_ERROR);
    errorLabel.addStyleName(StyleUtils.NAME_ERROR);

    final Holder<Widget> errorDisplay = new Holder<Widget>(errorLabel);

    final Supplier<String> errorSupplier = new Supplier<String>() {
      public String get() {
        return callback.getMessage(UiHelper.getValue(input.get()));
      }
    };

    box.addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          event.preventDefault();
          state.set(State.CANCELED);
          dialog.hide();
          return;
        }

        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          String message = errorSupplier.get();
          if (BeeUtils.isEmpty(message)) {
            event.preventDefault();
            state.set(State.CONFIRMED);
            dialog.hide();
            return;
          } else {
            showError(errorDisplay.get(), message);
          }
        }

        if (timer != null) {
          timer.schedule(timeout);
        }
      }
    });

    UiHelper.add(panel, input, initializer, WIDGET_INPUT);

    UiHelper.add(panel, errorDisplay, initializer, WIDGET_ERROR);

    addCommandGroup(dialog, panel, confirmHtml, cancelHtml, initializer, state, errorDisplay,
        errorSupplier);

    dialog.addCloseHandler(new CloseHandler<Popup>() {
      public void onClose(CloseEvent<Popup> event) {
        if (timer != null) {
          timer.cancel();
        }

        switch (state.get()) {
          case CONFIRMED:
            callback.onSuccess(BeeUtils.trim(UiHelper.getValue(input.get())));
            break;
          case EXPIRED:
            callback.onTimeout(BeeUtils.trim(UiHelper.getValue(input.get())));
            break;
          default:
            callback.onCancel();
        }
      }
    });

    UiHelper.setWidget(dialog, panel, initializer, WIDGET_PANEL);

    dialog.setAnimationEnabled(true);
    dialog.center();

    UiHelper.focus(input.get());

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

  public void inputWidget(String caption, Widget widget, final InputCallback callback,
      final int timeout, String confirmHtml, String cancelHtml, WidgetInitializer initializer) {
    Assert.notNull(widget);
    Assert.notNull(callback);

    final Holder<State> state = Holder.of(State.OPEN);

    final DialogBox dialog = new DialogBox(caption);

    final Timer timer = (timeout > 0) ? createTimer(dialog, state) : null;

    Flow panel = new Flow();
    panel.addStyleName(STYLE_INPUT_PANEL);

    widget.addStyleName(STYLE_INPUT_WIDGET);
    UiHelper.add(panel, widget, initializer, WIDGET_INPUT);

    final Holder<Widget> errorDisplay = new Holder<Widget>(null);
    if (widget instanceof NotificationListener) {
      errorDisplay.set(widget);
    } else {
      BeeLabel errorLabel = new BeeLabel();
      errorLabel.addStyleName(STYLE_INPUT_ERROR);
      errorLabel.addStyleName(StyleUtils.NAME_ERROR);
      errorDisplay.set(errorLabel);
      UiHelper.add(panel, errorDisplay, initializer, WIDGET_ERROR);
    }

    final Supplier<String> errorSupplier = new Supplier<String>() {
      public String get() {
        return callback.getErrorMessage();
      }
    };

    addCommandGroup(dialog, panel, confirmHtml, cancelHtml, initializer, state, errorDisplay,
        errorSupplier);
    
    Binder.addKeyDownHandler(dialog, new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          event.preventDefault();
          state.set(State.CANCELED);
          dialog.hide();
          return;
        } else if (UiHelper.isSave(event.getNativeEvent())) {
          String message = errorSupplier.get();
          if (BeeUtils.isEmpty(message)) {
            event.preventDefault();
            state.set(State.CONFIRMED);
            dialog.hide();
            return;
          } else {
            showError(errorDisplay.get(), message);
          }
        }

        if (timer != null) {
          timer.schedule(timeout);
        }
      }
    });

    dialog.addCloseHandler(new CloseHandler<Popup>() {
      public void onClose(CloseEvent<Popup> event) {
        if (timer != null) {
          timer.cancel();
        }

        switch (state.get()) {
          case CONFIRMED:
            callback.onSuccess();
            break;
          case EXPIRED:
            callback.onTimeout();
            break;
          default:
            callback.onCancel();
        }
      }
    });

    UiHelper.setWidget(dialog, panel, initializer, WIDGET_PANEL);

    dialog.setAnimationEnabled(true);
    dialog.center();

    UiHelper.focus(widget);

    if (timer != null) {
      timer.schedule(timeout);
    }
  }

  private boolean addCommandGroup(final Popup dialog, HasWidgets panel, String confirmHtml,
      String cancelHtml, WidgetInitializer initializer, final Holder<State> state,
      final Holder<Widget> errorDisplay, final Supplier<String> errorSupplier) {
    if (BeeUtils.allEmpty(confirmHtml, cancelHtml)) {
      return false;
    }

    Panel commandGroup = new Flow();
    commandGroup.addStyleName(STYLE_INPUT_COMMAND_GROUP);

    if (!BeeUtils.isEmpty(confirmHtml)) {
      BeeButton confirm = new BeeButton(confirmHtml);
      confirm.addStyleName(STYLE_INPUT_COMMAND);
      confirm.addStyleName(STYLE_INPUT_CONFIRM);

      confirm.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          String message = errorSupplier.get();
          if (BeeUtils.isEmpty(message)) {
            state.set(State.CONFIRMED);
            dialog.hide();
          } else {
            showError(errorDisplay.get(), message);
          }
        }
      });

      UiHelper.add(commandGroup, confirm, initializer, WIDGET_CONFIRM);
    }

    if (!BeeUtils.isEmpty(cancelHtml)) {
      BeeButton cancel = new BeeButton(cancelHtml);
      cancel.addStyleName(STYLE_INPUT_COMMAND);
      cancel.addStyleName(STYLE_INPUT_CANCEL);

      cancel.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          state.set(State.CANCELED);
          dialog.hide();
        }
      });

      UiHelper.add(commandGroup, cancel, initializer, WIDGET_CANCEL);
    }

    UiHelper.add(panel, commandGroup, initializer, WIDGET_COMMAND_GROUP);
    return true;
  }

  private Timer createTimer(final Popup dialog, final Holder<State> state) {
    return new Timer() {
      @Override
      public void run() {
        state.set(State.EXPIRED);
        dialog.hide();
      }
    };
  }

  private void showError(Widget widget, String message) {
    if (!BeeUtils.isEmpty(message)) {
      if (widget instanceof HasText) {
        ((HasText) widget).setText(message);
      } else if (widget instanceof NotificationListener) {
        ((NotificationListener) widget).notifySevere(message);
      } else {
        BeeKeeper.getScreen().notifySevere(message);
      }
    }
  }
}
