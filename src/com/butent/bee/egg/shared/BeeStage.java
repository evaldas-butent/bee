package com.butent.bee.egg.shared;

public class BeeStage implements HasService, HasStage {
  public static final String STAGE_GET_PARAMETERS = "stage_get";
  public static final String STAGE_CONFIRM = "stage_confirm";

  private String service;
  private String stage;

  public BeeStage() {
    super();
  }

  public BeeStage(String service, String stage) {
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
