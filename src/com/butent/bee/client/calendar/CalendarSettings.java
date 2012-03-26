package com.butent.bee.client.calendar;

public class CalendarSettings {

  public enum Click {
    Double, Single, Drag, None
  }

  public static CalendarSettings DEFAULT_SETTINGS = new CalendarSettings();

  private int pixelsPerInterval = 30;
  private int intervalsPerHour = 2;

  private int workingHourStart = 8;
  private int workingHourEnd = 17;
  private int scrollToHour = 8;
  
  private boolean offsetHourLabels = false;

  private boolean enableDragDrop = true;
  private boolean dragDropCreation = true;

  private Click timeBlockClickNumber = Click.Single;

  public CalendarSettings() {
  }

  public boolean getEnableDragDropCreation() {
    return dragDropCreation;
  }

  public int getIntervalsPerHour() {
    return intervalsPerHour;
  }

  public int getPixelsPerInterval() {
    return pixelsPerInterval;
  }

  public int getScrollToHour() {
    return scrollToHour;
  }

  public Click getTimeBlockClickNumber() {
    return timeBlockClickNumber;
  }

  public int getWorkingHourEnd() {
    return workingHourEnd;
  }

  public int getWorkingHourStart() {
    return workingHourStart;
  }

  public boolean isEnableDragDrop() {
    return enableDragDrop;
  }

  public boolean isOffsetHourLabels() {
    return offsetHourLabels;
  }

  public void setEnableDragDrop(boolean enableDragDrop) {
    this.enableDragDrop = enableDragDrop;
  }

  public void setEnableDragDropCreation(boolean dragDropCreation) {
    this.dragDropCreation = dragDropCreation;
  }

  public void setIntervalsPerHour(int intervals) {
    intervalsPerHour = intervals;
  }

  public void setOffsetHourLabels(boolean offsetHourLabels) {
    this.offsetHourLabels = offsetHourLabels;
  }

  public void setPixelsPerInterval(int px) {
    pixelsPerInterval = px;
  }

  public void setScrollToHour(int hour) {
    scrollToHour = hour;
  }

  public void setTimeBlockClickNumber(Click timeBlockClickNumber) {
    this.timeBlockClickNumber = timeBlockClickNumber;
  }

  public void setWorkingHourEnd(int end) {
    workingHourEnd = end;
  }

  public void setWorkingHourStart(int start) {
    workingHourStart = start;
  }
}
