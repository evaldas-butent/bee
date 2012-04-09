package com.butent.bee.client.calendar;

import com.butent.bee.shared.ui.HasCaption;

public class CalendarSettings {

  public enum TimeBlockClick implements HasCaption {
    Double, Single, Drag;

    public String getCaption() {
      return this.name();
    }
  }

  private int pixelsPerInterval = 30;
  private int intervalsPerHour = 2;

  private int workingHourStart = 8;
  private int workingHourEnd = 17;
  private int scrollToHour = 8;
  
  private int defaultDisplayedDays = 4;
  
  private boolean offsetHourLabels = false;

  private boolean enableDragDrop = true;
  private boolean dragDropCreation = true;

  private TimeBlockClick timeBlockClickNumber = TimeBlockClick.Single;

  public CalendarSettings() {
  }

  public int getDefaultDisplayedDays() {
    return defaultDisplayedDays;
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

  public TimeBlockClick getTimeBlockClickNumber() {
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

  public void setDefaultDisplayedDays(int defaultDisplayedDays) {
    this.defaultDisplayedDays = defaultDisplayedDays;
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

  public void setTimeBlockClickNumber(TimeBlockClick timeBlockClickNumber) {
    this.timeBlockClickNumber = timeBlockClickNumber;
  }

  public void setWorkingHourEnd(int end) {
    workingHourEnd = end;
  }

  public void setWorkingHourStart(int start) {
    workingHourStart = start;
  }
}
