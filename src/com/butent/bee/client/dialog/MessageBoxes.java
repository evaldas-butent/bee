package com.butent.bee.client.dialog;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Implements a message box user interface component, sending text messages to the user.
 */

public class MessageBoxes {

  public void alert(Object... obj) {
    Assert.notNull(obj);
    Assert.parameterCount(obj.length, 1);
    Window.alert(BeeUtils.concat(BeeConst.CHAR_EOL, obj));
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

  public void confirm(String message, BeeCommand command) {
    confirm(null, message, command);
  }

  public void confirm(String message, BeeCommand command, String dialogStyleName) {
    confirm(null, message, command, dialogStyleName);
  }
  
  public void confirm(String caption, List<String> messages, BeeCommand command) {
    confirm(caption, messages, command, null, null);
  }

  public void confirm(String caption, List<String> messages, final BeeCommand command,
      String dialogStyleName) {
    confirm(caption, messages, command, dialogStyleName, null);
  }
  
  public void confirm(String caption, List<String> messages, final BeeCommand command,
      String dialogStyleName, String messageStyleName) {
    Assert.notNull(messages);
    int count = messages.size();
    Assert.isPositive(count);
    Assert.notNull(command);
    
    final Popup panel;
    if (BeeUtils.isEmpty(caption)) {
      panel = new Popup(true);
    } else {
      panel = new DialogBox(caption);
    }
    
    Vertical content = new Vertical();
    content.setSpacing(10);
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
    
    BeeImage ok = new BeeImage(Global.getImages().ok(), new BeeCommand() {
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
    
    BeeImage cancel = new BeeImage(Global.getImages().cancel(), new BeeCommand() {
      @Override
      public void execute() {
        panel.hide();
      }
    });
    buttons.add(cancel);
    buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_LEFT);
    
    content.add(buttons);
    
    if (!BeeUtils.isEmpty(dialogStyleName)) {
      if (panel instanceof DialogBox) {
        ((DialogBox) panel).getCaption().asWidget().addStyleName(dialogStyleName);
        content.addStyleName(dialogStyleName);
      } else {
        panel.addStyleName(dialogStyleName);
      }
    }
    
    panel.setWidget(content);
    panel.setAnimationEnabled(true);
    panel.center();
  }
  
  public void confirm(String caption, String message, BeeCommand command) {
    confirm(caption, message, command, null);
  }

  public void confirm(String caption, String message, BeeCommand command, String dialogStyleName) {
    Assert.notEmpty(message);
    confirm(caption, Lists.newArrayList(message), command, dialogStyleName);
  }
  
  public boolean nativeConfirm(Object... obj) {
    Assert.notNull(obj);
    Assert.parameterCount(obj.length, 1);
    return Window.confirm(BeeUtils.concat(BeeConst.CHAR_EOL, obj));
  }

  public void showError(Object... x) {
    CloseButton b = new CloseButton("ok");
    Popup box = createPopup(b, x);
    box.addStyleName(StyleUtils.NAME_ERROR);

    box.center();
    b.setFocus(true);
  }

  public void showGrid(String cap, Object data, String... columnLabels) {
    Assert.notNull(data);
    IsTable<?, ?> table = DataUtils.createTable(data, columnLabels);
    Assert.notNull(table);

    int c = table.getNumberOfColumns();
    Assert.isPositive(c);

    int r = table.getNumberOfRows();
    if (r <= 0) {
      BeeKeeper.getLog().warning(cap, "data table empty");
      return;
    }
    
    FlexTable grid = new FlexTable();
    int index = 0;
    
    if (!BeeUtils.isEmpty(cap)) {
      grid.setHTML(index, 0, cap.trim());
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
    
    CloseButton close = new CloseButton("ok");
    grid.setWidget(index, 0, close);
    grid.alignCenter(index, 0);
    if (c > 1) {
      grid.getFlexCellFormatter().setColSpan(index, 0, c);
    }

    Popup box = new Popup(true);
    box.setAnimationEnabled(true);

    box.setWidget(grid);

    box.center();
    close.setFocus(true);
  }

  public void showInfo(Object... x) {
    CloseButton b = new CloseButton("ok");
    Popup box = createPopup(b, x);

    box.center();
    b.setFocus(true);
  }

  public void showWidget(Widget widget) {
    Assert.notNull(widget);

    Popup box = new Popup(true);
    box.setAnimationEnabled(true);

    box.setWidget(widget);
    box.center();
  }
  
  private Popup createPopup(Widget bottom, Object... obj) {
    Assert.notNull(obj);
    int n = obj.length;
    Assert.parameterCount(n, 1);

    Vertical vp = new Vertical();

    for (int i = 0; i < n; i++) {
      if (obj[i] instanceof Widget) {
        vp.add((Widget) obj[i]);
      } else if (obj[i] instanceof String) {
        vp.add(new BeeLabel((String) obj[i]));
      } else if (obj[i] instanceof Collection) {
        for (Object item : (Collection<?>) obj[i]) {
          vp.add(new BeeLabel(BeeUtils.transform(item)));
        }
      } else if (ArrayUtils.isArray(obj[i])) {
        for (int j = 0; j < ArrayUtils.length(obj[i]); j++) {
          vp.add(new BeeLabel(BeeUtils.transform(ArrayUtils.get(obj[i], j))));
        }
      } else if (obj[i] != null) {
        vp.add(new BeeLabel(BeeUtils.transform(obj[i])));
      }
    }
    
    if (bottom != null) {
      vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      vp.add(bottom);
    }

    Popup popup = new Popup(true);
    popup.setAnimationEnabled(true);

    popup.setWidget(vp);
    return popup;
  }
}
