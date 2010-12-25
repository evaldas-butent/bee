package com.butent.bee.egg.client.visualization;

import java.util.Date;

public class DateRange {
  private Date start;
  
  private Date end;
  
  public DateRange(Date start, Date end) {
    this.start = start;
    this.end = end;
  }

  public final Date getEnd() {
    return this.end;
  }

  public final Date getStart() {
    return this.start;
  }
}