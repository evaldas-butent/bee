package com.butent.bee.client.dialog;

import com.google.common.base.Supplier;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.event.Previewer.PreviewConsumer;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.view.edit.EditorAssistant;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasHtml;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.State;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

/**
 * Implements a user interface component, which enables to produce a input box for information input
 * from the user.
 */

public class InputBoxes {

  public static final String SILENT_ERROR = "-";

  private static final String STYLE_INPUT_PANEL = "bee-InputPanel";
  private static final String STYLE_INPUT_PROMPT = "bee-InputPrompt";
  private static final String STYLE_INPUT_STRING = "bee-InputString";
  private static final String STYLE_INPUT_WIDGET = "bee-InputWidget";
  private static final String STYLE_INPUT_ERROR = "bee-InputError";
  private static final String STYLE_INPUT_COMMAND_GROUP = "bee-InputCommandGroup";
  private static final String STYLE_INPUT_COMMAND = "bee-InputCommand";
  private static final String STYLE_INPUT_CONFIRM = "bee-InputConfirm";
  private static final String STYLE_INPUT_CANCEL = "bee-InputCancel";

  private static final String STYLE_INPUT_DELETE = "bee-InputDelete";
  private static final String STYLE_INPUT_PRINT = "bee-InputPrint";
  private static final String STYLE_INPUT_SAVE = "bee-InputSave";
  private static final String STYLE_INPUT_CLOSE = "bee-InputClose";

  public void inputString(String caption, String prompt, final StringCallback callback,
      String defaultValue, int maxLength, Element target, double width, CssUnit widthUnit,
      final int timeout, String confirmHtml, String cancelHtml, WidgetInitializer initializer) {

    Assert.notNull(callback);

    final Holder<State> state = Holder.of(State.OPEN);

    final DialogBox dialog = DialogBox.create(caption);
    UiHelper.initialize(dialog, initializer, DialogConstants.WIDGET_DIALOG);

    final Timer timer = (timeout > 0) ? new DialogTimer(dialog, state) : null;

    Panel panel = new Flow();
    panel.addStyleName(STYLE_INPUT_PANEL);

    if (!BeeUtils.isEmpty(prompt)) {
      Label label = new Label(prompt.trim());
      label.addStyleName(STYLE_INPUT_PROMPT);

      UiHelper.add(panel, label, initializer, DialogConstants.WIDGET_PROMPT);
    }

    InputText box = new InputText();
    box.addStyleName(STYLE_INPUT_STRING);

    if (!BeeUtils.isEmpty(defaultValue)) {
      String value = (maxLength > 0) 
          ? BeeUtils.left(defaultValue.trim(), maxLength) : defaultValue.trim();
      box.setValue(value);
    }
    if (maxLength > 0) {
      box.setMaxLength(maxLength);
    }
    if (width > 0) {
      StyleUtils.setWidth(box, width, widthUnit);
    }

    final Holder<Widget> input = new Holder<Widget>(box);

    Label errorLabel = new Label();
    errorLabel.addStyleName(STYLE_INPUT_ERROR);
    errorLabel.addStyleName(StyleUtils.NAME_ERROR);

    final Holder<Widget> errorDisplay = new Holder<Widget>(errorLabel);

    final Supplier<String> errorSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return callback.getMessage(EditorAssistant.getValue(input.get()));
      }
    };

    box.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          event.preventDefault();
          state.set(State.CANCELED);
          dialog.close();
          return;
        }

        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          String message = errorSupplier.get();
          if (BeeUtils.isEmpty(message)) {
            event.preventDefault();
            state.set(State.CONFIRMED);
            dialog.close();
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

    UiHelper.add(panel, input, initializer, DialogConstants.WIDGET_INPUT);

    UiHelper.add(panel, errorDisplay, initializer, DialogConstants.WIDGET_ERROR);

    addCommandGroup(dialog, panel, confirmHtml, cancelHtml, initializer, state, errorDisplay,
        errorSupplier);

    dialog.addCloseHandler(new CloseEvent.Handler() {
      @Override
      public void onClose(CloseEvent event) {
        if (timer != null) {
          timer.cancel();
        }

        switch (state.get()) {
          case CONFIRMED:
            callback.onSuccess(BeeUtils.trim(EditorAssistant.getValue(input.get())));
            break;
          case EXPIRED:
            callback.onTimeout(BeeUtils.trim(EditorAssistant.getValue(input.get())));
            break;
          default:
            callback.onCancel();
        }
      }
    });

    UiHelper.setWidget(dialog, panel, initializer, DialogConstants.WIDGET_PANEL);

    dialog.setAnimationEnabled(true);
    dialog.showRelativeTo(target);

    UiHelper.focus(input.get());

    if (timer != null) {
      timer.schedule(timeout);
    }
  }

  public void inputWidget(String caption, IsWidget widget, final InputCallback callback,
      String dialogStyle, Element target, Set<Action> enabledActions, 
      WidgetInitializer initializer) {

    Assert.notNull(widget);
    Assert.notNull(callback);

    final DialogBox dialog = DialogBox.withoutCloseBox(caption, dialogStyle);
    UiHelper.initialize(dialog, initializer, DialogConstants.WIDGET_DIALOG);

    Flow panel = new Flow();
    panel.addStyleName(STYLE_INPUT_PANEL);

    widget.asWidget().addStyleName(STYLE_INPUT_WIDGET);
    UiHelper.add(panel, widget.asWidget(), initializer, DialogConstants.WIDGET_INPUT);

    final Holder<Widget> errorDisplay = new Holder<Widget>(null);
    if (widget instanceof NotificationListener) {
      errorDisplay.set(widget.asWidget());
    } else {
      Label errorLabel = new Label();
      errorLabel.addStyleName(STYLE_INPUT_ERROR);
      errorLabel.addStyleName(StyleUtils.NAME_ERROR);
      errorDisplay.set(errorLabel);
      UiHelper.add(panel, errorDisplay, initializer, DialogConstants.WIDGET_ERROR);
    }

    final ScheduledCommand onSave = new ScheduledCommand() {
      @Override
      public void execute() {
        String message = callback.getErrorMessage();
        if (BeeUtils.isEmpty(message)) {
          dialog.close();
          callback.onSuccess();
        } else {
          showError(errorDisplay.get(), message);
        }
      }
    };

    Image save = new Image(Global.getImages().silverSave(), onSave);
    save.addStyleName(STYLE_INPUT_SAVE);
    UiHelper.initialize(save, initializer, DialogConstants.WIDGET_SAVE);
    dialog.addAction(Action.SAVE, save);
    
    if (enabledActions != null) {
      if (enabledActions.contains(Action.DELETE)) {
        Image delete = new Image(Global.getImages().silverDelete(), new ScheduledCommand() {
          @Override
          public void execute() {
            callback.onDelete(dialog);
          }
        });

        delete.addStyleName(STYLE_INPUT_DELETE);
        UiHelper.initialize(delete, initializer, DialogConstants.WIDGET_DELETE);
        dialog.addAction(Action.DELETE, delete);
      }

      if (enabledActions.contains(Action.PRINT)) {
        Image print = new Image(Global.getImages().silverPrint(), new ScheduledCommand() {
          @Override
          public void execute() {
            Printer.print(dialog);
          }
        });

        print.addStyleName(STYLE_INPUT_PRINT);
        UiHelper.initialize(print, initializer, DialogConstants.WIDGET_PRINT);
        dialog.addAction(Action.PRINT, print);
      }
    }

    final ScheduledCommand onClose = new ScheduledCommand() {
      @Override
      public void execute() {
        callback.onClose(new CloseCallback() {
          @Override
          public void onClose() {
            dialog.close();
            callback.onCancel();
          }

          @Override
          public void onSave() {
            onSave.execute();
          }
        });
      }
    };

    Image close = new Image(Global.getImages().silverClose(), onClose);
    close.addStyleName(STYLE_INPUT_CLOSE);
    UiHelper.initialize(close, initializer, DialogConstants.WIDGET_CLOSE);
    dialog.addAction(Action.CLOSE, close);

    dialog.setOnSave(new PreviewConsumer() {
      @Override
      public void accept(NativePreviewEvent input) {
        if (input != null) {
          input.cancel();
        }
        onSave.execute();
      }
    });

    dialog.setOnEscape(new PreviewConsumer() {
      @Override
      public void accept(NativePreviewEvent input) {
        if (input != null) {
          input.cancel();
        }
        onClose.execute();
      }
    });

    UiHelper.setWidget(dialog, panel, initializer, DialogConstants.WIDGET_PANEL);

    dialog.setAnimationEnabled(true);
    dialog.showRelativeTo(target);

    UiHelper.focus(widget.asWidget());
  }

  private static boolean addCommandGroup(final Popup dialog, HasWidgets panel, String confirmHtml,
      String cancelHtml, WidgetInitializer initializer, final Holder<State> state,
      final Holder<Widget> errorDisplay, final Supplier<String> errorSupplier) {

    if (BeeUtils.allEmpty(confirmHtml, cancelHtml)) {
      return false;
    }

    Panel commandGroup = new Flow();
    commandGroup.addStyleName(STYLE_INPUT_COMMAND_GROUP);

    if (!BeeUtils.isEmpty(confirmHtml)) {
      Button confirm = new Button(confirmHtml);
      confirm.addStyleName(STYLE_INPUT_COMMAND);
      confirm.addStyleName(STYLE_INPUT_CONFIRM);

      confirm.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String message = errorSupplier.get();
          if (BeeUtils.isEmpty(message)) {
            state.set(State.CONFIRMED);
            dialog.close();
          } else {
            showError(errorDisplay.get(), message);
          }
        }
      });

      UiHelper.add(commandGroup, confirm, initializer, DialogConstants.WIDGET_CONFIRM);
    }

    if (!BeeUtils.isEmpty(cancelHtml)) {
      Button cancel = new Button(cancelHtml);
      cancel.addStyleName(STYLE_INPUT_COMMAND);
      cancel.addStyleName(STYLE_INPUT_CANCEL);

      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          state.set(State.CANCELED);
          dialog.close();
        }
      });

      UiHelper.add(commandGroup, cancel, initializer, DialogConstants.WIDGET_CANCEL);
    }

    UiHelper.add(panel, commandGroup, initializer, DialogConstants.WIDGET_COMMAND_GROUP);
    return true;
  }

  private static void showError(Widget widget, String message) {
    if (!BeeUtils.isEmpty(message) && !SILENT_ERROR.equals(message)) {
      if (widget instanceof HasHtml) {
        ((HasHtml) widget).setHtml(message);
      } else if (widget instanceof NotificationListener) {
        ((NotificationListener) widget).notifySevere(message);
      } else {
        BeeKeeper.getScreen().notifySevere(message);
      }
    }
  }
}
