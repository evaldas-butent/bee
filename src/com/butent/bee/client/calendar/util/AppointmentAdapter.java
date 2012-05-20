package com.butent.bee.client.calendar.util;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.time.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter {

  private final Appointment appointment;

  private final List<TimeBlock> intersectingBlocks;
  
  private int cellStart;
  private int cellSpan;
  private int columnStart = -1;
  private int columnSpan;
  private int appointmentStart;
  private int appointmentEnd;

  private double cellPercentFill;
  private double cellPercentStart;
  private double top;
  private double left;
  private double width;
  private double height;

  public AppointmentAdapter(Appointment appointment) {
    this.appointment = appointment;
    this.appointmentStart = TimeUtils.minutesSinceDayStarted(appointment.getStart());
    this.appointmentEnd = TimeUtils.minutesSinceDayStarted(appointment.getEnd());

    this.intersectingBlocks = new ArrayList<TimeBlock>();
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public int getAppointmentEnd() {
    return appointmentEnd;
  }

  public int getAppointmentStart() {
    return appointmentStart;
  }

  public double getCellPercentFill() {
    return cellPercentFill;
  }

  public double getCellPercentStart() {
    return cellPercentStart;
  }

  public int getCellSpan() {
    return cellSpan;
  }

  public int getCellStart() {
    return cellStart;
  }

  public int getColumnSpan() {
    return columnSpan;
  }

  public int getColumnStart() {
    return columnStart;
  }

  public double getHeight() {
    return height;
  }

  public List<TimeBlock> getIntersectingBlocks() {
    return intersectingBlocks;
  }

  public double getLeft() {
    return left;
  }

  public double getTop() {
    return top;
  }

  public double getWidth() {
    return width;
  }

  public void setAppointmentEnd(int appointmentEnd) {
    this.appointmentEnd = appointmentEnd;
  }

  public void setAppointmentStart(int appointmentStart) {
    this.appointmentStart = appointmentStart;
  }

  public void setCellPercentFill(double cellPercentFill) {
    this.cellPercentFill = cellPercentFill;
  }

  public void setCellPercentStart(double cellPercentStart) {
    this.cellPercentStart = cellPercentStart;
  }

  public void setCellSpan(int cellSpan) {
    this.cellSpan = cellSpan;
  }

  public void setCellStart(int cellStart) {
    this.cellStart = cellStart;
  }

  public void setColumnSpan(int columnSpan) {
    this.columnSpan = columnSpan;
  }

  public void setColumnStart(int columnStart) {
    this.columnStart = columnStart;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public void setLeft(double left) {
    this.left = left;
  }

  public void setTop(double top) {
    this.top = top;
  }

  public void setWidth(double width) {
    this.width = width;
  }
}
