package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Handles a mouse click event on a particular region.
 */

public abstract class RegionClickHandler extends Handler {
  /**
   * Occurs when a mouse click happens on a particular region.
   */
  public class RegionClickEvent {
    private String region;

    public RegionClickEvent(String region) {
      this.region = region;
    }

    public String getRegion() {
      return region;
    }
  }

  public abstract void onRegionClick(RegionClickEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onRegionClick(new RegionClickEvent(properties.getString("region")));
  }
}