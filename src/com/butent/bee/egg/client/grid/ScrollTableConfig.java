package com.butent.bee.egg.client.grid;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.composite.SpinnerListener;
import com.butent.bee.egg.client.composite.ValueSpinner;
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
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Collections;

public class ScrollTableConfig {
  private class ColumnRef {
    private String show;
    private String order;
    private String curW;
    private String minW;
    private String prefW;
    private String maxW;

    public String getCurW() {
      return curW;
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
    public void setCurW(String curW) {
      this.curW = curW;
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
      if (!validate()) {
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
      
      x = BeeUtils.fitStart(x, offsetWidth, DomUtils.getClientWidth() - 10, 0);
      y = BeeUtils.fitStart(y, offsetHeight, DomUtils.getClientHeight() - 10, 0);
      popup.setPopupPosition(x, y);
    }
  }
  
  private class WidthListener implements SpinnerListener {
    private int src;
    private String dst;

    private WidthListener(int src, String dst) {
      this.src = src;
      this.dst = dst;
    }

    @Override
    public void onSpinning(long value) {
      int tot = 0, v;
      String id = null;
      
      for (int i = 0; i < columnCount; i++) {
        if (!isVisible(i)) {
          continue;
        }
        switch (src) {
          case 0 :
            id = cRef[i].getCurW();
            break;
          case 1 :
            id = cRef[i].getMinW();
            break;
          case 2 :
            id = cRef[i].getPrefW();
            break;
          case 3 :
            id = cRef[i].getMaxW();
            break;
          default:
            Assert.untouchable();
        }
        v = DomUtils.getValueInt(id);
        if (v > 0) {
          tot += v;
        }
      }
      DomUtils.setText(dst, BeeUtils.toString(tot));
    }
  }
  
  private class VisibilityHandler implements ClickHandler {
    public void onClick(ClickEvent event) {
      if (event.getSource() instanceof BeeSimpleCheckBox) {
        if (((BeeSimpleCheckBox) event.getSource()).isChecked()) {
          visibleCount++;
        } else {
          visibleCount--;
        }
        DomUtils.setText(cntId, getCounts());
      }
      totalWidths();
    }
  }

  private ScrollTable<?> scrollTable;
  private TableDefinition<?> tableDefinition;
 
  private int cellPadding;
  private int cellSpacing;

  private ResizePolicy resizePolicy;
  private SelectionPolicy selectionPolicy;

  private int columnCount, visibleCount;
  private ColumnWidthInfo[] columnWidth;
  private boolean[] columnVisible;

  private int availableWidth;
  private int dataWidth;
  
  private String cpId, csId, rpName, spName;
  private String cntId, curTotId, minTotId, prefTotId, maxTotId;
  private ColumnRef[] cRef;

  private WidthListener curWidthListener, minWidthListener, prefWidthListener, maxWidthListener;
  
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
    cpId = panel.append(new ValueSpinner(cellPadding, 0, 20), x + 100, y, 52);
    panel.add(new BeeLabel("Cell Spacing"), x + 180, y);
    csId = panel.append(new ValueSpinner(cellSpacing, 0, 20), x + 280, y, 52);
    panel.append(new BeeLabel(BeeUtils.concat(1, "Width:", dataWidth,
        BeeUtils.parenthesize(availableWidth)), HasHorizontalAlignment.ALIGN_RIGHT),
        w - 150 - xmrg, y, 150);
    y += 36;
    
    panel.add(new BeeLabel("Grid Resize"), x, y);
    rpName = BeeUtils.createUniqueName("rp");
    panel.add(new RadioGroup(rpName, resizePolicy, ResizePolicy.values()), x + 100, y);
    y += 30;
    
    panel.add(new BeeLabel("Row Selection"), x, y);
    spName = BeeUtils.createUniqueName("sp");
    panel.add(new RadioGroup(spName, selectionPolicy, SelectionPolicy.values()), x + 100, y);
    y += 30 + ymrg;

    int xCur = x + 200;
    int xMin = xCur + 64;
    int xPref = xMin + 64;
    int xMax = xPref + 64;
    int ww = 44;
    
    panel.add(new BeeLabel("Columns:"), x, y);
    cntId = panel.append(new BeeLabel(getCounts()), x + 60, y);
    curTotId = panel.append(new BeeLabel(HasHorizontalAlignment.ALIGN_RIGHT), xCur, y, ww);
    minTotId = panel.append(new BeeLabel(HasHorizontalAlignment.ALIGN_RIGHT), xMin, y, ww);
    prefTotId = panel.append(new BeeLabel(HasHorizontalAlignment.ALIGN_RIGHT), xPref, y, ww);
    maxTotId = panel.append(new BeeLabel(HasHorizontalAlignment.ALIGN_RIGHT), xMax, y, ww);
    y += 30;
    
    Absolute cp = new Absolute();
    int cx = xmrg;
    int cy = 0;
    ww = 60;
    
    cRef = new ColumnRef[columnCount];
    ValueSpinner vs;
    curWidthListener = new WidthListener(0, curTotId);
    minWidthListener = new WidthListener(1, minTotId);
    prefWidthListener = new WidthListener(2, prefTotId);
    maxWidthListener = new WidthListener(3, maxTotId);
    
    VisibilityHandler vh = new VisibilityHandler();
    
    for (int i = 0; i < columnCount; i++) {
      cRef[i] = new ColumnRef();
      BeeSimpleCheckBox scb = new BeeSimpleCheckBox(columnVisible[i]);
      scb.addClickHandler(vh);
      cRef[i].setShow(cp.append(scb, cx, cy));

      cRef[i].setOrder(cp.append(new ValueSpinner(i, 0, columnCount - 1), cx + 24, cy, 48));
      cp.append(new BeeLabel(scrollTable.getColumnCaption(i, false)), cx + 80, cy, 116);

      vs = new ValueSpinner(columnWidth[i].getCurrentWidth(), 1, 500);
      vs.addSpinnerListener(curWidthListener);
      cRef[i].setCurW(cp.append(vs, xCur, cy, ww));

      vs = new ValueSpinner(columnWidth[i].getMinimumWidth(), 1, 500);
      vs.addSpinnerListener(minWidthListener);
      cRef[i].setMinW(cp.append(vs, xMin, cy, ww));

      vs = new ValueSpinner(columnWidth[i].getPreferredWidth(), 1, 500);
      vs.addSpinnerListener(prefWidthListener);
      cRef[i].setPrefW(cp.append(vs, xPref, cy, ww));

      vs = new ValueSpinner(columnWidth[i].getMaximumWidth(), -1, 500);
      vs.addSpinnerListener(maxWidthListener);
      cRef[i].setMaxW(cp.append(vs, xMax, cy, ww));

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
    
    totalWidths();
  }
  
  private String getCounts() {
    if (visibleCount < columnCount) {
      return BeeUtils.concat(1, visibleCount, BeeConst.CHAR_PLUS,
          columnCount - visibleCount, BeeConst.CHAR_EQ, columnCount);
    }
    return BeeUtils.toString(columnCount);
  }
  
  private void init(ScrollTable<?> st) {
    Assert.notNull(st);
    scrollTable = st;
    tableDefinition = st.getTableDefinition();
    
    cellPadding = st.getCellPadding();
    cellSpacing = st.getCellSpacing();

    resizePolicy = st.getResizePolicy();
    selectionPolicy = st.getDataTable().getSelectionPolicy();
    
    columnCount = tableDefinition.getColumnDefinitionCount();
    availableWidth = st.getAvailableWidth();
    dataWidth = st.getDataTable().getElement().getScrollWidth();
    
    columnWidth = new ColumnWidthInfo[columnCount];
    columnVisible = new boolean[columnCount];
    
    visibleCount = 0;
    for (int i = 0; i < columnCount; i++) {
      columnWidth[i] = st.getColumnWidthInfo(i, false);
      columnVisible[i] = tableDefinition.isColumnVisible(i);
      if (columnVisible[i]) {
        visibleCount++;
      }
    }
  }
  
  private boolean isVisible(int column) {
    return DomUtils.isChecked(cRef[column].getShow());
  }
  
  private boolean setupColumns() {
    boolean reload = false;
    boolean show;
    int order, curW, minW, prefW, maxW; 
    
    for (int i = 0; i < columnCount; i++) {
      show = isVisible(i);
      
      curW = DomUtils.getValueInt(cRef[i].getCurW());
      minW = DomUtils.getValueInt(cRef[i].getMinW());
      prefW = DomUtils.getValueInt(cRef[i].getPrefW());
      maxW = DomUtils.getValueInt(cRef[i].getMaxW());
      
      if (show != columnVisible[i]) {
        tableDefinition.setColumnVisible(i, show);
        reload = true;
      }
      
      if (show && curW > 0 && curW != columnWidth[i].getCurrentWidth()) {
        scrollTable.setColumnWidth(i, curW);
      }
      
      if (minW > 0 && minW != columnWidth[i].getMinimumWidth()) {
        tableDefinition.getColumnDefinition(i).setMinimumColumnWidth(minW);
      }
      if (prefW > 0 && prefW != columnWidth[i].getPreferredWidth()) {
        tableDefinition.getColumnDefinition(i).setPreferredColumnWidth(prefW);
      }
      if (maxW != 0 && maxW != columnWidth[i].getMaximumWidth()) {
        tableDefinition.getColumnDefinition(i).setMaximumColumnWidth(maxW);
      }
    }
    
    for (int i = 0; i < columnCount; i++) {
      order = DomUtils.getValueInt(cRef[i].getOrder());
      
      if (order != i && order >= 0 && order < columnCount) {
        Collections.swap(tableDefinition.getColumnDefs(), i, order);
        reload = true;
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
    
    if (padding != cellPadding && padding >= 0) {
      scrollTable.setCellPadding(padding);
    }
    if (spacing != cellSpacing && spacing >= 0) {
      scrollTable.setCellSpacing(spacing);
    }
    
    if (resize != resizePolicy.ordinal() && resize >= 0 && resize < ResizePolicy.values().length) {
      ResizePolicy rp = ResizePolicy.values()[resize];
      scrollTable.setResizePolicy(rp);
    }
    if (select != selectionPolicy.ordinal() && select >= 0 && select < SelectionPolicy.values().length) {
      SelectionPolicy sp = SelectionPolicy.values()[select];
      scrollTable.getDataTable().setSelectionPolicy(sp);
      scrollTable.setHeadersObsolete(true);
      reload = true;
    }
    
    return reload;
  }
  
  private void totalWidths() {
    curWidthListener.onSpinning(0);
    minWidthListener.onSpinning(0);
    prefWidthListener.onSpinning(0);
    maxWidthListener.onSpinning(0);
  }
  
  private boolean validate() {
    boolean ok = visibleCount > 0;
    if (!ok) {
      BeeGlobal.showError("Invisible Pink Unicorns are beings of great spiritual power");
    }
    
    return ok;
  }
}
