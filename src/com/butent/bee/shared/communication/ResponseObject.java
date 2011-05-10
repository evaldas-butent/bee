package com.butent.bee.shared.communication;

import com.google.common.collect.Lists;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.logging.Level;

/**
 * Enables to use bundles of response messages in one object.
 */

public class ResponseObject {

  public static ResponseObject error(Object... err) {
    return new ResponseObject().addError(err);
  }

  public static ResponseObject info(Object... obj) {
    return new ResponseObject().addInfo(obj);
  }

  public static ResponseObject response(Object response) {
    return new ResponseObject().setResponse(response);
  }

  public static ResponseObject warning(Object... obj) {
    return new ResponseObject().addWarning(obj);
  }

  private Collection<ResponseMessage> messages = Lists.newArrayList();
  private Object response = null;

  public ResponseObject addError(Object... err) {
    messages.add(new ResponseMessage(Level.SEVERE, BeeUtils.concat(1, err)));
    return this;
  }

  public ResponseObject addError(Throwable err) {
    addError(err.toString());
    return this;
  }

  public ResponseObject addInfo(Object... obj) {
    messages.add(new ResponseMessage(Level.INFO, BeeUtils.concat(1, obj)));
    return this;
  }

  public ResponseObject addWarning(Object... obj) {
    messages.add(new ResponseMessage(Level.WARNING, BeeUtils.concat(1, obj)));
    return this;
  }

  public Collection<ResponseMessage> getMessages() {
    return messages;
  }

  public Object getResponse() {
    return response;
  }

  public boolean hasError() {
    for (ResponseMessage message : messages) {
      if (BeeUtils.equals(message.getLevel(), Level.SEVERE)) {
        return true;
      }
    }
    return false;
  }

  public ResponseObject setResponse(Object response) {
    this.response = response;
    return this;
  }
}
