package com.butent.bee.egg.server.http;

import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.Transformable;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.logging.Level;

public class ResponseMessage implements Transformable {
  private BeeDate date = new BeeDate();
  private String level = null;
  private String message;

  public ResponseMessage(Level level, Object... obj) {
    this.level = level.getName();
    this.message = BeeUtils.concat(1, obj);
  }

  public ResponseMessage(Level level, String message) {
    this.level = level.getName();
    this.message = message;
  }

  public ResponseMessage(Object... obj) {
    this.message = BeeUtils.concat(1, obj);
  }

  public ResponseMessage(String message) {
    this.message = message;
  }

  public BeeDate getDate() {
    return date;
  }

  public String getLevel() {
    return level;
  }

  public String getMessage() {
    return message;
  }

  public void setDate(BeeDate date) {
    this.date = date;
  }

  public void setLevel(String level) {
    this.level = level;
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
