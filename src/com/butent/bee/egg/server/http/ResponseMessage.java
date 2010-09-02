package com.butent.bee.egg.server.http;

import java.util.logging.Level;

import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.Transformable;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class ResponseMessage implements Transformable {
  private BeeDate date = new BeeDate();
  private String level = null;
  private String message;

  public ResponseMessage(String message) {
    this.message = message;
  }

  public ResponseMessage(Object... obj) {
    this.message = BeeUtils.concat(1, obj);
  }

  public ResponseMessage(Level level, String message) {
    this.level = level.getName();
    this.message = message;
  }

  public ResponseMessage(Level level, Object... obj) {
    this.level = level.getName();
    this.message = BeeUtils.concat(1, obj);
  }

  public BeeDate getDate() {
    return date;
  }

  public void setDate(BeeDate date) {
    this.date = date;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return BeeUtils.concat(1, date.toLog(), level, message);
  }

  public String transform() {
    return toString();
  }

}
