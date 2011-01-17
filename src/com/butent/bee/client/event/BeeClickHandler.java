package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.HasStage;
import com.butent.bee.shared.utils.BeeUtils;

public class BeeClickHandler implements ClickHandler {

  public void onClick(ClickEvent event) {
    Object source = event.getSource();
    
    if (source instanceof HasBeeClickHandler) {
      ((HasBeeClickHandler) source).onBeeClick(event);
      return;
    }

    if (source instanceof HasCommand) {
      BeeCommand cmnd = ((HasCommand) source).getCommand();

      if (cmnd != null) {
        cmnd.execute();
        return;
      }
    }

    if (source instanceof HasService) {
      String svc = ((HasService) source).getService();

      if (!BeeUtils.isEmpty(svc)) {
        String stg;
        if (source instanceof HasStage) {
          stg = ((HasStage) source).getStage();
        } else {
          stg = null;
        }

        BeeKeeper.getBus().dispatchService(svc, stg, event);
        return;
      }
    }
  }

}
