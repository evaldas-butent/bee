package com.butent.bee.client.calendar;

public class Attendee {

  private String name;
  private String email;

  private Attending attending = Attending.Maybe;

  private String imageUrl;

  public Attending getAttending() {
    return attending;
  }

  public String getEmail() {
    return email;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getName() {
    return name;
  }

  public void setAttending(Attending attending) {
    this.attending = attending;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setName(String name) {
    this.name = name;
  }
}
