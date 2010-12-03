package com.butent.bee.egg.client.grid;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.composite.SpinnerListener;
import com.butent.bee.egg.client.composite.ValueSpinner;
import com.butent.bee.egg.client.dialog.BeePopupPanel;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.DndEvent;
import com.butent.bee.egg.client.event.EventUtils;
import com.butent.bee.egg.client.event.HasAllDndHandlers;
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

import java.util.Arrays;

public class ScrollTableConfig {
  private class ColumnRef {
    private String show;
    private String order;
    private String cap;
    private String curW;
    private String minW;
    private String prefW;
    private String maxW;
    
    private int row;
    
    private ColumnRef(int row) {
      this.row = row;
    }
    
    public String getCap() {
      return cap;
    }
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
    public int getRow() {
      return row;
    }
    public String getShow() {
      return show;
    }

    public void setCap(String cap) {
      this.cap = cap;
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
    public void setRow(int row) {
      this.row = row;
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
      
      boolean reload = setupColumns();
      reload |= setupGrid();
      
      if (reload) {
        scrollTable.setHeadersObsolete(true);
        scrollTable.reloadPage();
      }

      if (BeeUtils.same(action, "fill")) {
        scrollTable.fillWidth();
      } else if (BeeUtils.same(action, "redraw")) {
        scrollTable.scheduleRedraw();
      } else if (BeeUtils.same(action, "reset")) {
        scrollTable.resetColumnWidths();
      }
      
      popup.hide();
    }
  }

  private class DndHandler implements HasAllDndHandlers {
    private String sourceId = null;
    private String targetId = null;
    private Scroll panel = null;
    private int top = -1;
    private int height = -1;
    private int bottom = -1;
    private int maxScroll = -1;
    
    public boolean onDrag(DndEvent event) {
      if (panel == null || maxScroll <= 0) {
        return true;
      }
      int y = event.getClientY();
      int pos = panel.getScrollPosition();

      if (y < top && pos > 0) {
        panel.setScrollPosition(Math.max(pos + y - top, 0));
      } else if (y > bottom && pos < maxScroll) {
        panel.setScrollPosition(Math.min(pos + y - bottom, maxScroll));
      }
      return true;
    }

    public boolean onDragEnd(DndEvent event) {
      event.getElement().removeClassName(BeeStyle.DND_SOURCE);
      if (!BeeUtils.isEmpty(sourceId) && !BeeUtils.isEmpty(targetId)
          && !BeeUtils.same(sourceId, targetId)) {
        moveColumn(sourceId, targetId);
      }
      return true;
    }

    public boolean onDragEnter(DndEvent event) {
      Element elem = event.getElement();
      if (isTarget(elem)) {
        elem.addClassName(BeeStyle.DND_OVER);
      }
      return true;
    }

    public boolean onDragLeave(DndEvent event) {
      Element elem = event.getElement();
      if (isTarget(elem)) {
        elem.removeClassName(BeeStyle.DND_OVER);
      }
      return true;
    }

    public boolean onDragOver(DndEvent event) {
      event.setDropEffect(DndEvent.EFFECT_MOVE);
      event.preventDefault();
      return false;
    }

    public boolean onDragStart(DndEvent event) {
      Element elem = event.getElement();
      sourceId = elem.getId();
      targetId = null;
      
      if (panel != null) {
        top = panel.getAbsoluteTop();
        height = panel.getElement().getClientHeight();
        bottom = top + height;
        maxScroll = panel.getElement().getScrollHeight() - height;
      }
      
      elem.addClassName(BeeStyle.DND_SOURCE);
      event.setData(sourceId);
      event.setEffectAllowed(DndEvent.EFFECT_MOVE);

      return true;
    }

    public boolean onDrop(DndEvent event) {
      Element elem = event.getElement();
      event.stopPropagation();

      if (isTarget(elem)) {
        elem.removeClassName(BeeStyle.DND_OVER);
        targetId = elem.getId();
      }
      return false;
    }

    private boolean isTarget(Element elem) {
      if (elem == null) {
        return false;
      }

      String id = elem.getId();
      return !BeeUtils.isEmpty(id) && !BeeUtils.same(id, sourceId);
    }
    
    private void setPanel(Scroll panel) {
      this.panel = panel;
    }
  }
  
  private class PopupCloseHandler implements CloseHandler<PopupPanel> {
    public void onClose(CloseEvent<PopupPanel> event) {
      EventUtils.removeDndHandler(dndHandler);
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
  private DndHandler dndHandler = new DndHandler();
  
  private BeePopupPanel popup;
  
  private int colInfoTop = 0;
  private int colInfoHeight = 30;

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
    int cy = colInfoTop;
    ww = 60;
    
    cRef = new ColumnRef[columnCount];
    ValueSpinner vs;
    curWidthListener = new WidthListener(0, curTotId);
    minWidthListener = new WidthListener(1, minTotId);
    prefWidthListener = new WidthListener(2, prefTotId);
    maxWidthListener = new WidthListener(3, maxTotId);
    
    VisibilityHandler vh = new VisibilityHandler();
    
    for (int i = 0; i < columnCount; i++) {
      cRef[i] = new ColumnRef(i);
      BeeSimpleCheckBox scb = new BeeSimpleCheckBox(columnVisible[i]);
      scb.addClickHandler(vh);
      cRef[i].setShow(cp.append(scb, cx, cy));

      cRef[i].setOrder(cp.append(new ValueSpinner(i, 0, columnCount - 1), cx + 24, cy, 48));

      BeeLabel cap = new BeeLabel(scrollTable.getColumnCaption(i, false));
      EventUtils.makeDndSource(cap.getElement(), dndHandler);
      EventUtils.makeDndTarget(cap.getElement(), dndHandler);
      cRef[i].setCap(cp.append(cap, cx + 80, cy, 116));

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

      cy += colInfoHeight;
    }
    
    DomUtils.setWidth(cp, w - 20);
    DomUtils.setHeight(cp, cy);
    
    z = colInfoTop + colInfoHeight * 10;
    if (cy > z) {
      Scroll sp = new Scroll(cp);
      panel.append(sp, 0, y, w);
      DomUtils.setHeight(sp, z);
      y += z;
      dndHandler.setPanel(sp);
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
    
    popup = new BeePopupPanel(true, true);
    popup.setWidget(panel);
    popup.setStyleName(BeeStyle.CONFIG_PANEL);
    popup.enableGlass();
    
    popup.addCloseHandler(new PopupCloseHandler());
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
  
  private int getTop(int row) {
    return colInfoTop + colInfoHeight * row;
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
  
  private void moveColumn(String srcId, String dstId) {
    Assert.notEmpty(srcId);
    Assert.notEmpty(dstId);
    int fr = -1;
    int to = -1;
    
    for (int i = 0; i < columnCount; i++) {
      String capId = cRef[i].getCap();
      if (BeeUtils.same(capId, srcId)) {
        fr = cRef[i].getRow();
      } else if (BeeUtils.same(capId, dstId)) {
        to = cRef[i].getRow();
      }
    }
    Assert.betweenExclusive(fr, 0, columnCount);
    Assert.betweenExclusive(to, 0, columnCount);
    Assert.isTrue(fr != to);
    
    int min = Math.min(fr, to);
    int max = Math.max(fr, to);
    int oldRow, newRow;

    for (int i = 0; i < columnCount; i++) {
      oldRow = cRef[i].getRow();
      if (oldRow < min || oldRow > max) {
        continue;
      }
      
      newRow = (oldRow == fr) ? to : (fr < to) ? oldRow - 1 : oldRow + 1;
      setRowTop(i, getTop(newRow));
      cRef[i].setRow(newRow);
      
      Widget widget = DomUtils.getWidget(popup, cRef[i].getOrder());
      if (widget instanceof ValueSpinner) {
        ((ValueSpinner) widget).updateValue(newRow);
      }
    }
  }
  
  private boolean orderColumns() {
    boolean order = false;
    if (columnCount <= 1) {
      return order;
    }
    int[] arr = new int[columnCount];
    Arrays.fill(arr, -1);
    int z;

    for (int i = 0; i < columnCount; i++) {
      z = DomUtils.getValueInt(cRef[i].getOrder());
      if (z >= 0 && z < columnCount && arr[z] < 0) {
        arr[z] = i;
      }
    }
    
    for (int i = 0; i < columnCount; i++) {
      if (BeeUtils.contains(i, arr)) {
        continue;
      }

      z = BeeUtils.limit(DomUtils.getValueInt(cRef[i].getOrder()), 0, columnCount - 1);
      for (int j = 0; j < columnCount; j++) {
        if (arr[z] < 0) {
          arr[z] = i;
          break;
        }
        z++;
        if (z >= columnCount) {
          z = 0;
        }
      }
    }
    
    for (int i = 0; i < columnCount; i++) {
      if (arr[i] != i) {
        order = true;
        break;
      }
    }
    if (!order) {
      return order;
    }
    
    for (int i = 0; i < columnCount; i++) {
      tableDefinition.getColumnDefinition(arr[i]).setColumnOrder(i);
    }
    tableDefinition.orderColumnDefs();
    
    return order;
  }

  private void setRowTop(int row, int top) {
    BeeKeeper.getStyle().setTop(cRef[row].getShow(), top); 
    BeeKeeper.getStyle().setTop(cRef[row].getOrder(), top); 
    BeeKeeper.getStyle().setTop(cRef[row].getCap(), top); 
    BeeKeeper.getStyle().setTop(cRef[row].getCurW(), top); 
    BeeKeeper.getStyle().setTop(cRef[row].getMinW(), top); 
    BeeKeeper.getStyle().setTop(cRef[row].getPrefW(), top); 
    BeeKeeper.getStyle().setTop(cRef[row].getMaxW(), top); 
  }
  
  private boolean setupColumns() {
    boolean reload = false;
    boolean show;
    int curW, minW, prefW, maxW; 
    
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
    
    if (orderColumns()) {
      reload = true;
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
      reload = true;
    }
    if (spacing != cellSpacing && spacing >= 0) {
      scrollTable.setCellSpacing(spacing);
      reload = true;
    }
    
    if (resize != resizePolicy.ordinal() 
        && resize >= 0 && resize < ResizePolicy.values().length) {
      ResizePolicy rp = ResizePolicy.values()[resize];
      scrollTable.setResizePolicy(rp);
    }
    if (select != selectionPolicy.ordinal() 
        && select >= 0 && select < SelectionPolicy.values().length) {
      SelectionPolicy sp = SelectionPolicy.values()[select];
      scrollTable.getDataTable().setSelectionPolicy(sp);
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
