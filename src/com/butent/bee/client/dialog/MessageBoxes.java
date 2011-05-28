package com.butent.bee.client.dialog;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.CellType;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Iterator;
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

  public boolean close(Object src) {
    boolean ok = false;

    if (src instanceof Widget) {
      PopupPanel p = DomUtils.parentPopup((Widget) src);
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
    
    final PopupPanel panel;
    if (BeeUtils.isEmpty(caption)) {
      panel = new Popup();
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
    showInfo(x);
  }

  public void showGrid(String cap, Object data, String... columnLabels) {
    Assert.notNull(data);
    showInfo(cap, Global.cellGrid(data, CellType.TEXT, columnLabels));
  }

  public void showInfo(Object... x) {
    Assert.notNull(x);
    int n = x.length;
    Assert.parameterCount(n, 1);

    Vertical vp = new Vertical();

    for (int i = 0; i < n; i++) {
      if (x[i] instanceof Widget) {
        vp.add((Widget) x[i]);

        if (x[i] instanceof CellGrid) {
          vp.setCellHeight((Widget) x[i], "200px");
          vp.setCellWidth((Widget) x[i], "400px");
        } else if (x[i] instanceof BeeTree) {
          vp.setCellHeight((Widget) x[i], "500px");
          vp.setCellWidth((Widget) x[i], "400px");
        }

      } else if (x[i] instanceof String) {
        vp.add(new BeeLabel((String) x[i]));
      } else if (x[i] instanceof Collection) {
        for (Iterator<?> iter = ((Collection<?>) x[i]).iterator(); iter.hasNext();) {
          vp.add(new BeeLabel(iter.next()));
        }
      } else if (ArrayUtils.isArray(x[i])) {
        for (int j = 0; j < ArrayUtils.length(x[i]); j++) {
          vp.add(new BeeLabel(ArrayUtils.get(x[i], j)));
        }
      } else if (x[i] != null) {
        vp.add(new BeeLabel(x[i]));
      }
    }

    CloseButton b = new CloseButton("ok");

    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    vp.add(b);

    Popup box = new Popup();
    box.setAnimationEnabled(true);

    box.setWidget(vp);

    box.center();
    b.setFocus(true);
  }

  public void showWidget(Widget widget) {
    Assert.notNull(widget);

    Popup box = new Popup();
    box.setAnimationEnabled(true);

    box.setWidget(widget);
    box.center();
  }
}
