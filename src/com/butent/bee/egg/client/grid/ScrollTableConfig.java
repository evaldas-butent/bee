package com.butent.bee.egg.client.grid;

import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.dialog.BeePopupPanel;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.grid.ScrollTable.ResizePolicy;
import com.butent.bee.egg.client.grid.SelectionGrid.SelectionPolicy;
import com.butent.bee.egg.client.layout.Absolute;
import com.butent.bee.egg.client.layout.Scroll;
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
      
      if (reload) {
        scrollTable.reloadPage();
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

  private ScrollTable<?> scrollTable;
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

  public ScrollTableConfig(ScrollTable<?> st) {
    init(st);
  }
  
  public void show() {
    Assert.isPositive(columnCount);

    Absolute panel = new Absolute();
    int xmrg = 8;
    int ymrg = 4;
    int x = xmrg;
    int y = ymrg;
    int w = 500;
    int z;
    
    panel.add(new BeeLabel("Cell Padding"), x, y);
    cpId = panel.append(new Spinner(cellPadding, 0, 20), x + 100, y, 52);
    panel.add(new BeeLabel("Cell Spacing"), x + 200, y);
    csId = panel.append(new Spinner(cellSpacing, 0, 20), x + 300, y, 52);
    y += 36;
   
    panel.add(new BeeLabel("Grid Resize"), x, y);
    rpName = BeeUtils.createUniqueName("rp");
    panel.add(new RadioGroup(rpName, resizePolicy, ResizePolicy.values()), x + 100, y);
    y += 30;
    
    panel.add(new BeeLabel("Row Selection"), x, y);
    spName = BeeUtils.createUniqueName("sp");
    panel.add(new RadioGroup(spName, selectionPolicy, SelectionPolicy.values()), x + 100, y);
    y += 30 + ymrg;
    
    panel.add(new BeeLabel("Column Count"), x, y);
    panel.add(new BeeLabel(columnCount), x + 100, y);
    panel.add(new BeeLabel("Data Width  " + dataWidth), x + 200, y);
    panel.add(new BeeLabel("Available Width  " + availableWidth), x + 320, y);
    y += 30;
    
    Absolute cp = new Absolute();
    int cx = xmrg;
    int cy = 0;
    
    cRef = new ColumnRef[columnCount];
    
    for (int i = 0; i < columnCount; i++) {
      cRef[i] = new ColumnRef();
      cRef[i].setShow(cp.append(new BeeSimpleCheckBox(columnVisible[i]), cx, cy));
      cRef[i].setOrder(cp.append(new Spinner(i, 0, columnCount - 1), cx + 24, cy, 48));
      cp.append(new BeeLabel(scrollTable.getHeaderTable().getText(0, i)), cx + 80, cy, 116);

      cRef[i].setCurrW(cp.append(new Spinner(columnWidth[i].getCurrentWidth(), 1, 500),
          cx + 200, cy, 60));
      cRef[i].setMinW(cp.append(new Spinner(columnWidth[i].getMinimumWidth(), 1, 500), 
          cx + 264, cy, 60));
      cRef[i].setPrefW(cp.append(new Spinner(columnWidth[i].getPreferredWidth(), 1, 500), 
          cx + 328, cy, 60));
      cRef[i].setMaxW(cp.append(new Spinner(columnWidth[i].getMaximumWidth(), -1, 500), 
          cx + 392, cy, 60));
      
      cy += 30;
    }
    
    DomUtils.setWidth(cp, w - 20);
    DomUtils.setHeight(cp, cy);
    
    z = 300;
    if (cy > z) {
      Scroll sp = new Scroll(cp);
      panel.append(sp, 0, y, w);
      DomUtils.setHeight(sp, z);
      y += z;
    } else {
      panel.append(cp, 0, y, w);
      y += cy;
    }
    y += ymrg;
    
    x = 50;
    z = (w - x * 2) / 5;
    
    panel.append(new BeeButton("Apply", new ConfigCommand("apply")), x, y, z - 20);
    panel.append(new BeeButton("Fill", new ConfigCommand("fill")), x += z, y, z - 20);
    panel.append(new BeeButton("Redraw", new ConfigCommand("redraw")), x += z, y, z - 20);
    panel.append(new BeeButton("Reset", new ConfigCommand("reset")), x += z, y, z - 20);
    panel.append(new BeeButton("Cancel", new ConfigCommand("cancel")), x += z, y, z - 20);

    y += 32;
    
    DomUtils.setWidth(panel, w);
    DomUtils.setHeight(panel, y);
    
    popup = new BeePopupPanel();
    popup.setWidget(panel);
    popup.setStyleName(BeeStyle.CONFIG_PANEL);
    popup.setPopupPositionAndShow(new PopupPosition());
  }
  
  private void init(ScrollTable<?> st) {
    Assert.notNull(st);
    scrollTable = st;
    
    cellPadding = st.getCellPadding();
    cellSpacing = st.getCellSpacing();

    resizePolicy = st.getResizePolicy();
    selectionPolicy = st.getDataTable().getSelectionPolicy();
    
    columnCount = st.getDataTable().getColumnCount();
    availableWidth = st.getAvailableWidth();
    dataWidth = st.getDataTable().getElement().getScrollWidth();
    
    columnWidth = new ColumnWidthInfo[columnCount];
    columnVisible = new boolean[columnCount];
    
    tableDefinition = st.getTableDefinition();
    
    for (int i = 0; i < columnCount; i++) {
      columnWidth[i] = new ColumnWidthInfo(st.getMinimumColumnWidth(i),
          st.getMaximumColumnWidth(i), st.getPreferredColumnWidth(i), st.getColumnWidth(i));
      columnVisible[i] = tableDefinition.isColumnVisible(i);
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
