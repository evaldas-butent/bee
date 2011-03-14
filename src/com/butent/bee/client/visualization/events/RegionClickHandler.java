package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

public abstract class RegionClickHandler extends Handler {
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