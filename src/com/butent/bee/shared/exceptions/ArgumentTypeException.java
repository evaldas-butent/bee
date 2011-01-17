package com.butent.bee.shared.exceptions;

@SuppressWarnings("serial")
public class ArgumentTypeException extends BeeRuntimeException {
  private String argType = null;
  private String reqType = null;

  public ArgumentTypeException() {
    super();
  }

  public ArgumentTypeException(String message) {
    super(message);
  }

  public ArgumentTypeException(String argType, String reqType) {
    this();
    this.argType = argType;
    this.reqType = reqType;
  }

  public ArgumentTypeException(String argType, String reqType, String message) {
    this(message);
    this.argType = argType;
    this.reqType = reqType;
  }

  public ArgumentTypeException(String message, Throwable cause) {
    super(message, cause);
  }

  public ArgumentTypeException(Throwable cause) {
    super(cause);
  }

  public String getArgType() {
    return argType;
  }

  public String getReqType() {
    return reqType;
  }

  public void setArgType(String argType) {
    this.argType = argType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

}
