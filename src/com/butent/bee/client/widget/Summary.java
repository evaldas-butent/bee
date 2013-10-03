package com.butent.bee.client.widget;

import com.butent.bee.client.dom.DomUtils;

public class Summary extends CustomHasHtml {

  public Summary() {
    super(DomUtils.createElement(DomUtils.TAG_SUMMARY));
  }

  public Summary(String text) {
    this();
    setHtml(text);
  }
  
  @Override
  public String getIdPrefix() {
    return "summary";
  }
  
  @Override
  protected void init() {
    super.init();
    addStyleName("bee-Summary");
  }
}

