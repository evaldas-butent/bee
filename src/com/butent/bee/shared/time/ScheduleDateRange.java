package com.butent.bee.shared.time;

public class ScheduleDateRange {

  private final DateRange range;
  private final ScheduleDateMode mode;

  public ScheduleDateRange(DateRange range, ScheduleDateMode mode) {
    this.range = range;
    this.mode = mode;
  }

  public DateRange getRange() {
    return range;
  }

  public ScheduleDateMode getMode() {
    return mode;
  }
}
