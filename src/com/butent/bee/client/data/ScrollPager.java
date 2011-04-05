package com.butent.bee.client.data;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Scroll;
import com.butent.bee.client.widget.Html;

public class ScrollPager extends AbstractPager {

  private final Scroll scroll = new Scroll();
  private int lastPos = -1;

  public ScrollPager(int pageSize, int rowCount) {
    Widget widget = new Html();
    DomUtils.setWidth(widget, 0);
    DomUtils.setHeight(widget, rowCount * 1000 / pageSize);
    
    scroll.add(widget);
    initWidget(scroll);

    scroll.addScrollHandler(new ScrollHandler() {
      public void onScroll(ScrollEvent event) {
        int pos = scroll.getVerticalScrollPosition();
        int maxPos = scroll.getWidget().getOffsetHeight();
        if (pos < 0 || pos == lastPos || pos > maxPos) {
          return;
        }
        
        HasRows display = getDisplay();
        if (display == null) {
          return;
        }
        
        int ps = display.getVisibleRange().getLength(); 
        int rc = display.getRowCount();
        if (ps <= 0 || ps >= rc) {
          return;
        }
        
        int z = pos * rc / maxPos;
        if (z + ps > rc) {
          z = rc - ps;
        }
        
        display.setVisibleRange(z, ps);
        
//        BeeKeeper.getLog().info(pos, lastPos, maxPos, ps, rc, z);
        lastPos = pos;
      }
    });
  }

  @Override
  protected void onRangeOrRowCountChanged() {
    HasRows display = getDisplay();
    if (display != null) {
      int start = display.getVisibleRange().getStart();
      int rc = display.getRowCount();
      int maxPos = scroll.getWidget().getOffsetHeight();
      int pos = start * maxPos / rc;
      
      if (pos >= 0 && pos <= maxPos) {
        scroll.setVerticalScrollPosition(pos);
        lastPos = pos;
//        BeeKeeper.getLog().info("r", start, rc, maxPos, pos);
      }  
    }
  }
}
