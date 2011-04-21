package com.butent.bee.client.view.navigation;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class RangeInfo extends AbstractPagerImpl {
  public RangeInfo() {
    initWidget(new BeeLabel());
  }

  @Override
  protected void onRangeOrRowCountChanged() {
    HasRows display = getDisplay();
    Assert.notNull(display);
    
    Range range = display.getVisibleRange();
    int start = range.getStart() + 1;
    int end = BeeUtils.min(start + range.getLength() - 1, display.getRowCount());
    
    Widget label = getWidget();
    if (label instanceof BeeLabel) {
      ((BeeLabel) label).setText(start + " - " + end + " / " + display.getRowCount());
    }
  }
}
