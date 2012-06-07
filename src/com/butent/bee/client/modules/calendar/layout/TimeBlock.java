package com.butent.bee.client.modules.calendar.layout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class TimeBlock {

  private final List<AppointmentAdapter> appointments = Lists.newArrayList();
  private final Map<Integer, Integer> occupiedColumns = Maps.newHashMap();

  private int totalColumns = 1;
  private int order;
  
  private String name;
  private int start;
  private int end;

  private int top;
  private int bottom;

  public List<AppointmentAdapter> getAppointments() {
    return appointments;
  }

  public int getBottom() {
    return bottom;
  }

  public int getEnd() {
    return end;
  }

  public int getFirstAvailableColumn() {
    int col = 0;
    while (true) {
      if (occupiedColumns.containsKey(col)) {
        col++;
      } else {
        return col;
      }
    }
  }

  public String getName() {
    return name;
  }

  public Map<Integer, Integer> getOccupiedColumns() {
    return occupiedColumns;
  }

  public int getOrder() {
    return order;
  }

  public int getStart() {
    return start;
  }

  public int getTop() {
    return top;
  }

  public int getTotalColumns() {
    return totalColumns;
  }

  public boolean intersectsWith(AppointmentAdapter appt) {
    return intersectsWith(appt.getAppointmentStart(), appt.getAppointmentEnd());
  }

  public boolean intersectsWith(int apptStart, int apptEnd) {
    if (apptStart >= this.getStart() && apptStart < this.getEnd()) {
      return true;
    }
    return apptEnd > this.getStart() && apptStart < this.getStart();
  }

  public void setBottom(int bottom) {
    this.bottom = bottom;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setTop(int top) {
    this.top = top;
  }

  public void setTotalColumns(int totalColumns) {
    this.totalColumns = totalColumns;
  }
}
