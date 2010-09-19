package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.BeeService.DATA_TYPE;

public class BeeResource {
  private String content;
  private BeeService.DATA_TYPE type;

  public BeeResource(String content) {
    super();
    this.content = content;
  }

  public BeeResource(String content, DATA_TYPE type) {
    super();
    this.content = content;
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public BeeService.DATA_TYPE getType() {
    return type;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setType(BeeService.DATA_TYPE type) {
    this.type = type;
  }
}
