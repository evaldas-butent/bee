package com.butent.bee.client.view.edit;

import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;

public class EditorFactory {
  
  public static Editor createEditor(BeeColumn column) {
    Assert.notNull(column);
    return new BeeTextBox();
  }

  private EditorFactory() {
  }
}
