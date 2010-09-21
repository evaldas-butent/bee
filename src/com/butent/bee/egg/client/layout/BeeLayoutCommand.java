package com.butent.bee.egg.client.layout;

import com.butent.bee.egg.client.utils.BeeCommand;

import java.util.ArrayList;
import java.util.List;

public class BeeLayoutCommand extends BeeCommand {
  private List<HasLayoutCallback> layoutWidgets = new ArrayList<HasLayoutCallback>();
  
  public BeeLayoutCommand(HasLayoutCallback... widgets) {
    super();
    
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
