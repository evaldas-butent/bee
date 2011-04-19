package com.butent.bee.client.grid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.utils.BeeUtils;

public class CellGrid extends CellTable<IsRow> implements HasId {

  public interface GridResources extends CellTable.Resources {
    @Source(GridStyle.DEFAULT_CSS)
    GridStyle cellTableStyle();
  }
  
  public interface GridStyle extends CellTable.Style {
    String DEFAULT_CSS = "CellGrid.css"; 
  }
  
  private static GridResources defaultResources = null;
  
  private static GridResources getDefaultResources() {
    if (defaultResources == null) {
      defaultResources = GWT.create(GridResources.class);
    }
    return defaultResources;
  }

  private int headerCellHeight = 24;
  private int bodyCellHeight = 22;
  private int footerCellHeight = -1;
  
  private int borderWidth = 1;
  
  public CellGrid(int pageSize) {
    super(pageSize, getDefaultResources());
    init();
  }

  public void createId() {
    DomUtils.createId(this, "cellgrid");
  }

  public int getBodyCellHeight() {
    return bodyCellHeight;
  }
  
  public int getBorderWidth() {
    return borderWidth;
  }

  public String getCssBodyCellHeight() {
    return BeeUtils.toString(getBodyCellHeight()) + Unit.PX.getType();
  }
  
  public String getCssFooterCellHeight() {
    return BeeUtils.toString(getFooterCellHeight()) + Unit.PX.getType();
  }

  public String getCssHeaderCellHeight() {
    return BeeUtils.toString(getHeaderCellHeight()) + Unit.PX.getType();
  }

  public int getFooterCellHeight() {
    return footerCellHeight;
  }

  public int getHeaderCellHeight() {
    return headerCellHeight;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void initData(IsTable<?, ?> data) {
    Assert.notNull(data);

    setRowCount(data.getNumberOfRows());
    setRowData(0, data.getRows().getList());
  }
  
  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    createId();
    addStyleName("bee-CellGrid");
  }
}
