package com.butent.bee.shared;

/**
 * Manages object stages in the system, this can be used for marking, visibility and so on.
 */

public class Stage implements HasService, HasStage {

  private String service;
  private String stage;

  /**
   * Creates a new {@code Stage} object.
   */
  public Stage() {
    super();
  }

  /**
   * Creates a new {@code Stage} object with service and stage names.
   * 
   * @param service name of service
   * @param stage name of stage
   */
  public Stage(String service, String stage) {
    this();
    this.service = service;
    this.stage = stage;
  }

  /**
   * Returns the name of service.
   * 
   * @return the name of service
   */
  public String getService() {
    return service;
  }

  /**
   * Returns the name of stage.
   * 
   * @return the name of stage
   */
  public String getStage() {
    return stage;
  }

  /**
   * Sets the new name of service.
   * 
   * @param svc name of service
   */
  public void setService(String svc) {
    this.service = svc;
  }

  /**
   * Sets the new name of stage.
   * 
   * @param stg name of stage
   */
  public void setStage(String stg) {
    this.stage = stg;
  }
}
