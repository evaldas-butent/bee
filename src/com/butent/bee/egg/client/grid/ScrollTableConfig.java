package com.butent.bee.egg.client.grid;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.dialog.BeePopupPanel;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.BeeLayoutPanel;
import com.butent.bee.egg.client.pst.AbstractScrollTable;
import com.butent.bee.egg.client.pst.HasTableDefinition;
import com.butent.bee.egg.client.pst.AbstractScrollTable.ResizePolicy;
import com.butent.bee.egg.client.pst.PagingScrollTable;
import com.butent.bee.egg.client.pst.SelectionGrid.SelectionPolicy;
import com.butent.bee.egg.client.pst.TableDefinition;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.client.widget.BeeSimpleCheckBox;
import com.butent.bee.egg.client.widget.Spinner;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Collections;

public class ScrollTableConfig {
  private class ColumnRef {
    private String show;
    private String order;
    private String currW;
    private String minW;
    private String prefW;
    private String maxW;

    public String getCurrW() {
      return currW;
    }
    public String getMaxW() {
      return maxW;
    }
    public String getMinW() {
      return minW;
    }
    public String getOrder() {
      return order;
    }
    public String getPrefW() {
      return prefW;
    }
    public String getShow() {
      return show;
    }
    public void setCurrW(String currW) {
      this.currW = currW;
    }
    public void setMaxW(String maxW) {
      this.maxW = maxW;
    }
    public void setMinW(String minW) {
      this.minW = minW;
    }
    public void setOrder(String order) {
      this.order = order;
    }
    public void setPrefW(String prefW) {
      this.prefW = prefW;
    }
    public void setShow(String show) {
      this.show = show;
    }
  }
  
  private class ConfigCommand extends BeeCommand {
    private String action;

    public ConfigCommand(String action) {
      super();
      this.action = action;
    }

    @Override
    public void execute() {
      if (BeeUtils.same(action, "cancel")) {
        popup.hide();
        return;
      }
      
      boolean reload = setupGrid();
      reload |= setupColumns();
      
      if (reload && scrollTable instanceof PagingScrollTable<?>) {
        ((PagingScrollTable<?>) scrollTable).reloadPage();
      }

      if (BeeUtils.same(action, "fill")) {
        scrollTable.fillWidth();
      } else if (BeeUtils.same(action, "redraw")) {
        scrollTable.redraw();
      } else if (BeeUtils.same(action, "reset")) {
        scrollTable.resetColumnWidths();
      }
      
      popup.hide();
    }
  }
  
  private class PopupPosition implements PositionCallback {
    public void setPosition(int offsetWidth, int offsetHeight) {
      int x = scrollTable.getAbsoluteLeft() + scrollTable.getOffsetWidth() - offsetWidth;
      int y = scrollTable.getAbsoluteTop();
      popup.setPopupPosition(x, y);
    }
  }

  private AbstractScrollTable scrollTable;
  private TableDefinition<?> tableDefinition;
 
  private int cellPadding;
  private int cellSpacing;

  private ResizePolicy resizePolicy;
  private SelectionPolicy selectionPolicy;

  private int columnCount;
  private ColumnWidthInfo[] columnWidth;
  private boolean[] columnVisible;

  private int availableWidth;
  private int dataWidth;
  
  private String cpId, csId, rpName, spName;
  private ColumnRef[] cRef;
  
  private BeePopupPanel popup;

  public ScrollTableConfig(AbstractScrollTable ast) {
    init(ast);
  }
  
  public void show() {
    Assert.isPositive(columnCount);

    BeeLayoutPanel lp = new BeeLayoutPanel();
    int xmrg = 8;
    int ymrg = 4;
    int x = xmrg;
    int y = ymrg;
    int w = 500;
    int z;
    
    lp.addLeftTop(new BeeLabel("Cell Padding"), x, y);
    cpId = lp.addLeftWidthTop(new Spinner(cellPadding, 0, 20), x + 100, 52, y);
    lp.addLeftTop(new BeeLabel("Cell Spacing"), x + 200, y);
    csId = lp.addLeftWidthTop(new Spinner(cellSpacing, 0, 20), x + 300, 52, y);
    y += 36;
   
    lp.addLeftTop(new BeeLabel("Grid Resize"), x, y);
    rpName = BeeUtils.createUniqueName("rp");
    lp.addLeftTop(new RadioGroup(rpName, resizePolicy, ResizePolicy.values()), x + 100, y);
    y += 30;
    
    lp.addLeftTop(new BeeLabel("Row Selection"), x, y);
    spName = BeeUtils.createUniqueName("sp");
    lp.addLeftTop(new RadioGroup(spName, selectionPolicy, SelectionPolicy.values()), x + 100, y);
    y += 30 + ymrg;
    
    lp.addLeftTop(new BeeLabel("Column Count"), x, y);
    lp.addLeftTop(new BeeLabel(columnCount), x + 100, y);
    lp.addLeftTop(new BeeLabel("Data Width  " + dataWidth), x + 200, y);
    lp.addLeftTop(new BeeLabel("Available Width  " + availableWidth), x + 320, y);
    y += 30;
    
    BeeLayoutPanel cp = new BeeLayoutPanel();
    int cx = xmrg;
    int cy = 0;
    
    cRef = new ColumnRef[columnCount];
    
    for (int i = 0; i < columnCount; i++) {
      cRef[i] = new ColumnRef();
      cRef[i].setShow(cp.addLeftTop(new BeeSimpleCheckBox(columnVisible[i]), cx, cy));
      cRef[i].setOrder(cp.addLeftWidthTop(new Spinner(i, 0, columnCount - 1), cx + 24, 48, cy));
      cp.addLeftWidthTop(new BeeLabel(scrollTable.getHeaderTable().getText(0, i)), cx + 80, 116, cy);

      cRef[i].setCurrW(cp.addLeftWidthTop(new Spinner(columnWidth[i].getCurrentWidth(), 1, 500), 
          cx + 200, 60, cy));
      cRef[i].setMinW(cp.addLeftWidthTop(new Spinner(columnWidth[i].getMinimumWidth(), 1, 500), 
          cx + 264, 60, cy));
      cRef[i].setPrefW(cp.addLeftWidthTop(new Spinner(columnWidth[i].getPreferredWidth(), 1, 500), 
          cx + 328, 60, cy));
      cRef[i].setMaxW(cp.addLeftWidthTop(new Spinner(columnWidth[i].getMaximumWidth(), -1, 500), 
          cx + 392, 60, cy));
      
      cy += 30;
    }
    
    DomUtils.setWidth(cp, w - 20);
    DomUtils.setHeight(cp, cy);
    
    z = BeeUtils.min(cy, 300);
    
    lp.add(cp, cy > 300);
    lp.setWidgetLeftRight(cp, 0, Unit.PX, 0, Unit.PX);
    lp.setWidgetTopHeight(cp, y, Unit.PX, z, Unit.PX);

    y += z + ymrg;
    
    x = 50;
    z = (w - x * 2) / 5;
    
    lp.addLeftWidthTop(new BeeButton("Apply", new ConfigCommand("apply")), x, z - 20, y);
    lp.addLeftWidthTop(new BeeButton("Fill", new ConfigCommand("fill")), x += z, z - 20, y);
    lp.addLeftWidthTop(new BeeButton("Redraw", new ConfigCommand("redraw")), x += z, z - 20, y);
    lp.addLeftWidthTop(new BeeButton("Reset", new ConfigCommand("reset")), x += z, z - 20, y);
    lp.addLeftWidthTop(new BeeButton("Cancel", new ConfigCommand("cancel")), x += z, z - 20, y);

    y += 32;
    
    DomUtils.setWidth(lp, w);
    DomUtils.setHeight(lp, y);
    
    popup = new BeePopupPanel();
    popup.setWidget(lp);
    popup.setStyleName(BeeStyle.CONFIG_PANEL);
    popup.setPopupPositionAndShow(new PopupPosition());
  }
  
  private void init(AbstractScrollTable ast) {
    Assert.notNull(ast);
    scrollTable = ast;
    
    cellPadding = ast.getCellPadding();
    cellSpacing = ast.getCellSpacing();

    resizePolicy = ast.getResizePolicy();
    selectionPolicy = ast.getDataTable().getSelectionPolicy();
    
    columnCount = ast.getDataTable().getColumnCount();
    availableWidth = ast.getAvailableWidth();
    dataWidth = ast.getDataTable().getElement().getScrollWidth();
    
    columnWidth = new ColumnWidthInfo[columnCount];
    columnVisible = new boolean[columnCount];
    
    if (ast instanceof HasTableDefinition<?>) {
      tableDefinition = ((HasTableDefinition<?>) ast).getTableDefinition();
    } else {
      tableDefinition = null;
    }
    
    for (int i = 0; i < columnCount; i++) {
      columnWidth[i] = new ColumnWidthInfo(ast.getMinimumColumnWidth(i),
          ast.getMaximumColumnWidth(i), ast.getPreferredColumnWidth(i), ast.getColumnWidth(i));
      if (tableDefinition == null) {
        columnVisible[i] = true;
      } else {
        columnVisible[i] = tableDefinition.isColumnVisible(i);
      }
    }
  }
  
  private boolean setupColumns() {
    boolean reload = false;
    boolean show;
    int order, currW, minW, prefW, maxW; 
    
    for (int i = 0; i < columnCount; i++) {
      show = DomUtils.isChecked(cRef[i].getShow());
      
      currW = DomUtils.getValueInt(cRef[i].getCurrW());
      minW = DomUtils.getValueInt(cRef[i].getMinW());
      prefW = DomUtils.getValueInt(cRef[i].getPrefW());
      maxW = DomUtils.getValueInt(cRef[i].getMaxW());
      
      if (show != columnVisible[i]) {
        BeeKeeper.getLog().info("col", i, show ? "visible" : "hidden");
        if (tableDefinition != null) {
          tableDefinition.setColumnVisible(i, show);
          reload = true;
        }
      }
      
      if (currW > 0 && currW != columnWidth[i].getCurrentWidth()) {
        BeeKeeper.getLog().info("col", i, "width", currW);
        scrollTable.setColumnWidth(i, currW);
      }
      
      if (minW > 0 && minW != columnWidth[i].getMinimumWidth()) {
        BeeKeeper.getLog().info("col", i, "min", minW);
        if (tableDefinition != null) {
          tableDefinition.getColumnDefinition(i).setMinimumColumnWidth(minW);
        }
      }
      if (prefW > 0 && prefW != columnWidth[i].getPreferredWidth()) {
        BeeKeeper.getLog().info("col", i, "pref", prefW);
        if (tableDefinition != null) {
          tableDefinition.getColumnDefinition(i).setPreferredColumnWidth(prefW);
        }
      }
      if (maxW != 0 && maxW != columnWidth[i].getMaximumWidth()) {
        BeeKeeper.getLog().info("col", i, "max", maxW);
        if (tableDefinition != null) {
          tableDefinition.getColumnDefinition(i).setMaximumColumnWidth(maxW);
        }
      }
    }
    
    for (int i = 0; i < columnCount; i++) {
      order = DomUtils.getValueInt(cRef[i].getOrder());
      
      if (order != i && order >= 0 && order < columnCount) {
        BeeKeeper.getLog().info("col", i, "order", order);
        if (tableDefinition != null) {
          Collections.swap(tableDefinition.getColumnDefs(), i, order);
          reload = true;
        }
      }
    }
    
    return reload;
  }
  
  private boolean setupGrid() {
    boolean reload = false;
    
    int padding = DomUtils.getValueInt(cpId);
    int spacing = DomUtils.getValueInt(csId);
    
    int resize = RadioGroup.getValue(rpName);
    int select = RadioGroup.getValue(spName);
    
    if (padding != cellPadding) {
      BeeKeeper.getLog().info("padding", padding, "old", cellPadding);
      if (padding >= 0) {
        scrollTable.setCellPadding(padding);
      }
    }
    if (spacing != cellSpacing) {
      BeeKeeper.getLog().info("spacing", spacing, "old", cellSpacing);
      if (spacing >= 0) {
        scrollTable.setCellSpacing(spacing);
      }
    }
    
    if (resize != resizePolicy.ordinal() && resize >= 0 && resize < ResizePolicy.values().length) {
      ResizePolicy rp = ResizePolicy.values()[resize];
      BeeKeeper.getLog().info("resize", rp.name(), "old", resizePolicy.name());
      scrollTable.setResizePolicy(rp);
    }
    if (select != selectionPolicy.ordinal() && select >= 0 && select < SelectionPolicy.values().length) {
      SelectionPolicy sp = SelectionPolicy.values()[select];
      BeeKeeper.getLog().info("select", sp.name(), "old", selectionPolicy.name());
      scrollTable.getDataTable().setSelectionPolicy(sp);
      reload = true;
    }
    
    return reload;
  }
  
}
