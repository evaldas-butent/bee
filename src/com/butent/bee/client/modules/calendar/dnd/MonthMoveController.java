package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.ItemWidget;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.view.MonthView;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class MonthMoveController implements MoveEvent.Handler {

  private static final int START_SENSITIVITY_PIXELS = 3;

  private final MonthView monthView;

  private int headerHeight;

  private ItemWidget itemWidget;
  private int relativeLeft;
  private int relativeTop;

  private int sourceWidth;
  private int sourceHeight;

  private int targetLeft;
  private int targetTop;

  private int targetWidth;
  private int targetHeight;

  private int pointerOffsetX;
  private int pointerOffsetY;

  private int selectedRow = BeeConst.UNDEF;
  private int selectedColumn = BeeConst.UNDEF;

  public MonthMoveController(MonthView monthView) {
    this.monthView = monthView;
  }

  @Override
  public void onMove(MoveEvent event) {
    Mover mover = event.getMover();
    if (mover == null) {
      return;
    }

    if (event.isMoving()) {
      if (getItemWidget() == null) {
        if (!startDrag(mover)) {
          return;
        }
      }

      int x = mover.getCurrentX();
      int y = mover.getCurrentY();

      if (BeeUtils.betweenExclusive(x, getTargetLeft(), getTargetLeft() + getTargetWidth())
          && BeeUtils.betweenExclusive(y, getTargetTop() + getHeaderHeight(),
              getTargetTop() + getTargetHeight())) {

        int left = BeeUtils.clamp(x - getPointerOffsetX() - getTargetLeft(), 0,
            getTargetWidth() - getSourceWidth());
        if (left != getRelativeLeft()) {
          StyleUtils.setLeft(getItemWidget(), left);
          setRelativeLeft(left);
        }

        int top = BeeUtils.clamp(y - getPointerOffsetY() - getTargetTop(), getHeaderHeight(),
            getTargetHeight() - getSourceHeight());
        if (top != getRelativeTop()) {
          StyleUtils.setTop(getItemWidget(), top);
          setRelativeTop(top);
        }
      }

      updatePosition();

    } else if (event.isFinished()) {
      if (getItemWidget() != null) {
        drop();
        setItemWidget(null);
      }
    }
  }

  public void setHeaderHeight(int headerHeight) {
    this.headerHeight = headerHeight;
  }

  private void drop() {
    JustDate date = monthView.getCellDate(getSelectedRow(), getSelectedColumn());

    if (getItemWidget().isAppointment()) {
      Appointment appointment = (Appointment) getItemWidget().getItem();

      if (!TimeUtils.sameDate(date, appointment.getStart())) {
        DateTime start = TimeUtils.combine(date, appointment.getStart());
        DateTime end = TimeUtils.combine(date, appointment.getEnd());

        monthView.updateAppointment(appointment, start, end, BeeConst.UNDEF, BeeConst.UNDEF);
      }
    }

    monthView.getCalendarWidget().refresh(false);
  }

  private int getHeaderHeight() {
    return headerHeight;
  }

  private ItemWidget getItemWidget() {
    return itemWidget;
  }

  private int getPointerOffsetX() {
    return pointerOffsetX;
  }

  private int getPointerOffsetY() {
    return pointerOffsetY;
  }

  private int getRelativeLeft() {
    return relativeLeft;
  }

  private int getRelativeTop() {
    return relativeTop;
  }

  private int getSelectedColumn() {
    return selectedColumn;
  }

  private int getSelectedRow() {
    return selectedRow;
  }

  private int getSourceHeight() {
    return sourceHeight;
  }

  private int getSourceWidth() {
    return sourceWidth;
  }

  private int getTargetHeight() {
    return targetHeight;
  }

  private int getTargetLeft() {
    return targetLeft;
  }

  private int getTargetTop() {
    return targetTop;
  }

  private int getTargetWidth() {
    return targetWidth;
  }

  private void setItemWidget(ItemWidget itemWidget) {
    this.itemWidget = itemWidget;
  }

  private void setPointerOffsetX(int pointerOffsetX) {
    this.pointerOffsetX = pointerOffsetX;
  }

  private void setPointerOffsetY(int pointerOffsetY) {
    this.pointerOffsetY = pointerOffsetY;
  }

  private void setRelativeLeft(int relativeLeft) {
    this.relativeLeft = relativeLeft;
  }

  private void setRelativeTop(int relativeTop) {
    this.relativeTop = relativeTop;
  }

  private void setSelectedColumn(int selectedColumn) {
    this.selectedColumn = selectedColumn;
  }

  private void setSelectedRow(int selectedRow) {
    this.selectedRow = selectedRow;
  }

  private void setSourceHeight(int sourceHeight) {
    this.sourceHeight = sourceHeight;
  }

  private void setSourceWidth(int sourceWidth) {
    this.sourceWidth = sourceWidth;
  }

  private void setTargetHeight(int targetHeight) {
    this.targetHeight = targetHeight;
  }

  private void setTargetLeft(int targetLeft) {
    this.targetLeft = targetLeft;
  }

  private void setTargetTop(int targetTop) {
    this.targetTop = targetTop;
  }

  private void setTargetWidth(int targetWidth) {
    this.targetWidth = targetWidth;
  }

  private boolean startDrag(Mover mover) {
    if (Math.abs(mover.getStartX() - mover.getCurrentX()) < START_SENSITIVITY_PIXELS
        && Math.abs(mover.getStartY() - mover.getCurrentY()) < START_SENSITIVITY_PIXELS) {
      return false;
    }

    ItemWidget widget = CalendarUtils.getItemWidget(mover);
    if (widget == null) {
      return false;
    }

    setItemWidget(widget);

    Widget target = widget.getParent();

    setSourceWidth(widget.getOffsetWidth());
    setSourceHeight(widget.getOffsetHeight());

    setTargetLeft(target.getElement().getAbsoluteLeft());
    setTargetTop(target.getElement().getAbsoluteTop());

    setTargetWidth(target.getElement().getClientWidth());
    setTargetHeight(target.getElement().getClientHeight());

    setRelativeLeft(widget.getElement().getOffsetLeft());
    setRelativeTop(widget.getElement().getOffsetTop());

    setPointerOffsetX(mover.getStartX() - widget.getElement().getAbsoluteLeft());
    setPointerOffsetY(mover.getStartY() - widget.getElement().getAbsoluteTop());

    StyleUtils.setWidth(getItemWidget(), getSourceWidth());
    getItemWidget().addStyleName(CalendarStyleManager.DRAG);

    return true;
  }

  private void updatePosition() {
    int row = monthView.getRow(getRelativeTop() + getPointerOffsetY());
    int col = monthView.getColumn(getRelativeLeft() + getPointerOffsetX());

    if (getSelectedRow() != row || getSelectedColumn() != col) {
      if (!BeeConst.isUndef(getSelectedRow())) {
        monthView.setCellStyle(getSelectedRow(), getSelectedColumn(),
            CalendarStyleManager.POSITIONER, false);
      }
      monthView.setCellStyle(row, col, CalendarStyleManager.POSITIONER, true);

      setSelectedRow(row);
      setSelectedColumn(col);
    }
  }
}
