package com.butent.bee.egg.client.communication;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.composite.TextEditor;
import com.butent.bee.egg.client.layout.BeeSplit;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeResource;

public class ResponseHandler {

  public static void showXmlInfo(int pc, int[] sizes, String content) {
    Assert.betweenInclusive(pc, 1, 3);
    Assert.arrayLength(sizes, pc);
    Assert.notEmpty(content);
    
    BeeResource[] resources = new BeeResource[pc];
    int start = 0;
    
    for (int i = 0; i < pc; i++) {
      resources[i] = new BeeResource();
      resources[i].deserialize(content.substring(start, start + sizes[i]));
      start += sizes[i];
    }
    
    if (pc <= 1) {
      BeeKeeper.getUi().showResource(resources[0]);
      return;
    }

    int h = BeeKeeper.getUi().getActivePanelHeight();
    
    BeeSplit panel = new BeeSplit();
    panel.addNorth(new TextEditor(resources[0]), h / pc);
    
    if (pc == 2) {
      panel.add(new TextEditor(resources[1]));
    } else {
      panel.addSouth(new TextEditor(resources[2]), h / pc);
      panel.add(new TextEditor(resources[1]));
    }
    
    BeeKeeper.getUi().updateActivePanel(panel);
  }

}
