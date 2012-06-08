package com.butent.bee.client.calendar.drop;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;

import com.butent.bee.client.dnd.DragContext;
import com.butent.bee.client.dnd.drop.AbsolutePositionDropController;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

public class MonthViewDropController extends AbsolutePositionDropController {

  private static final String BACKGROUND = "backgroundColor";

  private int daysPerWeek;
  private int weeksPerMonth;
  private JustDate firstDateDisplayed;

  private FlexTable monthGrid;

  private Element[] highlightedCells;

  public MonthViewDropController(AbsolutePanel dropTarget, FlexTable monthGrid) {
    super(dropTarget);
    this.monthGrid = monthGrid;
  }

  public int getColumn(DragContext context, Draggable draggable) {
    int x = context.desiredDraggableX - dropTargetOffsetX + draggable.relativeX;
    return (int) Math.floor(x / (monthGrid.getOffsetWidth() / daysPerWeek));
  }

  public JustDate getFirstDateDisplayed() {
    return firstDateDisplayed;
  }

  public int getRow(DragContext context, Draggable draggable) {
    int y = context.desiredDraggableY - dropTargetOffsetY + draggable.relativeY;
    return (int) Math.floor(y / (monthGrid.getOffsetHeight() / weeksPerMonth)) + 1;
  }

  @Override
  public void onDrop(DragContext context) {
    super.onDrop(context);

    for (Element elem : highlightedCells) {
      if (elem != null) {
        DOM.setStyleAttribute(elem, BACKGROUND, "#FFFFFF");
      }
    }
    highlightedCells = null;

    Draggable draggable = draggableList.get(0);

    Appointment appointment = ((AppointmentWidget) context.draggable).getAppointment();

    long originalStartToEndTimeDistance =
        appointment.getEnd().getTime() - appointment.getStart().getTime();

    int row = getRow(context, draggable) - 1;
    int col = getColumn(context, draggable);
    int cell = row * daysPerWeek + col;

    DateTime newStart = TimeUtils.combine(TimeUtils.nextDay(firstDateDisplayed, cell), appointment.getStart());
    DateTime newEnd = new DateTime(newStart.getTime() + originalStartToEndTimeDistance);

    appointment.setStart(newStart);
    appointment.setEnd(newEnd);
  }

  @Override
  public void onMove(DragContext context) {
    Draggable draggable = draggableList.get(0);
    if (draggable == null) {
      return;
    }

    int col = getColumn(context, draggable);
    int row = getRow(context, draggable);

    Element currHoveredCell = monthGrid.getFlexCellFormatter().getElement(row, col);

    if (highlightedCells == null || highlightedCells.length < 0 ||
        !currHoveredCell.equals(highlightedCells[0])) {
      if (highlightedCells != null) {
        for (Element elem : highlightedCells) {
          if (elem != null) {
            DOM.setStyleAttribute(elem, BACKGROUND, "#FFFFFF");
          }
        }
      }

      DateTime startDate = ((AppointmentWidget) draggable.widget).getAppointment().getStart();
      DateTime endDate = ((AppointmentWidget) draggable.widget).getAppointment().getEnd();

      int dateDiff = TimeUtils.dayDiff(startDate, endDate) + 1;
      dateDiff = (dateDiff <= 0) ? 1 : dateDiff;

      highlightedCells = getCells(row, col, dateDiff);
      for (Element elem : highlightedCells) {
        if (elem != null) {
          DOM.setStyleAttribute(elem, BACKGROUND, "#C3D9FF");
        }
      }
    }
  }

  public void setDaysPerWeek(int daysPerWeek) {
    this.daysPerWeek = daysPerWeek;
  }

  public void setFirstDateDisplayed(JustDate firstDateDisplayed) {
    this.firstDateDisplayed = firstDateDisplayed;
  }

  public void setWeeksPerMonth(int weeksPerMonth) {
    this.weeksPerMonth = weeksPerMonth;
  }

  protected Element[] getCells(int row, int col, int days) {
    Element[] elems = new Element[days];

    for (int i = 0; i < days; i++) {
      if (col > daysPerWeek - 1) {
        col = 0;
        row++;
      }

      try {
        elems[i] = monthGrid.getFlexCellFormatter().getElement(row, col);
      } catch (Exception ex) {
        break;
      }
      col++;
    }
    return elems;
  }
}
