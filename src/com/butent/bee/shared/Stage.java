package com.butent.bee.shared;

public class Stage implements HasService, HasStage {
  public static final String STAGE_GET_PARAMETERS = "stage_get";
  public static final String STAGE_CONFIRM = "stage_confirm";

  private String service;
  private String stage;

  public Stage() {
    super();
  }

  public Stage(String service, String stage) {
    this();
    this.service = service;
    this.stage = stage;
  }

  public String getService() {
    return service;
  }

  public String getStage() {
    return stage;
  }

  public void setService(String svc) {
    this.service = svc;
  }

  public void setStage(String stg) {
    this.stage = stg;
  }
}
