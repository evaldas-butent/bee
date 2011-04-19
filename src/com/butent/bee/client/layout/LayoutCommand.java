package com.butent.bee.client.layout;

import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.shared.Assert;

import java.util.ArrayList;
import java.util.List;

public class LayoutCommand extends BeeCommand {
  private List<HasLayoutCallback> layoutWidgets = new ArrayList<HasLayoutCallback>();
  
  public LayoutCommand(HasLayoutCallback... widgets) {
    super();
    Assert.notNull(widgets);    
    for (HasLayoutCallback w : widgets) {
      this.layoutWidgets.add(w);
    }
  }

  @Override
  public void execute() {
    for (HasLayoutCallback w : layoutWidgets) {
      w.onLayout();
    }
  }
}
