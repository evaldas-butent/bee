package com.butent.bee.client.output;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;

public class Reports extends Flow {

  public Reports() {
    super();
    addStyleName("bee-ReportsContainer");
  }
  
  public void addReport(String caption, final Command command) {
    Assert.notEmpty(caption);
    Assert.notNull(command);
    
    Label widget = new Label(caption);
    widget.addStyleName("bee-ReportsItem");
    
    if (command != null) {
      widget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          command.execute();
        }
      });
    }

    add(widget);
  }
}
