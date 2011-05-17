package com.butent.bee.shared;

/**
 * Contains possible object states in the system (open, closed, changed etc).
 */

public enum State {
  UNKNOWN, INITIALIZED, OPEN, CLOSED, ERROR, EXPIRED, CANCELED, CHANGED
}
