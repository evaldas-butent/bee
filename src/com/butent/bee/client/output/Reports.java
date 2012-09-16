package com.butent.bee.client.output;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;

public class Reports extends Flow {

  public Reports() {
    super();
    addStyleName("bee-ReportsContainer");
  }
  
  public void addReport(String caption, Command command) {
    Assert.notEmpty(caption);
    Assert.notNull(command);
    
    Html widget = new Html(caption, command);
    widget.addStyleName("bee-ReportsItem");

    add(widget);
  }
}
