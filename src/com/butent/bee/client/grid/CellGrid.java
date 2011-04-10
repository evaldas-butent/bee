package com.butent.bee.client.grid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;

public class CellGrid extends CellTable<IsRow> implements HasId {
  public interface GridStyle extends CellTable.Style {
    String DEFAULT_CSS = "CellGrid.css"; 
  }
  
  public interface GridResources extends CellTable.Resources {
    @Source(GridStyle.DEFAULT_CSS)
    GridStyle cellTableStyle();
  }
  
  private static GridResources defaultResources = null;
  
  private static GridResources getDefaultResources() {
    if (defaultResources == null) {
      defaultResources = GWT.create(GridResources.class);
    }
    return defaultResources;
  }
  
  public CellGrid(int pageSize) {
    super(pageSize, getDefaultResources());
    init();
  }

  public void createId() {
    DomUtils.createId(this, "cellgrid");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void initData(IsTable<?, ?> data) {
    Assert.notNull(data);

    setRowCount(data.getNumberOfRows());
    setRowData(0, data.getRows().getList());
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  protected void onBrowserEvent2(Event event) {
    super.onBrowserEvent2(event);
    
    if (event.getTypeInt() == Event.ONMOUSEWHEEL && !isKeyboardNavigationSuppressed()) {
      int y = event.getMouseWheelVelocityY();
      int rc = getRowCount();
      int start = getPageStart();
      int length = getPageSize();
      
      if (length > 0 && rc > length) {
        int p = -1;
        if (y > 0 && start + length < rc) {
          p = start + 1;
        } else if (y < 0 && start > 0) {
          p = start - 1;
        }
        
        if (p >= 0) {
          setVisibleRange(p, length);
        }
      }
    }
  }
  
  private void init() {
    createId();
  }
}
