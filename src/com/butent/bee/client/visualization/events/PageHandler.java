package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

public abstract class PageHandler extends Handler {
  public class PageEvent {
    private int page;

    public PageEvent(int page) {
      this.page = page;
    }

    public int getPage() {
      return page;
    }
  }

  public abstract void onPage(PageEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onPage(new PageEvent(properties.getInt("page")));
  }
}