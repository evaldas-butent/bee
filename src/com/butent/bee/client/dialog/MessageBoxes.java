package com.butent.bee.client.dialog;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a message box user interface component, sending text messages to the user.
 */

public class MessageBoxes {

  private static final String STYLE_CHOICE_DIALOG = "bee-ChoiceDialog";

  private static final String STYLE_CHOICE_PANEL = "bee-ChoicePanel";
  private static final String STYLE_CHOICE_PROMPT = "bee-ChoicePrompt";
  private static final String STYLE_CHOICE_CONTAINER = "bee-ChoiceContainer";
  private static final String STYLE_CHOICE_GROUP = "bee-ChoiceGroup-";
  private static final String STYLE_CHOICE_DEFAULT = "bee-ChoiceDefault";
  private static final String STYLE_CHOICE_CANCEL = "bee-ChoiceCancel";

  private static final int CHOICE_MAX_HORIZONTAL_ITEMS = 10;
  private static final int CHOICE_MAX_HORIZONTAL_CHARS = 100;

  public void alert(String... lines) {
    Assert.notNull(lines);
    Assert.parameterCount(lines.length, 1);
    Window.alert(BeeUtils.buildLines(lines));
  }

  public void choice(String caption, String prompt, List<String> options,
      final ChoiceCallback callback, final int defaultValue, final int timeout,
      String cancelHtml, WidgetInitializer initializer) {
    Assert.notEmpty(options);
    Assert.notNull(callback);

    final Holder<State> state = Holder.of(State.OPEN);

    final DialogBox dialog = new DialogBox(caption, STYLE_CHOICE_DIALOG);
    UiHelper.initialize(dialog, initializer, DialogConstants.WIDGET_DIALOG);

    final Timer timer = (timeout > 0) ? new DialogTimer(dialog, state) : null;

    Panel panel = new Flow();
    panel.addStyleName(STYLE_CHOICE_PANEL);

    if (!BeeUtils.isEmpty(prompt)) {
      BeeLabel label = new BeeLabel(prompt.trim());
      label.addStyleName(STYLE_CHOICE_PROMPT);

      UiHelper.add(panel, label, initializer, DialogConstants.WIDGET_PROMPT);
    }

    int size = options.size();

    boolean vertical = size > CHOICE_MAX_HORIZONTAL_ITEMS;
    if (!vertical && size > 1) {
      int len = 0;
      for (String option : options) {
        len += option.trim().length();
      }
      vertical = len > CHOICE_MAX_HORIZONTAL_CHARS;
    }

    TabBar group = new TabBar(STYLE_CHOICE_GROUP, vertical);

    for (int i = 0; i < size; i++) {
      Widget widget = UiHelper.initialize(new Html(options.get(i)), initializer,
          DialogConstants.WIDGET_COMMAND_ITEM);
      group.addItem(widget);
    }

    final Holder<Integer> cancelIndex = Holder.absent();

    if (!BeeUtils.isEmpty(cancelHtml)) {
      Widget widget = UiHelper.initialize(new Html(cancelHtml), initializer,
          DialogConstants.WIDGET_CANCEL);
      if (widget != null) {
        widget.addStyleName(STYLE_CHOICE_CANCEL);
        group.addItem(widget);
        cancelIndex.set(group.getItemCount() - 1);
      }
    }

    Flow container = new Flow();
    container.addStyleName(STYLE_CHOICE_CONTAINER);
    container.addStyleName(STYLE_CHOICE_CONTAINER + BeeConst.STRING_MINUS
        + (vertical ? StyleUtils.NAME_VERTICAL : StyleUtils.NAME_HORIZONTAL));

    UiHelper.add(container, group, initializer, DialogConstants.WIDGET_COMMAND_GROUP);
    panel.add(container);

    final Holder<Integer> selectedIndex = Holder.absent();

    group.addSelectionHandler(new SelectionHandler<Integer>() {
      public void onSelection(SelectionEvent<Integer> event) {
        selectedIndex.set(event.getSelectedItem());
        dialog.hide();
      }
    });

    dialog.setHideOnEscape(true);

    dialog.addCloseHandler(new CloseHandler<Popup>() {
      public void onClose(CloseEvent<Popup> event) {
        if (timer != null) {
          timer.cancel();
        }

        if (selectedIndex.isNotNull() && !cancelIndex.contains(selectedIndex.get())) {
          callback.onSuccess(selectedIndex.get());
        } else if (State.EXPIRED.equals(state.get())) {
          callback.onTimeout();
        } else {
          callback.onCancel();
        }
      }
    });

    UiHelper.setWidget(dialog, panel, initializer, DialogConstants.WIDGET_PANEL);

    dialog.setAnimationEnabled(true);
    dialog.center();

    int focusIndex;

    if (group.isIndex(defaultValue)) {
      group.getTabWidget(defaultValue).addStyleName(STYLE_CHOICE_DEFAULT);
      group.selectTab(defaultValue, false);
      focusIndex = defaultValue;
    } else if (cancelIndex.isNotNull()) {
      focusIndex = cancelIndex.get();
    } else {
      focusIndex = 0;
    }

    group.focusTab(focusIndex);

    if (timer != null) {
      timer.schedule(timeout);
    }
  }

  public boolean close(Widget source) {
    boolean ok = false;

    if (source != null) {
      Popup p = DomUtils.getParentPopup(source);
      if (p != null) {
        p.hide();
        ok = true;
      }
    }
    return ok;
  }

  public void confirm(String message, Command command) {
    confirm(null, message, command);
  }

  public void confirm(String message, Command command, String dialogStyleName) {
    confirm(null, message, command, dialogStyleName);
  }

  public void confirm(String caption, List<String> messages, Command command) {
    confirm(caption, messages, command, null, null);
  }

  public void confirm(String caption, List<String> messages, Command command,
      String dialogStyleName) {
    confirm(caption, messages, command, dialogStyleName, null);
  }

  public void confirm(String caption, List<String> messages, final Command command,
      String dialogStyleName, String messageStyleName) {
    Assert.notNull(messages);
    int count = messages.size();
    Assert.isPositive(count);
    Assert.notNull(command);

    final Popup panel;
    if (BeeUtils.isEmpty(caption)) {
      panel = new Popup(true, true);
    } else {
      panel = new DialogBox(caption);
    }

    Vertical content = new Vertical();
    content.setCellSpacing(10);
    content.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    for (String message : messages) {
      if (!BeeUtils.isEmpty(message)) {
        BeeLabel label = new BeeLabel(message);
        if (!BeeUtils.isEmpty(messageStyleName)) {
          label.addStyleName(messageStyleName);
        }
        content.add(label);
      }
    }

    Horizontal buttons = new Horizontal();

    BeeImage ok = new BeeImage(Global.getImages().ok(), new Command() {
      @Override
      public void execute() {
        panel.hide();
        command.execute();
      }
    });
    buttons.add(ok);
    buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_RIGHT);

    Html spacer = new Html();
    spacer.setWidth(StyleUtils.toCssLength(6, Unit.EM));
    buttons.add(spacer);

    BeeImage cancel = new BeeImage(Global.getImages().cancel(), new Command() {
      @Override
      public void execute() {
        panel.hide();
      }
    });
    buttons.add(cancel);
    buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_LEFT);

    content.add(buttons);

    if (!BeeUtils.isEmpty(dialogStyleName)) {
      panel.addStyleName(dialogStyleName);
    }

    panel.setWidget(content);
    panel.setAnimationEnabled(true);
    panel.center();

    panel.setHideOnEscape(true);
    panel.setHideOnSave(true);
    panel.setOnSave(command);

    DomUtils.makeFocusable(panel);
    DomUtils.setFocus(panel, true);
  }

  public void confirm(String caption, String message, Command command) {
    confirm(caption, message, command, null);
  }

  public void confirm(String caption, String message, Command command, String dialogStyleName) {
    Assert.notEmpty(message);
    confirm(caption, Lists.newArrayList(message), command, dialogStyleName);
  }

  public boolean nativeConfirm(String... lines) {
    Assert.notNull(lines);
    Assert.parameterCount(lines.length, 1);
    return Window.confirm(BeeUtils.buildLines(lines));
  }

  public void showError(String... messages) {
    CloseButton b = new CloseButton(DialogConstants.OK);
    Popup box = createPopup(b, messages);
    box.addStyleName(StyleUtils.NAME_ERROR);

    box.center();
    b.setFocus(true);
  }

  public void showInfo(String... messages) {
    CloseButton b = new CloseButton(DialogConstants.OK);
    Popup box = createPopup(b, messages);

    box.center();
    b.setFocus(true);
  }

  public void showTable(String caption, IsTable<?, ?> table) {
    Assert.notNull(table);

    int c = table.getNumberOfColumns();
    Assert.isPositive(c);

    int r = table.getNumberOfRows();
    if (r <= 0) {
      BeeKeeper.getLog().warning(caption, "data table empty");
      return;
    }

    FlexTable grid = new FlexTable();
    int index = 0;

    if (!BeeUtils.isEmpty(caption)) {
      grid.setHTML(index, 0, caption.trim());
      grid.alignCenter(index, 0);
      if (c > 1) {
        grid.getFlexCellFormatter().setColSpan(index, 0, c);
      }
      index++;
    }

    for (int j = 0; j < c; j++) {
      grid.setHTML(index, j, table.getColumnLabel(j));
      grid.alignCenter(index, j);
    }
    index++;

    for (int i = 0; i < r; i++) {
      for (int j = 0; j < c; j++) {
        String value = table.getString(i, j);
        if (!BeeUtils.isEmpty(value)) {
          grid.setHTML(index, j, value);
          if (ValueType.isNumeric(table.getColumnType(j))) {
            grid.alignRight(index, j);
          }
        }
      }
      grid.getRowFormatter().setStyleName(index,
          (index % 2 == 0) ? CellGrid.STYLE_EVEN_ROW : CellGrid.STYLE_ODD_ROW);
      index++;
    }

    CloseButton close = new CloseButton(DialogConstants.OK);
    grid.setWidget(index, 0, close);
    grid.alignCenter(index, 0);
    if (c > 1) {
      grid.getFlexCellFormatter().setColSpan(index, 0, c);
    }

    Popup box = new Popup(true, true);
    box.setAnimationEnabled(true);

    box.setWidget(grid);

    box.center();
    close.setFocus(true);
  }

  public void showWidget(Widget widget) {
    Assert.notNull(widget);

    Popup box = new Popup(true, true);
    box.setAnimationEnabled(true);

    box.setWidget(widget);
    box.center();
  }

  private Popup createPopup(Widget bottom, String... messages) {
    Assert.notNull(messages);

    Vertical vp = new Vertical();
    for (String s : messages) {
      vp.add(new BeeLabel(s));
    }

    if (bottom != null) {
      vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      vp.add(bottom);
    }

    Popup popup = new Popup(true, true);
    popup.setAnimationEnabled(true);

    popup.setWidget(vp);
    return popup;
  }
}
