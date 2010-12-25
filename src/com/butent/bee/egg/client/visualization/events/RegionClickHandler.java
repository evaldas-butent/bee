package com.butent.bee.egg.client.visualization.events;

import com.butent.bee.egg.client.ajaxloader.Properties;
import com.butent.bee.egg.client.ajaxloader.Properties.TypeException;

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
  protected void onEvent(Properties properties) throws TypeException {
    onRegionClick(new RegionClickEvent(properties.getString("region")));
  }
}