package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;

import com.butent.bee.client.calendar.Attendee;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class Appointment implements Comparable<Appointment> {

  private Long id = null;
  
  private String title = null;
  private String description = null;

  private DateTime start = null;
  private DateTime end = null;
  
  private String location = null;
  private String createdBy = null;

  private final List<Attendee> attendees = Lists.newArrayList();

  private boolean allDay = false;
  
  private String customStyle = null;

  private boolean readOnly = false;

  public Appointment() {
  }

  public Appointment clone() {
    Appointment clone = new Appointment();

    clone.setId(this.id);
    clone.setTitle(this.title);
    clone.setDescription(this.description);

    clone.setStart(this.start);
    clone.setEnd(this.end);
    
    clone.setLocation(this.location);
    clone.setCreatedBy(this.createdBy);
    clone.getAttendees().addAll(this.attendees);

    clone.setAllDay(this.allDay);
    
    clone.setCustomStyle(this.customStyle);
    
    clone.setReadOnly(this.readOnly);

    return clone;
  }

  public int compareTo(Appointment appointment) {
    int compare = BeeUtils.compare(getStart(), appointment.getStart());
    if (compare == BeeConst.COMPARE_EQUAL) {
      compare = BeeUtils.compare(appointment.getEnd(), getEnd());
    }
    return compare;
  }

  public List<Attendee> getAttendees() {
    return attendees;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public String getCustomStyle() {
    return customStyle;
  }

  public String getDescription() {
    return description;
  }

  public DateTime getEnd() {
    return end;
  }

  public Long getId() {
    return id;
  }

  public String getLocation() {
    return location;
  }

  public DateTime getStart() {
    return start;
  }

  public String getTitle() {
    return title;
  }

  public boolean isAllDay() {
    return allDay;
  }

  public boolean isMultiDay() {
    if (getStart() != null) {
      return !TimeUtils.sameDate(getStart(), getEnd());
    } else {
      return false;
    }
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setAllDay(boolean allDay) {
    this.allDay = allDay;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public void setCustomStyle(String customStyle) {
    this.customStyle = customStyle;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setEnd(DateTime end) {
    this.end = end;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setStart(DateTime start) {
    this.start = start;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
