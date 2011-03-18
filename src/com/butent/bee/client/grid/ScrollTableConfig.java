package com.butent.bee.client.grid;

import com.google.common.primitives.Ints;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.BeeStyle;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.composite.SpinnerListener;
import com.butent.bee.client.composite.ValueSpinner;
import com.butent.bee.client.dialog.BeePopupPanel;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.DndEvent;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.HasAllDndHandlers;
import com.butent.bee.client.grid.ScrollTable.ResizePolicy;
import com.butent.bee.client.grid.SelectionGrid.SelectionPolicy;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.Scroll;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeSimpleCheckBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;

public class ScrollTableConfig {
  private class ColumnRef {
    private String show;
    private String order;
    private String cap;
    private String datW;
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

    public String getDatW() {
      return datW;
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

    public void setDatW(String datW) {
      this.datW = datW;
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
    private Action action;

    public ConfigCommand(Action action) {
      super();
      this.action = action;
    }

    @Override
    public void execute() {
      if (action == Action.CANCEL) {
        popup.hide();
        return;
      }
      if (!validate()) {
        return;
      }

      boolean reload = false;
      boolean fill = resizePolicy.isFixedWidth() && resizePolicy.isSacrificial();
      boolean updInp = false;
      boolean updRp = false;

      ResizePolicy rp = null;
      SelectionPolicy sp = null;

      int z = DomUtils.getValueInt(cpId);
      if (z != cellPadding && z >= 0) {
        scrollTable.setCellPadding(z);
        reload = true;
      }
      z = DomUtils.getValueInt(csId);
      if (z != cellSpacing && z >= 0) {
        scrollTable.setCellSpacing(z);
        reload = true;
      }

      z = RadioGroup.getValue(rpName);
      if (z != resizePolicy.ordinal() && BeeUtils.isOrdinal(ResizePolicy.class, z)) {
        rp = ResizePolicy.values()[z];
        scrollTable.setResizePolicy(rp);
        fill = rp.isFixedWidth() && rp.isSacrificial();
        updRp = true;
      }

      z = RadioGroup.getValue(spName);
      if (z != selectionPolicy.ordinal() && BeeUtils.isOrdinal(SelectionPolicy.class, z)) {
        sp = SelectionPolicy.values()[z];
        scrollTable.getDataTable().setSelectionPolicy(sp);
        if (selectionPolicy.hasInputColumn() || sp.hasInputColumn()) {
          reload = true;
          updInp = true;
        }
      }

      updCur = false;
      updMin = false;
      updPref = false;
      updMax = false;

      reload |= setupColumns();
      if (reload) {
        scrollTable.setHeadersObsolete(true);
        scrollTable.reload();
      }

      switch (action) {
        case APPLY:
          if (updMin || updPref || updMax || updInp) {
            scrollTable.recalculateColumnWidths(updCur, fill);
          } else if ((updCur || updRp) && fill) {
            scrollTable.recalculateColumnWidths(updCur, fill);
          }
          break;
        case DISTR_CUR:
          scrollTable.recalculateColumnWidths(true, fill);
          break;
        case DISTR_PREF:
          scrollTable.recalculateColumnWidths(false, fill);
          break;
        case FILL_CUR:
          scrollTable.recalculateColumnWidths(true, true);
          break;
        case FILL_PREF:
          scrollTable.recalculateColumnWidths(false, true);
          break;
        default:
          Assert.untouchable();
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
        if (((BeeSimpleCheckBox) event.getSource()).getValue()) {
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
          case 0:
            id = cRef[i].getCurW();
            break;
          case 1:
            id = cRef[i].getMinW();
            break;
          case 2:
            id = cRef[i].getPrefW();
            break;
          case 3:
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

  private class CopyHandler implements DoubleClickHandler {
    public void onDoubleClick(DoubleClickEvent event) {
      String srcId = EventUtils.getTargetId(event.getNativeEvent().getCurrentEventTarget());
      if (BeeUtils.isEmpty(srcId)) {
        srcId = EventUtils.getTargetId(event.getNativeEvent().getEventTarget());
        if (BeeUtils.isEmpty(srcId)) {
          return;
        }
      }
      boolean hasModifier = EventUtils.hasModifierKey(event.getNativeEvent());
      event.preventDefault();
      event.stopPropagation();

      final char modeDat = 'd';
      final char modeCur = 'c';
      final char modePref = 'p';
      final char modeUndef = '?';

      char srcMd = modeUndef;
      char dstMd = modeUndef;
      boolean all = false;

      if (BeeUtils.same(srcId, datTotId)) {
        all = true;
        srcMd = modeDat;
        dstMd = hasModifier ? modePref : modeCur;
      } else if (BeeUtils.same(srcId, curTotId)) {
        all = true;
        srcMd = modeCur;
        dstMd = modePref;
      } else if (BeeUtils.same(srcId, prefTotId)) {
        all = true;
        srcMd = modePref;
        dstMd = modeCur;
      }

      int value = 0, min, max;
      String dstId;
      Widget dstWidget;
      int cnt = 0;

      for (int i = 0; i < columnCount; i++) {
        if (srcMd == modeUndef) {
          if (BeeUtils.same(srcId, cRef[i].getDatW())) {
            srcMd = modeDat;
            dstMd = hasModifier ? modePref : modeCur;
          } else if (BeeUtils.same(srcId, cRef[i].getCurW())) {
            srcMd = modeCur;
            dstMd = modePref;
          } else if (BeeUtils.same(srcId, cRef[i].getPrefW())) {
            srcMd = modePref;
            dstMd = modeCur;
          } else {
            continue;
          }
        }

        if (!isVisible(i)) {
          if (all) {
            continue;
          } else {
            break;
          }
        }

        switch (srcMd) {
          case modeDat:
            value = columnWidth[i].getDataWidth();
            break;
          case modeCur:
            value = DomUtils.getValueInt(cRef[i].getCurW());
            break;
          case modePref:
            value = DomUtils.getValueInt(cRef[i].getPrefW());
            break;
          default:
            Assert.untouchable();
        }

        if (value > 0) {
          min = DomUtils.getValueInt(cRef[i].getMinW());
          max = DomUtils.getValueInt(cRef[i].getMaxW());
          value = BeeUtils.limit(value, min, max);

          dstId = (dstMd == modeCur) ? cRef[i].getCurW() : cRef[i].getPrefW();
          dstWidget = DomUtils.getWidget(popup, dstId);
          if (dstWidget instanceof ValueSpinner) {
            if (((ValueSpinner) dstWidget).getValue() != value) {
              ((ValueSpinner) dstWidget).updateValue(value);
              cnt++;
            }
          }
        }
        if (!all) {
          break;
        }
      }

      if (cnt > 0) {
        switch (dstMd) {
          case modeCur:
            curWidthListener.onSpinning(0);
            break;
          case modePref:
            prefWidthListener.onSpinning(0);
            break;
          default:
            Assert.untouchable();
        }
      }
    }
  }

  private enum Action {
    APPLY, DISTR_CUR, DISTR_PREF, FILL_CUR, FILL_PREF, CANCEL
  }

  private ScrollTable scrollTable;
  private TableDefinition tableDefinition;

  private int cellPadding;
  private int cellSpacing;

  private ResizePolicy resizePolicy;
  private SelectionPolicy selectionPolicy;

  private int columnCount, visibleCount;
  private ColumnWidth[] columnWidth;
  private boolean[] columnVisible;

  private int availableWidth;
  private int dataWidth;

  private String cpId, csId, rpName, spName;
  private String cntId, datTotId, curTotId, minTotId, prefTotId, maxTotId;
  private ColumnRef[] cRef;

  private WidthListener curWidthListener, minWidthListener, prefWidthListener, maxWidthListener;
  private DndHandler dndHandler = new DndHandler();
  private CopyHandler copyHandler = new CopyHandler();

  private BeePopupPanel popup;

  private int colInfoTop = 0;
  private int colInfoHeight = 30;

  private boolean updCur, updMin, updPref, updMax;

  public ScrollTableConfig(ScrollTable st) {
    init(st);
  }

  public void show() {
    Assert.isPositive(columnCount);

    Absolute panel = new Absolute();
    int xMrg = 8;
    int yMrg = 4;
    int x = xMrg;
    int y = yMrg;
    int w = 500;
    int z;

    panel.add(new BeeLabel("Cell Padding"), x, y);
    cpId = panel.append(new ValueSpinner(cellPadding, 0, 20), x + 100, y, 52);
    panel.add(new BeeLabel("Cell Spacing"), x + 180, y);
    csId = panel.append(new ValueSpinner(cellSpacing, 0, 20), x + 280, y, 52);
    panel.append(new BeeLabel(BeeUtils.concat(1, "Width:", dataWidth,
        BeeUtils.parenthesize(availableWidth)), HasHorizontalAlignment.ALIGN_RIGHT),
        w - 150 - xMrg, y, 150);
    y += 36;

    panel.add(new BeeLabel("Grid Resize"), x, y);
    rpName = BeeUtils.createUniqueName("rp");
    panel.add(new RadioGroup(rpName, resizePolicy, ResizePolicy.values()), x + 100, y);
    y += 30;

    panel.add(new BeeLabel("Row Selection"), x, y);
    spName = BeeUtils.createUniqueName("sp");
    panel.add(new RadioGroup(spName, selectionPolicy, SelectionPolicy.values()), x + 100, y);
    y += 30 + yMrg;

    int ww = 50;
    int xDat = w - ww * 5 - DomUtils.getScrollbarWidth() - xMrg;
    int xCur = xDat + ww;
    int xMin = xCur + ww;
    int xPref = xMin + ww;
    int xMax = xPref + ww;

    panel.add(new BeeLabel("Columns:"), x, y);
    cntId = panel.append(new BeeLabel(getCounts()), x + 60, y);

    int wMrg = 0;
    BeeLabel totLbl;
    totLbl = new BeeLabel(HasHorizontalAlignment.ALIGN_RIGHT);
    totLbl.addDoubleClickHandler(copyHandler);
    datTotId = panel.append(totLbl, xDat, y, ww - 10);

    totLbl = new BeeLabel(HasHorizontalAlignment.ALIGN_CENTER);
    totLbl.addDoubleClickHandler(copyHandler);
    curTotId = panel.append(totLbl, xCur, y, ww - wMrg);

    minTotId = panel.append(new BeeLabel(HasHorizontalAlignment.ALIGN_CENTER), xMin, y, ww - wMrg);

    totLbl = new BeeLabel(HasHorizontalAlignment.ALIGN_CENTER);
    totLbl.addDoubleClickHandler(copyHandler);
    prefTotId = panel.append(totLbl, xPref, y, ww - wMrg);

    maxTotId = panel.append(new BeeLabel(HasHorizontalAlignment.ALIGN_CENTER), xMax, y, ww - wMrg);
    y += 30;

    Absolute cp = new Absolute();
    int cx = xMrg;
    int cy = colInfoTop;
    wMrg = 5;

    cRef = new ColumnRef[columnCount];
    ValueSpinner vs;
    curWidthListener = new WidthListener(0, curTotId);
    minWidthListener = new WidthListener(1, minTotId);
    prefWidthListener = new WidthListener(2, prefTotId);
    maxWidthListener = new WidthListener(3, maxTotId);

    VisibilityHandler vh = new VisibilityHandler();
    int minW = scrollTable.getMinColumnWidth();
    int maxW = scrollTable.getMaxColumnWidth();

    for (int i = 0; i < columnCount; i++) {
      cRef[i] = new ColumnRef(i);
      BeeSimpleCheckBox scb = new BeeSimpleCheckBox(columnVisible[i]);
      scb.addClickHandler(vh);
      cRef[i].setShow(cp.append(scb, cx, cy));

      cRef[i].setOrder(cp.append(new ValueSpinner(i, 0, columnCount - 1), cx + 24, cy, 48));

      BeeLabel cap = new BeeLabel(scrollTable.getColumnCaption(i, false));
      EventUtils.makeDndSource(cap.getElement(), dndHandler);
      EventUtils.makeDndTarget(cap.getElement(), dndHandler);
      cRef[i].setCap(cp.append(cap, cx + 80, cy, xDat - cx - 84));

      BeeLabel fit = new BeeLabel(BeeUtils.toString(columnWidth[i].getDataWidth()),
          HasHorizontalAlignment.ALIGN_RIGHT);
      fit.setTitle(BeeUtils.concat(1, columnWidth[i].getHeaderWidth(),
          columnWidth[i].getFooterWidth()));
      fit.addDoubleClickHandler(copyHandler);
      cRef[i].setDatW(cp.append(fit, xDat, cy, ww - 10));

      vs = new ValueSpinner(columnWidth[i].getCurWidth(), minW, maxW);
      vs.addSpinnerListener(curWidthListener);
      vs.addDomHandler(copyHandler, DoubleClickEvent.getType());
      cRef[i].setCurW(cp.append(vs, xCur, cy, ww - wMrg));

      vs = new ValueSpinner(columnWidth[i].getMinWidth(), minW, maxW);
      vs.addSpinnerListener(minWidthListener);
      cRef[i].setMinW(cp.append(vs, xMin, cy, ww - wMrg));

      vs = new ValueSpinner(columnWidth[i].getPrefWidth(), minW, maxW);
      vs.addSpinnerListener(prefWidthListener);
      vs.addDomHandler(copyHandler, DoubleClickEvent.getType());
      cRef[i].setPrefW(cp.append(vs, xPref, cy, ww - wMrg));

      vs = new ValueSpinner(columnWidth[i].getMaxWidth(), minW, maxW);
      vs.addSpinnerListener(maxWidthListener);
      cRef[i].setMaxW(cp.append(vs, xMax, cy, ww - wMrg));

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
    y += yMrg;

    x = 10;
    wMrg = 10;
    z = (w - x * 2 + wMrg) / 6;

    panel.append(new BeeButton("Apply", new ConfigCommand(Action.APPLY)), x, y, z - wMrg);
    panel.append(new BeeButton("Distribute Current", new ConfigCommand(Action.DISTR_CUR)),
        x += z, y, z - wMrg);
    panel.append(new BeeButton("Distribute Preferred", new ConfigCommand(Action.DISTR_PREF)),
        x += z, y, z - wMrg);
    panel.append(new BeeButton("Fill Current", new ConfigCommand(Action.FILL_PREF)),
        x += z, y, z - wMrg);
    panel.append(new BeeButton("Fill Preferred", new ConfigCommand(Action.FILL_PREF)),
        x += z, y, z - wMrg);
    panel.append(new BeeButton("Cancel", new ConfigCommand(Action.CANCEL)), x += z, y, z - wMrg);

    y += 40 + yMrg;

    DomUtils.setWidth(panel, w);
    DomUtils.setHeight(panel, y);

    popup = new BeePopupPanel(true, true);
    popup.setWidget(panel);
    popup.setStyleName(BeeStyle.CONFIG_PANEL);
    popup.enableGlass();

    popup.addCloseHandler(new PopupCloseHandler());
    popup.setPopupPositionAndShow(new PopupPosition());

    totalWidths();
    DomUtils.preventChildSelection(panel.getElement(), true);
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

  private void init(ScrollTable st) {
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

    columnWidth = new ColumnWidth[columnCount];
    columnVisible = new boolean[columnCount];

    st.recalculateIdealColumnWidths();

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
      if (Ints.contains(arr, i)) {
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
    BeeKeeper.getStyle().setTop(cRef[row].getDatW(), top);
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
      if (!show) {
        continue;
      }

      if (curW > 0 && curW != columnWidth[i].getCurWidth()) {
        scrollTable.setColumnWidth(i, curW);
        updCur = true;
      }

      if (minW > 0 && minW != columnWidth[i].getMinWidth()) {
        tableDefinition.getColumnDefinition(i).setMinimumColumnWidth(minW);
        updMin = true;
      }
      if (prefW > 0 && prefW != columnWidth[i].getPrefWidth()) {
        tableDefinition.getColumnDefinition(i).setPreferredColumnWidth(prefW);
        updPref = true;
      }
      if (maxW > 0 && maxW != columnWidth[i].getMaxWidth()) {
        tableDefinition.getColumnDefinition(i).setMaximumColumnWidth(maxW);
        updMax = true;
      }
    }

    if (orderColumns()) {
      reload = true;
    }

    return reload;
  }

  private void totalIdeal() {
    int tot = 0;
    for (int i = 0; i < columnCount; i++) {
      if (!isVisible(i)) {
        continue;
      }
      int v = columnWidth[i].getDataWidth();
      if (v > 0) {
        tot += v;
      }
    }
    DomUtils.setText(datTotId, BeeUtils.toString(tot));
  }

  private void totalWidths() {
    totalIdeal();
    curWidthListener.onSpinning(0);
    minWidthListener.onSpinning(0);
    prefWidthListener.onSpinning(0);
    maxWidthListener.onSpinning(0);
  }

  private boolean validate() {
    boolean ok = visibleCount > 0;
    if (!ok) {
      Global.showError("Invisible Pink Unicorns are beings of great spiritual power");
    }
    return ok;
  }
}
