package com.butent.bee.shared;

/**
 * Contains possible object states in the system (open, closed, changed etc).
 */

public enum State {
  INITIALIZED, OPEN, CLOSED, ERROR, EXPIRED, CONFIRMED, CANCELED, CHANGED, PENDING,
  LOADING, LOADED, CREATED, NEW, EDITED, ACTIVATED, INSERTED, REMOVED, UPDATING, CLOSING, UNLOADING
}
