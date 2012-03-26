package com.butent.bee.client.calendar;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Date;
import java.util.List;

public class Appointment implements Comparable<Appointment> {

  private String id = null;
  private String title = null;
  private String description = null;

  private Date start = null;
  private Date end = null;
  
  private String location = null;
  private String createdBy = null;
  private List<Attendee> attendees = Lists.newArrayList();

  private boolean allDay = false;
  
  private AppointmentStyle style = AppointmentStyle.DEFAULT;
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
    
    clone.setStyle(this.style);
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

  public Date getEnd() {
    return end;
  }

  public String getId() {
    return id;
  }

  public String getLocation() {
    return location;
  }

  public Date getStart() {
    return start;
  }

  public AppointmentStyle getStyle() {
    return style;
  }

  public String getTitle() {
    return title;
  }

  public boolean isAllDay() {
    return allDay;
  }

  public boolean isMultiDay() {
    if (getEnd() != null && getStart() != null) {
      return !DateUtils.areOnTheSameDay(getEnd(), getStart());
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

  public void setAttendees(List<Attendee> attendees) {
    this.attendees = attendees;
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

  public void setEnd(Date end) {
    this.end = end;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public void setStyle(AppointmentStyle style) {
    this.style = style;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
