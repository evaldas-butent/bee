package com.butent.bee.client.calendar;

public class Attendee {
  
  private final long id;
  private final String name;

  public Attendee(long id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
