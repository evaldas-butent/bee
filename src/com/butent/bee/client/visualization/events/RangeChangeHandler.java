package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

import java.util.Date;

/**
 * Handles a data range change event in visualizations.
 */

public abstract class RangeChangeHandler extends Handler {

  /**
   * Occurs when a data range change event happens in visualizations.
   */
  public class RangeChangeEvent {
    private Date end;
    private Date start;

    public RangeChangeEvent(Date start, Date end) {
      this.start = start;
      this.end = end;
    }

    public Date getEnd() {
      return end;
    }

    public Date getStart() {
      return start;
    }
  }

  public abstract void onRangeChange(RangeChangeEvent event);

  @Override
  protected void onEvent(Properties properties) {
    Date start = properties.getDate("start");
    Date end = properties.getDate("end");
    onRangeChange(new RangeChangeEvent(start, end));
  }
}